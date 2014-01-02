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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.style;

import nextapp.echo2.app.MutableStyleSheet;
import nextapp.echo2.app.StyleSheet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Caches style sheets loaded from resources.
 * <p/>
 * Stylesheets may come from two sources; a default and an override source.
 * <p/>
 * The default stylesheet is loaded first, and styles from the override stylesheet added afterwards.
 *
 * @author Tim Anderson
 */
public class StyleSheetCache extends AbstractStyleSheetCache {

    /**
     * The resolution pattern.
     */
    private final Pattern resolutionPattern = Pattern.compile("(\\d+)x(\\d+)");

    /**
     * The default template.
     */
    private final StyleSheetTemplate defaultTemplate;

    /**
     * The override template.
     */
    private final StyleSheetTemplate overrideTemplate;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(StyleSheetCache.class);


    /**
     * Constructs a {@link StyleSheetCache}.
     *
     * @param defaultBaseName the default resource base name
     * @throws IOException         for any I/O error
     * @throws StyleSheetException if a resource cannot be found
     */
    public StyleSheetCache(String defaultBaseName) throws IOException {
        this(defaultBaseName, null);
    }


    /**
     * Constructs a {@link StyleSheetCache}.
     *
     * @param defaultBaseName  the default resource base name
     * @param overrideBaseName the override resource base name. May be {@code null}
     * @throws IOException         for any I/O error
     * @throws StyleSheetException if a resource cannot be found
     */
    public StyleSheetCache(String defaultBaseName, String overrideBaseName) throws IOException {
        defaultTemplate = getTemplate(defaultBaseName, true);
        overrideTemplate = getTemplate(overrideBaseName, false);
        Map<String, String> properties = getProperties(defaultBaseName + ".properties", true);
        if (overrideBaseName != null) {
            String name = overrideBaseName + ".properties";
            Map<String, String> overrides = getProperties(name, false);
            if (overrides != null) {
                log.info("Overriding default stylesheet properties using " + name);
                properties.putAll(overrides);
            } else {
                log.info("No style overrides found for: " + name);
            }
        }
        setDefaultProperties(properties);
        addResolutions(defaultBaseName, overrideBaseName);
    }

    /**
     * Returns a style sheet for the specified properties.
     *
     * @param properties the properties, used for token replacement
     * @return the style sheet for the specified properties
     * @throws StyleSheetException if the style sheet cannot be created
     */
    @Override
    public StyleSheet getStyleSheet(Map<String, String> properties) {
        MutableStyleSheet result = defaultTemplate.getStyleSheet(properties);
        if (overrideTemplate != null) {
            MutableStyleSheet override = overrideTemplate.getStyleSheet(properties);
            result.addStyleSheet(override);
        }
        return result;
    }

    /**
     * Loads a stylesheet template.
     *
     * @param baseName the template base name
     * @param required determines if the template is required or not
     * @throws IOException         for any I/O error
     * @throws StyleSheetException if the template cannot be found and is required
     */
    private StyleSheetTemplate getTemplate(String baseName, boolean required) throws IOException {
        StyleSheetTemplate result = null;
        String name = baseName + ".stylesheet";
        InputStream resource = getResource(name, required);
        if (resource != null) {
            if (log.isInfoEnabled()) {
                log.info("Loading stylesheet " + name);
            }
            result = new StyleSheetTemplate(resource);
        }
        return result;
    }

    /**
     * Adds resolutions defined in a <em>defaultBaseName</em>-resolutions.properties file, overriding them with those
     * defined in a <em>overrideBaseName</em>-resolutions.properties file if present.
     *
     * @param defaultBaseName  the default resource base name
     * @param overrideBaseName the override resource base name. May be {@code null}
     * @throws StyleSheetException for any error
     * @throws IOException         for any I/O error
     */
    private void addResolutions(String defaultBaseName, String overrideBaseName) throws IOException {
        addResolutions(defaultBaseName);
        addResolutions(overrideBaseName);
    }

    /**
     * Adds resolutions defined in a <em>baseName</em>-resolutions.properties file, if present.
     *
     * @throws StyleSheetException for any error
     * @throws IOException         for any I/O error
     */
    private void addResolutions(String baseName) throws IOException {
        Map<String, String> resolutions = getProperties(baseName + "-resolutions.properties", false);
        if (resolutions != null) {
            for (Map.Entry<String, String> entry : resolutions.entrySet()) {
                if (entry.getKey().startsWith("resolution")) {
                    addResolution(entry.getValue(), baseName);
                }
            }
        }
    }

    /**
     * Adds properties for the specified resolution.
     *
     * @param resolution the screen resolution, of the form <em>&lt;width&gt;x&lt;height&gt;
     * @param baseName   the resource base name
     * @throws IOException for any I/O error
     */
    private void addResolution(String resolution, String baseName) throws IOException {
        Matcher matcher = resolutionPattern.matcher(resolution);
        if (matcher.matches()) {
            int width = Integer.valueOf(matcher.group(1));
            int height = Integer.valueOf(matcher.group(2));
            String name = baseName + "-" + resolution + ".properties";
            Map<String, String> properties = getProperties(name, true);
            Dimension size = new Dimension(width, height);
            if (log.isInfoEnabled()) {
                if (getResolution(size) != null) {
                    log.info("Replacing resolution " + width + "x" + height + " with " + name);
                } else {
                    log.info("Adding resolution " + width + "x" + height + " from " + name);
                }
            }
            addResolution(size, properties);
        } else {
            throw new StyleSheetException(StyleSheetException.ErrorCode.InvalidResolution, resolution);
        }
    }

    /**
     * Returns properties for the specified resource.
     *
     * @param name     the resource name
     * @param required determines if the resource is required or not
     * @return the resource, or {@code null} if it does exist and wasn't required
     * @throws IOException         if an I/O error occurs
     * @throws StyleSheetException if the resource cannot be found and is required
     */
    private Map<String, String> getProperties(String name, boolean required) throws IOException {
        Map<String, String> result = null;
        InputStream stream = getResource(name, required);
        if (stream != null) {
            result = new HashMap<String, String>();
            Properties properties = new Properties();
            properties.load(stream);
            for (String key : properties.stringPropertyNames()) {
                result.put(key, (String) properties.get(key));
            }
        }
        return result;
    }

    /**
     * Returns the stream for a resource.
     *
     * @param resource the resource path
     * @param required determines if the resource is required or not
     * @return the stream, or {@code null} if its not required
     * @throws StyleSheetException if the resource cannot be found and is required
     */
    private InputStream getResource(String resource, boolean required) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resource);
        if (stream == null) {
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        }
        if (stream == null) {
            if (required) {
                throw new StyleSheetException(StyleSheetException.ErrorCode.ResourceNotFound, resource);
            }
        }
        return stream;
    }

}