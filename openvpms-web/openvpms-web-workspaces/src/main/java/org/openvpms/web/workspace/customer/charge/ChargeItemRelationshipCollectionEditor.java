/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.edit.AlertListener;
import org.openvpms.web.component.im.edit.ActCollectionResultSetFactory;
import org.openvpms.web.component.im.edit.CollectionResultSetFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.StockOnHand;
import org.openvpms.web.workspace.patient.charge.TemplateChargeItems;
import org.openvpms.web.workspace.patient.mr.Prescriptions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Editor for <em>actRelationship.customerAccountInvoiceItem</em> and
 * <em>actRelationship.customerAccountCreditItem</em> act relationships.
 * Sets a {@link EditorQueue} on {@link CustomerChargeActItemEditor} instances.
 *
 * @author Tim Anderson
 */
public class ChargeItemRelationshipCollectionEditor extends AbstractChargeItemRelationshipCollectionEditor {

    /**
     * Last Selected Item Date.
     */
    private Date lastItemDate = null;

    /**
     * Listener invoked when {@link #onAdd()} is invoked.
     */
    private Runnable listener;

    /**
     * The alert identifier, used to cancel any existing alert.
     */
    private long alertId = -1;

    /**
     * The templates.
     */
    private List<TemplateChargeItems> templates = new ArrayList<>();

    /**
     * The start time node name.
     */
    private static final String START_TIME = "startTime";

    /**
     * Constructs a {@link ChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public ChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        this(property, act, context, ActCollectionResultSetFactory.INSTANCE);
    }

    /**
     * Constructs a {@link ChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public ChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context,
                                                  CollectionResultSetFactory factory) {
        super(property, act, context, factory, createEditContext(act, context));
        Prescriptions prescriptions = null;
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE)) {
            List<Act> items = getCurrentActs();
            prescriptions = new Prescriptions(items, ServiceHelper.getBean(PrescriptionRules.class));
        }
        CustomerChargeEditContext editContext = getEditContext();
        editContext.setPrescriptions(prescriptions);
    }

    /**
     * Returns the save context.
     *
     * @return the save context
     */
    public ChargeSaveContext getSaveContext() {
        return getEditContext().getSaveContext();
    }

    /**
     * Sets the popup editor manager.
     *
     * @param queue the popup editor manager. May be {@code null}
     */
    public void setEditorQueue(EditorQueue queue) {
        getEditContext().setEditorQueue(queue);
    }

    /**
     * Returns the popup editor manager.
     *
     * @return the popup editor manager. May be {@code null}
     */
    public EditorQueue getEditorQueue() {
        return getEditContext().getEditorQueue();
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        super.remove(object);
        FinancialAct act = (FinancialAct) object;
        CustomerChargeEditContext editContext = getEditContext();
        editContext.getStock().remove(act);
        Prescriptions prescriptions = editContext.getPrescriptions();
        if (prescriptions != null) {
            prescriptions.removeItem(act);
        }
        Alerts alerts = editContext.getAlerts();
        alerts.removeItem(act);
    }

    /**
     * Registers a listener that is invoked when the user adds an item.
     * <p/>
     * Note that this is not invoked for template expansion.
     *
     * @param listener the listener to invoke. May be {@code null}
     */
    public void setAddItemListener(Runnable listener) {
        this.listener = listener;
    }

    /**
     * Returns the templates that were expanded.
     *
     * @return the templates
     */
    public List<TemplateChargeItems> getTemplates() {
        return templates;
    }

    /**
     * Clears the templates.
     */
    public void clearTemplates() {
        templates.clear();
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        return initialiseEditor(new DefaultCustomerChargeActItemEditor((FinancialAct) object, (Act) getObject(),
                                                                       getEditContext(), context));
    }

    /**
     * Returns an editor for an object, creating one if it doesn't exist.
     *
     * @param object the object to edit
     * @return an editor for the object
     */
    @Override
    public CustomerChargeActItemEditor getEditor(IMObject object) {
        return (CustomerChargeActItemEditor) super.getEditor(object);
    }

