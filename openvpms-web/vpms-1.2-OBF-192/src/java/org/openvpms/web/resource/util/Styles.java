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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.resource.util;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.StyleSheet;
import nextapp.echo2.app.componentxml.ComponentXmlException;
import nextapp.echo2.app.componentxml.StyleSheetLoader;
import nextapp.echo2.webcontainer.ContainerContext;
import nextapp.echo2.webrender.ClientProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Stylesheet helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class Styles {

    /**
     * Default style name.
     */
    public static final String DEFAULT = "default";

    /**
     * Error style name.
     */
    public static final String ERROR = "error";

    /**
     * Selected style name.
     */
    public static final String SELECTED = "selected";

    /**
     * The default style sheet.
     */
    public static final StyleSheet DEFAULT_STYLE_SHEET;

    /**
     * Path to default style sheet.
     */
    private static final String PATH = "org/openvpms/web/resource/style/default.stylesheet";

    /**
     * List of screen width & corresponding style suffixes. Must be ordered
     * higher width to lower.
     */
    private static Width[] WIDTHS = {
            new Width(1024, "-1024x768"),
            new Width(800, "-800x600")
    };

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(Styles.class);


    /**
     * Returns a style name, amended for the client resolution, if one exists.
     *
     * @param component the component class
     * @param style     the style name
     * @return the (possibly modified) style name
     */
    public static String getStyle(Class component, String style) {
        String result = style;
        ApplicationInstance app = ApplicationInstance.getActive();
        ContainerContext context = (ContainerContext) app.getContextProperty(
                ContainerContext.CONTEXT_PROPERTY_NAME);
        if (context != null) {
            ClientProperties properties = context.getClientProperties();
            int width = properties.getInt(ClientProperties.SCREEN_WIDTH, -1);
            if (width != -1) {
                for (Width w : WIDTHS) {
                    if (width <= w.width) {
                        String name = style + w.styleSuffix;
                        if (app.getStyle(component, name) != null) {
                            result = name;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static class Width {
        public final int width;
        public final String styleSuffix;

        public Width(int width, String styleSuffix) {
            this.width = width;
            this.styleSuffix = styleSuffix;
        }
    }

    static {
        try {
            StyleSheet styles = StyleSheetLoader.load(PATH,
                                                      Styles.class.getClassLoader());
            if (styles == null) {
                styles = StyleSheetLoader.load(PATH,
                                               Thread.currentThread().getContextClassLoader());
            }
            DEFAULT_STYLE_SHEET = styles;
            if (DEFAULT_STYLE_SHEET == null) {
                _log.error("Stylesheet not found: " + PATH);
            }
        } catch (ComponentXmlException exception) {
            _log.error("Failed to load stylesheet=" + PATH, exception);
            throw new RuntimeException(exception);
        }
    }

}
