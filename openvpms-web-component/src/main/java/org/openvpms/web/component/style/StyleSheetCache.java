/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.style;

import nextapp.echo2.app.StyleSheet;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Caches style sheets loaded from resources.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StyleSheetCache extends AbstractStyleSheetCache {

    /**
     * Base name for all style sheets resources.
     */
    private final String baseName;

    /**
     * The resolution pattern.
     */
    private final Pattern resolutionPattern = Pattern.compile("(\\d+)x(\\d+)");

    /**
     * The template.
     */
    private final StyleSheetTemplate template;


    /**
     * Constructs a <tt>StyleSheetCache</tt>.
     *
     * @param baseName the resource base name
     * @throws IOException         for any I/O error
     * @throws StyleSheetException if a resource cannot be found
     */
    public StyleSheetCache(String baseName) throws IOException {
        this.baseName = baseName;
        template = new StyleSheetTemplate(getResource(baseName + ".stylesheet", true));
        Map<String, String> defaults = getProperties(baseName + ".properties", true);
        setDefaultProperties(defaults);
        addResolutions();
    }

    /**
     * Returns a style sheet for the specified properties.
     *
     * @param properties the properties, used for token replacement
     * @return the style sheet for the specified properties
     * @throws StyleSheetException if the style sheet cannot be created
     */
    public StyleSheet getStyleSheet(Map<String, String> properties) {
        return template.getStyleSheet(properties);
    }

    /**
     * Adds resolutions defined in a <tt><em>baseName</em>-resolutions.properties</tt> file, if present.
     *
     * @throws StyleSheetException for any error
     * @throws IOException         for any I/O error
     */
    private void addResolutions() throws IOException {
        Map<String, String> resolutions = getProperties(baseName + "-resolutions.properties", false);
        if (resolutions != null) {
            for (Map.Entry<String, String> entry : resolutions.entrySet()) {
                if (entry.getKey().startsWith("resolution")) {
                    addResolution(entry.getValue());
                }
            }
        }
    }

    /**
     * Adds properties for the specified resolution.
     *
     * @param resolution the screen resolution, of the form <em>&lt;width&gt;x&lt;height&gt;
     * @throws IOException for any I/O error
     */
    private void addResolution(String resolution) throws IOException {
        Matcher matcher = resolutionPattern.matcher(resolution);
        if (matcher.matches()) {
            int width = Integer.valueOf(matcher.group(1));
            int height = Integer.valueOf(matcher.group(2));
            Map<String, String> properties = getProperties(baseName + "-" + resolution + ".properties", true);
            addResolution(new Dimension(width, height), properties);
        } else {
            throw new StyleSheetException(StyleSheetException.ErrorCode.InvalidResolution, resolution);
        }
    }

    /**
     * Returns properties for the specified resource.
     *
     * @param name     the resource name
     * @param required determines if the resource is required or not
     * @return the resource, or <tt>null</tt> if it does exist and wasn't required
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
     * @return the stream, or <tt>null</tt> if its not required
     * @throws StyleSheetException if the resource cannot be found and is required
     */
    private InputStream getResource(String resource, boolean required) {
        InputStream stream = StyleSheetCache.class.getClassLoader().getResourceAsStream(resource);
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