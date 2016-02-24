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

package org.openvpms.web.workspace.customer.communication;

/**
 * Communication archetypes.
 *
 * @author Tim Anderson
 */
public class CommunicationArchetypes {

    /**
     * Email communication archetype short name.
     */
    public static final String EMAIL = "act.customerCommunicationEmail";

    /**
     * Mail communication archetype short name.
     */
    public static final String MAIL = "act.customerCommunicationMail";

    /**
     * Note archetype short name.
     */
    public static final String NOTE = "act.customerCommunicationNote";

    /**
     * Phone archetype short name.
     */
    public static final String PHONE = "act.customerCommunicationPhone";

    /**
     * SMS archetype short name.
     */
    public static final String SMS = "act.customerCommunicationSMS";

    /**
     * All communication acts.
     */
    public static final String ACTS = "act.customerCommunication*";
}
