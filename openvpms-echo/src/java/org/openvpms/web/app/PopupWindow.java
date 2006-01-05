package org.openvpms.web.app;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.WindowPane;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class PopupWindow {

    public static WindowPane popup(Component content, String title) {
        WindowPane window = new WindowPane();
        window.setResizable(true);
        window.setStyleName("Default");
        window.setTitle(title);

        ContentPane container = new ContentPane();
        container.add(content);
        window.add(container);

        OpenVPMSApp.getInstance().getContent().add(window);

        return window;
    }

}
