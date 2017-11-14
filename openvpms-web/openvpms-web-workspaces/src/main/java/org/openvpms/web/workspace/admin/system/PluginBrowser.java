/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.system;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.filetransfer.UploadEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.plugin.manager.PluginManager;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.AbstractUploadListener;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import java.util.Arrays;

/**
 * Plugin browser.
 *
 * @author Tim Anderson
 */
public class PluginBrowser extends AbstractTabComponent {

    /**
     * The plugin manager.
     */
    private final PluginManager manager;

    /**
     * The container.
     */
    private Component component;

    /**
     * Plugin status.
     */
    private Label status;

    /**
     * The plugins table.
     */
    private PagedIMTable<Bundle> plugins;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PluginBrowser.class);

    /**
     * Configuration archetype.
     */
    private static final String PLUGIN_CONFIGURATION = "entity.pluginConfiguration";

    /**
     * Start plugin button identifier.
     */
    private static final String START_ID = "button.start";

    /**
     * Stop plugin button identifier.
     */
    private static final String STOP_ID = "button.stop";

    /**
     * Install plugin button identifier.
     */
    private static final String INSTALL_ID = "button.install";

    /**
     * Refresh display button identifier.
     */
    private static final String REFRESH_ID = "button.refresh";

    /**
     * Configure plugin manager button identifier.
     */
    private static final String CONFIGURE_ID = "button.configure";

    /**
     * Constructs a {@link PluginBrowser}.
     *
     * @param help the help context for the tab
     */
    public PluginBrowser(HelpContext help) {
        super(help);
        status = LabelFactory.create(Styles.BOLD);
        plugins = new PagedIMTable<>(new PluginTableModel());
        plugins.getTable().addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                enableButtons();
            }
        });
        component = ColumnFactory.create(Styles.INSET,
                                         ColumnFactory.create(Styles.WIDE_CELL_SPACING, status,
                                                              plugins.getComponent()));
        manager = ServiceHelper.getBean(PluginManager.class);
        ButtonSet buttons = getButtonSet();
        buttons.add(START_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onStart();
            }
        });
        buttons.add(STOP_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onStop();
            }
        });
        buttons.add(INSTALL_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onInstall();
            }
        });
        buttons.add(REFRESH_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                refresh();
            }
        });
        buttons.add(CONFIGURE_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onConfigure();
            }
        });
    }

    /**
     * Invoked when the tab is displayed.
     */
    @Override
    public void show() {
        refresh();
    }

    /**
     * Returns the tab component.
     *
     * @return the tab component
     */
    @Override
    public Component getComponent() {
        // Cannot cache the SplitPane for some reason. Get a:
        // "Cannot process ServerMessage (Phase 2) Error: Element c_246 already exists in document; cannot add"
        // message.
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "SplitPaneWithButtonRow",
                                       getButtons(), component);
    }

    /**
     * Enables/disables buttons.
     */
    private void enableButtons() {
        ButtonSet buttons = getButtonSet();
        Bundle selected = plugins.getSelected();
        boolean started = manager.isStarted();
        boolean enable = started && selected != null && selected.getBundleId() != Constants.SYSTEM_BUNDLE_ID;
        // don't allow the system bundle to be restarted
        buttons.setEnabled(INSTALL_ID, started);
        buttons.setEnabled(START_ID, enable);
        buttons.setEnabled(STOP_ID, enable);
        buttons.setEnabled(REFRESH_ID, started);
    }

    /**
     * Refreshes the display.
     */
    private void refresh() {
        int active = 0;
        Bundle selected = plugins.getSelected();
        Bundle[] bundles = manager.getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getState() == Bundle.ACTIVE) {
                active++;
            }
        }
        status.setText(Messages.format("admin.system.plugin.active", active, bundles.length));
        int pageSize = 20;
        ResultSet<Bundle> set = new ListResultSet<>(Arrays.asList(bundles), pageSize);
        plugins.setResultSet(set);
        if (selected != null) {
            for (int i = 0; i < bundles.length; ++i) {
                if (bundles[i].getBundleId() == selected.getBundleId()) {
                    plugins.getModel().setPage(i / pageSize);
                    plugins.setSelected(bundles[i]);
                    break;
                }
            }
        }

        enableButtons();
    }

    /**
     * Starts a plugin.
     */
    private void onStart() {
        Bundle selected = plugins.getSelected();
        if (selected != null && selected.getState() != Bundle.ACTIVE
            && selected.getBundleId() != Constants.SYSTEM_BUNDLE_ID) {
            String title = Messages.get("admin.system.plugin.start.title");
            String message = Messages.format("admin.system.plugin.start.message", getName(selected));
            ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                @Override
                public void onYes() {
                    start(selected);
                }
            });
        }
        refresh();
    }

    /**
     * Starts a plugin.
     *
     * @param plugin the plugin
     */
    private void start(Bundle plugin) {
        try {
            plugin.start();
        } catch (Throwable exception) {
            log.warn("Failed to start bundle=" + getName(plugin), exception);
            ErrorDialog.show(Messages.get("admin.system.plugin.start.title"),
                             Messages.format("admin.system.plugin.start.error", getName(plugin),
                                             exception.getMessage()));
        }
    }

    /**
     * Stops a plugin.
     */
    private void onStop() {
        final Bundle selected = plugins.getSelected();
        if (selected != null && selected.getState() != Bundle.RESOLVED
            && selected.getBundleId() != Constants.SYSTEM_BUNDLE_ID) {
            String title = Messages.get("admin.system.plugin.stop.title");
            String message = Messages.format("admin.system.plugin.stop.message", getName(selected));
            ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                @Override
                public void onYes() {
                    stop(selected);
                }
            });
        } else {
            refresh();
        }
    }

    /**
     * Stops a plugin.
     *
     * @param plugin the plugin
     */
    private void stop(Bundle plugin) {
        try {
            plugin.stop();
        } catch (Throwable exception) {
            log.warn("Failed to stop bundle=" + getName(plugin), exception);
            ErrorDialog.show(Messages.get("admin.system.plugin.stop.title"),
                             Messages.format("admin.system.plugin.stop.error", getName(plugin),
                                             exception.getMessage()));
        }
        refresh();
    }

    /**
     * Installs a plugin.
     */
    private void onInstall() {
        UploadListener listener = new AbstractUploadListener() {
            @Override
            public void fileUpload(UploadEvent uploadEvent) {
                try {
                    manager.install(uploadEvent.getFileName(), uploadEvent.getInputStream());
                } catch (Exception exception) {
                    ErrorHelper.show(exception);
                }
                refresh();
            }
        };
        UploadDialog dialog = new UploadDialog(listener, getHelpContext());
        dialog.show();
    }

    /**
     * Configures the plugin manager.
     */
    private void onConfigure() {
        ArchetypeQuery query = new ArchetypeQuery(PLUGIN_CONFIGURATION, false, false);
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(query);
        Entity configuration;
        if (iterator.hasNext()) {
            configuration = iterator.next();
        } else {
            configuration = (Entity) IMObjectCreator.create(PLUGIN_CONFIGURATION);
        }
        HelpContext help = getHelpContext().topic(configuration, "edit");
        LayoutContext context = new DefaultLayoutContext(true, new LocalContext(), help);
        final IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(configuration, context);
        editor.getComponent();
        EditDialog dialog = EditDialogFactory.create(editor, context.getContext());
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                if (editor.isSaved()) {
                    try {
                        manager.stop();
                        manager.start();
                    } catch (Exception exception) {
                        ErrorHelper.show(exception);
                    }
                }
                refresh();
            }
        });
        dialog.show();
    }

    /**
     * Returns a bundle name.
     *
     * @param bundle the bundle
     * @return the bundle name
     */
    private static Object getName(Bundle bundle) {
        String result = bundle.getHeaders().get(Constants.BUNDLE_NAME);
        if (result == null) {
            result = bundle.getSymbolicName();
        }
        return result;
    }

    private static class PluginTableModel extends AbstractIMTableModel<Bundle> {

        private static final int ID_INDEX = 0;

        private static final int NAME_INDEX = ID_INDEX + 1;

        private static final int VERSION_INDEX = NAME_INDEX + 1;

        private static final int STATUS_INDEX = VERSION_INDEX + 1;

        public PluginTableModel() {
            TableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(ID_INDEX, "admin.system.plugin.id"));
            model.addColumn(createTableColumn(NAME_INDEX, "admin.system.plugin.name"));
            model.addColumn(createTableColumn(VERSION_INDEX, "admin.system.plugin.version"));
            model.addColumn(createTableColumn(STATUS_INDEX, "admin.system.plugin.status"));
            setTableColumnModel(model);
        }

        /**
         * Returns the sort criteria.
         *
         * @param column    the primary sort column
         * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
         * @return the sort criteria, or {@code null} if the column isn't sortable
         */
        @Override
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            return null;
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        @Override
        protected Object getValue(Bundle object, TableColumn column, int row) {
            Object result = null;
            switch (column.getModelIndex()) {
                case ID_INDEX:
                    result = object.getBundleId();
                    break;
                case NAME_INDEX:
                    result = getName(object);
                    break;
                case VERSION_INDEX:
                    result = object.getVersion().toString();
                    break;
                case STATUS_INDEX:
                    switch (object.getState()) {
                        case Bundle.UNINSTALLED:
                            result = Messages.get("admin.system.plugin.status.uninstalled");
                            break;
                        case Bundle.INSTALLED:
                            result = Messages.get("admin.system.plugin.status.installed");
                            break;
                        case Bundle.RESOLVED:
                            result = Messages.get("admin.system.plugin.status.resolved");
                            break;
                        case Bundle.STARTING:
                            result = Messages.get("admin.system.plugin.status.starting");
                            break;
                        case Bundle.STOPPING:
                            result = Messages.get("admin.system.plugin.status.stopping");
                            break;
                        case Bundle.ACTIVE:
                            result = Messages.get("admin.system.plugin.status.active");
                            break;
                    }
            }
            return result;
        }
    }
}
