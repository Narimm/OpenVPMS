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

package org.openvpms.component.common.exception;

/**
 * All OpenVPMS exceptions must extend {@link RuntimeExceptio} and implement
 * this interface. We are opting to used unchecked over checked exceptions but
 * to declare them both in the javadoc and the method sinature. The client is
 * then responsible for deciding whether or not to handle the exception. If
 * there is a need to define a checked exception then it still must extend this
 * interface
 * <p>
 * The errorCode that is passed to the constructors is a enumerated type which
 * is then used to retrieve a message from a resource file.
 * <p>
 * In most cases the exception will be handled at the top of each tier (i.e.
 * business, presentation tier). Some messages may require parameters to be
 * passed in before they can be rendered as strings. There are several
 * constructors to support this feature.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface OpenVPMSException {
    /**
     * The base name of the resource file, which holds the error messages.
     */
    public static final String ERRMESSAGES_FILE = "errmessages";

}
