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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import static org.openvpms.hl7.impl.OrderState.addNote;

/**
 * Base class for order message processors.
 *
 * @author Tim Anderson
 */
abstract class OrderMessageProcessor {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The user rules.
     */
    private final UserRules userRules;

    /**
     * Constructs a {@link ORMProcessor}.
     *
     * @param service the archetype service
     * @param rules   the patient rules
     */
    public OrderMessageProcessor(IArchetypeService service, PatientRules rules, UserRules userRules) {
        this.service = service;
        this.rules = rules;
        this.userRules = userRules;
    }

    /**
     * Returns a reference to the original order, adding a note if one is not present.
     *
     * @param orderShortName the order archetype short name
     * @param orc            the order segment
     * @param bean           the act
     * @param state          the state
     * @return the original order
     */
    protected Act getOrder(String orderShortName, ORC orc, ActBean bean, OrderState state) {
        Act result = null;
        long id = HL7MessageHelper.getId(orc.getPlacerOrderNumber());

        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(orderShortName, id);
            result = (Act) service.get(reference);
            Party patient = state.getPatient();
            if (result != null && patient != null) {
                ActBean orderBean = new ActBean(result, service);
                IMObjectReference patientRef = orderBean.getNodeParticipantRef("patient");
                if (patientRef != null && !ObjectUtils.equals(patient.getObjectReference(), patientRef)) {
                    String displayName = DescriptorHelper.getDisplayName(orderShortName, service);
                    addNote(bean, "Patient is different to that in the original " + displayName + ". Was '"
                                  + ArchetypeQueryHelper.getName(patientRef, service) + "' (" + patientRef.getId() + ")"
                                  + ". Now '" + patient.getName() + "' (" + patient.getId() + ")");
                }
            }
        } else {
            addNote(bean, "Unknown Placer Order Number: '" + orc.getPlacerOrderNumber().getEntityIdentifier() + "'");
        }
        return result;
    }

    /**
     * Creates a new {@link OrderState} using the PID segment.
     *
     * @param pid      the pid
     * @param location the practice location reference
     * @return a new state
     * @throws HL7Exception if the patient does not exist
     */
    protected OrderState createState(PID pid, IMObjectReference location) throws HL7Exception {
        Party patient = null;
        Party customer = null;
        long id;
        if (pid.getPatientIdentifierListReps() != 0) {
            // use PID-3
            id = HL7MessageHelper.getId(pid.getPatientIdentifierList(0));
        } else {
            // use PID-2
            id = HL7MessageHelper.getId(pid.getPatientID());
        }
        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(PatientArchetypes.PATIENT, id);
            patient = getPatient(reference);
        }
        String note = null;
        if (patient == null) {
            XPN xpn = pid.getPatientName(0);
            String firstName = xpn.getGivenName().getValue();
            String lastName = xpn.getFamilyName().getSurname().getValue();
            String name;
            if (!StringUtils.isEmpty(lastName) && !StringUtils.isEmpty(firstName)) {
                name = firstName + " " + lastName;
            } else if (!StringUtils.isEmpty(lastName)) {
                name = lastName;
            } else {
                name = firstName;
            }
            note = "Unknown patient, Id='" + pid.getPatientID().getIDNumber() + "', name='" + name + "'";
        } else {
            customer = rules.getOwner(patient);
        }
        return createState(patient, customer, note, location, service);
    }

    protected abstract OrderState createState(Party patient, Party customer, String note,
                                              IMObjectReference location, IArchetypeService service);

    protected IArchetypeService getService() {
        return service;
    }

    protected User getClinician(long id) {
        IMObjectReference reference = new IMObjectReference(UserArchetypes.USER, id);
        User user = (User) service.get(reference);
        return (user != null && userRules.isClinician(user)) ? user : null;
    }

    /**
     * Returns a patient given its reference.
     *
     * @param reference the patient reference
     * @return the corresponding patient or {@code null} if one is found
     */
    private Party getPatient(IMObjectReference reference) {
        return (Party) service.get(reference);
    }

}
