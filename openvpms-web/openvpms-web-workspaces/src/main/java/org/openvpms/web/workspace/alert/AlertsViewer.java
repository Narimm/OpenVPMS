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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.alert.AccountTypeAlert;
import org.openvpms.web.component.alert.ActiveAlertLayoutStrategy;
import org.openvpms.web.component.alert.Alert;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;


/**
 * Displays alerts in a popup dialog.
 *
 * @author Tim Anderson
 */
public class AlertsViewer extends ModalDialog {

    /**
     * The alerts to display.
     */
    private final List<Alert> alerts;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Constructs an {@link AlertsViewer} to display alerts for multiple alert types.
     *
     * @param alerts  the alerts
     * @param context the context
     * @param help    the help context
     */
    public AlertsViewer(List<Alert> alerts, Context context, HelpContext help) {
        super(Messages.get("alerts.title"), "AlertsViewer", CLOSE, help);
        this.context = context;
        this.alerts = alerts;
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Column left = ColumnFactory.create(Styles.INSET);
        Column right = ColumnFactory.create(Styles.INSET);
        for (Alert alert : alerts) {
            Button button = ButtonFactory.create(null, "small");
            button.setText(alert.getName());
            Color colour = alert.getColour();
            if (colour != null) {
                button.setBackground(colour);
                button.setForeground(alert.getTextColour());
            }

            button.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    show(alert, right);
                }
            });
            left.add(button);
        }
        if (!alerts.isEmpty()) {
            show(alerts.get(0), right);
        }

        SplitPane pane = SplitPaneFactory.create(SplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT, "AlertsViewer.Layout",
                                                 left, right);
        getLayout().add(pane);
    }

    /**
     * Shows an alert.
     *
     * @param alert     the alert
     * @param container the container
     */
    private void show(Alert alert, Column container) {
        container.removeAll();
        DefaultLayoutContext layout = new DefaultLayoutContext(AlertsViewer.this.context, getHelpContext());
        AccountType accountType = (alert instanceof AccountTypeAlert)
                                  ? ((AccountTypeAlert) alert).getAccountType() : null;
        IMObjectViewer viewer = new IMObjectViewer((IMObject) alert.getAlert(), null,
                                                   new ActiveAlertLayoutStrategy(accountType), layout);
        container.add(viewer.getComponent());
    }

}
