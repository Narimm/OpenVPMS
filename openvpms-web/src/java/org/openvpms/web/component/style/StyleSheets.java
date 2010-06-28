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
import java.util.Map;


/**
 * Manages style sheets for different screen resolutions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface StyleSheets {

    /**
     * Returns a style sheet for the specified screen resolution.
     *
     * @param width  the screen width
     * @param height the screen height
     * @return the style sheet for the specified resolution
     * @throws StyleSheetException if the style sheet cannot be created
     */
    StyleSheet getStyleSheet(int width, int height);

    /**
     * Returns a style sheet for the specified screen resolution.
     *
     * @param size the screen resolution
     * @return the style sheet for the specified resolution
     * @throws StyleSheetException if the style sheet cannot be created
     */
    StyleSheet getStyleSheet(Dimension size);

    /**
     * Returns a style sheet for the specified properties.
     *
     * @param properties the properties, used for token replacement
     * @return the style sheet for the specified properties
     * @throws StyleSheetException if the style sheet cannot be created
     */
    StyleSheet getStyleSheet(Map<String, String> properties);

    /**
     * Returns the default properties for token replacement.
     * <p/>
     * These properties apply to all screen resolutionss
     *
     * @return the default properties
     */
    Map<String, String> getDefaultProperties();

    /**
     * Returns the properties for token replacement, for the specified screen resolution.
     * <p/>
     * All properties are evaluated.
     *
     * @param width  the screen width
     * @param height the screen height
     * @return the properties
     */
    Map<String, String> getProperties(int width, int height);

    /**
     * Returns the properties for token replacement, for the specified screen resolution.
     *
     * @param width    the screen width
     * @param height   the screen height
     * @param evaluate determines if properties should be evaluated
     * @return the properties
     */
    Map<String, String> getProperties(int width, int height, boolean evaluate);

    /**
     * Returns the properties for token replacement, for the specified screen resolution.
     *
     * @param size     the screen resolution
     * @param evaluate determines if properties should be evaluated
     * @return the properties
     */
    Map<String, String> getProperties(Dimension size, boolean evaluate);

    /**
     * Evaluates properties for the specified screen resolution.
     *
     * @param properties the properties to evaluate
     * @param width      the screen width
     * @param height     the screen height
     * @return the evaluated properties
     */
    Map<String, String> evaluate(Map<String, String> properties, int width, int height);

    /**
     * Returns the screen resolutions for which there are specific properties.
     *
     * @return the screen resolutions
     */
    Dimension[] getResolutions();

    /**
     * Returns the unevaluated properties for the specified resolution.
     *
     * @param size the resolution
     * @return the unevaluated properties, or <tt>null</tt> if none are found
     */
    Map<String, String> getResolution(Dimension size);
}
