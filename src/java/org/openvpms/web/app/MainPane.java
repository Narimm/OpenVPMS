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
 * @version $LastChangedDate$
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
    private static final String LEFTPANE_STYLE = "MainPane.Left";
    private static final String RIGHTPANE_STYLE = "MainPane.Right";


    /**
     * Construct a new <code>MainPane</code>.
     */
    public MainPane() {
        super(ORIENTATION_HORIZONTAL);
        setStyleName(STYLE);

        _menu = RowFactory.create(BUTTON_ROW_STYLE);
        _subMenu = ColumnFactory.create(BUTTON_COLUMN_STYLE);
        _subsystem = ContentPaneFactory.create(WORKSPACE_STYLE);

        Button button = addSubsystem(new CustomerSubsystem());
        addSubsystem(new PatientSubsystem());
        addSubsystem(new SupplierSubsystem());
        addSubsystem(new WorkflowSubsystem());
        addSubsystem(new FinancialSubsystem());
        addSubsystem(new ProductSubsystem());
        addSubsystem(new AdminSubsystem());

        SplitPane left = SplitPaneFactory.create(ORIENTATION_VERTICAL,
                LEFTPANE_STYLE);
        SplitPane right = SplitPaneFactory.create(ORIENTATION_VERTICAL,
                RIGHTPANE_STYLE);

        left.add(new Label());
        left.add(_subMenu);
        right.add(_menu);
        right.add(_subsystem);

        add(left);
        add(right);

        button.doAction();
    }

    protected void select(final Subsystem subsystem) {
        _subsystem.removeAll();
        _subMenu.removeAll();
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
