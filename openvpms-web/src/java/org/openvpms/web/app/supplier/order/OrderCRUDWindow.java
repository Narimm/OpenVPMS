/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier.order;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.act.ActStatus;
import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import org.openvpms.archetype.rules.supplier.DeliveryStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.AbstractIMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.app.supplier.SupplierActCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActCopyHandler;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;
import java.util.List;


/**
 * CRUD window for supplier orders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OrderCRUDWindow extends SupplierActCRUDWindow<FinancialAct> {

    /**
     * The copy button.
     */
    private Button copy;

    /**
     * The invoice button.
     */
    private Button invoice;

    /**
     * Copy button identifier.
     */
    private static final String COPY_ID = "copy";

    /**
     * Invoice button identifier.
     */
    private static final String INVOICE_ID = "invoice";

    /**
     * order short name.
     */
    private static final String ORDER_TYPE = "act.supplierOrder";

    /**
     * Estimation item short name.
     */
    private static final String ORDER_ITEM_TYPE = "act.supplierOrderItem";

    /**
     * Estimation item relationship short name.
     */
    private static final String ORDER_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.supplierOrderItem";

    /**
     * Invoice act short name.
     */
    private static final String INVOICE_TYPE
            = "act.supplierAccountChargesInvoice";

    /**
     * Invoice act item short name.
     */
    private static final String INVOICE_ITEM_TYPE
            = "act.supplierAccountInvoiceItem";

    /**
     * Invoice act item relationship short name.
     */
    private static final String INVOICE_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.supplierAccountInvoiceItem";


    /**
     * Create a new <tt>OrderCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public OrderCRUDWindow(Archetypes<FinancialAct> archetypes) {
        super(archetypes);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        copy = ButtonFactory.create(COPY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onCopy();
            }
        });
        invoice = ButtonFactory.create(INVOICE_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onInvoice();
            }
        });
        enableButtons(buttons, false);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.removeAll();
        if (enable) {
            ActBean bean = new ActBean(getObject());
            if (DeliveryStatus.FULL.equals(bean.getString("deliveryStatus"))) {
                buttons.add(getEditButton());
            }
            buttons.add(getCreateButton());
            if (!ActStatus.POSTED.equals(bean.getStatus())) {
                buttons.add(getDeleteButton());
            }
            buttons.add(getPostButton());
            buttons.add(getPreviewButton());
            buttons.add(copy);
            buttons.add(invoice);
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Invoked when a new order has been created.
     * <p/>
     * This implementation pops up browser to select the supplier, then displays
     * an edit dialog for the order.
     *
     * @param act the new order
     */
    @Override
    protected void onCreated(final FinancialAct act) {
        String shortName = "party.supplier*";
        final TaskContext initial = new DefaultTaskContext();
        Query<Party> query = QueryFactory.create(shortName, initial,
                                                 Party.class);
        EditIMObjectTask editor = new EditIMObjectTask(shortName, true);
        SelectIMObjectTask<Party> select = new SelectIMObjectTask<Party>(
                Messages.get("supplier.order.type"), query, editor);
        select.start(initial);
        select.addTaskListener(new TaskListener() {
            public void taskEvent(TaskEvent event) {
                if (event.getType().equals(TaskEvent.Type.COMPLETED)) {
                    Party s = initial.getSupplier();
                    if (s != null) {
                        try {
                            ActBean bean = new ActBean(act);
                            bean.addParticipation("participation.supplier", s);
                            edit(act);
                        } catch (OpenVPMSException exception) {
                            ErrorHelper.show(exception);
                        }
                    }
                }
            }
        });
    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {
        IMObject object = getObject();
        try {
            IMObjectCopier copier = new IMObjectCopier(new ActCopyHandler());
            List<IMObject> objects = copier.apply(object);
            FinancialAct act = (FinancialAct) objects.get(0);
            act.setStatus(IN_PROGRESS);
            act.setActivityStartTime(new Date());
            setPrintStatus(act, false);
            SaveHelper.save(objects);
            setObject(act);
            CRUDWindowListener<FinancialAct> listener = getListener();
            if (listener != null) {
                listener.saved(act, false);
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("supplier.order.copy.failed");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Invoked when the 'invoice' button is pressed.
     */
    protected void onInvoice() {
        final FinancialAct act = getObject();
        String title = Messages.get("supplier.order.invoice.title");
        String message = Messages.get("supplier.order.invoice.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    invoice(act);
                }
            }
        });
        dialog.show();
    }

    /**
     * Invoice out an order to the supplier.
     *
     * @param order the order
     */
    private void invoice(FinancialAct order) {
        try {
            IMObjectCopier copier = new IMObjectCopier(new InvoiceHandler());
            List<IMObject> objects = copier.apply(order);
            FinancialAct invoice = (FinancialAct) objects.get(0);
            invoice.setStatus(IN_PROGRESS);
            invoice.setActivityStartTime(new Date());
            setPrintStatus(invoice, false);
            SaveHelper.save(objects);

            if (!POSTED.equals(order.getStatus())) {
                order.setStatus(POSTED);
                SaveHelper.save(order);
                setObject(order);
                CRUDWindowListener<FinancialAct> listener = getListener();
                if (listener != null) {
                    listener.saved(order, false);
                }
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("supplier.order.invoice.failed");
            ErrorHelper.show(title, exception);
        }
    }

    private static class InvoiceHandler extends AbstractIMObjectCopyHandler {

        /**
         * Map of invoice types to their corresponding credit types.
         */
        private static final String[][] TYPE_MAP = {
                {ORDER_TYPE, INVOICE_TYPE},
                {ORDER_ITEM_TYPE, INVOICE_ITEM_TYPE},
                {ORDER_ITEM_RELATIONSHIP_TYPE, INVOICE_ITEM_RELATIONSHIP_TYPE},
        };

        /**
         * Returns a target node for a given source
         *
         * @param source the source node
         * @param target the target archetype
         */
        @Override
        protected NodeDescriptor getTargetNode(NodeDescriptor source,
                                               ArchetypeDescriptor target) {
            String name = source.getName();
            if (target.getShortName().equals(INVOICE_ITEM_TYPE)) {
                if (name.equals("qty")) {
                    return target.getNodeDescriptor("quantity");
                } else if (name.equals("total")) {
                    return target.getNodeDescriptor("amount");
                }
            } else if (target.getShortName().equals(INVOICE_TYPE)) {
                if (name.equals("total")) {
                    return target.getNodeDescriptor("amount");
                }
            }
            return super.getTargetNode(source, target);
        }

        /**
         * Determines how {@link IMObjectCopier} should treat an object.
         *
         * @param object  the source object
         * @param service the archetype service
         * @return <code>object</code> if the object shouldn't be copied,
         *         <code>null</code> if it should be replaced with
         *         <code>null</code>, or a new instance if the object should be
         *         copied
         */
        public IMObject getObject(IMObject object, IArchetypeService service) {
            IMObject result;
            if (object instanceof Act || object instanceof ActRelationship
                    || object instanceof Participation) {
                String shortName = object.getArchetypeId().getShortName();
                for (String[] map : TYPE_MAP) {
                    String orderType = map[0];
                    String invoiceType = map[1];
                    if (orderType.equals(shortName)) {
                        shortName = invoiceType;
                        break;
                    }
                }
                result = service.create(shortName);
                if (result == null) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.FailedToCreateArchetype,
                            shortName);
                }
            } else {
                result = object;
            }
            return result;
        }
    }

}
