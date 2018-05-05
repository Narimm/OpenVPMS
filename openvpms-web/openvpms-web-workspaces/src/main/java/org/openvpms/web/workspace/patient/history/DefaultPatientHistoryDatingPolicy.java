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

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_EVENT;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_PROBLEM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.DOCUMENT_FORM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.DOCUMENT_LETTER;

/**
 * Default implementation of {@link PatientHistoryDatingPolicy}.
 *
 * @author Tim Anderson
 */
public class DefaultPatientHistoryDatingPolicy implements PatientHistoryDatingPolicy {

    /**
     * Determines if the date can be edited on forms and letters.
     */
    private final boolean editFormsAndLetters;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * Constructs a {@link DefaultPatientHistoryDatingPolicy}.
     *
     * @param service         the archetype service
     * @param practiceService the practice service
     */
    public DefaultPatientHistoryDatingPolicy(IArchetypeService service, PracticeService practiceService) {
        this(false, service, practiceService);
    }

    /**
     * Constructs a {@link DefaultPatientHistoryDatingPolicy}.
     *
     * @param editFormsAndLetters if {@code true}, the dates of forms and letters can be edited when locking is enabled
     * @param service             the archetype service
     * @param practiceService     the practice service
     */
    public DefaultPatientHistoryDatingPolicy(boolean editFormsAndLetters, IArchetypeService service,
                                             PracticeService practiceService) {
        this.editFormsAndLetters = editFormsAndLetters;
        this.service = service;
        this.practiceService = practiceService;
    }

    /**
     * Determines if the {@code startTime} node of an act can be edited.
     *
     * @param act the act
     * @return {@code true} if the {@code startTime} node can be edited, otherwise {@code false}
     */
    @Override
    public boolean canEditStartTime(Act act) {
        String status = act.getStatus();
        return !ActStatus.POSTED.equals(status) && !ActStatus.CANCELLED.equals(status)
               && !isReadOnly(act) && (practiceService.getRecordLockPeriod() == null
                                       || TypeHelper.isA(act, CLINICAL_EVENT, CLINICAL_PROBLEM)
                                       || (editFormsAndLetters && TypeHelper.isA(act, DOCUMENT_FORM, DOCUMENT_LETTER)));
    }

    /**
     * Determines if the startTime node of an act is read-only.
     *
     * @param act the act
     * @return {@code true} if the startTime node is read-only
     */
    private boolean isReadOnly(Act act) {
        IMObjectBean bean = new IMObjectBean(act, service);
        NodeDescriptor startTime = bean.getDescriptor("startTime");
        return startTime != null && startTime.isReadOnly();
    }
}
