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


import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.query.FilteredResultSet;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.webdav.resource.ResourceLock;
import org.openvpms.web.webdav.resource.ResourceLockManager;

import java.util.Date;
import java.util.List;

/**
 * Browser for WebDAV locks managed by the {@link ResourceLockManager}.
 * <p/>
 * This allows locks to be administratively deleted.
 *
 * @author Tim Anderson
 */
public class WebDAVLockBrowser extends AbstractTabComponent {

    /**
     * The lock manager.
     */
    private final ResourceLockManager lockManager;

    /**
     * The locks.
     */
    private PagedIMTable<ResourceLock> locks;

    /**
     * The search filter property.
     */
    private SimpleProperty search = new SimpleProperty("search", null, String.class, Messages.get("query.search"));

    /**
     * The search field.
     */
    private TextField field;

    /**
     * The browser component.
     */
    private Component component;

    /**
     * The query button identifier.
     */
    private static final String QUERY_ID = "button.query";

    /**
     * The delete button identifier.
     */
    private static final String DELETE_ID = "button.delete";

    /**
     * Constructs a {@link WebDAVLockBrowser}.
     *
     * @param help the help topic
     */
    public WebDAVLockBrowser(HelpContext help) {
        super(help);
        lockManager = ServiceHelper.getBean(ResourceLockManager.class);
    }

    /**
     * Invoked when the tab is displayed.
     */
    @Override
    public void show() {
        refresh();
        FocusHelper.setFocus(field);
    }

