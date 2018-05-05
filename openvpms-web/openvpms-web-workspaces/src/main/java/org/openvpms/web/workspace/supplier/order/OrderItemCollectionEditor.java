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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.order;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.edit.AlertListener;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.resource.i18n.Messages;

import java.util.HashSet;
import java.util.Set;

/**
 * Collection editor for <em>actRelationship.supplierOrderItem</em> relationships.
 * <p/>
 * This:
 * <ul>
 * <li>displays the stock on hand quantity for each line.</li>
 * <li>warns of duplicate products</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class OrderItemCollectionEditor extends ActRelationshipCollectionEditor {

    /**
     * The edit context.
     */
    private final OrderEditContext editContext;

    /**
     * The stock location
     */
    private final IMObjectReference stockLocation;

    /**
     * Alert identifier, used to cancel an existing alert.
     */
    private long alertId = -1;

    /**
     * Constructs an {@link OrderItemCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public OrderItemCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);

        editContext = new OrderEditContext();
        ActBean bean = new ActBean(act);
        stockLocation = bean.getNodeParticipantRef("stockLocation");
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
        OrderItemEditor editor = new OrderItemEditor((FinancialAct) object, (Act) getObject(), editContext, context);
        editor.setProductListener(getProductListener());
        return editor;
    }

    /**
     * Registers a listener to be notified of alerts.
     *
     * @param listener the listener. May be {@code null}
     */
    @Override
    public void setAlertListener(AlertListener listener) {
        super.setAlertListener(listener);
        if (listener != null) {
            checkDuplicateProducts();  // to display any duplicates when editing an existing order
        }
    }

    /**
     * Adds any object being edited to the collection, if it is valid.
     *
     * @param validator the validator
     * @return {@code true} if the object is valid, otherwise {@code false}
     */
    @Override
    protected boolean addCurrentEdits(Validator validator) {
        boolean valid = super.addCurrentEdits(validator);
        if (valid) {
            checkDuplicateProducts();
        }
        return valid;
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        super.remove(object);
        checkDuplicateProducts(); // only a single alert is displayed
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        IMTableModel<IMObject> model;
        if (stockLocation != null) {
            context = new DefaultLayoutContext(context);
            context.setComponentFactory(new TableComponentFactory(context));
            model = new OrderItemTableModel(stockLocation, editContext.getStock(), context);
        } else {
            // can't display stock
            model = super.createTableModel(context);
        }
        return model;
    }

    /**
     * Checks if the order has any duplicate products, raising an alert if a duplicate is detected.
     */
    private void checkDuplicateProducts() {
        AlertListener listener = getAlertListener();
        if (listener != null) {
            if (alertId != -1) {
                listener.cancel(alertId);
                alertId = -1;
            }
            Set<IMObjectReference> products = new HashSet<>();
            for (Act act : getCurrentActs()) {
                ActBean bean = new ActBean(act);
                IMObjectReference productRef = bean.getNodeParticipantRef("product");
                if (productRef != null && products.contains(productRef)) {
                    Product product = (Product) getObject(productRef);
                    if (product != null) {
                        alertId = listener.onAlert(Messages.format("supplier.order.duplicate", product.getName()));
                        break;
                    }
                } else {
                    products.add(productRef);
                }
            }
        }
    }

}
