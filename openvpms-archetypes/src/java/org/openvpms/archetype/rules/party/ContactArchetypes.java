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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.party;


/**
 * Contact archetypes.
 *
 * @author Tim Anderson
 */
public class ContactArchetypes {

    /**
     * Phone contact archetype short name.
     */
    public static final String PHONE = "contact.phoneNumber";

    /**
     * Email contact archetype short name.
     */
    public static final String EMAIL = "contact.email";

    /**
     * Location contact archetype short name.
     */
    public static final String LOCATION = "contact.location";

    /**
     * Website contact archetype short name.
     */
    public static final String WEBSITE = "contact.website";

    /**
     * Contact purpose lookup archetype short name.
     */
    public static final String PURPOSE = "lookup.contactPurpose";

    /**
     * Home lookup.contactPurpose code.
     */
    public static final String HOME_PURPOSE = "HOME";

    /**
     * Work lookup.contactPurpose code.
     */
    public static final String WORK_PURPOSE = "WORK";

    /**
     * Mobile lookup.contactPurpose code.
     */
    public static final String MOBILE_PURPOSE = "MOBILE";

    /**
     * Fax lookup.contactPurpose code.
     */
    public static final String FAX_PURPOSE = "FAX";

    /**
     * Correspondence lookup.contactPurpose code.
     */
    public static final String CORRESPONDENCE_PURPOSE = "CORRESPONDENCE";

    /**
     * Billing lookup.contactPurpose code.
     */
    public static final String BILLING_PURPOSE = "BILLING";

    /**
     * Reminder lookup.contactPurpose code.
     */
    public static final String REMINDER_PURPOSE = "REMINDER";
}
