package org.openvpms.web.webdav.milton;

import io.milton.http.ExistingEntityHandler;
import io.milton.http.HttpManager;
import io.milton.http.LockInfo;
import io.milton.http.LockInfoSaxHandler;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.Request;
import io.milton.http.ResourceHandlerHelper;
import io.milton.http.Response;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.LockedException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.exceptions.PreConditionFailedException;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.resource.LockableResource;
import io.milton.resource.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * A handler for WebDAV LOCK requests.
 *
 * @author Tim Anderson
 */
public class LockHandler implements ExistingEntityHandler {

    /**
     * The response handler.
     */
    private final WebDavResponseHandler responseHandler;

    /**
     * The resource handler.
     */
    private final ResourceHandlerHelper resourceHandler;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(LockHandler.class);

    /**
     * The methods supports by the handler.
     */
    private static final String[] METHODS = {Request.Method.LOCK.code};

    /**
     * Constructs a {@link LockHelper}.
     *
     * @param responseHandler the response handler
     * @param resourceHandler the resource handler helper
     */
    public LockHandler(WebDavResponseHandler responseHandler, ResourceHandlerHelper resourceHandler) {
        this.responseHandler = responseHandler;
        this.resourceHandler = resourceHandler;
    }

    /**
     * Returns the http methods supported by this handler.
     *
     * @return the supported http methods
     */
    @Override
    public String[] getMethods() {
        return METHODS;
    }

    /**
     * Determines if a resource may be locked.
     *
     * @param resource the resource
     * @return {@code true} if the resource may be locked
     */
    @Override
    public boolean isCompatible(Resource resource) {
        return resource instanceof LockableResource;
    }

    /**
     * Processes a request.
     *
     * @param manager  the http manager
     * @param request  the client request
     * @param response the servlet response
     * @throws ConflictException
     * @throws NotAuthorizedException
     * @throws BadRequestException
     */
    @Override
    public void process(HttpManager manager, Request request, Response response)
            throws ConflictException, NotAuthorizedException, BadRequestException {
        resourceHandler.process(manager, request, response, this);
    }

    /**
     * Processes a request for a resource.
     *
     * @param manager  the http manager
     * @param request  the client request
     * @param response the servlet response
     * @param resource the resource the request applies to
     * @throws NotAuthorizedException
     * @throws ConflictException
     * @throws BadRequestException
     */
    @Override
    public void processResource(HttpManager manager, Request request, Response response, Resource resource)
            throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandler.processResource(manager, request, response, resource, this);
    }

    /**
     * Processes a lock for an existing resource.
     *
     * @param manager  the http manager
     * @param request  the client request
     * @param response the servlet response
     * @param resource the resource to lock
     * @throws NotAuthorizedException
     * @throws BadRequestException
     * @throws ConflictException
     * @throws NotFoundException
     */
    @Override
    public void processExistingResource(HttpManager manager, Request request, Response response, Resource resource)
            throws NotAuthorizedException, BadRequestException, ConflictException, NotFoundException {
        LockableResource lockable = (LockableResource) resource;
        response.setContentTypeHeader(Response.XML);
        String header = request.getIfHeader();
        if (StringUtils.isEmpty(header)) {
            createLock(request, response, lockable);
        } else {
            refreshLock(request, response, lockable, header);
        }
    }

    /**
     * Creates a lock on a resource.
     *
     * @param request  the client request
     * @param response the servlet response
     * @param resource the resource to lock
     * @throws BadRequestException
     * @throws NotAuthorizedException
     */
    private void createLock(Request request, Response response, LockableResource resource)
            throws BadRequestException, NotAuthorizedException {
        LockInfo lockInfo;
        LockTimeout timeout = LockTimeout.parseTimeout(request);
        try {
            lockInfo = LockInfoSaxHandler.parseLockInfo(request);
        } catch (SAXException | IOException exception) {
            throw new BadRequestException("Failed to read lock: " + exception.getMessage(), exception);
        }

        if (resourceHandler.isLockedOut(request, resource)) {
            responseHandler.respondLocked(request, response, resource);
        } else {
            LockResult result;
            try {
                result = resource.lock(timeout, lockInfo);
                if (result.isSuccessful()) {
                    respondSuccess(result, request, response);
                } else {
                    respondFailure(result, response);
                }
            } catch (PreConditionFailedException exception) {
                responseHandler.respondPreconditionFailed(request, response, resource);
            } catch (LockedException exception) {
                responseHandler.respondLocked(request, response, resource);
            }
        }
    }

    /**
     * Refresh a lock on a resource.
     *
     * @param request  the client request
     * @param response the servlet response
     * @param resource the resource to lock
     * @param ifHeader the If header
     * @throws NotAuthorizedException
     */
    private void refreshLock(Request request, Response response, LockableResource resource, String ifHeader)
            throws NotAuthorizedException {
        String token = LockHelper.parseToken(ifHeader);
        if (log.isDebugEnabled()) {
            log.debug("Refreshing lock on " + request.getAbsolutePath() + " with token=" + token);
        }
        LockResult result;
        try {
            result = resource.refreshLock(token);
            if (result.isSuccessful()) {
                respondSuccess(result, request, response);
            } else {
                respondFailure(result, response);
            }
        } catch (PreConditionFailedException exception) {
            responseHandler.respondPreconditionFailed(request, response, resource);
        }
    }

    /**
     * Invoked when a lock request is successful. This includes the lock token in the response.
     *
     * @param result   the lock result
     * @param request  the client request
     * @param response the servlet response
     */
    private void respondSuccess(LockResult result, Request request, Response response) {
        response.setStatus(Response.Status.SC_OK);
        String lock = LockHelper.serialiseLockResponse(result.getLockToken(), request.getAbsoluteUrl());
        if (log.isDebugEnabled()) {
            log.debug("Lock response: " + lock);
        }
        try {
            response.getOutputStream().write(lock.getBytes());
        } catch (IOException exception) {
            log.warn("Failed to write lock response", exception);
        }
    }

    /**
     * Invoked when a lock request fails.
     *
     * @param result   the lock result
     * @param response the servlet response
     */
    private void respondFailure(LockResult result, Response response) {
        if (log.isDebugEnabled()) {
            log.debug("Lock failed: " + result.getFailureReason());
        }
        response.setStatus(result.getFailureReason().status);
    }

}
