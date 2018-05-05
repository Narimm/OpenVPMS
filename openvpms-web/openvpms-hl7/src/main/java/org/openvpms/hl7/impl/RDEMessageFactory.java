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

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.EI;
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.RXO;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.patient.PatientContext;

import java.math.BigDecimal;
import java.util.Date;

import static org.openvpms.hl7.impl.PopulateHelper.populateDTM;

/**
 * Factory for RDE (pharmacy order) messages.
 *
 * @author Tim Anderson
 */
public class RDEMessageFactory extends AbstractMessageFactory {

    /**
     * Dispensing units node.
     */
    private static final String DISPENSING_UNITS = "dispensingUnits";

    /**
     * Selling units node.
     */
    private static final String SELLING_UNITS = "sellingUnits";


    /**
     * Constructs an {@link RDEMessageFactory}.
     *
     * @param messageContext the message context
     * @param service        the archetype service
     * @param lookups        the lookup service
     */
    public RDEMessageFactory(HapiContext messageContext, IArchetypeService service, ILookupService lookups) {
        super(messageContext, service, lookups);
    }

    /**
     * Creates a new order.
     *
     * @param context            the patient context
     * @param product            the product to order
     * @param quantity           the quantity to order
     * @param placerOrderNumber  the order identifier
     * @param sendingApplication the sending application. May be {@code null}
     * @param date               the order date
     * @param config             the message population configuration
     * @return a new message
     */
    public Message createOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                               String sendingApplication, Date date, HL7Mapping config) {
        return createOrder(context, "NW", product, quantity, placerOrderNumber, sendingApplication, date, config);
    }

    /**
     * Creates an update order.
     *
     * @param context            the patient context
     * @param product            the product to order
     * @param quantity           the quantity to order
     * @param placerOrderNumber  the order identifier
     * @param sendingApplication the sending application. May be {@code null}
     * @param date               the order date
     * @param config             the message population configuration
     * @return a new message
     */
    public Message updateOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                               String sendingApplication, Date date, HL7Mapping config) {
        return createOrder(context, "RP", product, quantity, placerOrderNumber, sendingApplication, date, config);
    }

    /**
     * Creates a cancel order.
     *
     * @param context            the patient context
     * @param product            the product to order
     * @param quantity           the quantity to order
     * @param placerOrderNumber  the order identifier
     * @param sendingApplication the sending application. May be {@code null}
     * @param config             the message population configuration
     * @param date               the order date
     * @return a new message
     */
    public Message cancelOrder(PatientContext context, Product product, BigDecimal quantity, long placerOrderNumber,
                               String sendingApplication, HL7Mapping config, Date date) {
        return createOrder(context, "CA", product, quantity, placerOrderNumber, sendingApplication, date, config);
    }

    /**
     * Creates a discontinue order.
     *
     * @param context            the patient context
     * @param product            the product to order
     * @param quantity           the quantity to order
     * @param placerOrderNumber  the order identifier
     * @param sendingApplication the sending application. May be {@code null}
     * @param config             the message population configuration
     * @param date               the order date
     * @return a new message
     */
    public Message discontinueOrder(PatientContext context, Product product, BigDecimal quantity,
                                    long placerOrderNumber, String sendingApplication, HL7Mapping config, Date date) {
        return createOrder(context, "DC", product, quantity, placerOrderNumber, sendingApplication, date, config);
    }

    /**
     * Creates an order message.
     *
     * @param context            the patient context
     * @param orderControl       the type of order
     * @param product            the product to order
     * @param quantity           the quantity to order
     * @param placerOrderNumber  the order identifier
     * @param sendingApplication the sending application. May be {@code null}
     * @param date               the order date
     * @param config             the message population configuration
     * @return a new message
     */
    private RDE_O11 createOrder(PatientContext context, String orderControl, Product product, BigDecimal quantity,
                                long placerOrderNumber, String sendingApplication, Date date, HL7Mapping config) {
        RDE_O11 rde;
        try {
            rde = new RDE_O11(getModelClassFactory());
            init(rde, "RDE", "O11");
            populate(rde.getPATIENT().getPID(), context, config);
            populate(rde.getPATIENT().getPATIENT_VISIT().getPV1(), context, config);
            ORC orc = rde.getORDER().getORC();
            orc.getOrderControl().setValue(orderControl);
            EI orderNumber = orc.getPlacerOrderNumber();
            orderNumber.getEntityIdentifier().setValue(Long.toString(placerOrderNumber));
            if (sendingApplication != null) {
                orderNumber.getNamespaceID().setValue(sendingApplication);
            }
            populateDTM(orc.getDateTimeOfTransaction().getTime(), date, config);
            if (context.getClinicianId() != -1) {
                PopulateHelper.populateClinician(orc.getEnteredBy(0), context);
            }
            RXO rxo = rde.getORDER().getORDER_DETAIL().getRXO();
            PopulateHelper.populateProduct(rxo.getRequestedGiveCode(), product);
            IMObjectBean bean = new IMObjectBean(product, getArchetypeService());
            String dispensingCode = getOptional(bean, DISPENSING_UNITS);
            if (dispensingCode != null) {
                String dispensingName = getLookupService().getName(product, DISPENSING_UNITS);
                PopulateHelper.populateCE(rxo.getRequestedGiveUnits(), dispensingCode, dispensingName);
            }
            String sellingCode = bean.getString(SELLING_UNITS);
            String dispensingInstructions = getOptional(bean, "dispInstructions");
            if (dispensingInstructions != null) {
                rxo.getProviderSAdministrationInstructions(0).getText().setValue(dispensingInstructions);
            }
            rxo.getRequestedDispenseAmount().setValue(quantity.toString());
            if (sellingCode != null) {
                String sellingName = getLookupService().getName(product, SELLING_UNITS);
                PopulateHelper.populateCE(rxo.getRequestedDispenseUnits(), sellingCode, sellingName);
            }
            populateAllergies(rde.getPATIENT(), context);
        } catch (Throwable exception) {
            throw new IllegalStateException(exception);
        }
        return rde;
    }

    /**
     * Returns the value of a node, if it is present.
     *
     * @param bean the bean
     * @param node the node
     * @return the node value. May be {@code null}
     */
    private String getOptional(IMObjectBean bean, String node) {
        return bean.hasNode(node) ? bean.getString(node) : null;
    }

}
