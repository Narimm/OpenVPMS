package org.openvpms.web.app;

import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.app.admin.AdminSubsystem;
import org.openvpms.web.app.customer.CustomerSubsystem;
import org.openvpms.web.app.financial.FinancialSubsystem;
import org.openvpms.web.app.patient.PatientSubsystem;
import org.openvpms.web.app.product.ProductSubsystem;
import org.openvpms.web.app.supplier.SupplierSubsystem;
import org.openvpms.web.app.workflow.WorkflowSubsystem;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.ColumnFactory;
import org.openvpms.web.component.ContentPaneFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.SplitPaneFactory;
import org.openvpms.web.component.subsystem.Subsystem;
import org.openvpms.web.component.subsystem.Workspace;


/**
 * Main application pane.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
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
     * The pane for the current subsystem.
     */
    private ContentPane _subsystem;

    /**
     * The style name.
     */
    private static final String STYLE = "MainPane";

    /**
     * The menu style name..
     */
    private static final String MENU_PANE_STYLE = "MainPane.Menu";

    /**
     * The menu row style.
     */
    private static final String BUTTON_ROW_STYLE = "ControlRow";

    /**
     * The menu column style.
     */
    private static final String BUTTON_COLUMN_STYLE = "ControlColumn";

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

        Button button = addSubsystem(new CustomerSubsystem());
        addSubsystem(new PatientSubsystem());
        addSubsystem(new SupplierSubsystem());
        addSubsystem(new WorkflowSubsystem());
        addSubsystem(new FinancialSubsystem());
        addSubsystem(new ProductSubsystem());
        addSubsystem(new AdminSubsystem());

        ContentPane mainMenuPane;
        ContentPane subMenuPane;
        SplitPane layout;

        mainMenuPane = ContentPaneFactory.create(MENU_PANE_STYLE);
        mainMenuPane.add(_menu);

        _subMenu = ColumnFactory.create(BUTTON_COLUMN_STYLE);
        subMenuPane = ContentPaneFactory.create(MENU_PANE_STYLE, _subMenu);

        _subsystem = ContentPaneFactory.create(WORKSPACE_STYLE);
        layout = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                LAYOUT_STYLE, mainMenuPane, _subsystem);

        add(subMenuPane);
        add(layout);

        button.doAction();
    }

    protected void select(final Subsystem subsystem) {
        _subsystem.removeAll();
        _subMenu.removeAll();
        for (int i = 0; i < 10; ++i) {  // @todo fix pad layout hack
            _subMenu.add(new Label(""));
        }
        Workspace current = subsystem.getWorkspace();
        if (current == null) {
            current = subsystem.getDefaultWorkspace();
        }
        if (current != null) {
            _subsystem.add(current.getComponent());
        }
        List<Workspace> workspaces = subsystem.getWorkspaces();
        for (final Workspace workspace : workspaces) {
            Button button = ButtonFactory.create(
                    null, BUTTON_STYLE, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    select(subsystem, workspace);
                }
            });
            button.setText(workspace.getTitle());
            _subMenu.add(button);
        }
    }

    protected void select(Subsystem subsystem, Workspace workspace) {
        subsystem.setWorkspace(workspace);
        _subsystem.removeAll();
        _subsystem.add(workspace.getComponent());
    }

    protected Button addSubsystem(final Subsystem subsystem) {
        Button button = ButtonFactory.create(
                null, BUTTON_STYLE, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                select(subsystem);
            }
        });
        button.setText(subsystem.getTitle());
        _menu.add(button);
        return button;
    }

}
