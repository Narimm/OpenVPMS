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

import io.milton.common.ContentTypeUtils;
import io.milton.http.Auth;
import io.milton.http.LockInfo;
import io.milton.http.LockManager;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.LockedException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.exceptions.PreConditionFailedException;
import io.milton.resource.GetableResource;
import io.milton.resource.LockableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.ReplaceableResource;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.web.webdav.session.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 * Represents a WebDAV document that may be retrieved and replaced.
 * <p/>
 * Note that this doesn't handle any authentication. It assumes that authentication is handled before any operations
 * are performed.
 *
 * @author Tim Anderson
 */
class DocumentResource implements GetableResource, ReplaceableResource, PropFindableResource, LockableResource, IMObjectResource {

    /**
     * The session.
     */
    private final Session session;

    /**
     * The document name.
     */
    private final String name;

    /**
     * The document reference.
     */
    private final IMObjectReference reference;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lock manager.
     */
    private final LockManager lockManager;

    /**
     * The cached state of the document. This is to avoid retrieving the blob until required.
     */
    private State cachedState;

    /**
     * Constructs a {@link DocumentResource}.
     *
     * @param name        the document name
     * @param session     the session
     * @param reference   the document reference
     * @param service     the archetype service
     * @param handlers    the document handlers
     * @param lockManager the lock manager
     */
    public DocumentResource(String name, Session session, IMObjectReference reference, IArchetypeService service,
                            DocumentHandlers handlers, LockManager lockManager) {
        this.name = name;
        this.session = session;
        this.reference = reference;
        this.handlers = handlers;
        this.service = service;
        this.lockManager = lockManager;
    }

    /**
     * Returning a null value is allowed, and disables the ETag field.
     * <p/>
     * If a unique id is returned it will be combined with the modified date (if available)
     * to produce an ETag which identifies this version of this resource.
     *
     * @return a string which uniquely identifies this resource. This will be
     * used in the ETag header field, and affects caching of resources.
     */
    @Override
    public String getUniqueId() {
        return reference.getArchetypeId().getShortName() + "-" + reference.getId();
    }

