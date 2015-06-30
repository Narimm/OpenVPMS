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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v25.message.ORM_O01;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.patient.PatientContext;

import java.util.Date;

import static org.openvpms.hl7.impl.PopulateHelper.populateDTM;

/**
 * Factory for ORM messages.
 *
 * @author Tim Anderson
 */
public class ORMMessageFactory extends AbstractMessageFactory {

    /**
     * Constructs an {@link ORMMessageFactory}.
     *
     * @param messageContext the message context
     * @param service        the archetype service
     * @param lookups        the lookup service
     */
    public ORMMessageFactory(HapiContext messageContext, IArchetypeService service, ILookupService lookups) {
        super(messageContext, service, lookups);
    }

    /**
     * Creates an order.
     *
     * @param context           the patient context
     * @param placerOrderNumber the placer order number
     * @param serviceId         the universal service identifier
     * @param date              the order date
     * @param config            the mapping configuration
     * @return new order
     */
    public ORM_O01 createOrder(PatientContext context, long placerOrderNumber, String serviceId, Date date,
                               HL7Mapping config) {
        ORM_O01 orm;
        orm = create(context, "NW", placerOrderNumber, serviceId, date, config);
        return orm;
    }

    /**
     * Cancels an order.
     *
     * @param context           the patient context
     * @param placerOrderNumber the placer order number
     * @param serviceId         the universal service identifier
     * @param date              the order date
     * @param config            the mapping configuration
     * @return new order
     */
    public ORM_O01 cancelOrder(PatientContext context, long placerOrderNumber, String serviceId, Date date,
                               HL7Mapping config) {
        ORM_O01 orm;
        orm = create(context, "CA", placerOrderNumber, serviceId, date, config);
        return orm;
    }

    /**
     * Creates an order.
     *
     * @param context           the patient context
     * @param orderControl      the type of order
     * @param placerOrderNumber the placer order number
     * @param serviceId         the universal service identifier
     * @param date              the order date
     * @param config            the mapping configuration
     * @return new order
     */
    private ORM_O01 create(PatientContext context, String orderControl, long placerOrderNumber, String serviceId,
                           Date date, HL7Mapping config) {
        ORM_O01 orm;
        try {
            orm = new ORM_O01(getModelClassFactory());
            init(orm, "ORM", "O01");
            populate(orm.getPATIENT().getPID(), context, config);
            populate(orm.getPATIENT().getPATIENT_VISIT().getPV1(), context, config);
            ORC orc = orm.getORDER().getORC();
            orc.getOrderControl().setValue(orderControl);
            String number = Long.toString(placerOrderNumber);
            orc.getPlacerOrderNumber().getEntityIdentifier().setValue(number);
            populateDTM(orc.getDateTimeOfTransaction().getTime(), date, config);
            OBR obr = orm.getORDER().getORDER_DETAIL().getOBR();
            obr.getSetIDOBR().setValue("1");
            obr.getPlacerOrderNumber().getEntityIdentifier().setValue(number);
            obr.getUniversalServiceIdentifier().getIdentifier().setValue(serviceId);
            populateDTM(obr.getRequestedDateTime().getTime(), date, config);
            if (context.getClinicianId() != -1) {
                PopulateHelper.populateClinician(orc.getOrderingProvider(0), context);
            }
            populateAllergies(orm.getPATIENT(), context);
        } catch (Throwable exception) {
            throw new IllegalStateException(exception);
        }
        return orm;
    }

}
