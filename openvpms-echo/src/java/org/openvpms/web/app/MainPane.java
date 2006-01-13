package org.openvpms.web.app;

import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.app.workspace.ArchetypeWorkspace;
import org.openvpms.web.app.workspace.ClassificationWorkspace;
import org.openvpms.web.app.workspace.CustomerWorkspace;
import org.openvpms.web.app.workspace.DummyWorkspace;
import org.openvpms.web.app.workspace.LookupWorkspace;
import org.openvpms.web.app.workspace.PatientWorkspace;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.ColumnFactory;
import org.openvpms.web.component.ContentPaneFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.SplitPaneFactory;
import org.openvpms.web.component.workspace.Action;
import org.openvpms.web.component.workspace.Workspace;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class MainPane extends SplitPane {

    /**
     * Menu button row.
     */
    private Row _menu;

    /**
     * Submenu button column.
     */
    private Column _subMenu;

    /**
     * The workspace.
     */
    private ContentPane _workspace;

    /**
     * The style name.
     */
    private static final String STYLE = "MainPane";

    /**
     * The menu style name..
     */
    private static final String MENU_STYLE = "MainPane.Menu";

    /**
     * The menu row style.
     */
    private static final String BUTTON_ROW_STYLE = "ControlRow";

    /**
     * The menu button style name.
     */
    private static final String BUTTON_STYLE = "MainPane.Menu.Button";

    /**
     * The workspace style name.
     */
    private static final String WORKSPACE_STYLE = "MainPane.Workspace";

    /**
     * The layout style name.
     */
    private static final String LAYOUT_STYLE = "MainPane.Layout";


    /**
     * Construct a new <code>MainPane</code>.
     */
    public MainPane() {
        super(ORIENTATION_HORIZONTAL);
        setStyleName(STYLE);

        _menu = RowFactory.create(BUTTON_ROW_STYLE);

        Button button = addWorkspace(new PatientWorkspace());
        addWorkspace(new CustomerWorkspace());
        addWorkspace(new DummyWorkspace("financial"));
        addWorkspace(new DummyWorkspace("workflow"));
        addWorkspace(new DummyWorkspace("clinical"));
        addWorkspace(new DummyWorkspace("config"));
        addWorkspace(new DummyWorkspace("admin"));
        addWorkspace(new DummyWorkspace("product"));
        addWorkspace(new DummyWorkspace("report"));
        addWorkspace(new LookupWorkspace());
        addWorkspace(new ArchetypeWorkspace());
        addWorkspace(new ClassificationWorkspace());
        ContentPane mainMenu = ContentPaneFactory.create(MENU_STYLE);
        mainMenu.add(_menu);

        _subMenu = ColumnFactory.create();
        ContentPane subMenuPane = ContentPaneFactory.create(MENU_STYLE, _subMenu);

        _workspace = ContentPaneFactory.create(WORKSPACE_STYLE);
        SplitPane layout = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                LAYOUT_STYLE, mainMenu, _workspace);

        add(subMenuPane);
        add(layout);

        button.doAction();
    }

    protected void select(final Workspace workspace) {
        _workspace.removeAll();
        _subMenu.removeAll();
        _workspace.add(workspace.getComponent());
        List<Action> actions = workspace.getActions();
        if (actions != null) {
            for (final Action action : actions) {
                Button button = new Button(action.getTitle());
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        select(workspace, action);
                    }
                });
                _subMenu.add(button);
            }
        }
    }

    protected void select(Workspace workspace, Action action) {
        workspace.setAction(action.getId());
    }

    protected Button addWorkspace(final Workspace workspace) {
        Button button = ButtonFactory.create(
                null, BUTTON_STYLE, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                select(workspace);
            }
        });
        button.setText(workspace.getTitle());
        _menu.add(button);
        return button;
    }

}
