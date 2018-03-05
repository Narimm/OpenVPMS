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

package org.openvpms.web.workspace.patient.history;

import org.joda.time.Period;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.system.ServiceHelper;


/**
 * Actions that may be performed on patient history acts.
 *
 * @author Tim Anderson
 */
public class PatientHistoryActions extends ActActions<Act> {

    /**
     * The singleton instance.
     */
    public static final PatientHistoryActions INSTANCE = new PatientHistoryActions();

    /**
     * Patient document archetypes.
     */
    private final String[] DOCUMENTS = {PatientArchetypes.DOCUMENT_ATTACHMENT,
                                        PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION,
                                        PatientArchetypes.DOCUMENT_FORM,
                                        PatientArchetypes.DOCUMENT_LETTER, PatientArchetypes.DOCUMENT_LETTER_VERSION,
                                        PatientArchetypes.DOCUMENT_IMAGE, PatientArchetypes.DOCUMENT_IMAGE_VERSION};


    /**
     * Default constructor.
     */
    protected PatientHistoryActions() {
        super();
    }

    /**
     * Determines if an act can be edited.
     * <p>
     * Patient investigations can always be edited, although the editor restricts functionality based on the status.
     *
     * @param act the act to check
     * @return {@code true} if the act isn't an invoice item, and its status isn't {@code POSTED}
     */
    @Override
    public boolean canEdit(Act act) {
        return !TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)
               && (super.canEdit(act) || TypeHelper.isA(act, InvestigationArchetypes.PATIENT_INVESTIGATION));
    }

    /**
     * Determines if an act can be deleted.
     * <p>
     * An act may be deleted if:
     * <ul>
     * <li>isn't {@code POSTED}, locked or an invoice item; and</li>
     * <li>it is an event, problem, note, or medication that isn't linked to anything else; or</li>
     * <li>it is a document; or</li>
     * <li>isn't linked to an an invoice item
     * </ul>
     *
     * @param act the act to check
     * @return {@code true} if the act can be deleted, otherwise {@code false}
     */
    @Override
    public boolean canDelete(Act act) {
        if (!super.canDelete(act)) {
            return false;
        } else if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)) {
            return false;
        } else if (TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT, PatientArchetypes.CLINICAL_PROBLEM,
                                  PatientArchetypes.CLINICAL_NOTE)) {
            return act.getSourceActRelationships().isEmpty();
        } else if (TypeHelper.isA(act, DOCUMENTS)) {
            return true;
        } else {
            // reject deletion if the item is linked to an invoice
            for (Relationship rel : act.getTargetActRelationships()) {
                if (rel.getSource().isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if an act can be posted (i.e finalised).
     * <p>
     * This implementation returns {@code true} if the act isn't an invoice item ant its status isn't {@code POSTED}
     * or {@code CANCELLED}.
     *
     * @param act the act to check
     * @return {@code true} if the act can be posted
     */
    @Override
    public boolean canPost(Act act) {
        return !TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM) && super.canPost(act);
    }

    /**
     * Determines if flow sheet reports can be imported.
     *
     * @param event    the visit. May be {@code null}
     * @param location the practice location. May be {@code null}
     * @param factory  the flow sheet service factory
     * @return {@code true} if flow sheet reports can be imported
     */
    public boolean canImportFlowSheet(Act event, Party location, FlowSheetServiceFactory factory) {
        return (event != null && factory.isSmartFlowSheetEnabled(location));
    }

    /**
     * Determines if an act is locked from editing.
     * <p>
     *
     * @param act the act
     * @return {@code true} if the act status is {@link ActStatus#POSTED}, or {@link #needsLock} returns {@code true}.
     */
    @Override
    public boolean isLocked(Act act) {
        return super.isLocked(act) || needsLock(act);
    }

    /**
     * Determines if an act needs locking.
     *
     * @param act thr act
     * @return {@code true} if the act needs locking
     */
    public static boolean needsLock(Act act) {
        MedicalRecordRules recordRules = ServiceHelper.getBean(MedicalRecordRules.class);
        PracticeService practiceService = ServiceHelper.getBean(PracticeService.class);
        Period period = practiceService.getRecordLockPeriod();
        return (period != null) && recordRules.needsLock(act, period);
    }

}
