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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.ProblemActStatus;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link DefaultPatientHistoryDatingPolicy} class.
 *
 * @author Tim Anderson
 */
public class DefaultPatientHistoryDatingPolicyTestCase extends ArchetypeServiceTest {

    /**
     * Policy when record locking enabled.
     */
    private PatientHistoryDatingPolicy locked1;

    /**
     * Policy when record locking enabled, but form and letters can have dates changed.
     */
    private PatientHistoryDatingPolicy lockedExceptFormsAndLetters;

    /**
     * Policy when record locking disabled.
     */
    private PatientHistoryDatingPolicy lockingDisabled;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        locked1 = createPolicy(new Period(2, PeriodType.days()), false);
        lockedExceptFormsAndLetters = createPolicy(new Period(2, PeriodType.days()), true);
        lockingDisabled = createPolicy(null, false);
    }

    /**
     * Tests the {@link DefaultPatientHistoryDatingPolicy#canEditStartTime(Act)} method.
     */
    @Test
    public void testCanEditDate() {
        Act event = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        Act problem = (Act) create(PatientArchetypes.CLINICAL_PROBLEM);
        Act addendum = (Act) create(PatientArchetypes.CLINICAL_ADDENDUM);
        Act attachment = (Act) create(PatientArchetypes.DOCUMENT_ATTACHMENT);
        Act attachmentVersion = (Act) create(PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION);
        Act form = (Act) create(PatientArchetypes.DOCUMENT_FORM);
        Act image = (Act) create(PatientArchetypes.DOCUMENT_IMAGE);
        Act imageVersion = (Act) create(PatientArchetypes.DOCUMENT_IMAGE_VERSION);
        Act investigation = (Act) create(InvestigationArchetypes.PATIENT_INVESTIGATION);
        Act investigationVersion = (Act) create(InvestigationArchetypes.PATIENT_INVESTIGATION_VERSION);

        Act letter = (Act) create(PatientArchetypes.DOCUMENT_LETTER);
        Act letterVersion = (Act) create(PatientArchetypes.DOCUMENT_LETTER_VERSION);
        Act medication = (Act) create(PatientArchetypes.PATIENT_MEDICATION);
        Act note = (Act) create(PatientArchetypes.CLINICAL_NOTE);
        Act weight = (Act) create(PatientArchetypes.PATIENT_WEIGHT);

        checkCanEdit(event, ActStatus.IN_PROGRESS, true, true, true);
        checkCanEdit(problem, ProblemActStatus.RESOLVED, true, true, true);
        checkCanEdit(addendum, ActStatus.IN_PROGRESS, false, false, false);
        // addendum can never have date changed, as it is new for 1.9 so doesn't need to support 1.8 behaviour

        checkCanEdit(attachment, ActStatus.IN_PROGRESS, false, false, true);
        checkCanEdit(attachmentVersion, ActStatus.IN_PROGRESS, false, false, false);
        checkCanEdit(form, ActStatus.IN_PROGRESS, false, true, true);
        checkCanEdit(image, ActStatus.IN_PROGRESS, false, false, true);
        checkCanEdit(imageVersion, ActStatus.IN_PROGRESS, false, false, false);
        checkCanEdit(investigation, ActStatus.IN_PROGRESS, false, false, true);
        checkCanEdit(investigationVersion, ActStatus.IN_PROGRESS, false, false, false);
        checkCanEdit(letter, ActStatus.IN_PROGRESS, false, true, true);
        checkCanEdit(letterVersion, ActStatus.IN_PROGRESS, false, false, false);
        checkCanEdit(medication, ActStatus.IN_PROGRESS, false, false, true);
        checkCanEdit(note, ActStatus.IN_PROGRESS, false, false, true);
        checkCanEdit(weight, ActStatus.IN_PROGRESS, false, false, true);

        // test cancellation
        investigation.setStatus(ActStatus.CANCELLED);
        checkCanEdit(investigation, ActStatus.CANCELLED, false, false, false);

        // test finalisation
        checkCanEdit(attachment, ActStatus.POSTED, false, false, false);
        checkCanEdit(attachmentVersion, ActStatus.POSTED, false, false, false);
        checkCanEdit(form, ActStatus.POSTED, false, false, false);
        checkCanEdit(image, ActStatus.POSTED, false, false, false);
        checkCanEdit(imageVersion, ActStatus.POSTED, false, false, false);
        checkCanEdit(investigation, ActStatus.POSTED, false, false, false);
        checkCanEdit(investigationVersion, ActStatus.POSTED, false, false, false);
        checkCanEdit(letter, ActStatus.POSTED, false, false, false);
        checkCanEdit(letterVersion, ActStatus.POSTED, false, false, false);
        checkCanEdit(medication, ActStatus.POSTED, false, false, false);
        checkCanEdit(note, ActStatus.POSTED, false, false, false);
        checkCanEdit(weight, ActStatus.POSTED, false, false, false);
    }

    /**
     * Verifies if the date can be edited for an act with a particular status, against several locking policies.
     *
     * @param act                  the act
     * @param status               the act status
     * @param locked               the expected result for the 'locked' policy
     * @param lockedWithExceptions the expected result for the 'locked except for forms and letters' policy
     * @param unlocked             the expected result for the 'locking disabled' policy
     */
    private void checkCanEdit(Act act, String status, boolean locked, boolean lockedWithExceptions,
                              boolean unlocked) {
        act.setStatus(status);
        assertEquals(locked, locked1.canEditStartTime(act));
        assertEquals(lockedWithExceptions, lockedExceptFormsAndLetters.canEditStartTime(act));
        assertEquals(unlocked, this.lockingDisabled.canEditStartTime(act));
    }

    /**
     * Creates a new {@link DefaultPatientHistoryDatingPolicy}
     *
     * @param period              the locking period. May be {@code null}
     * @param editFormsAndLetters if {@code true}, allow changes to the dates of forms and letters
     * @return a new policy
     */
    private DefaultPatientHistoryDatingPolicy createPolicy(Period period, boolean editFormsAndLetters) {
        PracticeService service = Mockito.mock(PracticeService.class);
        Mockito.when(service.getRecordLockPeriod()).thenReturn(period);
        return new DefaultPatientHistoryDatingPolicy(editFormsAndLetters, getArchetypeService(), service);
    }
}
