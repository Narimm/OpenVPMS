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

package org.openvpms.web.webdav.milton;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.webdav.resource.EditableDocuments;
import org.openvpms.web.webdav.session.Session;
import org.openvpms.web.webdav.session.SessionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the {@link SessionManager} interface.
 *
 * @author Tim Anderson
 */
public class DocumentSessionManager implements SessionManager {

    /**
     * The sessions.
     */
    private final Map<String, Session> sessions;

    /**
     * The documents that may be edited.
     */
    private final EditableDocuments documents;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DocumentSessionManager.class);

    /**
     * Constructs a {@link DocumentSessionManager}.
     *
     * @param documents   the types of documents that can be edited
     * @param maxSessions the maximum no. of sessions. When this is reached, old sessions will be removed
     * @param timeToLive  time-to-live before an unused session expires, in minutes
     */
    public DocumentSessionManager(EditableDocuments documents, int maxSessions, long timeToLive) {
        this.documents = documents;

        // use a LRU map to limit the no. of active sessions, and wrap this in an expiring map to remove them as
        // they expire
        LRUMap<String, Session> map = new LRUMap<>(maxSessions);
        sessions = new PassiveExpiringMap<>(timeToLive, TimeUnit.MINUTES, map);
    }

    /**
     * Returns a session, given its identifier.
     *
     * @param sessionId the session identifier
     * @return the session, or {@code null} if none is found
     */
    @Override
    public Session get(String sessionId) {
        Session session;
        synchronized (sessions) {
            session = sessions.get(sessionId);
            if (session != null) {
                add(session); // reset the expiry time
            }
        }
        return session;
    }

    /**
     * Creates a session for an act.
     *
     * @param act the act
     * @return a new session
     */
    @Override
    public Session create(DocumentAct act) {
        Session session = new SessionImpl(getUser(), act);
        synchronized (sessions) {
            add(session);
        }
        return session;
    }

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
    @Override
    public Session create(String sessionId, String actId, String name) {
        Session session = null;
        if (isValid(sessionId)) {
            DocumentAct act = getDocumentAct(actId);
            if (act != null && StringUtils.equals(act.getFileName(), name)) {
                synchronized (sessions) {
                    if (sessions.get(sessionId) == null) {
                        session = new SessionImpl(sessionId, getUser(), act);
                        add(session);
                    }
                }
            }
        }
        return session;
    }

    /**
     * Returns the current user.
     *
     * @return the current user
     * @throws IllegalStateException if no use is present in the security context
     */
    protected User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No current user");
        }
        return (User) authentication.getPrincipal();
    }

    /**
     * Returns a document act, given its identifier
     *
     * @param id the act identifier
     * @return the corresponding act, or {@code null} if there is none
     */
    protected DocumentAct getDocumentAct(String id) {
        DocumentAct result = null;
        try {
            result = documents.getDocumentAct(Long.valueOf(id));
        } catch (NumberFormatException ignore) {
            log.warn("Invalid document act identifier: " + id);
        }
        return result;
    }

    /**
     * Determines if a session identifier is valid.
     *
     * @param sessionId the session identifier
     * @return {@code true} if it is valid
     */
    private boolean isValid(String sessionId) {
        boolean valid = false;
        try {
            UUID.fromString(sessionId);
            valid = true;
        } catch (IllegalArgumentException exception) {
            log.warn("Invalid session identifier: " + sessionId);
        }
        return valid;
    }

    /**
     * Adds a session.
     * <p/>
     * If the session already exists, this resets the expiry time.
     *
     * @param session the session
     */
    private void add(Session session) {
        sessions.put(session.getSessionId(), session);
    }

    private static class SessionImpl implements Session {

        /**
         * The session id.
         */
        private final String sessionId;

        private final String userName;

        private final IMObjectReference act;

        private final String fileName;

        private final Date created;

        /**
         * Constructs a session.
         *
         * @param user the user the session belongs to
         * @param act  the act being edited
         */
        public SessionImpl(User user, DocumentAct act) {
            this(UUID.randomUUID().toString(), user, act);
        }

        /**
         * Constructs a session.
         *
         * @param sessionId the session identifier
         * @param user      the user the session belongs to
         * @param act       the act being edited
         */
        public SessionImpl(String sessionId, User user, DocumentAct act) {
            this.sessionId = sessionId;
            userName = user.getUsername();
            this.act = act.getObjectReference();
            this.fileName = act.getFileName();
            created = new Date();
        }

        @Override
        public String getSessionId() {
            return sessionId;
        }

        @Override
        public String getUserName() {
            return userName;
        }

        @Override
        public IMObjectReference getDocumentAct() {
            return act;
        }

        @Override
        public String getPath() {
            return "/" + sessionId + "/" + act.getId() + "/" + fileName;
        }

        @Override
        public Date getCreated() {
            return created;
        }
    }
}
