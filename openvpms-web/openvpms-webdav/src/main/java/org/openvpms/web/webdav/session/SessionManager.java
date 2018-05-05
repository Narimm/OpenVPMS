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

import org.openvpms.component.business.domain.im.act.DocumentAct;

/**
 * Manages {@link Session} instances.
 *
 * @author Tim Anderson
 */
public interface SessionManager {

    /**
     * Returns a session, given its identifier.
     *
     * @param sessionId the session identifier
     * @return the session, or {@code null} if none is found
     */
    Session get(String sessionId);

    /**
     * Creates a session for an act.
     *
     * @param act the act
     * @return a new session
     */
    Session create(DocumentAct act);

    /**
     * Creates a session for an act.
     * <p/>
     * Use this to re-create a session that has expired.
     *
     * @param sessionId the session identifier of the expired session
     * @param actId     the act id
     * @param name      the document name
     * @return a new session or {@code null} if any of the parameters are invalid
     */
    Session create(String sessionId, String actId, String name);

}
