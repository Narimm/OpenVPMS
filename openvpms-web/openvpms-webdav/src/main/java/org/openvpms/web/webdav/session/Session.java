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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.webdav.session;

import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.Date;

/**
 * A time-limited session that enables a user to edit a single resource.
 *
 * @author Tim Anderson
 * @see SessionManager
 */
public interface Session {

    /**
     * Returns the session identifier.
     *
     * @return the session identifier
     */
    String getSessionId();

    /**
     * Returns the user name (i.e. login name) of the user the session belongs to.
     *
     * @return the user name
     */
    String getUserName();

    /**
     * Returns the reference of the document act.
     *
     * @return the document act reference
     */
    IMObjectReference getDocumentAct();

    /**
     * Returns the path of the resource that this session is for.
     *
     * @return the resource path
     */
    String getPath();

    /**
     * Returns the date when the session was created.
     *
     * @return the date when the session was created
     */
    Date getCreated();

}
