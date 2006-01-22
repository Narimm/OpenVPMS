package org.openvpms.web.app;

import nextapp.echo2.app.ContentPane;

import org.openvpms.web.component.ComponentFactory;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
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
/*
        SplitPane split = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL);
        split.add(new TitlePane());
        split.add(new MainPane());
        add(split);
*/
        add(new MainPane());
    }

}
