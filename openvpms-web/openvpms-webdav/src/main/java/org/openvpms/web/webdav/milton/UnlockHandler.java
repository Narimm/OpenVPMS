package org.openvpms.web.webdav.milton;

import io.milton.http.ExistingEntityHandler;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.ResourceHandlerHelper;
import io.milton.http.Response;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.exceptions.PreConditionFailedException;
import io.milton.http.webdav.WebDavResponseHandler;
import io.milton.resource.LockableResource;
import io.milton.resource.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A handler for WebDAV UNLOCK requests.
 *
 * @author Tim Anderson
 */
public class UnlockHandler implements ExistingEntityHandler {

    /**
     * The response handler.
     */
    private final WebDavResponseHandler responseHandler;

    /**
     * The resource handler.
     */
    private final ResourceHandlerHelper resourceHandler;

    /**
     * The methods supports by the handler.
     */
    private static final String[] METHODS = {Request.Method.UNLOCK.code};

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(UnlockHandler.class);

    /**
     * Constructs an {@link UnlockHandler}.
     *
     * @param responseHandler the response handler
     * @param resourceHandler the resource handler
     */
    public UnlockHandler(WebDavResponseHandler responseHandler, ResourceHandlerHelper resourceHandler) {
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
     * Processes an unlock for an existing resource.
     *
     * @param manager  the http manager
     * @param request  the client request
     * @param response the servlet response
     * @param resource the resource to unlock
     * @throws NotAuthorizedException
     * @throws BadRequestException
     * @throws ConflictException
     * @throws NotFoundException
     */
    @Override
    public void processExistingResource(HttpManager manager, Request request, Response response, Resource resource)
            throws NotAuthorizedException, BadRequestException, ConflictException, NotFoundException {
        LockableResource lockable = (LockableResource) resource;
        String token = LockHelper.parseToken(request.getLockTokenHeader());

        if (log.isDebugEnabled()) {
            log.debug("Unlocking resource " + request.getAbsolutePath() + " with token=" + token);
        }

        try {
            lockable.unlock(token);
            responseHandler.respondNoContent(resource, response, request);
        } catch (PreConditionFailedException ex) {
            responseHandler.respondPreconditionFailed(request, response, resource);
        }
    }

}
