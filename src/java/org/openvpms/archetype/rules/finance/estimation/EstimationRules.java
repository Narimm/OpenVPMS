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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.estimation;

import org.openvpms.archetype.rules.act.ActCopyHandler;
import org.openvpms.archetype.rules.act.DefaultActCopyHandler;
import org.openvpms.archetype.rules.act.EstimationActStatus;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.*;
import static org.openvpms.archetype.rules.finance.estimation.EstimationArchetypes.*;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_MEDICATION;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MEDICATION;
import static org.openvpms.archetype.rules.product.ProductArchetypes.PRODUCT_PARTICIPATION;
import static org.openvpms.archetype.rules.user.UserArchetypes.CLINICIAN_PARTICIPATION;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Estimation Rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EstimationRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>EstimationRules</tt>.
     */
    public EstimationRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>EstimationRules</tt>.
     *
     * @param service the archetype service
     */
    public EstimationRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Copies an estimation.
     * <p/>
     * The copy is saved.
     *
     * @param estimation the estimation to copy
     * @return the copy
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act copy(Act estimation) {
        return copy(estimation, estimation.getTitle());
    }

    /**
     * Copies an estimation.
     * <p/>
     * The copy is saved.
     *
     * @param estimation the estimation to copy
     * @param title the title of the copy
     * @return the copy
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act copy(Act estimation, String title) {
        IMObjectCopier copier = new IMObjectCopier(new DefaultActCopyHandler());
        List<IMObject> objects = copier.apply(estimation);
        Act copy = (Act) objects.get(0);
        copy.setTitle(title);
        service.save(objects);
        return (Act) objects.get(0);
    }

    /**
     * Invoices an estimation.
     * <p/>
     * The estimation's status is changed to <em>INVOICED</em> and both the
     * estimation and invoice are saved.
     *
     * @param estimation the estimation to invoice
     * @param clinician  the clinician to assign to the invoice. May be
     *                   <tt>null</tt>
     * @return the invoice
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct invoice(Act estimation, User clinician) {
        IMObjectCopier copier = new IMObjectCopier(new EstimationHandler());
        List<IMObject> objects = copier.apply(estimation);
        List<Act> items = new ArrayList<Act>();
        for (IMObject object : objects) {
            if (TypeHelper.isA(object, INVOICE_ITEM)) {
                Act item = (Act) object;
                if (clinician != null) {
                    ActBean bean = new ActBean(item, service);
                    bean.setParticipant(CLINICIAN_PARTICIPATION, clinician);
                }
                items.add((Act) object);
            }
        }

        FinancialAct invoice = (FinancialAct) objects.get(0);
        if (clinician != null) {
            ActBean bean = new ActBean(invoice, service);
            bean.setParticipant(CLINICIAN_PARTICIPATION, clinician);
        }

        // add any dispensing acts
        List<IMObject> dispensing = addDispensingActs(items);
        objects.addAll(dispensing);

        // update the estimation
        estimation.setStatus(EstimationActStatus.INVOICED);
        objects.add(estimation);

        service.save(objects);
        return invoice;
    }

    /**
     * Adds <em>act.patientMedication</em> acts for each invoice item
     * with a medication act.
     *
     * @param items the invoice items
     * @return the dispensing acts and related objects
     * @throws ArchetypeServiceException for any error
     */
    private List<IMObject> addDispensingActs(List<Act> items) {
        List<IMObject> result = new ArrayList<IMObject>();
        for (Act item : items) {
            ActBean bean = new ActBean(item, service);
            IMObjectReference product
                    = bean.getParticipantRef(PRODUCT_PARTICIPATION);
            if (TypeHelper.isA(product, MEDICATION)) {
                IMObjectCopier copier
                        = new IMObjectCopier(new DispensingHandler(), service);
                List<IMObject> objects = copier.apply(item);
                Act medication = (Act) objects.get(0);
                bean.addRelationship(DISPENSING_ITEM_RELATIONSHIP, medication);
                result.addAll(objects);
            }
        }
        return result;
    }


    private static class EstimationHandler extends ActCopyHandler {

        /**
         * Map of estimation types to their corresponding invoice types.
         */
        private static final String[][] TYPE_MAP = {
                {ESTIMATION, INVOICE},
                {ESTIMATION_ITEM, INVOICE_ITEM},
                {ESTIMATION_ITEM_RELATIONSHIP, INVOICE_ITEM_RELATIONSHIP},
        };

        /**
         * Creates a new <tt>EstimationHandler</tt>.
         */
        public EstimationHandler() {
            super(TYPE_MAP);
        }

        /**
         * Returns a target node for a given source node.
         *
         * @param source     the source archetype
         * @param sourceNode the source node
         * @param target     the target archetype
         * @return a node to copy source to, or <tt>null</tt> if the node
         *         shouldn't be copied
         */
        @Override
        protected NodeDescriptor getTargetNode(ArchetypeDescriptor source,
                                               NodeDescriptor sourceNode,
                                               ArchetypeDescriptor target) {
            String name = sourceNode.getName();
            if (TypeHelper.isA(target, INVOICE_ITEM)) {
                if (name.equals("highQty")) {
                    return target.getNodeDescriptor("quantity");
                } else if (name.equals("highUnitPrice")) {
                    return target.getNodeDescriptor("unitPrice");
                }
            } else if (TypeHelper.isA(target, INVOICE)) {
                if (name.equals("highTotal")) {
                    return target.getNodeDescriptor("amount");
                }
            }
            return super.getTargetNode(source, sourceNode, target);
        }
    }

    private static class DispensingHandler extends ActCopyHandler {

        private static String TYPE_MAP[][] = {{INVOICE_ITEM,
                                               PATIENT_MEDICATION}};

        /**
         * Creates a new <tt>DispensingHandler</tt>.
         */
        public DispensingHandler() {
            super(TYPE_MAP);
        }

        /**
         * Helper to determine if a node is copyable.
         * <p/>
         * For invoice items, this only copies the <em>quantity</em>,
         * <em>patient</em>, <em>product</em>, <em>author</em> and
         * <em>clinician<em> nodes.
         *
         * @param archetype the archetype descriptor
         * @param node      the node descriptor
         * @param source    if <tt>true</tt> the node is the source; otherwise
         *                  its the target
         * @return <tt>true</tt> if the node is copyable; otherwise
         *         <tt>false</tt>
         */
        @Override
        protected boolean isCopyable(ArchetypeDescriptor archetype,
                                     NodeDescriptor node, boolean source) {
            boolean result = super.isCopyable(archetype, node, source);
            if (result && TypeHelper.isA(archetype, INVOICE_ITEM)) {
                String name = node.getName();
                result = "quantity".equals(name) || "patient".equals(name)
                        || "product".equals(name) || "author".equals(name)
                        || "clinician".equals(name);
            }
            return result;
        }
    }
}
