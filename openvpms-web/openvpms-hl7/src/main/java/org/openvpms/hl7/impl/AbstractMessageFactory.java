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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.AbstractMessage;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.v25.segment.AL1;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.PatientContext;

import java.io.IOException;
import java.util.List;

/**
 * Base class for {@code Message} factories.
 *
 * @author Tim Anderson
 */
abstract class AbstractMessageFactory {

    /**
     * The message context.
     */
    private final HapiContext messageContext;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * PID segment populator.
     */
    private final PIDPopulator pidPopulator;

    /**
     * PV1 segment populator.
     */
    private final PV1Populator pv1Populator;


    /**
     * Constructs an {@link AbstractMessageFactory}.
     *
     * @param messageContext the message context
     * @param service        the archetype service
     * @param lookups        the lookup service
     */
    public AbstractMessageFactory(HapiContext messageContext, IArchetypeService service, ILookupService lookups) {
        this.messageContext = messageContext;
        this.service = service;
        this.lookups = lookups;
        pidPopulator = new PIDPopulator(service, lookups);
        pv1Populator = new PV1Populator();
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the lookup service.
     *
     * @return the lookup service
     */
    protected ILookupService getLookupService() {
        return lookups;
    }

    /**
     * Returns the message model class factory.
     *
     * @return the message model class factory
     */
    protected ModelClassFactory getModelClassFactory() {
        return messageContext.getModelClassFactory();
    }

    /**
     * Initialises a message.
     *
     * @param message      the message
     * @param messageCode  the message code
     * @param triggerEvent the trigger event
     * @throws IOException  for any I/O error
     * @throws HL7Exception for any HL7 error
     */
    protected void init(AbstractMessage message, String messageCode, String triggerEvent)
            throws IOException, HL7Exception {
        message.setParser(messageContext.getGenericParser());
        message.initQuickstart(messageCode, triggerEvent, "P");
    }

    /**
     * Populates a PID segment.
     *
     * @param pid     the segment
     * @param context the patient context
     * @param config  the message population configuration
     * @throws HL7Exception for any error
     */
    protected void populate(PID pid, PatientContext context, MessageConfig config) throws HL7Exception {
        pidPopulator.populate(pid, context, config);
    }

    /**
     * Populates a PV1 segment.
     *
     * @param pv1     the segment
     * @param context the patient context
     * @param config  the message population configuration
     * @throws HL7Exception for any error
     */
    protected void populate(PV1 pv1, PatientContext context, MessageConfig config) throws HL7Exception {
        pv1Populator.populate(pv1, context, config);
    }

    /**
     * Adds patient allergies to a message group.
     *
     * @param group   the message group
     * @param context the patient context
     * @throws HL7Exception
     */
    protected void populateAllergies(Group group, PatientContext context) throws HL7Exception {
        List<Act> allergies = context.getAllergies();
        if (!allergies.isEmpty()) {
            for (int i = 0; i < allergies.size(); ++i) {
                AL1 al1 = (AL1) group.get("AL1", i);
                populateAllergy(al1, allergies.get(i), i + 1);
            }
        }
    }

    /**
     * Populates an allergy segment.
     *
     * @param al1 the segment
     * @param act the alert act
     * @param id  the segment identifier
     * @throws DataTypeException for any error
     */
    protected void populateAllergy(AL1 al1, Act act, int id) throws DataTypeException {
        al1.getSetIDAL1().setValue(Integer.toString(id));
        IMObjectBean bean = new IMObjectBean(act, getArchetypeService());
        al1.getAllergenCodeMnemonicDescription().getText().setValue(bean.getString("reason"));

        al1.getAllergenTypeCode().getIdentifier().setValue("MA");
        // Miscellaneous allergy - 3.4.6.2 AL1-2 Allergen Type Code

        al1.getAllergySeverityCode().getIdentifier().setValue("U");
        // Unknown -  3.4.6.4 AL-4 Allergy Severity Code. Could use the alert severity but not really meaningful
        // as it applies to all allergies

        al1.getAllergyReactionCode(0).setValue(bean.getString("notes"));
    }

}
