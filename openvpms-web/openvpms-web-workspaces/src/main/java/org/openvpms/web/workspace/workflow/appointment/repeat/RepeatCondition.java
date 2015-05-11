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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.apache.commons.collections4.Predicate;

import java.util.Date;

/**
 * Appointment repeat-until condition.
 *
 * @author Tim Anderson
 */
public interface RepeatCondition {

    /**
     * Creates a predicate for this condition.
     *
     * @return a new predicate
     */
    Predicate<Date> create();

    /**
     * Returns a string representation of the condition.
     *
     * @return a formatted string
     */
    String toString();
}