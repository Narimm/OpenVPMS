package org.openvpms.web.workspace.admin.system;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
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
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.ServletHelper;
import org.openvpms.web.echo.servlet.SessionMonitor;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.web.util.Log4jWebConfigurer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Browses current user sessions.
 *
 * @author Tim Anderson
 */
public class SessionBrowser extends AbstractTabComponent {

    /**
     * The browser component.
     */
    private Component component;

    /**
     * The logged in sessions.
     */
    private PagedIMTable<SessionMonitor.Session> sessions;

    /**
     * The search filter property.
     */
    private SimpleProperty search = new SimpleProperty("search", null, String.class, Messages.get("query.search"));

    /**
     * The search field.
     */
    private TextField field;

    /**
     * The query button identifier.
     */
    private static final String QUERY_ID = "button.query";

    /**
     * The 'Reload Log4j configuration' button identifier.
     */
    private static final String RELOAD_LOG4J = "button.reloadlog4j";

    /**
     * Constructs an {@link SessionBrowser}.
     *
     * @param help the help context
     */
    public SessionBrowser(HelpContext help) {
        super(help);
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

            ButtonRow buttons = new ButtonRow(focus);
            buttons.addButton(QUERY_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onQuery();
                }
            });
            sessions = new PagedIMTable<>(new SessionTableModel());

            Label label = LabelFactory.create();
            label.setText(search.getDisplayName());
            Row row = RowFactory.create(Styles.CELL_SPACING, label, field, buttons);
            component = ColumnFactory.create(Styles.INSET,
                                             ColumnFactory.create(Styles.WIDE_CELL_SPACING, row,
                                                                  sessions.getComponent()));
            focus.add(sessions.getComponent());
            getButtonSet().add(RELOAD_LOG4J, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onReloadLog();
                }
            });
            focus.add(getButtonSet().getFocusGroup());
        }
        return component;
    }

    /**
     * Invoked when the query button is pressed.
     */
    private void onQuery() {
        refresh();
        if (sessions.getTable().getObjects().isEmpty()) {
            FocusHelper.setFocus(sessions.getTable());
        }
    }

    /**
     * Refreshes the list of sessions.
     */
    private void refresh() {
        sessions.setResultSet(getSessions());
    }

    /**
     * Returns the users that are currently logged in.
     *
     * @return the users
     */
    private ResultSet<SessionMonitor.Session> getSessions() {
        List<SessionMonitor.Session> sessions = ServiceHelper.getBean(SessionMonitor.class).getSessions();
        ResultSet<SessionMonitor.Session> set = new ListResultSet<>(sessions, 20);
        final String query = search.getString();
        if (!StringUtils.isEmpty(query)) {
            set = new FilteredResultSet<SessionMonitor.Session>(set) {
                @Override
                protected void filter(SessionMonitor.Session object, List<SessionMonitor.Session> results) {
                    if (contains(object.getName(), query) || contains(object.getHost(), query)) {
                        results.add(object);
                    }
                }

                protected boolean contains(String value, String search) {
                    return value != null && value.toLowerCase().contains(search.toLowerCase());
                }
            };
        }
        set.sort(new SortConstraint[]{SessionTableModel.getSortOnName(true)});
        return set;
    }

    /**
     * Reloads the log4j configuration file.
     * <p>
     * Note that this is only here for lack of another place to put it.
     */
    private void onReloadLog() {
        HttpServletRequest request = ServletHelper.getRequest();
        if (request != null) {
            ServletContext servletContext = request.getSession().getServletContext();
            Log4jWebConfigurer.initLogging(servletContext);
            InformationDialog.show(Messages.get("admin.system.info.reloadlog4j"));
        }
    }

    private static class SessionTableModel extends AbstractIMTableModel<SessionMonitor.Session> {

        /**
         * The user column index.
         */
        private static final int USER_INDEX = 0;

        /**
         * The host column index.
         */
        private static final int HOST_INDEX = 1;

        /**
         * The logged in date/time index.
         */
        private static final int LOGGED_IN_INDEX = 2;

        /**
         * The last accessed date/time index.
         */
        private static final int LAST_ACCESSED_INDEX = 3;

        /**
         * Constructs a {@link SessionTableModel}.
         */
        public SessionTableModel() {
            TableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(USER_INDEX, "admin.system.session.user"));
            model.addColumn(createTableColumn(HOST_INDEX, "admin.system.session.host"));
            model.addColumn(createTableColumn(LOGGED_IN_INDEX, "admin.system.session.loggedin"));
            model.addColumn(createTableColumn(LAST_ACCESSED_INDEX, "admin.system.session.lastaccessed"));
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
        protected Object getValue(SessionMonitor.Session object, TableColumn column, int row) {
            Object result = null;
            switch (column.getModelIndex()) {
                case USER_INDEX:
                    result = UserHelper.getName(object.getName());
                    break;
                case HOST_INDEX:
                    result = object.getHost();
                    break;
                case LOGGED_IN_INDEX:
                    result = DateFormatter.formatDateTimeAbbrev(object.getLoggedIn());
                    break;
                case LAST_ACCESSED_INDEX:
                    result = DateFormatter.formatDateTimeAbbrev(object.getLastAccessed());
                    break;
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
                case USER_INDEX:
                    sort = getSortOnName(ascending);
                    break;
                case HOST_INDEX:
                    sort = new VirtualNodeSortConstraint("host", ascending, new Transformer() {
                        @Override
                        public Object transform(Object input) {
                            return ((SessionMonitor.Session) input).getHost();
                        }
                    });
                    break;
            }
            return (sort != null) ? new SortConstraint[]{sort} : null;
        }

        public static SortConstraint getSortOnName(boolean ascending) {
            SortConstraint sort;
            sort = new VirtualNodeSortConstraint("name", ascending, new Transformer() {
                @Override
                public Object transform(Object input) {
                    return ((SessionMonitor.Session) input).getName();
                }
            });
            return sort;
        }

    }

}
