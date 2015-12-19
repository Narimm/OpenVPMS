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

import io.milton.http.Auth;
import io.milton.http.LockManager;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.webdav.session.Session;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A resource used to associate a URL with a {@link Session}.
 *
 * @author Tim Anderson
 */
public class SessionResource implements CollectionResource, PropFindableResource {

    /**
     * The session.
     */
    private final Session session;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The lock manager.
     */
    private final LockManager lockManager;

    /**
     * The document resource. This is lazily created.
     */
    private DocumentActResource resource;

    /**
     * Constructs a {@link SessionResource}.
     *
     * @param session     the session
     * @param service     the archetype service
     * @param handlers    the document handlers
     * @param lockManager the lock manager
     */
    public SessionResource(Session session, IArchetypeService service, DocumentHandlers handlers,
                           LockManager lockManager) {
        this.session = session;
        this.service = service;
        this.handlers = handlers;
        this.lockManager = lockManager;
    }

    public Session getSession() {
        return session;
    }

    /**
     * Returns the child resource with the specified name.
     *
     * @param childName the child resource name
     * @return the corresponding resource, or {@code null} if it doesn't exist
     */
    @Override
    public Resource child(String childName) {
        return StringUtils.equals(Long.toString(session.getDocumentAct().getId()), childName) ? getChildResource() : null;
    }

    /**
     * Returns the children of this collection.
     *
     * @return the children of this collection
     * @throws NotAuthorizedException
     * @throws BadRequestException
     */
    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        Resource act = getChildResource();
        return act != null ? Collections.singletonList(act) : Collections.<Resource>emptyList();
    }

    /**
     * Returning a null value is allowed, and disables the ETag field.
     * <p/>
     * If a unique id is returned it will be combined with the modified date (if available)
     * to produce an ETag which identifies this version of this resource. Note that this
     * behaviour can be changed by injecting an alternative EtagGenerator instance into
     * the HttpManagerBuilder
     *
     * @return a string which uniquely identifies this resource. This will be
     * used in the ETag header field, and affects caching of resources.
     */
    @Override
    public String getUniqueId() {
        return session.getSessionId();
    }

    /**
     * Note that this name MUST be consistent with URL resolution in your ResourceFactory
     * <p/>
     * If they aren't consistent Milton will generate a different href in PropFind
     * responses then what clients have request and this will cause either an
     * error or no resources to be displayed
     *
     * @return - the name of this resource. Ie just the local name, within its folder
     */
    @Override
    public String getName() {
        return session.getSessionId();
    }

    /**
     * Check the given credentials, and return a relevant object if accepted.
     * <p/>
     * Returning null indicates credentials were not accepted
     *
     * @param user     the user name provided by the user's agent
     * @param password the password provided by the user's agent
     * @return if credentials are accepted, some object to attach to the Auth object. otherwise null
     */
    @Override
    public Object authenticate(String user, String password) {
        return user;
    }

    /**
     * Return true if the current user is permitted to access this resource using
     * the specified method.
     * <p/>
     * Note that the current user may be determined by the Auth associated with
     * the request, or by a separate, application specific, login mechanism such
     * as a session variable or cookie based system. This method should correctly
     * interpret all such mechanisms
     * <p/>
     * The auth given as a parameter will be null if authentication failed. The
     * auth associated with the request will still exist
     */
    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return true;
    }

    /**
     * Return the security realm for this resource. Just any string identifier.
     * <p/>
     * This will be used to construct authorization challenges and will be used
     * on Digest authentication to construct the expected response.
     *
     * @return - the security realm, for HTTP authentication
     */
    @Override
    public String getRealm() {
        return "WebDAV";
    }

    /**
     * The date and time that this resource, or any part of this resource, was last
     * modified. For dynamic rendered resources this should consider everything
     * which will influence its output.
     * <p/>
     * Resources for which no such date can be calculated should return null.
     * <p/>
     * This field, if not null, is used to reply to conditional GETs (ie GET with
     * if-modified-since). If the modified-since argument is later then the modified
     * date then we return a 304 - Not Modified.
     * <p/>
     * Although nulls are explicitly allowed by milton, certain client applications
     * might require modified dates for file browsing. For example, the command line
     * client on Vista doesn't work properly if this is null.
     *
     * @return null if not known, else the last date the resource was modified
     */
    @Override
    public Date getModifiedDate() {
        return session.getCreated();
    }

    /**
     * Determine if a redirect is required for this request, and if so return
     * the URL to redirect to. May be absolute or relative.
     * <p/>
     * Called after authorization check but before any method specific processing
     * <p/>
     * Return null for no redirect
     *
     * @param request the request
     * @return null for no redirect, else the path to redirect to
     * @throws NotAuthorizedException
     * @throws BadRequestException
     */
    @Override
    public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {
        return null;
    }

    /**
     * Returns the resource creation timestamp.
     *
     * @return the resource creation timestamp
     */
    @Override
    public Date getCreateDate() {
        return session.getCreated();
    }

    /**
     * Returns the document act resource.
     *
     * @return the document act resource. May be {@code null}
     */
    private synchronized Resource getChildResource() {
        if (resource == null) {
            DocumentAct act = (DocumentAct) service.get(session.getDocumentAct());
            if (act != null) {
                resource = new DocumentActResource(act, session, service, handlers, lockManager, session.getCreated());
            }
        }
        return resource;
    }

}
