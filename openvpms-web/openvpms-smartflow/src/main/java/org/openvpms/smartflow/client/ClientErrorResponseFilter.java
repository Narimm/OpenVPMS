package org.openvpms.smartflow.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openvpms.smartflow.model.Error;

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
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * A filter used to handle 40x series errors. These extract any error messages returned in the body of responses, and
 * include them in the thrown exception.
 *
 * @author Tim Anderson
 */
class ClientErrorResponseFilter implements ClientResponseFilter {

    /**
     * The object mapper.
     */
    private static ObjectMapper mapper = new ObjectMapper();

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
        if (responseContext.getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR) {
            if (responseContext.hasEntity()) {
                Error error = mapper.readValue(responseContext.getEntityStream(), Error.class);
                String message = error.getMessage();

                Response.Status status = Response.Status.fromStatusCode(responseContext.getStatus());
                WebApplicationException exception;
                switch (status) {
                    case BAD_REQUEST:
                        exception = new BadRequestException(message);
                        break;
                    case UNAUTHORIZED:
                        exception = new NotAuthorizedException(message, Response.status(status).build());
                        break;
                    case FORBIDDEN:
                        exception = new ForbiddenException(message);
                        break;
                    case NOT_FOUND:
                        exception = new NotFoundException(message);
                        break;
                    case METHOD_NOT_ALLOWED:
                        exception = new NotAllowedException(message);
                        break;
                    case NOT_ACCEPTABLE:
                        exception = new NotAcceptableException(message);
                        break;
                    case UNSUPPORTED_MEDIA_TYPE:
                        exception = new NotSupportedException(message);
                        break;
                    case INTERNAL_SERVER_ERROR:
                        exception = new InternalServerErrorException(message);
                        break;
                    case SERVICE_UNAVAILABLE:
                        exception = new ServiceUnavailableException(message);
                        break;
                    default:
                        exception = new WebApplicationException(message, status);
                }

                throw exception;
            }
        }
    }

}