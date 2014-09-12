package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.NM;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.ADT_A03;
import ca.uhn.hl7v2.model.v25.message.ADT_A09;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.PatientContext;

import java.math.BigDecimal;

/**
 * Factory for ADT (Admit, Discharge, Transfer) messages.
 *
 * @author Tim Anderson
 */
public class ADTMessageFactory extends AbstractMessageFactory {

    /**
     * LOINC code for measured body weight.
     */
    private static final String BODY_WEIGHT_MEASURED = "3141-9";

    /**
     * Constructs an {@link ADTMessageFactory}.
     *
     * @param messageContext the message context
     * @param service        the archetype service
     * @param lookups        the lookup service
     */
    public ADTMessageFactory(HapiContext messageContext, IArchetypeService service, ILookupService lookups) {
        super(messageContext, service, lookups);
    }

    /**
     * Creates an ADT A01 message.
     *
     * @param context the patient context
     * @return a new message
     */
    public Message createAdmit(PatientContext context) {
        return createADT_A01(context, "A01");
    }

    /**
     * Creates an ADT A11 message.
     *
     * @param context the patient context
     * @return a new message
     */
    public Message createCancelAdmit(PatientContext context) {
        ADT_A09 adt = new ADT_A09(getModelClassFactory());
        try {
            init(adt, "ADT", "A11");
            populate(adt.getPID(), context);
            populate(adt.getPV1(), context);

            BigDecimal weight = context.getPatientWeight();
            if (weight != null) {
                OBX obx = adt.getOBX(0);
                populateWeight(obx, context, adt, weight);
            }
        } catch (Exception exception) {
            throw new IllegalStateException(exception.getMessage());
        }
        return adt;
    }

    /**
     * Creates an ADT A03 message.
     *
     * @param context the patient context
     * @return a new message
     */
    public Message createDischarge(PatientContext context) {
        ADT_A03 adt = new ADT_A03(getModelClassFactory());
        try {
            init(adt, "ADT", "A03");
            populate(adt.getPID(), context);
            populate(adt.getPV1(), context);

            BigDecimal weight = context.getPatientWeight();
            if (weight != null) {
                OBX obx = adt.getOBX(0);
                populateWeight(obx, context, adt, weight);
            }
            populateAllergies(adt, context);
        } catch (Exception exception) {
            throw new IllegalStateException(exception.getMessage());
        }
        return adt;
    }

    /**
     * Creates an ADT A08 message.
     *
     * @param context the patient context
     * @return a new message
     */
    public Message createUpdate(PatientContext context) {
        return createADT_A01(context, "A08");
    }

    /**
     * Creates an {@code ADT_A01}.
     *
     * @param context      the patient context
     * @param triggerEvent the trigger event
     * @return a new message
     */
    private Message createADT_A01(PatientContext context, String triggerEvent) {
        ADT_A01 adt = new ADT_A01(getModelClassFactory());
        try {
            init(adt, "ADT", triggerEvent);
            populate(adt.getPID(), context);
            populate(adt.getPV1(), context);

            BigDecimal weight = context.getPatientWeight();
            if (weight != null) {
                OBX obx = adt.getOBX(0);
                populateWeight(obx, context, adt, weight);
            }
            populateAllergies(adt, context);
        } catch (Exception exception) {
            throw new IllegalStateException(exception.getMessage());
        }
        return adt;
    }

    /**
     * Populates an OBX segment with the patient weight.
     *
     * @param obx     the segment to populate
     * @param context the patient context
     * @param message the parent message
     * @param weight  the weight, in kilograms
     * @throws DataTypeException for any data error
     */
    private void populateWeight(OBX obx, PatientContext context, Message message, BigDecimal weight)
            throws DataTypeException {
        obx.getSetIDOBX().setValue("1");
        obx.getValueType().setValue("NM");
        CE identifier = obx.getObservationIdentifier();
        identifier.getIdentifier().setValue(BODY_WEIGHT_MEASURED);
        identifier.getText().setValue("BODY WEIGHT MEASURED");
        identifier.getNameOfCodingSystem().setValue("LN");
        NM nm = new NM(message);
        Varies observationValue = obx.getObservationValue(0);
        nm.setValue(weight.toString());
        observationValue.setData(nm);
        obx.getDateTimeOfTheObservation().getTime().setValue(context.getWeighDate());

        obx.getUnits().getIdentifier().setValue("kg");
        obx.getUnits().getText().setValue("kilogram");    // ISO 2955-1983
    }

}
