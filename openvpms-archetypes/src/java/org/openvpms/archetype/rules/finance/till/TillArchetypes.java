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

/**
 * Till archetypes.
 *
 * @author Tim Anderson
 */
public class TillArchetypes {

    /**
     * The till archetype short name.
     */
    public static final String TILL = "party.organisationTill";

    /**
     * Till balance act short name.
     */
    public static final String TILL_BALANCE = "act.tillBalance";

    /**
     * Till participation short name.
     */
    public static final String TILL_PARTICIPATION = "participation.till";

    /**
     * Till balance item relationship short name.
     */
    public static final String TILL_BALANCE_ITEM = "actRelationship.tillBalanceItem";

    /**
     * Till balance adjustment short name.
     */
    public static final String TILL_BALANCE_ADJUSTMENT = "act.tillBalanceAdjustment";

}
