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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.alert;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.util.ColourHelper;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.i18n.Messages;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Displays alerts in a popup dialog.
 *
 * @author Tim Anderson
 */
public class AlertsViewer extends PopupDialog {

    /**
     * The alerts to display.
     */
    private final List<Alert> alerts;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The alerts table.
     */
    private PagedIMTable<Alert> table;

    /**
     * The column containing the alerts table and optional viewer.
     */
    private Column column;

    /**
     * The alert viewer.
     */
    private Component viewer;

    /**
     * Constructs an {@code AlertsViewer} to display alerts for a single alert type.
     *
     * @param alert   the alerts
     * @param context the context
     * @param help    the help context
     */
    public AlertsViewer(Alert alert, Context context, HelpContext help) {
        this(Arrays.asList(alert), context, help);
        if (alert.getAlert() != null) {
            setTitle(DescriptorHelper.getDisplayName(alert.getAlert()));
        } else {
            setTitle(alert.getAlertType().getName());
        }
    }

    /**
     * Constructs an {@code AlertsViewer} to display alerts for multiple alert types.
     *
     * @param alerts  the alerts
     * @param context the context
     * @param help    the help context
     */
    public AlertsViewer(List<Alert> alerts, Context context, HelpContext help) {
        super(Messages.get("alerts.title"), "AlertsViewer", CLOSE, help);
        this.context = context;
        this.alerts = alerts;
        setModal(true);
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Column column = ColumnFactory.create("Inset", getComponent());
        getLayout().add(column);
    }

    /**
     * Renders the component.
     *
     * @return the component
     */
    private Component getComponent() {
        ResultSet<Alert> set = new ListResultSet<Alert>(alerts, 20);
        Model model = new Model();
        table = new PagedIMTable<Alert>(model, set);
        column = ColumnFactory.create("CellSpacing", table);

        if (alerts.size() == 1) {
            show(alerts.get(0));
        } else {
            table.getTable().addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    showSelected();
                }
            });
        }
        return column;
    }

    /**
     * Displays the selected alert.
     */
    private void showSelected() {
        Alert alert = table.getSelected();
        show(alert);
    }

    /**
     * Shows an alert.
     *
     * @param alert the alert to show. May be {@code null}
     */
    private void show(Alert alert) {
        if (alert != null) {
            if (viewer != null) {
                column.remove(viewer);
            }
            if (alert.getAlert() != null) {
                DefaultLayoutContext layout = new DefaultLayoutContext(context, getHelpContext());
                viewer = new IMObjectViewer(alert.getAlert(), layout).getComponent();
            } else {
                viewer = LabelFactory.create("alert.nodetail", "bold");
                ColumnLayoutData layout = new ColumnLayoutData();
                layout.setAlignment(Alignment.ALIGN_CENTER);
                viewer.setLayoutData(layout);
            }
            column.add(viewer);
        }
    }

    private static class Model extends AbstractIMTableModel<Alert> {

        /**
         * Priority column index.
         */
        private static final int PRIORITY = 0;

        /**
         * Alert column index.
         */
        private static final int ALERT = 1;

        /**
         * Reason column index.
         */
        private static final int REASON = 3;

        /**
         * Cache of priority lookup names, keyed on code.
         */
        private Map<String, String> priorities;

        /**
         * Constructs a new {@code Model}.
         */
        public Model() {
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(PRIORITY, "alert.priority"));
            model.addColumn(createTableColumn(ALERT, "alert.name"));
            model.addColumn(createTableColumn(REASON, "alert.reason"));
            setTableColumnModel(model);
        }

        /**
         * Returns the sort criteria.
         *
         * @param column    the primary sort column
         * @param ascending if {@code true} sort in ascending order; otherwise
         *                  sort in {@code descending} order
         * @return the sort criteria, or {@code null} if the column isn't
         *         sortable
         */
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
        protected Object getValue(Alert object, TableColumn column, int row) {
            int index = column.getModelIndex();
            Object result = null;
            switch (index) {
                case PRIORITY:
                    result = getPriority(object);
                    break;
                case ALERT:
                    result = getAlertType(object);
                    break;
                case REASON:
                    Act act = object.getAlert();
                    if (act != null) {
                        ActBean bean = new ActBean(act);
                        result = bean.getString("reason");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Illegal column=" + index);
            }
            return result;
        }

        /**
         * Returns a label representing the alert prioity.
         *
         * @param alert the alert
         * @return a label for the priority
         */
        private Label getPriority(Alert alert) {
            Label result = LabelFactory.create();
            IMObjectBean bean = new IMObjectBean(alert.getAlertType());
            result.setText(getPriorityName(bean.getString("priority")));
            return result;
        }

        /**
         * Returns a label representing the alert type.
         *
         * @param alert the alert
         * @return a label for the alert type
         */
        private Label getAlertType(Alert alert) {
            Label result = LabelFactory.create();
            result.setText(alert.getAlertType().getName());
            IMObjectBean bean = new IMObjectBean(alert.getAlertType());
            Color value = ColourHelper.getColor(bean.getString("colour"));
            if (value != null) {
                TableLayoutData layout = new TableLayoutData();
                layout.setBackground(value);
                result.setForeground(ColourHelper.getTextColour(value));
                result.setLayoutData(layout);
            }
            return result;
        }

        /**
         * Returns a priority name given its code.
         *
         * @param code the priority code.
         * @return the priority name, or {@code code} if none is found
         */
        private String getPriorityName(String code) {
            if (priorities == null) {
                priorities = LookupNameHelper.getLookupNames("lookup.patientAlertType", "priority");
            }
            String name = priorities.get(code);
            if (name == null) {
                name = code;
            }
            return name;
        }

    }
}
