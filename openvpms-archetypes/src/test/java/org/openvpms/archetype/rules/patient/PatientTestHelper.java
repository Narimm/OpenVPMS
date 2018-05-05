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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

import org.apache.commons.lang.WordUtils;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.openvpms.archetype.test.TestHelper.create;
import static org.openvpms.archetype.test.TestHelper.getLookup;
import static org.openvpms.archetype.test.TestHelper.save;

/**
 * Patient test helper methods.
 *
 * @author Tim Anderson
 */
public class PatientTestHelper {

    /**
     * Creates a new patient.
     *
     * @param name        the patient name
     * @param species     the patient species code
     * @param breed       the patient breed code
     * @param dateOfBirth the patient's date of birth. May be {@code null}
     * @param owner       the patient owner
     * @return a new patient
     */
    public static Party createPatient(String name, String species, String breed, Date dateOfBirth, Party owner) {
        Party patient = TestHelper.createPatient(owner);
        patient.setName(name);
        Lookup speciesLookup = getLookup(PatientArchetypes.SPECIES, species,
                                         WordUtils.capitalize(species.toLowerCase()), true);
        Lookup breedLookup = getLookup(PatientArchetypes.BREED, breed, speciesLookup,
                                       "lookupRelationship.speciesBreed");
        breedLookup.setName(WordUtils.capitalize(breed.toLowerCase()));
        save(breedLookup);
        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("species", species);
        bean.setValue("breed", breed);
        bean.setValue("dateOfBirth", dateOfBirth);
        bean.save();
        return patient;
    }

    /**
     * Creates a new patient.
     *
     * @param name        the patient name
     * @param species     the patient species code
     * @param breed       the patient breed code
     * @param sex         the patient sex
     * @param dateOfBirth the patient's date of birth. May be {@code null}
     * @param microchip   the microchip. May be {@code nul}
     * @param colour      the colour. May be {@code null}
     * @param owner       the patient owner
     * @return a new patient
     */
    public static Party createPatient(String name, String species, String breed, String sex, Date dateOfBirth,
                                      String microchip, String colour, Party owner) {
        Party patient = createPatient(name, species, breed, dateOfBirth, owner);
        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("sex", sex);
        if (microchip != null) {
            patient.addIdentity(TestHelper.createEntityIdentity(PatientArchetypes.MICROCHIP, microchip));
        }
        bean.setValue("colour", colour);
        bean.save();
        return patient;
    }


    /**
     * Helper to create an <em>act.patientMedication</em>.
     *
     * @param patient the patient
     * @return a new act
     */
    public static Act createMedication(Party patient) {
        return createMedication(patient, TestHelper.createProduct());
    }

    /**
     * Helper to create an <em>act.patientMedication</em>.
     *
     * @param patient the patient
     * @param product the product
     * @return a new act
     */
    public static Act createMedication(Party patient, Product product) {
        return createMedication(new Date(), patient, product);
    }

