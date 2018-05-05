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

import org.openvpms.component.business.domain.im.act.Act;

/**
 * Determines if patient history acts can have dates edited when medical record locking is enabled or disabled.
 * <p/>
 * This is to support 1.8 behaviour for practices that do not enable locking.
 *
 * @author Tim Anderson
 */
public interface PatientHistoryDatingPolicy {

    /**
     * Determines if the {@code startTime} node of an act can be edited.
     *
     * @param act the act
     * @return {@code true} if the {@code startTime} node can be edited, otherwise {@code false}
     */
    boolean canEditStartTime(Act act);
}
