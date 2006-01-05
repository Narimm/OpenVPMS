package org.openvpms.web.app;

import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;

import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.app.browser.ClassificationBrowser;
import org.openvpms.web.app.browser.PartyBrowser;
import org.openvpms.web.app.browser.ArchetypeBrowser;
import org.openvpms.web.app.browser.LookupBrowser;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class MainPane extends ContentPane {

    /**
     * The tabbed pane model.
     */
    private final DefaultTabModel _model = new DefaultTabModel();


    public void init() {
        super.init();
        doLayout();
    }

    protected void doLayout() {
        addTab("parties", new PartyBrowser());
        addTab("financials", new DummyTab());
        addTab("workflow", new DummyTab());
        addTab("clinical", new DummyTab());
        addTab("config", new DummyTab());
        addTab("admin", new DummyTab());
        addTab("products", new DummyTab());
        addTab("reports", new DummyTab());
        addTab("lookups", new LookupBrowser());
        addTab("archetypes", new ArchetypeBrowser());
        addTab("classifications", new ClassificationBrowser());

        TabbedPane pane = new TabbedPane();
        pane.setModel(_model);
        add(pane);

        pane.setSelectedIndex(0);
    }

    protected void addTab(String id, Component tab) {
        _model.addTab(ButtonFactory.getString(id), tab);
    }

}