    /**
     * Helper to create an <em>act.patientMedication</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @param product   the product
     * @return a new act
     */
    public static Act createMedication(Date startTime, Party patient, Product product) {
        Act act = createAct(PatientArchetypes.PATIENT_MEDICATION, startTime, patient, null);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("product", product);
        bean.save();
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @param patient the patient
     * @return a new act
     */
    public static Act createEvent(Party patient) {
        return createEvent(patient, null);
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     *
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createEvent(Party patient, User clinician) {
        return createAct(PatientArchetypes.CLINICAL_EVENT, new Date(), patient, clinician);
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     * <p>
     * This links the event to any items, and saves it.
     *
     * @param startTime the start time. May be {@code null}
     * @param patient   the patient
     * @return a new act
     */
    public static Act createEvent(Date startTime, Party patient, Act... items) {
        return createEvent(startTime, null, patient, null, items);
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     * <p>
     * This links the event to any items, and saves it.
     *
     * @param startTime the start time. May be {@code null}
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createEvent(Date startTime, Party patient, User clinician, Act... items) {
        return createEvent(startTime, null, patient, clinician, items);
    }

    /**
     * Helper to create an <em>act.patientClinicalEvent</em>.
     * <p>
     * This links the event to any items, and saves it.
     *
     * @param startTime the start time. May be {@code null}
     * @param endTime   the end time. May be {@code null}
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @param items     the items. May be history or invoice items
     * @return a new act
     */
    public static Act createEvent(Date startTime, Date endTime, Party patient, User clinician, Act... items) {
        Act act = createEvent(patient, clinician);
        act.setActivityStartTime(startTime);
        act.setActivityEndTime(endTime);
        ActBean bean = new ActBean(act);
        for (Act item : items) {
            if (item instanceof FinancialAct) {
                bean.addNodeRelationship("chargeItems", item);
            } else {
                bean.addNodeRelationship("items", item);
            }
        }
        List<Act> acts = new ArrayList<>();
        acts.add(act);
        acts.addAll(Arrays.asList(items));
        save(acts);
        return act;
    }

    /**
     * Helper to create an <em>act.patientClinicalProblem</em>.
     * <p>
     * This links the problem to any items, and saves it.
     *
     * @param startTime the start time
     * @param patient   the patient
     * @param items     the problem items
     * @return a new act
     */
    public static Act createProblem(Date startTime, Party patient, Act... items) {
        return createProblem(startTime, patient, null, items);
    }

    /**
     * Helper to create an <em>act.patientClinicalProblem</em>.
     * <p>
     * This links the problem to any items, and saves it.
     *
     * @param startTime the start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @param items     the problem items
     * @return a new act
     */
    public static Act createProblem(Date startTime, Party patient, User clinician, Act... items) {
        return createProblem(startTime, patient, clinician, null, "HEART_MURMUR", items);
    }

    /**
     * Helper to create an <em>act.patientClinicalProblem</em>.
     * <p>
     * This links the problem to any items, and saves it.
     *
     * @param startTime        the start time
     * @param patient          the patient
     * @param clinician        the clinician. May be {@code null}
     * @param presentComplaint the presenting complaint code. May be {@code null}
     * @param diagnosis        the diagnosis code. May be {@code null}
     * @param items            the problem items
     * @return a new act
     */
    public static Act createProblem(Date startTime, Party patient, User clinician,
                                    String presentComplaint, String diagnosis, Act... items) {
        Act act = createAct(PatientArchetypes.CLINICAL_PROBLEM, startTime, patient, clinician);
        ActBean bean = new ActBean(act);
        if (diagnosis != null) {
            act.setReason(getLookup("lookup.diagnosis", diagnosis).getCode());
        }
        if (presentComplaint != null) {
            bean.setValue("presentingComplaint", getLookup("lookup.presentingComplaint", presentComplaint).getCode());
        }
        for (Act item : items) {
            bean.addNodeRelationship("items", item);
        }
        List<Act> acts = new ArrayList<>();
        acts.add(act);
        acts.addAll(Arrays.asList(items));
        save(acts);
        return act;
    }

    /**
     * Creates an <em>act.patientClinicalNote</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @return a new act
     */
    public static Act createNote(Date startTime, Party patient) {
        return createNote(startTime, patient, null);
    }

    /**
     * Creates an <em>act.patientClinicalNote</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createNote(Date startTime, Party patient, User clinician) {
        return createNote(startTime, patient, clinician, null);
    }

    /**
     * Creates an <em>act.patientClinicalNote</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @param note      the note. May be {@code null}
     * @return a new act
     */
    public static Act createNote(Date startTime, Party patient, User clinician, String note) {
        Act act = createAct(PatientArchetypes.CLINICAL_NOTE, startTime, patient, clinician);
        if (note != null) {
            ActBean bean = new ActBean(act);
            bean.setValue("note", note);
        }
        save(act);
        return act;
    }

    /**
     * Adds an <em>act.patientClinicalAddendum</em> to another act.
     *
     * @param act      the act to link to. May be an <em>act.patientClinicalNote</em>, or an
     *                 <em>act.patientMedication</em>
     * @param addendum the addendum
     */
    public static void addAddendum(Act act, Act addendum) {
        ActBean bean = new ActBean(act);
        bean.addNodeRelationship("addenda", addendum);
        save(act, addendum);
    }

    /**
     * Creates an <em>act.patientClinicalAddendum</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createAddendum(Date startTime, Party patient, User clinician) {
        return createAddendum(startTime, patient, clinician, null);
    }

    /**
     * Creates an <em>act.patientClinicalAddendum</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @param note      the note. May be {@code null}
     * @return a new act
     */
    public static Act createAddendum(Date startTime, Party patient, User clinician, String note) {
        Act act = createAct(PatientArchetypes.CLINICAL_ADDENDUM, startTime, patient, clinician);
        if (note != null) {
            ActBean bean = new ActBean(act);
            bean.setValue("note", note);
        }
        save(act);
        return act;
    }

    /**
     * Creates a new <em>act.patientWeight</em> for a patient for the current date, and saves it.
     *
     * @param patient the patient
     * @param weight  the weight
     * @return the weight act
     */
    public static Act createWeight(Party patient, BigDecimal weight, WeightUnits units) {
        return createWeight(patient, new Date(), weight, units);
    }

    /**
     * Creates a new <em>act.patientWeight</em> for a patient and saves it.
     *
     * @param patient   the patient
     * @param startTime the start time
     * @param weight    the weight
     * @param units     the weight units
     * @return the weight act
     */
    public static Act createWeight(Party patient, Date startTime, BigDecimal weight, WeightUnits units) {
        Act act = createWeight(startTime, patient, null);
        ActBean bean = new ActBean(act);
        bean.setValue("weight", weight);
        bean.setValue("units", units.toString());
        save(act);
        return act;
    }

    /**
     * Creates an <em>act.patientWeight</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @return a new act
     */
    public static Act createWeight(Date startTime, Party patient) {
        return createWeight(startTime, patient, null);
    }

    /**
     * Creates an <em>act.patientWeight</em>.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createWeight(Date startTime, Party patient, User clinician) {
        Act act = createAct(PatientArchetypes.PATIENT_WEIGHT, startTime, patient, clinician);
        save(act);
        return act;
    }

    /**
     * Creates a patient act.
     *
     * @param shortName the act archetype short name
     * @param startTime the act start time
     * @param patient   the patient
     * @param clinician the clinician. May be {@code null}
     * @return a new act
     */
    public static Act createAct(String shortName, Date startTime, Party patient, User clinician) {
        Act act = (Act) create(shortName);
        act.setActivityStartTime(startTime);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("patient", patient);
        if (clinician != null) {
            bean.addNodeParticipation("clinician", clinician);
        }
        return act;
    }

    /**
     * Creates an investigation.
     *
     * @param patient           the patient
     * @param investigationType the investigation type
     * @return a new investigation
     */
    public static DocumentAct createInvestigation(Party patient, Entity investigationType) {
        return createInvestigation(patient, null, investigationType);
    }

    /**
     * Creates an investigation.
     *
     * @param patient           the patient
     * @param clinician         the clinician. May be {@code null}
     * @param investigationType the investigation type
     * @return a new investigation
     */
    public static DocumentAct createInvestigation(Party patient, User clinician, Entity investigationType) {
        return createInvestigation(patient, clinician, null, investigationType);
    }

    /**
     * Creates an investigation.
     *
     * @param patient           the patient
     * @param clinician         the clinician. May be {@code null}
     * @param location          the practice location. May be {@code null}
     * @param investigationType the investigation type
     * @return a new investigation
     */
    public static DocumentAct createInvestigation(Party patient, User clinician, Party location,
                                                  Entity investigationType) {
        return createInvestigation(new Date(), patient, clinician, location, investigationType);
    }

    /**
     * Creates an investigation.
     *
     * @param startTime         the act start time
     * @param patient           the patient
     * @param clinician         the clinician. May be {@code null}
     * @param location          the practice location. May be {@code null}
     * @param investigationType the investigation type
     * @return a new investigation
     */
    public static DocumentAct createInvestigation(Date startTime, Party patient, User clinician, Party location,
                                                  Entity investigationType) {
        DocumentAct act = (DocumentAct) createAct(InvestigationArchetypes.PATIENT_INVESTIGATION, startTime, patient,
                                                  clinician);
        ActBean bean = new ActBean(act);
        if (location != null) {
            bean.addNodeParticipation("location", location);
        }
        bean.addNodeParticipation("investigationType", investigationType);
        bean.save();
        return act;
    }

    /**
     * Creates an investigation version document act.
     *
     * @param startTime         the act start time
     * @param investigationType the investigation type
     * @return a new document act
     */
    public static Act createInvestigationVersion(Date startTime, Entity investigationType) {
        DocumentAct act = (DocumentAct) create(InvestigationArchetypes.PATIENT_INVESTIGATION_VERSION);
        act.setActivityStartTime(startTime);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("investigationType", investigationType);
        save(act);
        return act;
    }

    /**
     * Adds a report to an investigation.
     *
     * @param investigation the investigation or investigation version
     */
    public static void addReport(DocumentAct investigation) {
        Document document = (Document) create(DocumentArchetypes.TEXT_DOCUMENT);
        document.setName("Z Test document");
        investigation.setDocument(document.getObjectReference());
        save(investigation, document);
    }

    /**
     * Creates an attachment document act.
     *
     * @param patient   the patient
     * @param startTime the act start time
     * @return a new document act
     */
    public static DocumentAct createDocumentAttachment(Date startTime, Party patient) {
        return createDocumentAttachment(startTime, patient, null);
    }

    /**
     * Creates an attachment document act.
     *
     * @param patient   the patient
     * @param startTime the act start time
     * @param fileName  the document file name. May be {@code null}
     * @return a new document act
     */
    public static DocumentAct createDocumentAttachment(Date startTime, Party patient, String fileName) {
        return createDocumentAttachment(startTime, patient, fileName, null);
    }

    /**
     * Creates an attachment document act with an identity.
     *
     * @param patient   the patient
     * @param startTime the act start time
     * @param fileName  the document file name. May be {@code null}
     * @param identity  the act identity. May be {@code null}
     * @return a new document act
     */
    public static DocumentAct createDocumentAttachment(Date startTime, Party patient, String fileName,
                                                       ActIdentity identity) {
        DocumentAct act = (DocumentAct) createAct(PatientArchetypes.DOCUMENT_ATTACHMENT, startTime, patient, null);
        act.setFileName(fileName);
        if (identity != null) {
            act.addIdentity(identity);
        }
        save(act);
        return act;
    }

    /**
     * Creates an attachment version document act .
     *
     * @param startTime the act start time
     * @return a new document act
     */
    public static DocumentAct createDocumentAttachmentVersion(Date startTime) {
        DocumentAct act = (DocumentAct) create(PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION);
        act.setActivityStartTime(startTime);
        save(act);
        return act;
    }

    /**
     * Creates an <em>act.patientDocumentForm</em>.
     *
     * @param patient the patient
     * @return a new document form act
     */
    public static DocumentAct createDocumentForm(Party patient) {
        return createDocumentForm(patient, null);
    }

    /**
     * Creates a form document act.
     *
     * @param patient the patient
     * @param product the product. May be {@code null}
     * @return a new form document act
     */
    public static DocumentAct createDocumentForm(Party patient, Product product) {
        return createDocumentForm(new Date(), patient, product);
    }

    /**
     * Creates a form document act.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @param product   the product. May be {@code null}
     * @return a new form document act
     */
    public static DocumentAct createDocumentForm(Date startTime, Party patient, Product product) {
        return createDocumentForm(startTime, patient, product, null);
    }

    /**
     * Creates a form document act.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @param product   the product. May be {@code null}
     * @param template  the template. May be {@code null}
     * @return a new form document act
     */
    public static DocumentAct createDocumentForm(Date startTime, Party patient, Product product, Entity template) {
        DocumentAct act = (DocumentAct) createAct(PatientArchetypes.DOCUMENT_FORM, startTime, patient, null);
        if (product != null || template != null) {
            IMObjectBean bean = new IMObjectBean(act);
            bean.setTarget("product", product);
            bean.setTarget("documentTemplate", template);
        }
        save(act);
        return act;
    }

    /**
     * Creates an image document act.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @return a new document act
     */
    public static DocumentAct createDocumentImage(Date startTime, Party patient) {
        Act act = createAct(PatientArchetypes.DOCUMENT_IMAGE, startTime, patient, null);
        save(act);
        return (DocumentAct) act;
    }

    /**
     * Creates an image version document act .
     *
     * @param startTime the act start time
     * @return a new document act
     */
    public static DocumentAct createDocumentImageVersion(Date startTime) {
        DocumentAct act = (DocumentAct) create(PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION);
        act.setActivityStartTime(startTime);
        save(act);
        return act;
    }

    /**
     * Creates a letter document act.
     *
     * @param startTime the act start time
     * @param patient   the patient
     * @return a new document act
     */
    public static DocumentAct createDocumentLetter(Date startTime, Party patient, Act... versions) {
        Act act = createAct(PatientArchetypes.DOCUMENT_LETTER, startTime, patient, null);
        List<Act> toSave = new ArrayList<>();
        toSave.add(act);
        if (versions.length > 0) {
            ActBean bean = new ActBean(act);
            for (Act version : versions) {
                bean.addNodeRelationship("versions", version);
                toSave.add(version);
            }
        }
        save(toSave);
        return (DocumentAct) act;
    }

    /**
     * Creates a letter version document act .
     *
     * @param startTime the act start time
     * @return a new document act
     */
    public static DocumentAct createDocumentLetterVersion(Date startTime) {
        DocumentAct act = (DocumentAct) create(PatientArchetypes.DOCUMENT_LETTER_VERSION);
        act.setActivityStartTime(startTime);
        save(act);
        return act;
    }

    /**
     * Creates a new microchip identity.
     *
     * @param microchip the microchip
     * @return a new identity
     */
    public static EntityIdentity createMicrochip(String microchip) {
        EntityIdentity result = (EntityIdentity) create(PatientArchetypes.MICROCHIP);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("microchip", microchip);
        return result;
    }

    /**
     * Creates a new pet tag identity.
     *
     * @param tag the tag
     * @return a new identity
     */
    public static EntityIdentity createPetTag(String tag) {
        EntityIdentity result = (EntityIdentity) create(PatientArchetypes.PET_TAG);
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("petTag", tag);
        return result;
    }
}
