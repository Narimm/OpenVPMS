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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.till;


import org.openvpms.archetype.rules.act.ActStatus;

/**
 * Status types for <em>act.tillBalance</em> acts
 *
 * @author Tim Anderson
 */
public class TillBalanceStatus {

    /**
     * Uncleared status for <em>act.tillBalance</em>
     */
    public static final String UNCLEARED = "UNCLEARED";

    /**
     * Clear in progress status for <em>act.tillBalance</em>
     */
    public static final String IN_PROGRESS = ActStatus.IN_PROGRESS;
    /**
     * Cleared status for <em>act.tillBalance</em>.
     */
    public static final String CLEARED = "CLEARED";
}
