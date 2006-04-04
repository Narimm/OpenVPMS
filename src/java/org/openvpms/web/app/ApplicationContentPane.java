package org.openvpms.web.app;

import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.SplitPane;

import org.openvpms.web.component.util.ComponentFactory;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Content pane that displays the {@link TitlePane} and {@link MainPane}.  
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ApplicationContentPane extends ContentPane {

    /**
     * The layout pane style name.
     */
    private static final String LAYOUT_STYLE = "ApplicationContentPane.Layout";


    /**
     * Construct a new <code>ApplicationContentPane</code>
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
