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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter.i18n;

import java.text.MessageFormat;


/**
 * A <tt>Message</tt> contains a localised formatted string, obtained from {@link Messages}.
 * <p/>
 * Each message has a unique identifier which may be used to trace a message back to its source.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Message {

    /**
     * The group that the message belongs to.
     */
    private String groupId;

    /**
     * The message code within the group.
     */
    private int code;

    /**
     * The formatted message.
     */
    private String message;


    /**
     * Constructs a <tt>Message</tt>.
     *
     * @param groupId the group that the message belongs to
     * @param code    the message code. This is unique withing the group
     * @param message the formatted message
     */
    protected Message(String groupId, int code, String message) {
        this.groupId = groupId;
        this.code = code;
        this.message = message;
    }

    /**
     * Returns the message id.
     * <p/>
     * This is obtained by concatenating the {@link #getGroupId() project} and {@link #getCode() code}.
     * <p/>
     * The message id is unique.
     *
     * @return the message id
     */
    public String getId() {
        return MessageFormat.format("{0}-{1,number,0000}", groupId, code);
    }

    /**
     * Returns the identifier of the group that the message belongs to.
     * <p/>
     * This is ideally between 3 to 5 characters (e.g it could be the associated JIRA project identifier)
     * and must be unique across all groups.
     *
     * @return the group identifier
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Returns the message code.
     *
     * @return the message code
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the formatted message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the formatted message, prepended with the id.
     *
     * @return the id and message
     */
    public String toString() {
        return getId() + ": " + message;
    }

}
