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

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * Maps {@link NotFoundException} so that the message is included in the response.
 *
 * @author Tim Anderson
 */
public class NotFoundExceptionMapper extends AbstractExceptionMapper<NotFoundException> {

    /**
     * Default constructor.
     */
    public NotFoundExceptionMapper() {
        super(Response.Status.NOT_FOUND);
    }
}
