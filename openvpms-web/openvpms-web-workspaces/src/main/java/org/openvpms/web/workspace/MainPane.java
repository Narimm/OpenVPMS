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
 */

package org.openvpms.web.workspace;

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
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.ContextListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.workspace.Refreshable;
import org.openvpms.web.component.workspace.Workspace;
import org.openvpms.web.component.workspace.Workspaces;
import org.openvpms.web.component.workspace.WorkspacesFactory;
import org.openvpms.web.echo.button.ButtonColumn;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.HelpDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.ContentPaneFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.history.CustomerPatient;
import org.openvpms.web.workspace.history.CustomerPatientHistoryBrowser;
import org.openvpms.web.workspace.workflow.messaging.MessageMonitor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;


/**
 * Main application pane.
 *
 * @author Tim Anderson
 */
public class MainPane extends SplitPane implements ContextChangeListener, ContextListener {

    /**
     * The workspace groups.
     */
    private final List<Workspaces> workspaces = new ArrayList<Workspaces>();

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
     * Workspace summary component. May be {@code null}.
     */
    private Component summary;

    /**
     * Listener to refresh the summary.
     */
    private final PropertyChangeListener summaryRefresher;

    /**
     * The pane for the current workspace group.
     */
    private ContentPane currentWorkspaces;

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
     * The context.
     */
    private final GlobalContext context;

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
     * Constructs a {@code MainPane}.
     *
     * @param monitor the message monitor
     * @param context the context
     * @param factory the workspaces factory
     */
    public MainPane(MessageMonitor monitor, GlobalContext context, WorkspacesFactory factory) {
        super(ORIENTATION_HORIZONTAL);
        setStyleName(STYLE);
        this.monitor = monitor;
        this.context = context;
        listener = new MessageMonitor.MessageListener() {
            public void onMessage(Act message) {
                updateMessageStatus(message);
            }
        };
        user = context.getUser();

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
        currentWorkspaces = ContentPaneFactory.create(WORKSPACE_STYLE);

        Button button = addWorkspaces(factory.createCustomerWorkspaces(context));
        addWorkspaces(factory.createPatientWorkspaces(context));
        addWorkspaces(factory.createSupplierWorkspaces(context));
        addWorkspaces(factory.createWorkflowWorkspaces(context));
        addWorkspaces(factory.createProductWorkspaces(context));
        addWorkspaces(factory.createReportingWorkspaces(context));

        context.addListener(this);

        // if the current user is an admin, show the administration workspaces
        if (UserHelper.isAdmin(user)) {
            addWorkspaces(factory.createAdminWorkspaces(context));
        }

        menu.addButton("help", new ActionListener() {
            public void onAction(ActionEvent event) {
                HelpDialog dialog = new HelpDialog(ServiceHelper.getArchetypeService());
                dialog.show();
            }
        });
        menu.add(getManagementRow());

        SplitPane left = SplitPaneFactory.create(ORIENTATION_VERTICAL, LEFTPANE_STYLE);
        SplitPane right = SplitPaneFactory.create(ORIENTATION_VERTICAL, RIGHTPANE_STYLE);

        left.add(new Label());
        left.add(leftMenu);
        right.add(menu);
        right.add(currentWorkspaces);

        add(left);
        add(right);

        button.doAction();
    }

    /**
     * Life-cycle method invoked when the {@code Component} is added to a registered hierarchy.
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
     * Life-cycle method invoked when the {@code Component} is removed from a registered hierarchy.
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
        for (Workspaces workspaces : this.workspaces) {
            Workspace workspace = workspaces.getWorkspaceForArchetype(shortName);
            if (workspace != null) {
                workspace.getComponent();
                workspace.update(context);
                workspaces.setWorkspace(workspace);
                select(workspaces);
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
        for (Workspaces workspaces : this.workspaces) {
            Workspace workspace = workspaces.getWorkspaceForArchetype(shortName);
            if (workspace != null) {
                workspace.getComponent();
                workspaces.setWorkspace(workspace);
                select(workspaces);
                break;
            }
        }
    }

    /**
     * Invoked when a global context object changes, to refresh the current visible workspace, if necessary.
     *
     * @param key   the context key
     * @param value the context value. May be {@code null}
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
     * Selects a workspace group.
     *
     * @param workspaces the workspaces
     */
    protected void select(final Workspaces workspaces) {
        currentWorkspaces.removeAll();
        subMenu.removeAll();

        List<Workspace> list = workspaces.getWorkspaces();
        for (final Workspace workspace : list) {
            ActionListener listener = new ActionListener() {
                public void onAction(ActionEvent event) {
                    select(workspaces, workspace);
                }
            };
            Button button = subMenu.addButton(workspace.getTitleKey(), listener, true);
            button.setFocusTraversalParticipant(false);
        }
        Workspace current = workspaces.getWorkspace();
        if (current == null) {
            current = workspaces.getDefaultWorkspace();
        }
        if (current != null) {
            select(workspaces, current);
        }
    }

    /**
     * Select a workspace.
     *
     * @param workspaces the workspace group that owns the workspace
     * @param workspace  the workspace within the group to select
     */
    protected void select(Workspaces workspaces, Workspace workspace) {
        if (currentWorkspace != null) {
            currentWorkspace.removePropertyChangeListener(Workspace.SUMMARY_PROPERTY, summaryRefresher);
            currentWorkspace.hide();

            // set to null as workspace.getComponent() can trigger updates that invoke changed() which uses
            // currentWorkspace
            currentWorkspace = null;
        }
        workspaces.setWorkspace(workspace);
        currentWorkspaces.removeAll();
        currentWorkspaces.add(workspace.getComponent());

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
     * Adds a workspace group.
     *
     * @param workspaces the group to add
     * @return a button to invoke the group
     */
    protected Button addWorkspaces(final Workspaces workspaces) {
        ActionListener listener = new ActionListener() {
            public void onAction(ActionEvent e) {
                select(workspaces);
            }
        };
        Button button = menu.addButton(workspaces.getTitleKey(), listener);
        button.setFocusTraversalParticipant(false);
        this.workspaces.add(workspaces);
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
     * @param message the updated message
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
     * @param newMessages if {@code true} indicates there is new messages
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
        LayoutContext layout = new DefaultLayoutContext(context, currentWorkspace.getHelpContext());
        final CustomerPatientHistoryBrowser browser = new CustomerPatientHistoryBrowser(context, layout);
        BrowserDialog<CustomerPatient> dialog
                = new BrowserDialog<CustomerPatient>(Messages.get("history.title"), browser, layout.getHelpContext());
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                CustomerPatient selected = browser.getSelected();
                if (selected != null) {
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