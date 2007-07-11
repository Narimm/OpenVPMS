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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.archetype.rules.act.ActStatus;


/**
 * Act status types for <em>act.customerAppointment</em> and
 * <em>act.customerTask</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class WorkflowStatus extends ActStatus {

    /**
     * Pending status.
     */
    public static final String PENDING = "PENDING";

    /**
     * Billed status.
     */
    public static final String BILLED = "BILLED";

    /**
     * Represents a range of statuses for query purposes.
     */
    public enum StatusRange {
        ALL,                  // all acts 
        INCOMPLETE,           // incomplete acts
        COMPLETE              // complete acts
    }
}
