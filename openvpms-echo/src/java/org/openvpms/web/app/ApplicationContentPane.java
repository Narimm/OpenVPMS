package org.openvpms.web.app;

import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.SplitPane;
import org.openvpms.web.component.ComponentFactory;
import org.openvpms.web.component.SplitPaneFactory;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class ApplicationContentPane extends ContentPane {

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
        SplitPane split = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL);
        split.add(new TitlePane());
        split.add(new MainPane());
        add(split);
    }

}
