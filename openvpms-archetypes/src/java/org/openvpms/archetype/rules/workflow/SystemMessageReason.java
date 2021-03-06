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
package org.openvpms.archetype.rules.workflow;


/**
 * System message reasons.
 *
 * @author Tim Anderson
 */
public class SystemMessageReason {

    /**
     * Indicates an error has occurred.
     */
    public static final String ERROR = "ERROR";

    /**
     * Indicates successful completion.
     */
    public static final String COMPLETED = "COMPLETED";

    /**
     * Accepted order reason.
     */
    public static final String ORDER_ACCEPTED = "ORDER_ACCEPTED";

    /**
     * Rejected order reason.
     */
    public static final String ORDER_REJECTED = "ORDER_REJECTED";

    /**
     * Order invoiced reason.
     */
    public static final String ORDER_INVOICED = "ORDER_INVOICED";

}
