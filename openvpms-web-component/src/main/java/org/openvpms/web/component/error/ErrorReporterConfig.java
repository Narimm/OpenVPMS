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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.error;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Configuration for {@link ErrorReporter}.
 *
 * @author Tim Anderson
 */
public class ErrorReporterConfig {

    /**
     * List of exceptions to exclude from reporting.
     */
    private List<ExceptionConfig> excludes;

    /**
     * Excluded exceptions, keyed on class name.
     */
    private transient Map<String, ExceptionConfig> excludesMap;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ErrorReporterConfig.class);


    /**
     * Reads the configuration from a stream.
     *
     * @param stream the stream to read from
     * @return the configuration
     */
    public static ErrorReporterConfig read(InputStream stream) {
        return (ErrorReporterConfig) createStream().fromXML(stream);
    }

    /**
     * Sets the excluded exceptions.
     *
     * @param excludes the excluded exceptions
     */
    public synchronized void setExcludes(List<ExceptionConfig> excludes) {
        this.excludes = excludes;
        excludesMap = null;
    }

    /**
     * Returns the excluded exceptions.
     *
     * @return the excluded exceptions
     */
    public synchronized List<ExceptionConfig> getExcludes() {
        return excludes;
    }

    /**
     * Determines if the exception is excluded or not.
     *
     * @param exception the exception to check
     * @return {@code true} if the exception is excluded
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public boolean isExcluded(Throwable exception) {
        boolean result = false;
        Map<String, ExceptionConfig> map = getExcludesMap();
        ExceptionConfig config = map.get(exception.getClass().getName());
        if (config == null) {
            Throwable root = ExceptionHelper.getRootCause(exception);
            if (root.getClass() != exception.getClass()) {
                config = map.get(root.getClass().getName());
            }
        }
        if (config != null) {
            result = config.isExcluded(exception);
        }
        return result;
    }

    /**
     * Serialises this to XML.
     *
     * @param stream the stream to write to
     */
    public synchronized void write(OutputStream stream) {
        XStream xs = createStream();
        xs.toXML(this, stream);
    }

    /**
     * Returns a map of excluded exceptions, keyed on class name.
     *
     * @return the map of excluded exceptions
     */
    private synchronized Map<String, ExceptionConfig> getExcludesMap() {
        if (excludesMap == null) {
            excludesMap = new HashMap<String, ExceptionConfig>();
            if (excludes != null) {
                for (ExceptionConfig exclude : excludes) {
                    ExceptionConfig old = excludesMap.put(exclude.getClassName(), exclude);
                    if (old != null) {
                        log.warn("Replacing existing exclusion for class: " + exclude.getClassName());
                    }
                    checkClass(exclude);
                }
            }
        }
        return excludesMap;
    }

    /**
     * Logs a warning if the configured exception class doesn't exist.
     *
     * @param config the configuration
     */
    private void checkClass(ExceptionConfig config) {
        try {
            Thread.currentThread().getContextClassLoader().loadClass(config.getClassName());
        } catch (ClassNotFoundException exception) {
            log.warn("Class not found: " + config.getClassName());
        }
    }

    /**
     * Creates a new XStream to read/write ErrorReporterrConfig instances.
     *
     * @return a new XStream
     */
    private static XStream createStream() {
        XStream xs = new XStream();
        xs.alias("error-reporter-config", ErrorReporterConfig.class);
        xs.addImplicitCollection(ErrorReporterConfig.class, "excludes", "exclude-exception", ExceptionConfig.class);
        xs.addImplicitCollection(ExceptionConfig.class, "codes", "code", String.class);
        xs.addImplicitCollection(ExceptionConfig.class, "messages", "message", String.class);
        xs.addImplicitCollection(ExceptionConfig.class, "causes", "cause", ExceptionConfig.class);
        return xs;
    }

}
