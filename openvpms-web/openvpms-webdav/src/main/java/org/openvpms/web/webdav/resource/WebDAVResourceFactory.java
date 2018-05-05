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

package org.openvpms.web.webdav.resource;

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.resource.Resource;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.webdav.session.Session;
import org.openvpms.web.webdav.session.SessionManager;

/**
 * A {@code ResourceFactory} for {@link DocumentActResource} and {@link DocumentResource}.
 *
 * @author Tim Anderson
 */
public class WebDAVResourceFactory implements ResourceFactory {

    /**
     * The root path. This consists of the servlet context path + "/document".
     */
    private final String root;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The session manager.
     */
    private final SessionManager sessions;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The lock manager.
     */
    private final ResourceLockManager lockManager;

    /**
     * Constructs a {@link WebDAVResourceFactory}.
     *
     * @param contextPath the servlet context path
     * @param sessions    the session manager
     * @param service     the archetype service
     * @param handlers    the document handlers
     * @param lockManager the lock manager
     */
    public WebDAVResourceFactory(String contextPath, SessionManager sessions, IArchetypeService service,
                                 DocumentHandlers handlers, ResourceLockManager lockManager) {
        this.service = service;
        this.sessions = sessions;
        this.handlers = handlers;
        this.lockManager = lockManager;

        if (!contextPath.endsWith("/")) {
            contextPath += "/";
        }
        root = contextPath + "document";
    }

    /**
     * Locate an instance of a resource at the given url and on the given host.
     * <p/>
     * The host argument can be used for applications which implement virtual
     * domain hosting. But portable applications (ie those which do not depend on the host
     * name) should ignore the host argument.
     * <p/>
     * Note that the host will include the port number if it was specified in
     * the request
     * <p/>
     * The path argument is just the part of the request url with protocol, host, port
     * number, and request parameters removed
     * <p/>
     * E.g. for a request <PRE>http://milton.ettrema.com:80/downloads/index.html?ABC=123</PRE>
     * the corresponding arguments will be:
     * <PRE>
     * host: milton.ettrema.com:80
     * path: /downloads/index.html
     * </PRE>
     * Note that your implementation should not be sensitive to trailing slashes
     * E.g. these paths should return the same resource /apath and /apath/
     * <p/>
     * Return null if there is no associated {@see Resource} object.
     * <p/>
     * You should generally avoid using any request information other then that
     * provided in the method arguments. But if you find you need to you can access the
     * request and response objects from HttpManager.request() and HttpManager.response()
     *
     * @param host Full host name with port number, e.g. milton.ettrema.com:80
     * @param path Relative path on server, e.g. /downloads/index.html
     * @return the associated Resource object, or null if there is none
     */
    public Resource getResource(String host, String path) {
        Resource resource = null;
        String[] elements = getPathParts(path);
        if (elements.length == 1) {
            resource = getSessionResource(elements[0]);
        } else if (elements.length == 2) {
            resource = getDocumentActResource(elements[0], elements[1]);
        } else if (elements.length == 3) {
            resource = getDocumentResource(elements[0], elements[1], elements[2]);
        }
        return resource;
    }

    /**
     * Returns a {@link SessionResource} given its id.
     *
     * @param id the session identifier
     * @return the corresponding session resource, or {@code null} if none is found
     */
    private SessionResource getSessionResource(String id) {
        Session session = sessions.get(id);
        return (session != null) ? new SessionResource(session, service, handlers, lockManager) : null;
    }

    /**
     * Returns a {@link DocumentActResource} given its id.
     *
     * @param sessionId the session identifier
     * @param id        the document act id
     * @return the corresponding document act resource, or {@code null} if none is found
     */
    private DocumentActResource getDocumentActResource(String sessionId, String id) {
        SessionResource session = getSessionResource(sessionId);
        return session != null ? (DocumentActResource) session.child(id) : null;
    }

    /**
     * Returns a {@link DocumentResource} given the parent id and the document name.
     *
     * @param sessionId the session identifier
     * @param id        the document act id
     * @param name      the document name
     * @return the corresponding document resource, or {@code null} if none is found
     */
    private Resource getDocumentResource(String sessionId, String id, String name) {
        Resource result = null;
        SessionResource session = getSessionResource(sessionId);
        if (session == null) {
            // session may have expired, so try and re-recreate it
            session = getSessionResource(sessionId, id, name);
        }
        if (session != null) {
            DocumentActResource act = (DocumentActResource) session.child(id);
            if (act != null) {
                result = act.child(name);
            }
        }
        return result;
    }

    /**
     * Attempts to re-create an expired session, given the session identifier, act identifier, and document name.
     *
     * @param sessionId the session identifier
     * @param id        the document act id
     * @param name      the document name
     * @return the corresponding session resource, or {@code null} if it can't be created
     */
    private SessionResource getSessionResource(String sessionId, String id, String name) {
        Session session = sessions.create(sessionId, id, name);
        return (session != null) ? getSessionResource(sessionId) : null;
    }

    /**
     * Splits a path after the root into parts.
     *
     * @param path the path
     * @return the remaining parts after the root
     */
    private String[] getPathParts(String path) {
        path = StringUtils.removeEnd(path, "/.");
        path = StringUtils.removeStart(path, root);
        path = StringUtils.removeEnd(path, "/");
        return Path.path(path).getParts();
    }

}
