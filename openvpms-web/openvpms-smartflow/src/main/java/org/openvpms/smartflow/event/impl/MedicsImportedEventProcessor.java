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

package org.openvpms.smartflow.event.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.smartflow.model.Medic;
import org.openvpms.smartflow.model.Medics;
import org.openvpms.smartflow.model.event.MedicsImportedEvent;

import java.util.List;

/**
 * Processor for {@link MedicsImportedEvent}.
 *
 * @author Tim Anderson
 */
public class MedicsImportedEventProcessor extends EventProcessor<MedicsImportedEvent> {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(MedicsImportedEventProcessor.class);

    /**
     * Constructs an {@link MedicsImportedEventProcessor}.
     *
     * @param service the archetype service
     */
    public MedicsImportedEventProcessor(IArchetypeService service) {
        super(service);
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    @Override
    public void process(MedicsImportedEvent event) {
        Medics object = event.getObject();
        if (object != null && object.getMedics() != null) {
            imported(object.getMedics());
        }
    }

    /**
     * Process imported medics.
     *
     * @param medics the imported medics
     */
    private void imported(List<Medic> medics) {
        for (Medic medic : medics) {
            if (medic.getAsyncOperationStatus() != null && medic.getAsyncOperationStatus() < 0) {
                log.error("Failed to synchronise medic=[id=" + medic.getMedicId() + ", name=" + medic.getName()
                          + "]: " + medic.getAsyncOperationMessage());
            } else {
                log.info("Synchronised medic=[id=" + medic.getMedicId() + ", name=" + medic.getName() + "]");
            }
        }
    }
}
