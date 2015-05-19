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

/**
 * A predicate that evaluates true for the first {@code count} invocations, then returns {@code false}.
 *
 * @author Tim Anderson
 */
class TimesPredicate<T> implements Predicate<T> {

    /**
     * The maximum no. of invocations.
     */
    private final int count;

    /**
     * The current no. of invocations.
     */
    private int i;

    /**
     * Constructs a {@link TimesPredicate}.
     *
     * @param count the maximum no. of invocations
     */
    public TimesPredicate(int count) {
        this.count = count;
    }

    /**
     * Use the specified parameter to perform a test that returns true or false.
     *
     * @param object the object to evaluate
     * @return true or false
     */
    @Override
    public boolean evaluate(T object) {
        return i++ < count;
    }
}