    /**
     * Invoked when the "Add" button is pressed. Creates a new instance of the selected archetype, and displays it in
     * an editor.
     *
     * @return the new editor, or {@code null} if one could not be created
     */
    @Override
    protected IMObjectEditor onAdd() {
        IMObjectEditor editor = add();
        if (editor != null && listener != null) {
            EditorQueue queue = getEditorQueue();
            if (!queue.isComplete()) {
                queue.queue(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.run();
                        }
                    }
                });
            } else {
                listener.run();
            }
        }
        return editor;
    }

    /**
     * Returns the edit context.
     *
     * @return the edit context
     */
    @Override
    protected CustomerChargeEditContext getEditContext() {
        return (CustomerChargeEditContext) super.getEditContext();
    }

    /**
     * Initialises an editor.
     *
     * @param editor the editor
     */
    protected CustomerChargeActItemEditor initialiseEditor(final CustomerChargeActItemEditor editor) {
        // Set startTime to to last used value
        if (lastItemDate != null) {
            editor.getProperty(START_TIME).setValue(lastItemDate);
        }

        // add a listener to store the last used item starttime.
        ModifiableListener startTimeListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                lastItemDate = (Date) editor.getProperty(START_TIME).getValue();
            }
        };
        editor.getProperty(START_TIME).addModifiableListener(startTimeListener);
        editor.setProductListener(getProductListener()); // register the listener to expand templates
        return editor;
    }

    /**
     * Edit an object.
     *
     * @param object the object to edit
     * @return the editor
     */
    @Override
    protected IMObjectEditor edit(IMObject object) {
        CustomerChargeActItemEditor editor = (CustomerChargeActItemEditor) super.edit(object);
        editor.updateOnHandQuantity();
        return editor;
    }

    /**
     * Copies an act item for each product referred to in its template.
     *
     * @param editor   the editor
     * @param template the product template
     * @param quantity the quantity
     * @return the acts generated from the template
     */
    @Override
    protected List<Act> createTemplateActs(ActItemEditor editor, Product template, BigDecimal quantity) {
        List<Act> acts = super.createTemplateActs(editor, template, quantity);
        if (!acts.isEmpty()) {
            TemplateChargeItems items = new TemplateChargeItems(template, acts);
            templates.add(items);
        }

        AlertListener alertListener = getAlertListener();
        if (alertListener != null && !acts.isEmpty()) {
            int outOfStock = 0;
            StockOnHand stock = getEditContext().getStock();
            for (Act act : acts) {
                BigDecimal onHand = stock.getAvailableStock((FinancialAct) act);
                if (onHand != null && BigDecimal.ZERO.compareTo(onHand) >= 0) {
                    ++outOfStock;
                }
            }
            if (outOfStock != 0) {
                if (alertId != -1) {
                    alertListener.cancel(alertId);
                    alertId = -1;
                }
                alertId = alertListener.onAlert(Messages.format("customer.charge.outofstock", outOfStock));
            }
        }
        EditorQueue queue = getEditorQueue();
        if (!acts.isEmpty() && !queue.isComplete()) {
            // the collection is considered invalid while there are popups, so force a validation check when
            // popups have all closed.
            queue.queue(new Runnable() {
                @Override
                public void run() {
                    isValid(); //
                }
            });
        }
        return acts;
    }

    /**
     * Saves any current edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        CustomerChargeEditContext editContext = getEditContext();
        Prescriptions prescriptions = editContext.getPrescriptions();
        if (prescriptions != null) {
            prescriptions.save();
        }
        // Need to save prescriptions first, as invoice item deletion can cause StaleObjectStateExceptions otherwise
        super.doSave();

        // clear the stock on hand so it is recreated from saved state
        editContext.getStock().clear();
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));
        ChargeItemTableModel model = new ChargeItemTableModel(getCollectionPropertyEditor().getArchetypeRange(),
                                                              getEditContext().getStock(), context);
        return (IMTableModel) model;
    }

    /**
     * Creates a new charge edit context.
     *
     * @param act     the parent charge
     * @param context the layout context
     * @return a new charge edit context
     */
    private static CustomerChargeEditContext createEditContext(Act act, LayoutContext context) {
        ActBean bean = new ActBean(act);
        Party customer = (Party) IMObjectHelper.getObject(bean.getNodeParticipantRef("customer"), context.getContext());
        if (customer == null) {
            throw new IllegalStateException(act.getArchetypeId().getShortName() + " has no customer");
        }
        Party location = (Party) IMObjectHelper.getObject(bean.getNodeParticipantRef("location"), context.getContext());
        return new CustomerChargeEditContext(customer, location, context);
    }

}