    /**
     * Returns the tab component.
     *
     * @return the tab component
     */
    @Override
    public Component getComponent() {
        if (component == null) {
            FocusGroup focus = getFocusGroup();
            field = BoundTextComponentFactory.create(search, 20);
            field.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    refresh();
                }
            });
            focus.add(field);

            ButtonRow query = new ButtonRow(focus);
            query.addButton(QUERY_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onQuery();
                }
            });
            locks = new PagedIMTable<>(new LockTableModel());
            locks.getTable().addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    enableButtons();
                }
            });

            ButtonSet buttons = getButtonSet();
            buttons.add(DELETE_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onDelete();
                }
            });
            buttons.setEnabled(DELETE_ID, false);

            Label label = LabelFactory.create();
            label.setText(search.getDisplayName());
            Row row = RowFactory.create(Styles.CELL_SPACING, label, field, query);
            component = ColumnFactory.create(Styles.INSET,
                                             ColumnFactory.create(Styles.WIDE_CELL_SPACING, row, locks.getComponent()));
            focus.add(locks.getComponent());
            focus.add(getButtonSet().getFocusGroup());
        }
        // Cannot cache the SplitPane for some reason. Get a:
        // "Cannot process ServerMessage (Phase 2) Error: Element c_246 already exists in document; cannot add"
        // message.
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "SplitPaneWithButtonRow",
                                       getButtons(), component);
    }

    /**
     * Invoked when the query button is pressed.
     */
    private void onQuery() {
        refresh();
        if (!locks.getTable().getObjects().isEmpty()) {
            FocusHelper.setFocus(locks.getTable());
        }
    }

    /**
     * Enables buttons if a lock is selected, otherwise disables them
     */
    private void enableButtons() {
        getButtonSet().setEnabled(DELETE_ID, locks.getSelected() != null);
    }

    /**
     * Invoked when the delete button is pressed, to delete the selected lock.
     */
    private void onDelete() {
        final ResourceLock selected = locks.getSelected();
        if (selected != null) {
            String name = UserHelper.getName(selected.getUser());
            String title = Messages.get("admin.system.webdav.deletelock.title");
            String message = Messages.format("admin.system.webdav.deletelock.message", name);
            ConfirmationDialog dialog = new ConfirmationDialog(title, message, ConfirmationDialog.YES_NO);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onYes() {
                    lockManager.remove(selected);
                    refresh();
                    enableButtons();
                }
            });
            dialog.show();
        }
    }

    /**
     * Refreshes the locks table.
     */
    private void refresh() {
        locks.setResultSet(getLocks());
    }

    /**
     * Returns the current locks.
     *
     * @return the current locks
     */
    private ResultSet<ResourceLock> getLocks() {
        List<ResourceLock> locks = lockManager.getLocked();
        ResultSet<ResourceLock> set = new ListResultSet<>(locks, 20);
        final String query = search.getString();
        if (!StringUtils.isEmpty(query)) {
            set = new FilteredResultSet<ResourceLock>(set) {
                @Override
                protected void filter(ResourceLock object, List<ResourceLock> results) {
                    if (contains(object.getUser(), query) || contains(object.getName(), query)
                        || contains(UserHelper.getName(object.getUser()), query)) {
                        results.add(object);
                    }
                }

                protected boolean contains(String value, String search) {
                    return value != null && value.toLowerCase().contains(search.toLowerCase());
                }
            };
        }
        set.sort(new SortConstraint[]{LockTableModel.getSortOnLogin(true)});
        return set;
    }

    private static class LockTableModel extends AbstractIMTableModel<ResourceLock> {

        /**
         * The login name column index.
         */
        private static final int LOGIN_INDEX = 0;

        /**
         * The user column index.
         */
        private static final int USER_INDEX = 1;

        /**
         * The resource name column index.
         */
        private static final int RESOURCE_INDEX = 2;

        /**
         * The expiry date column index.
         */
        private static final int EXPIRY_DATE_INDEX = 3;

        /**
         * Constructs a {@link LockTableModel}.
         */
        public LockTableModel() {
            TableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(LOGIN_INDEX, "admin.system.login"));
            model.addColumn(createTableColumn(USER_INDEX, "admin.system.user"));
            model.addColumn(createTableColumn(RESOURCE_INDEX, "admin.system.webdav.lock.resource"));
            model.addColumn(createTableColumn(EXPIRY_DATE_INDEX, "admin.system.webdav.lock.expiry"));
            setTableColumnModel(model);
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
        protected Object getValue(ResourceLock object, TableColumn column, int row) {
            Object result = null;
            switch (column.getModelIndex()) {
                case LOGIN_INDEX:
                    result = object.getUser();
                    break;
                case USER_INDEX:
                    result = UserHelper.getName(object.getUser());
                    break;
                case RESOURCE_INDEX:
                    result = object.getName();
                    break;
                case EXPIRY_DATE_INDEX:
                    result = getExpiry(object);
            }
            return result;
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
            SortConstraint sort = null;
            switch (column) {
                case LOGIN_INDEX:
                    sort = getSortOnLogin(ascending);
                    break;
                case USER_INDEX:
                    sort = new VirtualNodeSortConstraint("user", ascending, new Transformer() {
                        @Override
                        public Object transform(Object input) {
                            String login = ((ResourceLock) input).getUser();
                            return UserHelper.getName(login);
                        }
                    });
                    break;
                case RESOURCE_INDEX:
                    sort = new VirtualNodeSortConstraint("resource", ascending, new Transformer() {
                        @Override
                        public Object transform(Object input) {
                            return ((ResourceLock) input).getName();
                        }
                    });
                    break;
            }
            return (sort != null) ? new SortConstraint[]{sort} : null;
        }

        public static SortConstraint getSortOnLogin(boolean ascending) {
            SortConstraint sort;
            sort = new VirtualNodeSortConstraint("login", ascending, new Transformer() {
                @Override
                public Object transform(Object input) {
                    return ((ResourceLock) input).getUser();
                }
            });
            return sort;
        }

        private String getExpiry(ResourceLock object) {
            LockToken token = object.getToken();
            Date from = token.getFrom();
            Date to = null;
            LockTimeout timeout = token.timeout;
            if (timeout.getSeconds() != null && timeout.getSeconds() != Long.MAX_VALUE) {
                to = new DateTime(from).plusSeconds((int) (long) timeout.getSeconds()).toDate();
            }
            return to != null ? DateFormatter.formatDateTimeAbbrev(to) : null;
        }

    }
}
