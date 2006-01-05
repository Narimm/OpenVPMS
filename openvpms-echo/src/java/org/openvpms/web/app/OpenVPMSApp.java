package org.openvpms.web.app;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.ContentPane;
import org.openvpms.web.component.Styles;
import org.openvpms.web.spring.SpringApplicationInstance;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class OpenVPMSApp extends SpringApplicationInstance {

    /**
     * The window.
     */
    private Window _window;


    /**
     * Invoked to initialize the application, returning the default window.
     *
     * @return the default window of the application
     */
    public Window init() {
        setStyleSheet(Styles.DEFAULT_STYLE_SHEET);
        _window = new Window();
        _window.setTitle("OpenVPMS");
        _window.setContent(new LoginPane());
        return _window;
    }

    /**
     * Returns the instance associated with the current thread.
     *
     * @return the current instance, or <code>null</code>
     */
    public static OpenVPMSApp getInstance() {
        return (OpenVPMSApp) ApplicationInstance.getActive();
    }

    /**
     * Sets the content pane.
     *
     * @param content the content pane
     */
    public void setContent(ContentPane content) {
        _window.setContent(content);
    }

    /**
     * Returns the content pane.
     *
     * @return the content pane
     */
    public ContentPane getContent() {
        return _window.getContent();
    }

    public void logout() {
        getDefaultWindow().removeAll();
        setContent(new LoginPane());
    }
}
