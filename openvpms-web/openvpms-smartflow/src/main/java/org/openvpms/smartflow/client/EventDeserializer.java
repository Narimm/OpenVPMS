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

package org.openvpms.smartflow.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.openvpms.smartflow.model.Anesthetics;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.Hospitalizations;
import org.openvpms.smartflow.model.InventoryItems;
import org.openvpms.smartflow.model.Medics;
import org.openvpms.smartflow.model.Notes;
import org.openvpms.smartflow.model.Treatment;
import org.openvpms.smartflow.model.Treatments;
import org.openvpms.smartflow.model.event.AdmissionEvent;
import org.openvpms.smartflow.model.event.AnestheticsEvent;
import org.openvpms.smartflow.model.event.DischargeEvent;
import org.openvpms.smartflow.model.event.Event;
import org.openvpms.smartflow.model.event.InventoryImportedEvent;
import org.openvpms.smartflow.model.event.MedicsImportedEvent;
import org.openvpms.smartflow.model.event.NotesEvent;
import org.openvpms.smartflow.model.event.TreatmentEvent;
import org.openvpms.smartflow.model.event.UnsupportedEvent;

import java.io.IOException;

/**
 * A deserializer for {@link Event}s.
 *
 * @author Tim Anderson
 */
public class EventDeserializer extends StdDeserializer<Event> {

    /**
     * Constructs an {@link EventDeserializer}.
     */
    protected EventDeserializer() {
        super(Event.class);
    }

