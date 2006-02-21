package org.openvpms.web.app;

import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.SplitPane;

import org.openvpms.web.component.util.ComponentFactory;
import org.openvpms.web.component.util.SplitPaneFactory;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class ApplicationContentPane extends ContentPane {

    /**
     * The layout pane style name.
     */
    private static final String LAYOUT_STYLE = "ApplicationContentPane.Layout";


    /**
     * Construact a new <code>ApplicationContentPane</code>
     */
    public ApplicationContentPane() {
    }

    /**
     * @see nextapp.echo2.app.Component#init()
     */
    public void init() {
        super.init();
        doLayout();
    }

    protected void doLayout() {
        ComponentFactory.setDefaults(this);
        SplitPane layout = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL, LAYOUT_STYLE);
        layout.add(new TitlePane());
        layout.add(new MainPane());
        add(layout);
    }

}
