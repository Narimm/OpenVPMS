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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.system.common.event.Listener;
import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.smartflow.client.FlowSheetException;
import org.openvpms.smartflow.event.EventDispatcher;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.HospitalizationList;
import org.openvpms.smartflow.model.Note;
import org.openvpms.smartflow.model.NotesList;
import org.openvpms.smartflow.model.Treatment;
import org.openvpms.smartflow.model.event.AdmissionEvent;
import org.openvpms.smartflow.model.event.DischargeEvent;
import org.openvpms.smartflow.model.event.Event;
import org.openvpms.smartflow.model.event.InventoryImportedEvent;
import org.openvpms.smartflow.model.event.MedicsImportedEvent;
import org.openvpms.smartflow.model.event.NotesEvent;
import org.openvpms.smartflow.model.event.TreatmentEvent;

import java.util.Collections;
import java.util.Map;

/**
 * Dispatches Smart Flow Sheet events.
 *
 * @author Tim Anderson
 */
public class DefaultEventDispatcher implements EventDispatcher {

    /**
     * The object mapper.
     */
    private final ObjectMapper mapper;

    /**
     * Listeners for specific messages.
     * These are held on to with soft references, to avoid memory leaks if the client doesn't de-register them.
     */
    private Map<String, Listener<Event>> listeners = Collections.synchronizedMap(
            new ReferenceMap<String, Listener<Event>>());

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(DefaultEventDispatcher.class);

    /**
     * Constructs a {@link DefaultEventDispatcher}.
     */
    public DefaultEventDispatcher() {
        mapper = new ObjectMapper();
    }

    /**
     * Dispatches the event associated with the specified message.
     *
     * @param message the message
     */
    @Override
    public void dispatch(BrokeredMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("messageID=" + message.getMessageId() + ", timeToLive=" + message.getTimeToLive() + "," +
                      "contentType=" + message.getContentType());
        }
        try {
            String content = IOUtils.toString(message.getBody());
            if (log.isDebugEnabled()) {
                log.debug("content=" + content);
            }
            Event event = mapper.readValue(content, Event.class);
            if (event instanceof AdmissionEvent) {
                admitted((AdmissionEvent) event);
            } else if (event instanceof DischargeEvent) {
                discharged((DischargeEvent) event);
            } else if (event instanceof TreatmentEvent) {
                treated((TreatmentEvent) event);
            } else if (event instanceof NotesEvent) {
                notesEntered((NotesEvent) event);
            } else if (event instanceof InventoryImportedEvent) {
                imported((InventoryImportedEvent) event);
            } else if (event instanceof MedicsImportedEvent) {
                imported((MedicsImportedEvent) event);
            }
        } catch (FlowSheetException exception) {
            throw exception;
        } catch (Exception exception) {
            Message error = FlowSheetMessages.failedToDeserializeMessage(message.getMessageId(),
                                                                         message.getContentType(),
                                                                         exception.getMessage());
            throw new FlowSheetException(error, exception);
        }
    }

    /**
     * Monitors for a single event with the specified id.
     *
     * @param id       the event identifier
     * @param listener the listener
     */
    @Override
    public void addListener(String id, Listener<Event> listener) {
        listeners.put(id, listener);
    }

    /**
     * Removes a listener for an event identifier.
     *
     * @param id the event identifier
     */
    @Override
    public void removeListener(String id) {
        listeners.remove(id);
    }

    /**
     * Invoked when one or more patients are admitted.
     *
     * @param event the admission event
     */
    protected void admitted(AdmissionEvent event) {
        if (log.isDebugEnabled()) {
            HospitalizationList list = event.getObject();
            if (list != null) {
                for (Hospitalization hospitalization : list.getHospitalizations()) {
                    log.debug("Admitted: " + hospitalization.getPatient().getName());
                }
            }
        }
    }

    /**
     * Invoked when one or more patients are discharged.
     *
     * @param event the discharge event
     */
    protected void discharged(DischargeEvent event) {
        HospitalizationList list = event.getObject();
        for (Hospitalization hospitalization : list.getHospitalizations()) {
            discharged(hospitalization);
        }
    }

    /**
     * Invoked when a patient is discharged.
     *
     * @param hospitalization the hospitalization
     */
    protected void discharged(Hospitalization hospitalization) {
        log.debug("Discharged: " + hospitalization.getPatient().getName());
    }

    /**
     * Invoked when one or more treatments take place.
     *
     * @param event the treatment event
     */
    protected void treated(TreatmentEvent event) {
        for (Treatment treatment : event.getObject()) {
            treated(treatment);
        }
    }

    /**
     * Invoked when a patient is treated.
     *
     * @param treatment the treatment
     */
    protected void treated(Treatment treatment) {

    }

    protected void notesEntered(NotesEvent event) {
        NotesList notes = event.getObject();
        if (notes != null && notes.getNotes() != null) {
            for (Note note : notes.getNotes()) {
                noteEntered(note);
            }
        }
    }

    protected void noteEntered(Note note) {

    }


    protected void imported(InventoryImportedEvent event) {
        String id = event.getObject().getId();
        handleEvent(id, event);
    }

    protected void imported(MedicsImportedEvent event) {
        String id = event.getObject().getId();
        handleEvent(id, event);
    }

    private void handleEvent(String id, Event event) {
        if (!StringUtils.isEmpty(id)) {
            Listener<Event> listener = listeners.remove(id);
            if (listener != null) {
                try {
                    listener.onEvent(event);
                } catch (Throwable exception) {
                    log.error("Failed to invoke listener event=" + event.getEventType(), exception);
                }
            }
        }
    }
}
