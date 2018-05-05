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

package org.openvpms.booking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.openvpms.booking.impl.BadRequestExceptionMapper;
import org.openvpms.booking.impl.BookingServiceImpl;
import org.openvpms.booking.impl.LocationServiceImpl;
import org.openvpms.booking.impl.NotFoundExceptionMapper;
import org.openvpms.booking.impl.ScheduleServiceImpl;
import org.openvpms.smartflow.client.ObjectMapperContextResolver;

import javax.ws.rs.ApplicationPath;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * The resource configuration for the booking API.
 *
 * @author Tim Anderson
 */
@ApplicationPath("ws/booking/v1")
public class BookingApplication extends ResourceConfig {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(BookingApplication.class);

    /**
     * Default constructor.
     */
    public BookingApplication() {
        register(JacksonFeature.class);
        register(new ObjectMapperContextResolver(TimeZone.getDefault()));
        register(BadRequestExceptionMapper.class);
        register(NotFoundExceptionMapper.class);
        register(LocationServiceImpl.class);
        register(ScheduleServiceImpl.class);
        register(BookingServiceImpl.class);
        if (log.isDebugEnabled()) {
            register(new LoggingFilter(new DebugLog(log), true));
        }
    }

    /**
     * Workaround to allow JAX-RS logging to be delegated to log4j.
     */
    private static final class DebugLog extends Logger {

        private final Log log;

        protected DebugLog(Log log) {
            super(GLOBAL_LOGGER_NAME, null);
            this.log = log;
        }

        @Override
        public void info(String msg) {
            log.debug(msg);
        }
    }

}
