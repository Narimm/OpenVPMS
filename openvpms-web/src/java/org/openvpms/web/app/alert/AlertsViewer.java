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
 *
 *  $Id$
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
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.util.ColourHelper;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Displays alerts in a popup dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AlertsViewer extends PopupDialog {

    /**
     * The alerts to display.
     */
    private final List<Alerts> alerts;

    /**
     * The alerts table.
     */
    private PagedIMTable<Element> table;

    /**
     * The column containing the alerts table and optional viewer.
     */
    private Column column;

    /**
     * The alert viewer.
     */
    private Component viewer;

    /**
     * Constructs an <tt>AlertsViewer</tt> to display alerts for a single alert type.
     *
     * @param alerts the alerts
     */
    public AlertsViewer(Alerts alerts) {
        this(Arrays.asList(alerts));
    }

    /**
     * Constructs an <tt>AlertsViewer</tt> to display alerts for multiple alert types.
     *
     * @param alerts the alerts
     */
    public AlertsViewer(List<Alerts> alerts) {
        super(Messages.get("alerts.title"), "AlertsViewer", CLOSE);
        setModal(true);
        this.alerts = alerts;
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
        List<Element> elements = new ArrayList<Element>();
        for (Alerts a : alerts) {
            Lookup type = a.getAlertType();
            List<Act> acts = a.getAlerts();
            if (!acts.isEmpty()) {
                for (Act act : acts) {
                    elements.add(new Element(type, act));
                }
            } else {
                elements.add(new Element(type, null));
            }
        }
        ResultSet<Element> set = new ListResultSet<Element>(elements, 20);
        Model model = new Model();
        table = new PagedIMTable<Element>(model, set);
        table.getTable().addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                onSelect();
            }
        });
        column = ColumnFactory.create("CellSpacing", table);
        return column;
    }

    /**
     * Displays the selected alert.
     */
    private void onSelect() {
        Element element = table.getSelected();
        if (element != null) {
            if (viewer != null) {
                column.remove(viewer);
            }
            if (element.act != null) {
                viewer = new IMObjectViewer(element.act, null).getComponent();
            } else {
                viewer = LabelFactory.create("alert.nodetail", "bold");
                ColumnLayoutData layout = new ColumnLayoutData();
                layout.setAlignment(Alignment.ALIGN_CENTER);
                viewer.setLayoutData(layout);
            }
            column.add(viewer);
        }
    }

    /**
     * Helper to wrap an alert type and alert act.
     */
    private static class Element {

        final Lookup lookup;

        final Act act;

        public Element(Lookup lookup, Act act) {
            this.lookup = lookup;
            this.act = act;
        }
    }

    private static class Model extends AbstractIMTableModel<Element> {

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
         * Constructs a new <tt>Model</tt>.
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
         * @param ascending if <tt>true</tt> sort in ascending order; otherwise
         *                  sort in <tt>descending</tt> order
         * @return the sort criteria, or <tt>null</tt> if the column isn't
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
        protected Object getValue(Element object, TableColumn column, int row) {
            int index = column.getModelIndex();
            Object result = null;
            switch (index) {
                case PRIORITY:
                    result = getPriority(object);
                    break;
                case ALERT:
                    result = object.lookup.getName();
                    break;
                case REASON:
                    Act act = object.act;
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
         * @param element the alert element
         * @return a label for the priority
         */
        private Label getPriority(Element element) {
            Label result = LabelFactory.create();
            IMObjectBean bean = new IMObjectBean(element.lookup);
            result.setText(getPriorityName(bean.getString("priority")));
            Color value = ColourHelper.getColor(bean.getString("colour"));
            if (value != null) {
                TableLayoutData layout = new TableLayoutData();
                layout.setBackground(value);
                if (ColourHelper.isCloserToBlackThanWhite(value)) {
                    result.setForeground(Color.WHITE);
                } else {
                    result.setForeground(Color.BLACK);
                }
                result.setLayoutData(layout);
            }
            return result;
        }

        /**
         * Returns a priority name given its code.
         *
         * @param code the priority code.
         * @return the priority name, or <tt>code</tt> if none is found
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
