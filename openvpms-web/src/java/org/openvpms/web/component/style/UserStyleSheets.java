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

import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * A cache of style sheets that enables individual users to override the default styles.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UserStyleSheets extends AbstractStyleSheetCache {

    /**
     * The underlying style sheet cache. This is used when a style has not been overridden.
     */
    private final StyleSheets cache;

    /**
     * Determines if the defaults style properties have been overridden.
     */
    private boolean overrideCacheDefaults = false;


    /**
     * Constructs an <tt>UserStyleSheets</tt>.
     *
     * @param cache the style sheet cache
     */
    public UserStyleSheets(StyleSheets cache) {
        this.cache = cache;
        setDefaultProperties(cache.getDefaultProperties());
    }

    /**
     * Returns a style sheet for the specified screen resolution.
     *
     * @param size the screen resolution
     * @return the style sheet for the specified resolution
     */
    @Override
    public StyleSheet getStyleSheet(Dimension size) {
        StyleSheet result = getCachedStyleSheet(size);
        if (result == null) {
            if (overrideCacheDefaults || hasResolution(size)) {
                result = super.getStyleSheet(size);
            } else {
                result = cache.getStyleSheet(size);
            }
        }
        return result;
    }

    /**
     * Sets the default properties.
     * <p/>
     * This should be called before any other operation
     *
     * @param properties the default properties
     */
    @Override
    public synchronized void setDefaultProperties(Map<String, String> properties) {
        super.setDefaultProperties(properties);
        overrideCacheDefaults = true;
        clearCache();
    }

    /**
     * Returns a style sheet for the specified properties.
     *
     * @param properties the properties, used for token replacement
     * @return the style sheet for the specified properties
     */
    public StyleSheet getStyleSheet(Map<String, String> properties) {
        return cache.getStyleSheet(properties);
    }

    /**
     * Sets the properties for a screen resolution.
     *
     * @param properties the properties
     * @param width      the screen width
     * @param height     the screen height
     */
    public void setProperties(Map<String, String> properties, int width, int height) {
        Dimension size = new Dimension(width, height);
        addResolution(size, properties);
    }

    /**
     * Reverts any changes.
     */
    public void reset() {
        setDefaultProperties(cache.getDefaultProperties());
        overrideCacheDefaults = false;
        for (Dimension size : getResolutions()) {
            removeResolution(size);
        }
    }

    /**
     * Returns the screen resolutions for which there are specific properties.
     * <p/>
     * These are ordered from smallest to largest.
     *
     * @return the screen resolutions
     */
    @Override
    public Dimension[] getResolutions() {
        Set<Dimension> overrides = getAllResolutions().keySet();
        Set<Dimension> resolutions = new HashSet<Dimension>(Arrays.asList(cache.getResolutions()));
        resolutions.addAll(overrides);
        return sortResolutions(resolutions);
    }

    /**
     * Returns the unevaluated properties for the specified resolution.
     *
     * @param size the resolution
     * @return the unevaluated properties, or <tt>null</tt> if none are found
     */
    @Override
    public Map<String, String> getResolution(Dimension size) {
        Map<String, String> result = super.getResolution(size);
        if (result == null) {
            result = cache.getResolution(size);
        }
        return result;
    }
}