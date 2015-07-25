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
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ORM_O01;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.hl7.io.Connectors;
import org.openvpms.hl7.io.MessageDispatcher;
import org.openvpms.hl7.laboratory.Laboratories;

import java.util.List;

/**
 * Service to process laboratory investigation cancellation events.
 *
 * @author Tim Anderson
 */
public class LaboratoryCancellationServiceImpl extends ServicesMessageReceiver {

    /**
     * The message processor.
     */
    private final ORMProcessor processor;


    /**
     * Constructs an {@link LaboratoryCancellationServiceImpl}.
     *
     * @param laboratories the laboratories
     * @param dispatcher   the dispatcher
     * @param connectors   the connectors
     * @param service      the archetype service
     * @param rules        the patient rules
     */
    public LaboratoryCancellationServiceImpl(Laboratories laboratories, MessageDispatcher dispatcher,
                                             Connectors connectors, IArchetypeService service,
                                             PatientRules rules, UserRules userRules) {
        super(laboratories, service, dispatcher, connectors);
        processor = new ORMProcessor(service, rules, userRules);
        listen();
    }

    /**
     * Determines if this can process a message.
     *
     * @param message an inbound HL7 message
     * @return {@code true} if this ReceivingApplication wishes to accept the message.
     */
    @Override
    public boolean canProcess(Message message) {
        return message instanceof ORM_O01;
    }

    /**
     * Processes a message.
     *
     * @param message  the message
     * @param location the practice location
     * @throws HL7Exception for any HL7 error
     */
    @Override
    public void process(Message message, IMObjectReference location) throws HL7Exception {
        List<Act> acts = process((ORM_O01) message, location);
        getService().save(acts);
    }

    /**
     * Processes a message.
     *
     * @param message  the message
     * @param location the practice location
     * @return the acts
     * @throws HL7Exception for any HL7 error
     */
    protected List<Act> process(ORM_O01 message, IMObjectReference location) throws HL7Exception {
        return processor.process(message, location);
    }

}
