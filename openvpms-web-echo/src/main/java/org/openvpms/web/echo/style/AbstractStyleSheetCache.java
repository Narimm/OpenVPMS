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

package org.openvpms.web.echo.style;

import nextapp.echo2.app.StyleSheet;

import java.awt.Dimension;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Implementation of {@link StyleSheets} that provides caching of style sheets.
 *
 * @author Tim Anderson
 */
public abstract class AbstractStyleSheetCache implements StyleSheets {

    /**
     * A map of resolutions and their corresponding properties.
     */
    private Map<Dimension, Map<String, String>> resolutions = new HashMap<Dimension, Map<String, String>>();

    /**
     * A map of resolutions and their corresponding evaluated properties.
     */
    private Map<Dimension, Map<String, String>> evaluatedResolutions = new HashMap<Dimension, Map<String, String>>();

    /**
     * Cached style sheets, keyed on resolution.
     */
    private Map<Dimension, Style> stylesheets = new HashMap<Dimension, Style>();

    /**
     * The style property evaluator.
     */
    private StylePropertyEvaluator evaluator;


    /**
     * Returns a style for the specified screen resolution.
     *
     * @param width  the screen width
     * @param height the screen height
     * @return the style sheet for the specified resolution
     * @throws StyleSheetException if the style sheet cannot be created
     */
    @Override
    public Style getStyle(int width, int height) {
        Dimension size = new Dimension(width, height);
        return getStyle(size);
    }

    /**
     * Returns a style sheet for the specified screen resolution.
     *
     * @param width  the screen width
     * @param height the screen height
     * @return the style sheet for the specified resolution
     */
    public StyleSheet getStyleSheet(int width, int height) {
        return getStyle(width, height).getStylesheet();
    }

    /**
     * Returns a style sheet for the specified screen resolution.
     *
     * @param size the screen resolution
     * @return the style sheet for the specified resolution
     * @throws StyleSheetException if the style sheet cannot be created
     */
    @Override
    public Style getStyle(Dimension size) {
        Style result = getCachedStyleSheet(size);
        if (result == null) {
            Map<String, String> properties = getProperties(size, true);
            result = new Style(getStyleSheet(properties), size, properties);
            stylesheets.put(size, result);
        }
        return result;
    }

    /**
     * Returns a style sheet for the specified screen resolution.
     *
     * @param size the screen resolution
     * @return the style sheet for the specified resolution
     */
    public synchronized StyleSheet getStyleSheet(Dimension size) {
        return getStyle(size).getStylesheet();
    }

    /**
     * Returns the default properties.
     *
     * @return the default properties. This map is read-only
     */
    public Map<String, String> getDefaultProperties() {
        return evaluator.getDefaultProperties();
    }

    /**
     * Returns the properties for token replacement, for the specified screen resolution.
     * <p/>
     * All properties are evaluated.
     *
     * @param width  the screen width
     * @param height the screen height
     * @return the properties
     */
    public Map<String, String> getProperties(int width, int height) {
        return getProperties(width, height, true);
    }

    /**
     * Returns the properties for token replacement, for the specified screen resolution.
     *
     * @param width    the screen width
     * @param height   the screen height
     * @param evaluate determines if properties should be evaluated
     * @return the properties
     */
    public Map<String, String> getProperties(int width, int height, boolean evaluate) {
        return getProperties(new Dimension(width, height), evaluate);
    }

    /**
     * Returns the properties for token replacement, for the specified screen resolution.
     *
     * @param size     the screen resolution
     * @param evaluate determines if properties should be evaluated
     * @return the properties
     */
    public synchronized Map<String, String> getProperties(Dimension size, boolean evaluate) {
        Map<String, String> properties;
        if (evaluate) {
            properties = evaluatedResolutions.get(size);
            if (properties == null) {
                Map<String, String> closest = getClosestResolution(size);
                properties = evaluator.getProperties(size.width, size.height, closest);
                properties = Collections.unmodifiableMap(properties);
                evaluatedResolutions.put(size, properties);
            }
        } else {
            Map<String, String> closest = getClosestResolution(size);
            properties = new HashMap<String, String>(getDefaultProperties());
            if (closest != null) {
                properties.putAll(closest);
            }
        }
        return properties;
    }

    /**
     * Evaluates properties for the specified screen resolution.
     *
     * @param properties the properties to evaluate
     * @param width      the screen width
     * @param height     the screen height
     * @return the evaluated properties
     */
    public synchronized Map<String, String> evaluate(Map<String, String> properties, int width, int height) {
        return evaluator.getProperties(width, height, properties);
    }

