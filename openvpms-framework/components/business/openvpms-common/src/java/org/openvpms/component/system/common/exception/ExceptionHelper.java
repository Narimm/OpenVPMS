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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.system.common.exception;

// commons-resources
import org.apache.commons.resources.Messages;

/**
 * Provides some helper methods for exception handling
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class ExceptionHelper {
    /**
     * Define the static method to render a message
     * 
     * @param messages
     *            the messages from the resource bundly
     * @param key
     *            the key into the message bundle
     * @param params
     *            the parameters to use when rendering
     * @return String the render messages as a string
     */
public static String renderMessage(Messages messages, String key, 
            Object[] params) {
        if (messages == null) {
            return new StringBuffer(key)
                .append(" [NO MESSAGE IN RESOURCE FILE FOR KEY")
                .toString();
        } else if ((params == null) || 
                   (params.length == 0)){
            return messages.getMessage(key);
        } else {
            return messages.getMessage(key, params);
        }
    }}
