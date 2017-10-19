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

package org.openvpms.archetype.rules.patient;


/**
 * Patient archetypes.
 *
 * @author Tim Anderson
 */
public class PatientArchetypes {

    /**
     * Patient archetype.
     */
    public static final String PATIENT = "party.patientpet";

    /**
     * Species lookup.
     */
    public static final String SPECIES = "lookup.species";

    /**
     * Breed lookup.
     */
    public static final String BREED = "lookup.breed";

    /**
     * Referred from relationship short name.
     */
    public static final String REFERRED_FROM = "entityRelationship.referredFrom";

    /**
     * Referred to relationship short name.
     */
    public static final String REFERRED_TO = "entityRelationship.referredTo";

    /**
     * Patient medication act short name.
     */
    public static final String PATIENT_MEDICATION = "act.patientMedication";

    /**
     * Patient participation archetype.
     */
    public static final String PATIENT_PARTICIPATION = "participation.patient";

    /**
     * Patient owner relationship short name.
     */
    public static final String PATIENT_OWNER
            = "entityRelationship.patientOwner";

    /**
     * Patient location relationship short name.
     */
    public static final String PATIENT_LOCATION = "entityRelationship.patientLocation";

    /**
     * Patient clinical event act short name.
     */
    public static final String CLINICAL_EVENT = "act.patientClinicalEvent";

    /**
     * Clinical event item act relationship archetype short name.
     */
    public static final String CLINICAL_EVENT_ITEM
            = "actRelationship.patientClinicalEventItem";

    /**
     * Clinical event charge item relationship archetype short name.
     */
    public static final String CLINICAL_EVENT_CHARGE_ITEM = "actRelationship.patientClinicalEventChargeItem";

    /**
     * Patient clinical note act short name.
     */
    public static final String CLINICAL_NOTE = "act.patientClinicalNote";

    /**
     * Patient clinical problem act short name.
     */
    public static final String CLINICAL_PROBLEM = "act.patientClinicalProblem";

    /**
     * Clinical problem item act relationship short name,
     */
    public static String CLINICAL_PROBLEM_ITEM = "actRelationship.patientClinicalProblemItem";

    /**
     * Patient clinical addendum act short name.
     */
    public static final String CLINICAL_ADDENDUM = "act.patientClinicalAddendum";

    /**
     * Clinical item act relationship short name.
     */
    public static final String CLINICAL_ITEM_ADDENDUM = "actRelationship.patientClinicalItemAddendum";

    /**
     * Patient weight act short name.
     */
    public static final String PATIENT_WEIGHT = "act.patientWeight";

    /**
     * Patient document attachment act short name.
     */
    public static final String DOCUMENT_ATTACHMENT = "act.patientDocumentAttachment";

    /**
     * Patient document attachment version act short name.
     */
    public static final String DOCUMENT_ATTACHMENT_VERSION = "act.patientDocumentAttachmentVersion";

    /**
     * Patient document form act short name.
     */
    public static final String DOCUMENT_FORM = "act.patientDocumentForm";

    /**
     * Patient document image act short name.
     */
    public static final String DOCUMENT_IMAGE = "act.patientDocumentImage";

    /**
     * Patient document image version act short name.
     */
    public static final String DOCUMENT_IMAGE_VERSION = "act.patientDocumentImageVersion";

    /**
     * Patient document letter act short name.
     */
    public static final String DOCUMENT_LETTER = "act.patientDocumentLetter";

    /**
     * Patient document letter version act short name.
     */
    public static final String DOCUMENT_LETTER_VERSION = "act.patientDocumentLetterVersion";

    /**
     * Patient alert act short name.
     */
    public static final String ALERT = "act.patientAlert";

    /**
     * Patient alert type archetype.
     */
    public static final String ALERT_TYPE = "entity.patientAlertType";

    /**
     * Patient prescription act short name.
     */
    public static final String PRESCRIPTION = "act.patientPrescription";

    /**
     * Prescription medication relationship short name.
     */
    public static final String PRESCRIPTION_MEDICATION = "actRelationship.patientPrescriptionMedication";

    /**
     * Customer note act short name.
     */
    public static final String CUSTOMER_NOTE = "act.patientCustomerNote";

    /**
     * Microchip short name.
     */
    public static final String MICROCHIP = "entityIdentity.microchip";

    /**
     * Pet tag archetype.
     */
    public static final String PET_TAG = "entityIdentity.petTag";

    /**
     * VeNom diagnosis archetype.
     */
    public static final String DIAGNOSIS_VENOM = "lookup.diagnosisVeNom";
}
