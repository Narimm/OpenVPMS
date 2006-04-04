package org.openvpms.web.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.app.admin.AdminSubsystem;
import org.openvpms.web.app.customer.CustomerSubsystem;
import org.openvpms.web.app.financial.FinancialSubsystem;
import org.openvpms.web.app.patient.PatientSubsystem;
import org.openvpms.web.app.product.ProductSubsystem;
import org.openvpms.web.app.supplier.SupplierSubsystem;
import org.openvpms.web.app.workflow.WorkflowSubsystem;
import org.openvpms.web.component.subsystem.Subsystem;
import org.openvpms.web.component.subsystem.Workspace;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ContentPaneFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Main application pane.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class MainPane extends SplitPane implements ContextChangeListener {

    /**
     * The subsystems.
     */
    private final List<Subsystem> _subsystems = new ArrayList<Subsystem>();

    /**
     * Menu button row.
     */
    private Row _menu;

    /**
     * The left menu, containing the submenu and summary.
     */
    private SplitPane _leftMenu;

    /**
     * Submenu button column.
     */
    private Column _subMenu;

    /**
     * Workspace summary. May be <code>null</code>.
     */
    private Component _summary;

    /**
     * Listener to refresh the summary.
     */
    private final PropertyChangeListener _summaryRefresher;

    /**
     * The pane for the current subsystem.
     */
    private ContentPane _subsystem;

    /**
     * The current workspace.
     */
    private Workspace _workspace;

    /**
     * The style name.
     */
    private static final String STYLE = "MainPane";

    /**
     * The menu row style.
     */
    private static final String BUTTON_ROW_STYLE = "ControlRow";

    /**
     * The left menu style.
     */
    private static final String LEFT_MENU_STYLE = "MainPane.Left.Menu";

    /**
     * The submenu column style.
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

        _summaryRefresher = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                refreshSummary();
            }
        };

        OpenVPMSApp.getInstance().setContextChangeListener(this);

        _menu = RowFactory.create(BUTTON_ROW_STYLE);
        _subMenu = ColumnFactory.create(BUTTON_COLUMN_STYLE);
        _leftMenu = SplitPaneFactory.create(ORIENTATION_VERTICAL,
                                            LEFT_MENU_STYLE, _subMenu);
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
        left.add(_leftMenu);
        right.add(_menu);
        right.add(_subsystem);

        add(left);
        add(right);

        button.doAction();
    }

    /**
     * Change the context.
     *
     * @param context the context to change to
     */
    public void changeContext(IMObject context) {
        String shortName = context.getArchetypeId().getShortName();
        for (Subsystem subsystem : _subsystems) {
            Workspace workspace = subsystem.getWorkspaceForArchetype(shortName);
            if (workspace != null) {
                workspace.setObject(context);
                subsystem.setWorkspace(workspace);
                select(subsystem);
                break;
            }
        }
    }

    /**
     * Selects a subsystem.
     *
     * @param subsystem the subsystem
     */
    protected void select(final Subsystem subsystem) {
        _subsystem.removeAll();
        _subMenu.removeAll();

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
        Workspace current = subsystem.getWorkspace();
        if (current == null) {
            current = subsystem.getDefaultWorkspace();
        }
        if (current != null) {
            select(subsystem, current);
        }
    }

    /**
     * Select a workspace.
     *
     * @param subsystem the subsystem that owns the workspace
     * @param workspace the workspace within the subsystem to select
     */
    protected void select(Subsystem subsystem, Workspace workspace) {
        if (_workspace != null) {
            _workspace.removePropertyChangeListener(
                    Workspace.SUMMARY_PROPERTY, _summaryRefresher);
        }
        subsystem.setWorkspace(workspace);
        _subsystem.removeAll();
        _subsystem.add(workspace.getComponent());

        _workspace = workspace;
        refreshSummary();
        _workspace.addPropertyChangeListener(Workspace.SUMMARY_PROPERTY,
                                             _summaryRefresher);
    }

    /**
     * Add a subsystem.
     *
     * @param subsystem the subsystem to add
     * @return a button to invoke the subsystem
     */
    protected Button addSubsystem(final Subsystem subsystem) {
        Button button = ButtonFactory.create(
                null, BUTTON_STYLE, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                select(subsystem);
            }
        });
        button.setText(subsystem.getTitle());
        _menu.add(button);
        _subsystems.add(subsystem);
        return button;
    }

    /**
     * Refreshes the workspace summary.
     */
    private void refreshSummary() {
        _leftMenu.remove(_summary);
        Component summary = _workspace.getSummary();
        if (summary != null) {
            summary = ColumnFactory.create("MainPane.Left.Menu.Summary",
                                           summary);
            _leftMenu.add(summary);
        }
        _summary = summary;
    }

}
