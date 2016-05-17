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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Provides an {@link ObjectMapper} that ensures date/times are serialized in ISO-8601 format.
 *
 * @author Tim Anderson
 * @see <a href="http://jersey.java.net/documentation/latest/user-guide.html#jackson-registration">Jackson</a>
 */
@Provider
@Produces("application/json")
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    /**
     * The object mapper.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructs {@link ObjectMapperContextResolver}.
     *
     * @param timeZone the time zone used to determine how date/times are serialized
     */
    public ObjectMapperContextResolver(TimeZone timeZone) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        format.setTimeZone(timeZone);
        mapper.setDateFormat(format);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Get a context of type {@code T} that is applicable to the supplied type.
     *
     * @param type the class of object for which a context is desired
     * @return a context for the supplied type or {@code null} if a context for the supplied type is not available from
     * this provider.
     */
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
