package org.openvpms.web.component;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import nextapp.echo2.app.StyleSheet;
import nextapp.echo2.app.componentxml.ComponentXmlException;
import nextapp.echo2.app.componentxml.StyleSheetLoader;

/**
 * Stylesheet helper.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
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
