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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.EI;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.order.CustomerOrder;
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

import static org.openvpms.archetype.rules.finance.order.CustomerOrder.addNote;

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
    protected Act getOrder(String orderShortName, ORC orc, ActBean bean, CustomerOrder state) {
        Act result = null;
        EI placerOrderNumber = orc.getPlacerOrderNumber();
        String entityIdentifier = placerOrderNumber.getEntityIdentifier().getValue();
        if (StringUtils.isEmpty(entityIdentifier)) {
            addNote(bean, "No Placer Order Number specified. Order placed outside OpenVPMS");
        } else {
            long id = getOrderId(placerOrderNumber);
            if (id >= 0) {
                IMObjectReference reference = new IMObjectReference(orderShortName, id);
                result = (Act) service.get(reference);
                if (result != null) {
                    Party patient = state.getPatient();
                    if (patient != null) {
                        ActBean orderBean = new ActBean(result, service);
                        IMObjectReference patientRef = orderBean.getNodeParticipantRef("patient");
                        if (patientRef != null && !ObjectUtils.equals(patient.getObjectReference(), patientRef)) {
                            String displayName = DescriptorHelper.getDisplayName(orderShortName, service);
                            addNote(bean, "Patient is different to that in the original " + displayName + ". Was '"
                                          + ArchetypeQueryHelper.getName(patientRef, service)
                                          + "' (" + patientRef.getId() + ")"
                                          + ". Now '" + patient.getName() + "' (" + patient.getId() + ")");
                        }
                    }
                } else {
                    String note = "Order with Placer Order Number '" + entityIdentifier + "'";
                    String namespaceId = placerOrderNumber.getNamespaceID().getValue();
                    if (!StringUtils.isEmpty(namespaceId)) {
                        note += " submitted by " + namespaceId;
                    }
                    note += " has no corresponding " + DescriptorHelper.getDisplayName(orderShortName);
                    addNote(bean, note);
                }
            } else {
                String note = "Order with Placer Order Number '" + entityIdentifier + "'";
                String namespaceId = placerOrderNumber.getNamespaceID().getValue();
                if (!StringUtils.isEmpty(namespaceId)) {
                    note += " submitted by " + namespaceId;
                }
                note += " was placed outside OpenVPMS";
                addNote(bean, note);
            }
        }
        return result;
    }

    /**
     * Returns the order identifier for a Placer Order Number.
     * <p/>
     * Note that as of OpenVPMS 1.9, orders originating in both Cubex and SFS use alphanumeric placer order numbers.
     * As a result, there will be no id collisions with these systems. However if an external system uses numeric
     * identifiers there will be a requirement to check the Sending Application (i.e. namespace id) included in
     * the Placer Order Number to determine where it originated.
     *
     * @param placerOrderNumber the placer order number
     * @return the identifier corresponding to an OpenVPMS order, or {@code -1} if it is not specified or was placed by
     * a different system
     */
    protected long getOrderId(EI placerOrderNumber) {
        return HL7MessageHelper.getId(placerOrderNumber);
    }

    /**
     * Creates a new {@link CustomerOrder} using the PID segment.
     *
     * @param pid      the pid
     * @param location the practice location reference
     * @return a new state
     * @throws HL7Exception if the patient does not exist
     */
    protected CustomerOrder createState(PID pid, IMObjectReference location) throws HL7Exception {
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

    /**
     * Creates state for an order.
     *
     * @param patient  the patient. May be {@code null}
     * @param customer the customer. May be {@code null}
     * @param note     the note. May be {@code null}
     * @param location the practice location. May be {@code null}
     * @param service  the archetype service
     * @return a new {@link CustomerOrder}
     */
    protected abstract CustomerOrder createState(Party patient, Party customer, String note,
                                              IMObjectReference location, IArchetypeService service);

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the clinician, given their id.
     *
     * @param id the clinician identifier
     * @return the clinician identifier, or {@code null} if it is not found
     */
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
