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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.booking.impl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Maps exceptions so that the message is included in the response.
 *
 * @author Tim Anderson
 */
public abstract class AbstractExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {

    /**
     * The status to return.
     */
    private final Response.Status status;

    /**
     * Constructs an {@link AbstractExceptionMapper}.
     *
     * @param status the status to return
     */
    public AbstractExceptionMapper(Response.Status status) {
        this.status = status;
    }

    /**
     * Map an exception to a {@link Response}.
     *
     * @param exception the exception to map to a response.
     * @return a response mapped from the supplied exception
     */
    @Override
    public Response toResponse(E exception) {
        return Response.status(status).type(MediaType.TEXT_PLAIN).entity(exception.getMessage()).build();
    }
}
