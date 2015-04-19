/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;


/**
 * Act status types for <em>act.customerTask</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskStatus extends WorkflowStatus {

    /**
     * Complete status range.
     */
    public static final String[] COMPLETE = {COMPLETED, CANCELLED};

    /**
     * Incomplete status range.
     */
    public static final String[] INCOMPLETE = {PENDING, IN_PROGRESS, BILLED};


    /**
     * Helper to determine if a status is in the 'COMPLETE' range.
     *
     * @param status the status
     * @return <tt>true</tt> if the status is in the 'COMPLETE' range, otherwise
     *         <tt>false</tt>
     */
    public static boolean isComplete(String status) {
        return inRange(status, COMPLETE);
    }

    /**
     * Helper to determine if a status is in the 'INCOMPLETE' range.
     *
     * @param status the status
     * @return <tt>true</tt> if the status is in the 'INCOMPLETE' range,
     *         otherwise <tt>false</tt>
     */
    public static boolean isIncomplete(String status) {
        return inRange(status, INCOMPLETE);
    }

    /**
     * Determines if a status falls in a status range.
     *
     * @param status the status
     * @param range  the status range
     * @return <tt>true</tt> if the status is in the range, otherwise
     *         <tt>false</tt>
     */
    private static boolean inRange(String status, String[] range) {
        for (String str : range) {
            if (str.equals(status)) {
                return true;
            }
        }
        return false;
    }

}
