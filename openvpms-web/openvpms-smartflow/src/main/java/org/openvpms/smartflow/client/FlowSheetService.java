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

package org.openvpms.smartflow.client;

import org.apache.commons.logging.Log;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.openvpms.smartflow.i18n.FlowSheetMessages;

import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Base class for Smart Flow Sheet services.
 *
 * @author Tim Anderson
 * @author benjamincharlton on 21/10/2015.
 */
public abstract class FlowSheetService {

    /**
     * The Smart Flow Sheet service root URL.
     */
    private final String url;

    /**
     * The EMR API key.
     */
    private final String emrApiKey;

    /**
     * The clinic API key.
     */
    private final String clinicApiKey;

    /**
     * The time zone.
     */
    private final TimeZone timeZone;

    /**
     * The logger.
     */
    private final Log log;

    /**
     * Empty form.
     */
    protected static final Form EMPTY_FORM = new Form();

    /**
     * Constructs a {@link FlowSheetService}.
     *
     * @param url          the Smart Flow Sheet URL
     * @param emrApiKey    the EMR API key
     * @param clinicApiKey the clinic API key
     * @param timeZone     the timezone. This determines how dates are serialized
     * @param log          the logger for JAX-RS requests
     */
    public FlowSheetService(String url, String emrApiKey, String clinicApiKey, TimeZone timeZone, Log log) {
        this.url = url;
        this.emrApiKey = emrApiKey;
        this.clinicApiKey = clinicApiKey;
        this.timeZone = timeZone;
        this.log = log;
    }

    /**
     * Throws a {@link FlowSheetException} with an appropriate error message for a {@code NotAuthorizedException}.
     *
     * @param exception the original exception
     */
    protected void notAuthorised(NotAuthorizedException exception) {
        log.error(exception, exception);
        throw new FlowSheetException(FlowSheetMessages.notAuthorised());
    }

    /**
     * Checks an exception for an SSLHandshakeException cause, and throws an {@link FlowSheetException} with appropriate
     * error message if it has one.
     *
     * @param exception the exception
     * @throws FlowSheetException if the exception has a SSLHandshakeException cause
     */
    protected void checkSSL(Throwable exception) {
        if (exception.getCause() instanceof SSLHandshakeException) {
            log.error(exception, exception);
            throw new FlowSheetException(FlowSheetMessages.cannotConnectUsingSSL(url));
        }
    }

    /**
     * Creates a JAX-RS client.
     *
     * @return a new JAX-RS client
     */
    protected javax.ws.rs.client.Client getClient() {
        ObjectMapperContextResolver resolver = new ObjectMapperContextResolver(timeZone);
        ClientConfig config = new ClientConfig()
                .register(resolver)
                .register(JacksonFeature.class)
                .register(new ErrorResponseFilter(resolver.getContext(Object.class)));
        javax.ws.rs.client.Client resource = ClientBuilder.newClient(config);
        if (log.isDebugEnabled()) {
            resource.register(new LoggingFilter(new DebugLog(log), true));
        }
        return resource;
    }

    /**
     * Returns a proxy for the specified type.
     *
     * @param type   the type
     * @param client the client
     * @return a proxy for the type
     */
    protected <T> T getResource(Class<T> type, javax.ws.rs.client.Client client) {
        WebTarget target = getWebTarget(client);
        return WebResourceFactory.newResource(type, target, false, getHeaders(), Collections.<Cookie>emptyList(),
                                              EMPTY_FORM);
    }

    /**
     * Returns a {@code WebTarget} for the SFS url.
     *
     * @param client the client
     * @return the target
     */
    protected WebTarget getWebTarget(javax.ws.rs.client.Client client) {
        return client.target(url);
    }

    /**
     * Returns the header parameters to use when invoking {@code WebResourceFactory.newResource(...}}.
     *
     * @return the header parameters
     */
    protected MultivaluedMap<String, Object> getHeaders() {
        MultivaluedMap<String, Object> header = new MultivaluedHashMap<>();
        header.add("emrApiKey", emrApiKey);
        header.add("clinicApiKey", clinicApiKey);
        header.add("timezoneName", timeZone.getID());
        return header;
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
