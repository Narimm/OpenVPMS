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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openvpms.smartflow.model.Error;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * A filter used to handle 40x and 50x series errors. These extract any error messages returned in the body of
 * responses, and includes them in the thrown exception.
 *
 * @author Tim Anderson
 */
public class ErrorResponseFilter implements ClientResponseFilter {

    /**
     * The object mapper.
     */
    private final ObjectMapper mapper;

    /**
     * Constructs an {@link ErrorResponseFilter}.
     *
     * @param mapper the mapper
     */
    public ErrorResponseFilter(@Context ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Filter method called after a response has been provided for a request (either by a
     * {@link ClientRequestFilter request filter} or when the HTTP invocation returns.
     * <p>
     * Filters in the filter chain are ordered according to their {@code javax.annotation.Priority}
     * class-level annotation value.
     *
     * @param requestContext  request context.
     * @param responseContext response context.
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Response.StatusType statusInfo = responseContext.getStatusInfo();
        if (statusInfo.getFamily() == Response.Status.Family.CLIENT_ERROR
            || statusInfo.getFamily() == Response.Status.Family.SERVER_ERROR) {
            if (responseContext.hasEntity() && MediaTypeHelper.isJSON(responseContext.getMediaType())) {
                Error error = mapper.readValue(responseContext.getEntityStream(), Error.class);
                String message = error.getMessage();

                WebApplicationException exception;
                int status = responseContext.getStatus();
                switch (status) {
                    case HttpServletResponse.SC_BAD_REQUEST:
                        exception = new BadRequestException(message);
                        break;
                    case HttpServletResponse.SC_UNAUTHORIZED:
                        exception = new NotAuthorizedException(message, Response.status(status).build());
                        break;
                    case HttpServletResponse.SC_FORBIDDEN:
                        exception = new ForbiddenException(message);
                        break;
                    case HttpServletResponse.SC_NOT_FOUND:
                        exception = new NotFoundException(message);
                        break;
                    case HttpServletResponse.SC_METHOD_NOT_ALLOWED:
                        exception = new NotAllowedException(message);
                        break;
                    case HttpServletResponse.SC_NOT_ACCEPTABLE:
                        exception = new NotAcceptableException(message);
                        break;
                    case HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE:
                        exception = new NotSupportedException(message);
                        break;
                    case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
                        exception = new InternalServerErrorException(message);
                        break;
                    case HttpServletResponse.SC_SERVICE_UNAVAILABLE:
                        exception = new ServiceUnavailableException(message);
                        break;
                    default:
                        // NOTE: SFS uses custom error statuses. E.g. 465 Access to the Document Denied
                        exception = new WebApplicationException(message, status);
                }

                throw exception;
            }
        }
    }

}