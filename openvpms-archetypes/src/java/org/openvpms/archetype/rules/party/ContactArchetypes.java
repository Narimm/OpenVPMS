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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.archetype.rules.party;


/**
 * Contact archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ContactArchetypes {

    /**
     * Phone contact archetype short name.
     */
    public static final String PHONE = "contact.phoneNumber";

    /**
     * Mobile phone contact archetype short name.
     *
     * @deprecated use {@link #PHONE}.
     */
    @Deprecated
    public static final String MOBILE = PHONE;

    /**
     * Email contact archetype short name.
     */
    public static final String EMAIL = "contact.email";

    /**
     * Fax contact archetype short name.
     */
    public static final String FAX = "contact.faxNumber";

    /**
     * Location contact archetype short name.
     */
    public static final String LOCATION = "contact.location";

}
