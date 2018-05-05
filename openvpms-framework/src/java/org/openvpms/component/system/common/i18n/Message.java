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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.component.system.common.i18n;

/**
 * A <tt>Message</tt> contains a localised formatted string, obtained from {@link Messages}.
 * <p>
 * Each message has a unique identifier which may be used to trace a message back to its source.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Message extends org.openvpms.component.i18n.Message {

    /**
     * Constructs a <tt>Message</tt>.
     *
     * @param groupId the group that the message belongs to
     * @param code    the message code. This is unique withing the group
     * @param message the formatted message
     */
    protected Message(String groupId, int code, String message) {
        super(groupId, code, message);
    }


}
