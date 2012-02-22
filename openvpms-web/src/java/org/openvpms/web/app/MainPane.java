/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TaskQueueHandle;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.archetype.rules.workflow.MessageStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.app.admin.AdminSubsystem;
import org.openvpms.web.app.customer.CustomerSubsystem;
import org.openvpms.web.app.history.CustomerPatient;
import org.openvpms.web.app.history.CustomerPatientHistoryBrowser;
import org.openvpms.web.app.patient.PatientSubsystem;
import org.openvpms.web.app.product.ProductSubsystem;
import org.openvpms.web.app.reporting.ReportingSubsystem;
import org.openvpms.web.app.supplier.SupplierSubsystem;
import org.openvpms.web.app.workflow.WorkflowSubsystem;
import org.openvpms.web.app.workflow.messaging.MessageMonitor;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.ContextListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.subsystem.Refreshable;
import org.openvpms.web.component.subsystem.Subsystem;
import org.openvpms.web.component.subsystem.Workspace;
import org.openvpms.web.component.util.ButtonColumn;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ContentPaneFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;


/**
 * Main application pane.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class MainPane extends SplitPane implements ContextChangeListener, ContextListener {

    /**
     * The subsystems.
     */
    private final List<Subsystem> subsystems = new ArrayList<Subsystem>();

    /**
     * Menu button row.
     */
    private ButtonRow menu;

    /**
     * The left menu, containing the submenu and summary.
     */
    private Column leftMenu;

    /**
     * Submenu button column.
     */
    private ButtonColumn subMenu;

    /**
     * Workspace summary component. May be <tt>null</tt>.
     */
    private Component summary;

    /**
     * Listener to refresh the summary.
     */
    private final PropertyChangeListener summaryRefresher;

    /**
     * The pane for the current subsystem.
     */
    private ContentPane currentSubsystem;

    /**
     * The current workspace.
     */
    private Workspace currentWorkspace;

    /**
     * The task queue, for refreshing {@link Refreshable} workspaces.
     */
    private TaskQueueHandle taskQueue;

    /**
     * The message monitor
     */
    private final MessageMonitor monitor;

    /**
     * The message listener.
     */
    private final MessageMonitor.MessageListener listener;

    /**
     * The user the listener was registered for.
     */
    private User user;

    /**
     * Mail button.
     */
    private Button messages;

    /**
     * The style name.
     */
    private static final String STYLE = "MainPane";

    /**
     * The left menu style.
     */
    private static final String LEFT_MENU_STYLE = "WideCellSpacing";

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
     * Reference to the new window icon.
     */
    private ImageReference NEW_WINDOW
            = new ResourceImageReference("/org/openvpms/web/resource/image/newwindow.gif");

    /**
     * Reference to the mail icon.
     */
    private static final ImageReference MAIL
            = new ResourceImageReference("/org/openvpms/web/resource/image/buttons/mail.png");

    /**
     * Reference to the new mail icon.
     */
    private static final ImageReference UNREAD_MAIL
            = new ResourceImageReference("/org/openvpms/web/resource/image/buttons/mail-unread.png");


    /**
     * Constructs a <tt>MainPane</tt>.
     *
     * @param monitor the message monitor
     */
    public MainPane(MessageMonitor monitor) {
        super(ORIENTATION_HORIZONTAL);
        setStyleName(STYLE);
        this.monitor = monitor;
        listener = new MessageMonitor.MessageListener() {
            public void onMessage(Act message) {
                updateMessageStatus(message);
            }
        };
        user = GlobalContext.getInstance().getUser();

        summaryRefresher = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                refreshSummary();
            }
        };

        OpenVPMSApp.getInstance().setContextChangeListener(this);

        menu = new ButtonRow(ButtonRow.STYLE, BUTTON_STYLE);
        SplitPaneLayoutData layout = new SplitPaneLayoutData();
        layout.setAlignment(new Alignment(Alignment.CENTER,
                Alignment.DEFAULT));
        menu.setLayoutData(layout);
        subMenu = new ButtonColumn(BUTTON_COLUMN_STYLE, BUTTON_STYLE);
        leftMenu = ColumnFactory.create(LEFT_MENU_STYLE, subMenu);
        currentSubsystem = ContentPaneFactory.create(WORKSPACE_STYLE);

        Button button = addSubsystem(new CustomerSubsystem());
        addSubsystem(new PatientSubsystem());
        addSubsystem(new SupplierSubsystem());
        addSubsystem(new WorkflowSubsystem());
        addSubsystem(new ProductSubsystem());
        addSubsystem(new ReportingSubsystem());

        GlobalContext context = GlobalContext.getInstance();
        context.addListener(this);

        // if the current user is an admin, show the administration subsystem
        if (UserHelper.isAdmin(user)) {
            addSubsystem(new AdminSubsystem());
        }

        menu.addButton("help", new ActionListener() {
            public void onAction(ActionEvent event) {
                new HelpDialog().show();
            }
        });
        menu.add(getManagementRow());

        SplitPane left = SplitPaneFactory.create(ORIENTATION_VERTICAL,
                                                 LEFTPANE_STYLE);
        SplitPane right = SplitPaneFactory.create(ORIENTATION_VERTICAL,
                                                  RIGHTPANE_STYLE);

        left.add(new Label());
        left.add(leftMenu);
        right.add(menu);
        right.add(currentSubsystem);

        add(left);
        add(right);

        button.doAction();
    }

    /**
     * Life-cycle method invoked when the <code>Component</code> is added
     * to a registered hierarchy.
     * <p/>
     * This implementation registers a listener for message notification.
     */
    @Override
    public void init() {
        super.init();
        if (user != null) {
            monitor.addListener(user, listener);
        }
    }

    /**
     * Life-cycle method invoked when the <code>Component</code> is removed
     * from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (user != null) {
            monitor.removeListener(user, listener);
        }
    }

    /**
     * Change the context.
     *
     * @param context the context to change to
     */
    public void changeContext(IMObject context) {
        String shortName = context.getArchetypeId().getShortName();
        for (Subsystem subsystem : subsystems) {
            Workspace workspace = subsystem.getWorkspaceForArchetype(shortName);
            if (workspace != null) {
                workspace.getComponent();
                workspace.update(context);
                subsystem.setWorkspace(workspace);
                select(subsystem);
                break;
            }
        }
    }

    /**
     * Change the context.
     *
     * @param shortName the archetype short name of the context to change to
     */
    public void changeContext(String shortName) {
        for (Subsystem subsystem : subsystems) {
            Workspace workspace = subsystem.getWorkspaceForArchetype(shortName);
            if (workspace != null) {
                workspace.getComponent();
                subsystem.setWorkspace(workspace);
                select(subsystem);
                break;
            }
        }
    }

    /**
     * Invoked when a global context object changes, to refresh the current
     * visible workspace, if necessary.
     *
     * @param key   the context key
     * @param value the context value. May be <tt>null</tt>
     */
    public void changed(String key, IMObject value) {
        if (currentWorkspace != null) {
            if ((value != null && currentWorkspace.canUpdate(value.getArchetypeId().getShortName()))
                    || currentWorkspace.canUpdate(key)) {
                // the key may be a short name. Use in the instance that the value
                // is null
                currentWorkspace.update(value);
            }
        }
    }

    /**
     * Selects a subsystem.
     *
     * @param subsystem the subsystem
     */
    protected void select(final Subsystem subsystem) {
        currentSubsystem.removeAll();
        subMenu.removeAll();

        List<Workspace> workspaces = subsystem.getWorkspaces();
        for (final Workspace workspace : workspaces) {
            ActionListener listener = new ActionListener() {
                public void onAction(ActionEvent event) {
                    select(subsystem, workspace);
                }
            };
            Button button = subMenu.addButton(workspace.getTitleKey(), listener, true);
            button.setFocusTraversalParticipant(false);
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
        if (currentWorkspace != null) {
            currentWorkspace.removePropertyChangeListener(Workspace.SUMMARY_PROPERTY, summaryRefresher);
            currentWorkspace.hide();

            // set to null as workspace.getComponent() can trigger updates that invoke changed() which uses
            // currentWorkspace
            currentWorkspace = null;
        }
        subsystem.setWorkspace(workspace);
        currentSubsystem.removeAll();
        currentSubsystem.add(workspace.getComponent());

        currentWorkspace = workspace;
        refreshSummary();
        currentWorkspace.addPropertyChangeListener(Workspace.SUMMARY_PROPERTY, summaryRefresher);
        currentWorkspace.show();
        if (currentWorkspace instanceof Refreshable) {
            queueRefresh();
        } else {
            removeTaskQueue();
        }
    }

    /**
     * Add a subsystem.
     *
     * @param subsystem the subsystem to add
     * @return a button to invoke the subsystem
     */
    protected Button addSubsystem(final Subsystem subsystem) {
        ActionListener listener = new ActionListener() {
            public void onAction(ActionEvent e) {
                select(subsystem);
            }
        };
        Button button = menu.addButton(subsystem.getTitleKey(), listener);
        button.setFocusTraversalParticipant(false);
        subsystems.add(subsystem);
        return button;
    }

    /**
     * Updates the message status button.
     */
    private void updateMessageStatus() {
        boolean update = false;
        if (user != null) {
            update = monitor.hasNewMessages(user);
        }
        updateMessageStatus(update);
    }

    /**
     * Updates the message status button when a message is updated.
     *
     * @param message the updated messsage
     */
    private void updateMessageStatus(Act message) {
        if (MessageStatus.PENDING.equals(message.getStatus())) {
            updateMessageStatus(true);
        } else {
            updateMessageStatus();
        }
    }

    /**
     * Updates the message status button.
     *
     * @param newMessages if <tt>true</tt> indicates there is new messages
     */
    private void updateMessageStatus(boolean newMessages) {
        if (newMessages) {
            messages.setIcon(UNREAD_MAIL);
            messages.setToolTipText(Messages.get("messages.unread.tooltip"));
        } else {
            messages.setIcon(MAIL);
            messages.setToolTipText(Messages.get("messages.read.tooltip"));
        }
    }

    /**
     * Creates a row containing right justified messages, new window and logout buttons.
     *
     * @return the row
     */
    private Row getManagementRow() {
        ButtonRow row = new ButtonRow(null, BUTTON_STYLE);
        messages = ButtonFactory.create(null, BUTTON_STYLE);
        messages.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                ContextApplicationInstance.getInstance().switchTo(MessageArchetypes.USER);
            }
        });
        updateMessageStatus();
        row.addButton(messages);

        Button newWindow = ButtonFactory.create(null, BUTTON_STYLE);
        newWindow.setIcon(NEW_WINDOW);
        newWindow.setToolTipText(Messages.get("newwindow.tooltip"));
        newWindow.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onNewWindow();
            }
        });
        row.addButton(newWindow);
        row.addButton("recent", new ActionListener() {
            public void onAction(ActionEvent event) {
                showHistory();
            }
        });
        row.addButton("logout", new ActionListener() {
            public void onAction(ActionEvent event) {
                onLogout();
            }
        });

        RowLayoutData rightAlign = new RowLayoutData();
        rightAlign.setAlignment(
                new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
        rightAlign.setWidth(new Extent(100, Extent.PERCENT));
        row.setLayoutData(rightAlign);
        return row;
    }

    /**
     * Refreshes the workspace summary.
     */
    private void refreshSummary() {
        leftMenu.remove(summary);
        Component newSummary = (currentWorkspace != null) ? currentWorkspace.getSummary() : null;
        if (newSummary != null) {
            summary = ColumnFactory.create("Inset", newSummary);
            leftMenu.add(summary);
        } else {
            summary = null;
        }
    }

    /**
     * Invoked when the 'new window' button is pressed.
     */
    private void onNewWindow() {
        OpenVPMSApp.getInstance().createWindow();
    }

    /**
     * Invoked when the 'logout' button is pressed.
     */
    private void onLogout() {
        OpenVPMSApp app = OpenVPMSApp.getInstance();
        int count = app.getActiveWindowCount();
        String msg;
        if (count > 1) {
            msg = Messages.get("logout.activewindows.message", count);
        } else {
            msg = Messages.get("logout.message");
        }
        String title = Messages.get("logout.title");
        final ConfirmationDialog dialog = new ConfirmationDialog(title, msg);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doLogout();
            }
        });
        dialog.show();
    }

    /**
     * Logs out the application.
     */
    private void doLogout() {
        removeTaskQueue();
        OpenVPMSApp app = OpenVPMSApp.getInstance();
        app.logout();
    }

    /**
     * Displays the customer/patient history browser.
     */
    private void showHistory() {
        final CustomerPatientHistoryBrowser browser = new CustomerPatientHistoryBrowser();
        BrowserDialog<CustomerPatient> dialog
                = new BrowserDialog<CustomerPatient>(Messages.get("history.title"), browser);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                CustomerPatient selected = browser.getSelected();
                if (selected != null) {
                    GlobalContext context = GlobalContext.getInstance();
                    context.setCustomer(selected.getCustomer());
                    context.setPatient(selected.getPatient());
                    Party party = browser.getSelectedParty();
                    if (party != null) {
                        changeContext(party);
                    }
                }
            }
        });
        dialog.show();
    }

    /**
     * Queues a refresh of the current workspace.
     */
    private void queueRefresh() {
        final ApplicationInstance app = ApplicationInstance.getActive();
        app.enqueueTask(getTaskQueue(), new Runnable() {
            public void run() {
                if (currentWorkspace instanceof Refreshable) {
                    Refreshable refreshable = (Refreshable) currentWorkspace;
                    if (refreshable.needsRefresh()) {
                        refreshable.refresh();
                    }
                    queueRefresh(); // queue a refresh again
                }
            }
        });
    }

    /**
     * Returns the task queue, creating it if it doesn't exist.
     *
     * @return the task queue
     */
    private TaskQueueHandle getTaskQueue() {
        if (taskQueue == null) {
            ApplicationInstance app = ApplicationInstance.getActive();
            taskQueue = app.createTaskQueue();
        }
        return taskQueue;
    }

    /**
     * Cleans up the task queue.
     */
    private void removeTaskQueue() {
        if (taskQueue != null) {
            final ApplicationInstance app = ApplicationInstance.getActive();
            app.removeTaskQueue(taskQueue);
            taskQueue = null;
        }
    }

}