    /**
     * Method that can be called to ask implementation to deserialize
     * JSON content into the value type this serializer handles.
     * Returned instance is to be constructed by method itself.
     * <p>
     * Pre-condition for this method is that the parser points to the
     * first event that is part of value to deserializer (and which
     * is never JSON 'null' literal, more on this below): for simple
     * types it may be the only value; and for structured types the
     * Object start marker or a FIELD_NAME.
     * </p>
     * <p>
     * The two possible input conditions for structured types result
     * from polymorphism via fields. In the ordinary case, Jackson
     * calls this method when it has encountered an OBJECT_START,
     * and the method implementation must advance to the next token to
     * see the first field name. If the application configures
     * polymorphism via a field, then the object looks like the following.
     * <pre>
     *      {
     *          "@class": "class name",
     *          ...
     *      }
     *  </pre>
     * Jackson consumes the two tokens (the <tt>@class</tt> field name
     * and its value) in order to learn the class and select the deserializer.
     * Thus, the stream is pointing to the FIELD_NAME for the first field
     * after the @class. Thus, if you want your method to work correctly
     * both with and without polymorphism, you must begin your method with:
     * <pre>
     *       if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
     *         jp.nextToken();
     *       }
     *  </pre>
     * This results in the stream pointing to the field name, so that
     * the two conditions align.
     * </p>
     *
     * Post-condition is that the parser will point to the last
     * event that is part of deserialized value (or in case deserialization
     * fails, event that was not recognized or usable, which may be
     * the same event as the one it pointed to upon call).
     *
     * Note that this method is never called for JSON null literal,
     * and thus deserializers need (and should) not check for it.
     *
     * @param parser  parser used for reading JSON content
     * @param context Context that can be used to access information about this deserialization activity.
     * @return the deserialized value
     */
    @Override
    public Event deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String clinicApiKey = null;
        String eventType = null;
        JsonToken currentToken;
        Event result = null;
        while ((currentToken = parser.nextValue()) != null) {
            switch (currentToken) {
                case VALUE_STRING:
                    switch (parser.getCurrentName()) {
                        case "clinicApiKey":
                            clinicApiKey = parser.getText();
                            break;
                        case "eventType":
                            eventType = parser.getText();
                            break;
                        default:
                            break;
                    }
                    break;
                case START_OBJECT:
                    if ("hospitalizations.created".equals(eventType)) {
                        result = createMultipleAdmission(parser, clinicApiKey, eventType);
                    } else if ("hospitalization.created".equals(eventType)) {
                        result = createAdmission(parser, clinicApiKey, eventType);
                    } else if ("hospitalizations.discharged".equals(eventType)) {
                        result = createMultipleDischarge(parser, clinicApiKey, eventType);
                    } else if ("hospitalization.discharged".equals(eventType)) {
                        result = createDischarge(parser, clinicApiKey, eventType);
                    } else if ("treatment.record_entered".equals(eventType)) {
                        result = createTreatment(parser, clinicApiKey, eventType);
                    } else if ("treatments.records_entered".equals(eventType)) {
                        result = createMultipleTreatments(parser, clinicApiKey, eventType);
                    } else if ("inventoryitems.imported".equals(eventType)) {
                        result = createInventoryImported(parser, clinicApiKey, eventType);
                    } else if ("medics.imported".equals(eventType)) {
                        result = createMedicsImported(parser, clinicApiKey, eventType);
                    } else if ("notes.entered".equals(eventType)) {
                        result = createNotesEvent(parser, clinicApiKey, eventType);
                    } else if ("anesthetics.finalized".equals(eventType)) {
                        result = createAnestheticsEvent(parser, clinicApiKey, eventType);
                    } else {
                        result = populate(new UnsupportedEvent(), clinicApiKey, eventType);
                        return result;
                    }
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    /**
     * Creates an admission event for multiple admissions.
     *
     * @param parser       the parser
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return a new multiple admission event
     * @throws IOException for any error
     */
    protected Event createMultipleAdmission(JsonParser parser, String clinicApiKey, String eventType)
            throws IOException {
        AdmissionEvent event = populate(new AdmissionEvent(), clinicApiKey, eventType);
        event.setObject(parser.readValueAs(Hospitalizations.class));
        return event;
    }

    /**
     * Creates an admission event.
     *
     * @param parser       the parser
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return a new admission event
     * @throws IOException for any error
     */
    protected Event createAdmission(JsonParser parser, String clinicApiKey, String eventType) throws IOException {
        AdmissionEvent event = populate(new AdmissionEvent(), clinicApiKey, eventType);
        event.setObject(parser.readValueAs(Hospitalizations.class));
        return event;
    }

    /**
     * Creates a discharge event.
     *
     * @param parser       the parser
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return a new discharge event
     * @throws IOException for any error
     */
    protected Event createDischarge(JsonParser parser, String clinicApiKey, String eventType) throws IOException {
        DischargeEvent event = populate(new DischargeEvent(), clinicApiKey, eventType);
        Hospitalization hospitalization = parser.readValueAs(Hospitalization.class);
        Hospitalizations list = new Hospitalizations();
        list.getHospitalizations().add(hospitalization);
        event.setObject(list);
        return event;
    }

    /**
     * Creates a discharge event for multiple discharges.
     *
     * @param parser       the parser
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return a new multiple discharge event
     * @throws IOException for any error
     */
    protected Event createMultipleDischarge(JsonParser parser, String clinicApiKey, String eventType)
            throws IOException {
        DischargeEvent event = populate(new DischargeEvent(), clinicApiKey, eventType);
        event.setObject(parser.readValueAs(Hospitalizations.class));
        return event;
    }

    /**
     * Creates a treatment event.
     *
     * @param parser       the parser
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return a new treatment event
     * @throws IOException for any error
     */
    private Event createTreatment(JsonParser parser, String clinicApiKey, String eventType) throws IOException {
        TreatmentEvent event = populate(new TreatmentEvent(), clinicApiKey, eventType);
        Treatment treatment = parser.readValueAs(Treatment.class);
        Treatments list = new Treatments();
        list.getTreatments().add(treatment);
        event.setObject(list);
        return event;
    }

    /**
     * Creates a treatment event for multiple treatments.
     *
     * @param parser       the parser
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return a new treatment event
     * @throws IOException for any error
     */
    private Event createMultipleTreatments(JsonParser parser, String clinicApiKey, String eventType)
            throws IOException {
        TreatmentEvent event = populate(new TreatmentEvent(), clinicApiKey, eventType);
        Treatments treatments = parser.readValueAs(Treatments.class);
        event.setObject(treatments);
        return event;
    }

    /**
     * Creates a notes event.
     *
     * @param parser       the parser
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return a new notes event
     * @throws IOException for any error
     */
    private Event createNotesEvent(JsonParser parser, String clinicApiKey, String eventType) throws IOException {
        NotesEvent event = populate(new NotesEvent(), clinicApiKey, eventType);
        Notes notes = parser.readValueAs(Notes.class);
        event.setObject(notes);
        return event;
    }

    /**
     * Creates an anesthetics event.
     *
     * @param parser       the parser
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return a new anesthetics event
     * @throws IOException for any error
     */
    private Event createAnestheticsEvent(JsonParser parser, String clinicApiKey, String eventType) throws IOException {
        AnestheticsEvent event = populate(new AnestheticsEvent(), clinicApiKey, eventType);
        Anesthetics anesthetics = parser.readValueAs(Anesthetics.class);
        event.setObject(anesthetics);
        return event;
    }

    /**
     * Creates an inventory imported event.
     *
     * @param parser       the parser
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return a new inventory imported event
     * @throws IOException for any error
     */
    private Event createInventoryImported(JsonParser parser, String clinicApiKey, String eventType) throws IOException {
        InventoryImportedEvent event = populate(new InventoryImportedEvent(), clinicApiKey, eventType);
        InventoryItems items = parser.readValueAs(InventoryItems.class);
        event.setObject(items);
        return event;
    }

    /**
     * Creates a medics imported event.
     *
     * @param parser       the parser
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return a new medics imported event
     * @throws IOException for any error
     */
    private Event createMedicsImported(JsonParser parser, String clinicApiKey, String eventType) throws IOException {
        MedicsImportedEvent event = populate(new MedicsImportedEvent(), clinicApiKey, eventType);
        Medics medics = parser.readValueAs(Medics.class);
        event.setObject(medics);
        return event;
    }

    /**
     * Populates an event.
     *
     * @param event        the event to populate
     * @param clinicApiKey the clinic API key
     * @param eventType    the event type
     * @return the event
     */
    private <T extends Event> T populate(T event, String clinicApiKey, String eventType) {
        event.setClinicApiKey(clinicApiKey);
        event.setEventType(eventType);
        return event;
    }

}
