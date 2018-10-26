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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.alert;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.alert.Alert;
import org.openvpms.web.component.alert.AlertDialog;
import org.openvpms.web.component.alert.AlertManager;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Provides a summary of customer/patient alerts.
 *
 * @author Tim Anderson
 */
public class AlertSummary {

    /**
     * The party the alerts are for.
     */
    private final Party party;

    /**
     * The resource bundle key.
     */
    private final String key;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The container.
     */
    private final Column container;

    /**
     * The alerts.
     */
    private List<Alert> alerts;

    /**
     * The no. of alerts to show.
     */
    private int showCount = 4;

    /**
     * Constructs an {@link AlertSummary}.
     *
     * @param party   the party the alerts are for
     * @param alerts  the alerts
     * @param context the context
     * @param help    the help context
     */
    public AlertSummary(Party party, List<Alert> alerts, Context context, HelpContext help) {
        this(party, alerts, "alerts.title", context, help);
    }

    /**
     * Constructs an {@link AlertSummary}.
     *
     * @param party   the party the alerts are for
     * @param alerts  the alerts
     * @param key     the resource bundle key
     * @param context the context
     * @param help    the help context
     */
    public AlertSummary(Party party, List<Alert> alerts, String key, Context context, HelpContext help) {
        this.party = party;
        this.alerts = alerts;
        this.key = key;
        this.context = context;
        this.help = help.subtopic("alert");
        container = ColumnFactory.create();
    }

    /**
     * Sets the number of alerts to display.
     *
     * @param count the no. of alerts to display. Defaults to {@code 4}.
     */
    public void setShowCount(int count) {
        showCount = count;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        doLayout();
        return container;
    }

    /**
     * Lays out the component.
     */
    protected void doLayout() {
        container.removeAll();
        if (!alerts.isEmpty()) {
            Row title = RowFactory.create(LabelFactory.create(key, Styles.BOLD));
            container.add(title);
            for (int i = 0; i < alerts.size() && i < showCount; ++i) {
                Alert element = alerts.get(i);
                container.add(getButton(element));
            }

            if (alerts.size() > showCount) {
                // add a View All button to show all current alerts in a popup
                Button viewAll = ButtonFactory.create("alerts.viewall", "small", new ActionListener() {
                    public void onAction(ActionEvent event) {
                        onShowAll();
                    }
                });
                Row right = RowFactory.create(viewAll);

                RowLayoutData rightLayout = new RowLayoutData();
                rightLayout.setAlignment(Alignment.ALIGN_RIGHT);
                rightLayout.setWidth(new Extent(100, Extent.PERCENT));
                right.setLayoutData(rightLayout);

                Row row;
                row = RowFactory.create(Styles.WIDE_CELL_SPACING, title, right);
                container.add(row, 0);
            }
        } else {
            container.setVisible(false);
        }
    }

    /**
     * Displays a dialog with all alerts.
     */
    protected void onShowAll() {
        AlertsViewer viewer = new AlertsViewer(party, alerts, context, help);
        viewer.addWindowPaneListener(new WindowPaneListener() {
            @Override
            public void onClose(WindowPaneEvent event) {
                refresh();
            }
        });
        viewer.show();
    }

    /**
     * Returns a button to render the alerts.
     *
     * @param alert the alerts
     * @return a new button
     */
    protected Button getButton(Alert alert) {
        return AlertHelper.createButton(alert, new ActionListener() {
            public void onAction(ActionEvent event) {
                AlertDialog dialog = new AlertDialog(alert, context, help);
                dialog.addWindowPaneListener(new WindowPaneListener() {
                    @Override
                    public void onClose(WindowPaneEvent event) {
                        refresh();
                    }
                });
                dialog.show();
            }
        });
    }

    /**
     * Refreshes the alerts.
     */
    private void refresh() {
        alerts = ServiceHelper.getBean(AlertManager.class).getAlerts(party);
        doLayout();
    }

}