    /**
     * Returns the screen resolutions for which there are specific properties.
     * <p/>
     * These are ordered from smallest to largest.
     *
     * @return the screen resolutions
     */
    public synchronized Dimension[] getResolutions() {
        return sortResolutions(resolutions.keySet());
    }

    /**
     * Returns the unevaluated properties for the specified resolution.
     *
     * @param size the resolution
     * @return the unevaluated properties, or {@code null} if none are found
     */
    public synchronized Map<String, String> getResolution(Dimension size) {
        return resolutions.get(size);
    }

    /**
     * Sets the default properties.
     * <p/>
     * This should be called before any other operation
     *
     * @param properties the default properties
     */
    protected synchronized void setDefaultProperties(Map<String, String> properties) {
        evaluator = new StylePropertyEvaluator(properties);
    }

    /**
     * Adds style sheet data for the specified resolution.
     *
     * @param size       the screen resolution
     * @param properties the properties to add
     */
    protected synchronized void addResolution(Dimension size, Map<String, String> properties) {
        resolutions.put(size, Collections.unmodifiableMap(properties));
        evaluatedResolutions.remove(size);
        stylesheets.remove(size);
    }

    /**
     * Removes the style sheet data for the specified resolution.
     *
     * @param size the
     */
    protected synchronized void removeResolution(Dimension size) {
        resolutions.remove(size);
        evaluatedResolutions.remove(size);
        stylesheets.remove(size);
    }

    /**
     * Returns all resolutions.
     *
     * @return the resolutions
     */
    protected Map<Dimension, Map<String, String>> getAllResolutions() {
        return resolutions;
    }

    /**
     * Returns the unevaluated properties that the specified resolution or if none are available, for the nearest
     * resolution.
     *
     * @param size the resolution
     * @return the closest properties, or {@code null} if none are available
     */
    protected synchronized Map<String, String> getClosestResolution(Dimension size) {
        Map<String, String> result = getResolution(size);
        if (result == null) {
            Dimension best = null;
            for (Dimension resolution : getResolutions()) {
                if (resolution.width <= size.width || best == null) {
                    if (best == null) {
                        best = resolution;
                    } else if (best.width != resolution.width
                               || (best.width == resolution.width && resolution.height <= size.height)) {
                        best = resolution;
                    }
                } else {
                    break;
                }
            }
            result = (best != null) ? getResolution(best) : null;
        }
        return result;
    }

    /**
     * Clears the cached stylesheets and evaluated resolution properties.
     */
    protected synchronized void clearCache() {
        stylesheets.clear();
        evaluatedResolutions.clear();
    }

    /**
     * Returns the cached style sheet for the specified resolution.
     *
     * @param width  the screen width
     * @param height the screen height
     * @return the cached style sheet, or {@code null} if none is found
     */
    protected Style getCachedStyleSheet(int width, int height) {
        return getCachedStyleSheet(new Dimension(width, height));
    }

    /**
     * Returns the cached style sheet for the specified resolution.
     *
     * @param size the screen resolution
     * @return the cached style sheet, or {@code null} if none is found
     */
    protected synchronized Style getCachedStyleSheet(Dimension size) {
        return stylesheets.get(size);
    }

    /**
     * Determines if the specified resolution has style sheet data.
     *
     * @param size the screen resolution
     * @return {@code true} if properties exist for the resolution; otherwise {@code false}
     */
    protected synchronized boolean hasResolution(Dimension size) {
        return resolutions.containsKey(size);
    }

    /**
     * Helper to return resolutions as an ordered array, from smallest to largest.
     *
     * @param resolutions the resolutions to sort
     * @return the screen resolutions
     */
    protected Dimension[] sortResolutions(Set<Dimension> resolutions) {
        Comparator<Dimension> comparator = new Comparator<Dimension>() {
            /**
             * Compares its two arguments for order.  Returns a negative integer,
             * zero, or a positive integer as the first argument is less than, equal
             * to, or greater than the second.<p>
             *
             * @param o1 the first object to be compared.
             * @param o2 the second object to be compared.
             * @return a negative integer, zero, or a positive integer as the
             *         first argument is less than, equal to, or greater than the
             *         second.
             * @throws ClassCastException if the arguments' types prevent them from
             *                            being compared by this comparator.
             */
            public int compare(Dimension o1, Dimension o2) {
                int result = o1.width - o2.width;
                if (result == 0) {
                    result = o1.height - o2.height;
                }
                return result;
            }
        };
        SortedSet<Dimension> result = new TreeSet<Dimension>(comparator);
        result.addAll(resolutions);
        return result.toArray(new Dimension[result.size()]);
    }

}

