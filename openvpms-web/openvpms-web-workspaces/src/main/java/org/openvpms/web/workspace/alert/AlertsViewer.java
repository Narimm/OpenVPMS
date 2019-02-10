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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.alert;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.alert.AccountTypeAlert;
import org.openvpms.web.component.alert.ActiveAlertLayoutStrategy;
import org.openvpms.web.component.alert.Alert;
import org.openvpms.web.component.alert.AlertManager;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Displays alerts in a popup dialog.
 *
 * @author Tim Anderson
 */
public class AlertsViewer extends ModalDialog {

    /**
     * The party the alerts belong to.
     */
    private final Party party;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The root pane.
     */
    private final SplitPane pane;

    /**
     * The alerts to display.
     */
    private List<Alert> alerts;

    /**
     * The selected alerts.
     */
    private Alert selected;

    /**
     * The button container.
     */
    private Column buttons;

    /**
     * The alert container.
     */
    private Column container;

    /**
     * The edit button id.
     */
    private static final String EDIT_ID = "button.edit";

    /**
     * Constructs an {@link AlertsViewer} to display alerts for multiple alert types.
     *
     * @param party   the party the alerts belong to
     * @param alerts  the alerts
     * @param context the context
     * @param help    the help context
     */
    public AlertsViewer(Party party, List<Alert> alerts, Context context, HelpContext help) {
        super(Messages.get("alerts.title"), "AlertsViewer", CLOSE, help);
        this.party = party;
        this.context = context;
        this.alerts = alerts;
        addButton(EDIT_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onEdit();
            }
        });
        buttons = ColumnFactory.create(Styles.INSET);
        container = ColumnFactory.create(Styles.INSET);
        pane = SplitPaneFactory.create(SplitPane.ORIENTATION_HORIZONTAL_LEFT_RIGHT, "AlertsViewer.Layout",
                                       buttons, container);
        getLayout().add(pane);
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        layoutButtons();
        if (!alerts.isEmpty()) {
            show(alerts.get(0));
        } else {
            noAlerts();
        }
    }

    /**
     * Lays out the alert buttons.
     */
    private void layoutButtons() {
        buttons.removeAll();
        for (Alert alert : alerts) {
            Button button = AlertHelper.createButton(alert, new ActionListener() {
                public void onAction(ActionEvent event) {
                    show(alert);
                }
            });
            buttons.add(button);
        }
    }

    /**
     * Shows an alert.
     *
     * @param alert the alert
     */
    private void show(Alert alert) {
        selected = alert;
        getButtons().setEnabled(EDIT_ID, alert.getAlert() != null);
        container.removeAll();
        DefaultLayoutContext layout = new DefaultLayoutContext(context, getHelpContext());
        AccountType accountType = (alert instanceof AccountTypeAlert)
                                  ? ((AccountTypeAlert) alert).getAccountType() : null;
        IMObjectViewer viewer = new IMObjectViewer((IMObject) alert.getAlert(), null,
                                                   new ActiveAlertLayoutStrategy(accountType), layout);
        container.add(viewer.getComponent());
    }

    /**
     * Invoked when there are no alerts to display.
     */
    private void noAlerts() {
        getButtons().setEnabled(EDIT_ID, false);
        pane.setSeparatorPosition(new Extent(0));
        pane.setResizable(false);
        buttons.removeAll();
        container.removeAll();
        ColumnLayoutData layout = new ColumnLayoutData();
        layout.setAlignment(new Alignment(Alignment.CENTER, Alignment.CENTER));
        Label empty = LabelFactory.create("alert.nomorealerts", Styles.BOLD);
        empty.setLayoutData(layout);
        container.add(ColumnFactory.create(Styles.LARGE_INSET, empty));
    }

    /**
     * Refreshes the display after an alert is edited.
     */
    private void refresh() {
        alerts = ServiceHelper.getBean(AlertManager.class).getAlerts(party);
        if (!alerts.isEmpty()) {
            layoutButtons();
            Alert show = null;
            if (selected != null) {
                show = alerts.stream().filter(alert -> alert.equals(selected)).findFirst().orElse(null);
            }
            if (show == null) {
                show = alerts.get(0);
            }
            show(show);
        } else {
            noAlerts();
        }
    }

    /**
     * Edits the alert.
     */
    private void onEdit() {
        IMObject object = (selected != null) ? IMObjectHelper.reload((IMObject) selected.getAlert()) : null;
        if (object != null) {
            IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(
                    object, new DefaultLayoutContext(context, getHelpContext()));
            EditDialog dialog = EditDialogFactory.create(editor, context);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                @Override
                public void onClose(WindowPaneEvent event) {
                    refresh();
                }
            });
            dialog.show();
        } else {
            refresh();
        }
    }

}