    /**
     * Note that this name MUST be consistent with URL resolution in your ResourceFactory
     * <p/>
     * If they aren't consistent Milton will generate a different href in PropFind
     * responses then what clients have request and this will cause either an
     * error or no resources to be displayed
     *
     * @return the name of this resource. i.e just the local name, within its folder
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Check the given credentials, and return a relevant object if accepted.
     * <p/>
     * Returning null indicates credentials were not accepted.
     *
     * @param user     the user name provided by the user's agent
     * @param password the password provided by the user's agent
     * @return if credentials are accepted, some object to attach to the Auth object, otherwise null
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
     */
    @Override
    public String getRealm() {
        // Not used. Spring Security handles this.
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
     */
    @Override
    public Date getModifiedDate() {
        return getState().modified;
    }

    /**
     * Determine if a redirect is required for this request, and if so return the URL to redirect to.
     * May be absolute or relative.
     * <p/>
     * Called after authorization check but before any method specific processing
     * <p/>
     * Return null for no redirect
     */
    public String checkRedirect(Request request) {
        return null;
    }

    /**
     * Send the resource's content using the given output stream. Implementations
     * should assume that bytes are being physically transmitted and that headers
     * have already been committed, although this might not be the case with
     * all web containers.
     * <p/>
     * This method will be used to serve GET requests, and also to generate
     * content following POST requests (if they have not redirected)
     * <p/>
     * The Range argument is not-null for partial content requests. In this case
     * implementations should (but are not required) to only send the data
     * range requested.
     * <p/>
     * The contentType argument is that which was resolved by negotiation in
     * the getContentType method. HTTP allows a given resource to have multiple
     * representations on the same URL. For example, a data series could be retrieved
     * as a chart as SVG, PNG, JPEG, or as text as CSV or XML. When the user agent
     * requests the resource is specified what content types it can accept. These
     * are matched against those that can be provided by the server and a preferred
     * representation is selected. That contentType is set in the response header
     * and is provided here so that the resource implementation can render itself
     * appropriately.
     *
     * @param out         the output stream to send the content to
     * @param range       null for normal GET's, not null for partial GET's. May be ignored
     * @param params      request parameters
     * @param contentType the contentType selected by negotiation
     * @throws IOException if there is an exception writing content to the output stream. This
     *                     indicates that the client has disconnected (as frequently occurs with http transfers).
     *                     DO NOT throw an IOException if there was an internal error generating the response
     *                     (eg if reading from a database)
     */
    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType)
            throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        Document document = getDocument();
        if (document == null) {
            throw new NotFoundException("Document " + reference + " no longer exists");
        }
        DocumentHandler handler = handlers.get(document);
        InputStream stream = handler.getContent(document);
        try {
            IOUtils.copy(stream, out);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * How many seconds to allow the content to be cached for, or null if caching is not allowed
     * <p/>
     * The provided auth object allows this method to determine an appropriate caching
     * time depending on authenticated context. For example, in a CMS in might
     * be appropriate to have a short expiry time for logged in users who might
     * be editing content, as opposed to non-logged in users who are just viewing the site.
     */
    public Long getMaxAgeSeconds(Auth auth) {
        return 60 * 60L;
    }

    /**
     * Given a comma separated listed of preferred content types acceptable for a client,
     * return one content type which is the best.
     * <p/>
     * Returns the most preferred  MIME type. E.g. text/html, image/jpeg, etc
     * <p/>
     * Must be IANA registered
     * <p/>
     * accepts is the accepts header. Eg: Accept: text/*, text/html, text/html;level=1
     * <p/>
     * See - http://www.iana.org/assignments/media-types/ for a list of content types
     * See - http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html for details about the accept header
     * <p/>
     * See here for a fun discussion of using content type and accepts for XHTML -
     * http://stackoverflow.com/questions/348736/is-writing-self-closing-tags-for-elements-not-traditionally-empty-bad-practice
     * <p/>
     * If you can't handle accepts interpretation, just return a single content type - E.g. text/html
     * <p/>
     * But typically you should do something like this:
     * <PRE>
     * String mime = ContentTypeUtils.findContentTypes( this.file );
     * return ContentTypeUtils.findAcceptableContentType( mime, preferredList );
     * </PRE>
     *
     * @param accepts the acceptable content types
     */
    @Override
    public String getContentType(String accepts) {
        State state = getState();
        return ContentTypeUtils.findAcceptableContentType(state.mimeType, accepts);
    }

    /**
     * The length of the content in this resource. If unknown return NULL
     */
    public Long getContentLength() {
        return reference != null ? getState().size : null;
    }

    /**
     * Replaces the document with new content.
     *
     * @param in     the new content stream
     * @param length the length, or {@code null} if the length is unknown
     * @throws BadRequestException if the document no longer exists
     */
    public void replaceContent(final InputStream in, final Long length) throws BadRequestException {
        final Document document = getDocument();
        if (document == null) {
            // ConflictException (HTTP 409) should only be used if a subsequent request would succeed
            throw new BadRequestException("Document " + reference + " no longer exists");
        }
        String mimeType = getState().mimeType;
        DocumentHandler handler = handlers.get(name, mimeType);
        int size = (length != null) ? length.intValue() : -1;
        handler.update(document, in, mimeType, size);
        service.save(document);
        synchronized (this) {
            cachedState.size = size;
            cachedState.version = document.getVersion();
            updateModified();
        }
    }

    public Date getCreateDate() {
        return session.getCreated();
    }

    /**
     * Lock this resource and return a token.
     *
     * @param timeout  the timeout in seconds. May be {@code null}
     * @param lockInfo the lock info
     * @return a result containing the token representing the lock if successful, otherwise a failure reason code
     */
    @Override
    public LockResult lock(LockTimeout timeout, LockInfo lockInfo)
            throws NotAuthorizedException, PreConditionFailedException, LockedException {
        return lockManager.lock(timeout, lockInfo, this);
    }

    /**
     * Renew the lock and return new lock info
     *
     * @param token the lock token
     * @return a result containing the token representing the lock if successful, otherwise a failure reason code
     * @throws NotAuthorizedException
     * @throws PreConditionFailedException
     */
    @Override
    public LockResult refreshLock(String token) throws NotAuthorizedException, PreConditionFailedException {
        return lockManager.refresh(token, this);
    }

    /**
     * If the resource is currently locked, and the tokenId  matches the current
     * one, unlock the resource
     *
     * @param tokenId the lock token
     */
    @Override
    public void unlock(String tokenId) throws NotAuthorizedException, PreConditionFailedException {
        lockManager.unlock(tokenId, this);
    }

    /**
     * Returns the current lock.
     *
     * @return the current lock, if the resource is locked, or {@code null}
     */
    @Override
    public LockToken getCurrentLock() {
        return lockManager.getCurrentToken(this);
    }

    /**
     * Returns the object reference.
     *
     * @return the object reference
     */
    @Override
    public IMObjectReference getReference() {
        return reference;
    }

    /**
     * Returns the object version.
     *
     * @return the version
     */
    @Override
    public long getVersion() {
        return getState().version;
    }

    /**
     * Lazily loads the document state minus the byte content.
     *
     * @return the document state
     */
    private synchronized State getState() {
        if (cachedState == null) {
            ArchetypeQuery query = new ArchetypeQuery(reference);
            query.getArchetypeConstraint().setAlias("doc");
            query.add(new NodeSelectConstraint("version"));
            query.add(new NodeSelectConstraint("mimeType"));
            query.add(new NodeSelectConstraint("size"));
            ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
            cachedState = new State();
            if (iterator.hasNext()) {
                ObjectSet set = iterator.next();
                cachedState.version = set.getLong("doc.version");
                cachedState.mimeType = set.getString("doc.mimeType");
                cachedState.size = set.getLong("doc.size");
                updateModified();
            }
        }
        return cachedState;
    }

    /**
     * Updates the document modified date.
     * <p/>
     * Note that this simply adds {@code version} seconds to the modified state.
     */
    private void updateModified() {
        if (cachedState.version == 0) {
            cachedState.modified = session.getCreated();
        } else {
            cachedState.modified = new DateTime(session.getCreated()).plusSeconds((int) cachedState.version).toDate();
        }
    }

    /**
     * Returns the document.
     *
     * @return the document, or {@code null} if it no longer exists
     */
    private Document getDocument() {
        return (Document) service.get(reference);
    }

    private class State {
        long version;
        String mimeType;
        long size;
        Date modified;
    }
}
