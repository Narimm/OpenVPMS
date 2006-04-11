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

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import nextapp.echo2.app.StyleSheet;
import nextapp.echo2.app.componentxml.ComponentXmlException;
import nextapp.echo2.app.componentxml.StyleSheetLoader;


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
    private static final String PATH = "/org/openvpms/web/resource/style/default.stylesheet";

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(Styles.class);

    static {
        try {
            StyleSheet styles = StyleSheetLoader.load(PATH,
                    Styles.class.getClassLoader());
            if (styles == null) {
                styles = StyleSheetLoader.load(PATH, Thread.currentThread().getContextClassLoader());
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
