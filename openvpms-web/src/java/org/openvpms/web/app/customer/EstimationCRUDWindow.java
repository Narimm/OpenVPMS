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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer;

import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActCopyHandler;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.util.AbstractIMObjectCopyHandler;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectCopier;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.resource.util.Messages;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import java.math.BigDecimal;
import java.util.Date;


/**
 * CRUD window for estimation acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EstimationCRUDWindow extends CustomerActCRUDWindow {

    /**
     * The copy button.
     */
    private Button _copy;

    /**
     * The invoice button.
     */
    private Button _invoice;

    /**
     * Copy button identifier.
     */
    private static final String COPY_ID = "copy";

    /**
     * Invoice button identifier.
     */
    private static final String INVOICE_ID = "invoice";

    /**
     * Estimation short name.
     */
    private static final String ESTIMATION_TYPE = "act.customerEstimation";

    /**
     * Estimation item short name.
     */
    private static final String ESTIMATION_ITEM_TYPE = "act.customerEstimationItem";

    /**
     * Estimation item relationship short name.
     */
    private static final String ESTIMATION_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerEstimationItem";

    /**
     * Invoice act short name.
     */
    private static final String INVOICE_TYPE
            = "act.customerAccountChargesInvoice";

    /**
     * Invoice act item short name.
     */
    private static final String INVOICE_ITEM_TYPE
            = "act.customerAccountInvoiceItem";

    /**
     * Invoice act item relationship short name.
     */
    private static final String INVOICE_ITEM_RELATIONSHIP_TYPE
            = "actRelationship.customerAccountInvoiceItem";


    /**
     * Create a new <code>EstimationCRUDWindow</code>.
     *
     * @param type         display name for the types of objects that this may
     *                     create
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public EstimationCRUDWindow(String type, String refModelName,
                                String entityName, String conceptName) {
        super(type, new ShortNameList(refModelName, entityName, conceptName));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        _copy = ButtonFactory.create(COPY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onCopy();
            }
        });
        _invoice = ButtonFactory.create(INVOICE_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onInvoice();
            }
        });
        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
        buttons.add(getPrintButton());
        buttons.add(_copy);
        buttons.add(_invoice);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        Row buttons = getButtons();
        buttons.removeAll();
        if (enable) {
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            buttons.add(getPrintButton());
            buttons.add(_copy);
            buttons.add(_invoice);
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {
        IMObject object = getObject();
        try {
            IMObjectCopier copier = new IMObjectCopier(new ActCopyHandler());
            Act act = (Act) copier.copy(object);
            act.setStatus(INPROGRESS_STATUS);
            act.setActivityStartTime(new Date());
            setPrintStatus(act, false);
            SaveHelper.save(act);
            setObject(act);
            CRUDWindowListener listener = getListener();
            if (listener != null) {
                listener.saved(act, false);
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("customer.estimation.copy.failed");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Invoked when the 'invoice' button is pressed.
     */
    protected void onInvoice() {
        final Act act = (Act) getObject();
        String title = Messages.get("customer.estimation.invoice.title");
        String message = Messages.get("customer.estimation.invoice.message");
        ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addActionListener(ConfirmationDialog.OK_ID,
                                 new ActionListener() {
                                     public void actionPerformed(
                                             ActionEvent event) {
                                         invoice(act);
                                     }
                                 });
        dialog.show();
    }

    /**
     * Invoice out an estimation to the customer.
     *
     * @param estimation the estimation
     */
    private void invoice(Act estimation) {
        try {
            IMObjectCopier copier = new IMObjectCopier(new InvoiceHandler());
            Act invoice = (Act) copier.copy(estimation);
            invoice.setStatus(INPROGRESS_STATUS);
            invoice.setActivityStartTime(new Date());
            setPrintStatus(invoice, false);
            calcAmount(invoice);
            SaveHelper.save(invoice);

            if (!estimation.getStatus().equals(POSTED_STATUS)) {
                estimation.setStatus(POSTED_STATUS);
                SaveHelper.save(estimation);
                setObject(estimation);
                CRUDWindowListener listener = getListener();
                if (listener != null) {
                    listener.saved(estimation, false);
                }
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("customer.estimation.invoice.failed");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Calculate the act total.
     *
     * @param act the act
     */
    private void calcAmount(Act act) {
        // todo - workaround for OVPMS-211
        ArchetypeDescriptor invoiceDesc
                = DescriptorHelper.getArchetypeDescriptor(INVOICE_TYPE);
        NodeDescriptor totalDesc = invoiceDesc.getNodeDescriptor("amount");
        BigDecimal total = ActHelper.sum(act, "total");
        // @todo - workaround for OBF-55
        totalDesc.setValue(act, new Money(total.toString()));
    }

    private static class InvoiceHandler extends AbstractIMObjectCopyHandler {

        /**
         * Map of invoice types to their corresponding credit types.
         */
        private static final String[][] TYPE_MAP = {
                {ESTIMATION_TYPE, INVOICE_TYPE},
                {ESTIMATION_ITEM_TYPE, INVOICE_ITEM_TYPE},
                {ESTIMATION_ITEM_RELATIONSHIP_TYPE, INVOICE_ITEM_RELATIONSHIP_TYPE},
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
            if (target.getShortName().equals(INVOICE_ITEM_TYPE)) {
                String name = source.getName();
                if (name.equals("highQty")) {
                    return target.getNodeDescriptor("quantity");
                } else if (name.equals("highUnitPrice")) {
                    return target.getNodeDescriptor("unitPrice");
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
                    String estimationType = map[0];
                    String invoiceType = map[1];
                    if (estimationType.equals(shortName)) {
                        shortName = invoiceType;
                        break;
                    }
                }
                result = service.create(shortName);
                if (result == null) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.FailedToCreateArchetype,
                            new String[]{shortName});
                }
            } else {
                result = object;
            }
            return result;
        }
    }
}
