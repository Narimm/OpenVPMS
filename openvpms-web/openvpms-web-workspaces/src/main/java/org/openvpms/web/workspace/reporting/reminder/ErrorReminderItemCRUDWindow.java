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

package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Reminder item CRUD window for items with error status.
 *
 * @author Tim Anderson
 * @see ReminderWorkspace
 */
class ErrorReminderItemCRUDWindow extends ReminderItemCRUDWindow {

    /**
     * Resolve error in selected reminder button identifier.
     */
    private static final String RESOLVE_ID = "button.resolve";

    /**
     * Resolve error in all reminders button identifier.
     */
    private static final String RESOLVE_ALL_ID = "button.resolveAll";

    /**
     * Constructs an {@link ErrorReminderItemCRUDWindow}.
     *
     * @param browser the browser
     * @param context the context
     * @param help    the help context
     */
    public ErrorReminderItemCRUDWindow(ReminderItemBrowser browser, Context context, HelpContext help) {
        super(browser, true, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button set
     */
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(RESOLVE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onResolve();
            }
        });
        buttons.add(RESOLVE_ALL_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onResolveAll();
            }
        });
        buttons.add(createCompleteButton());
        buttons.add(createCompleteAllButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(RESOLVE_ID, enable);
    }

    /**
     * Invoked when the 'Resolve' button is pressed. Resolves the error with the selected reminder item.
     */
    private void onResolve() {
        Act object = getObject();
        Act item = IMObjectHelper.reload(object);
        if (item != null && ReminderItemStatus.ERROR.equals(item.getStatus())) {
            resolveError(item);
        }
        onRefresh(object);
    }

    /**
     * Invoked to resolve errors in all reminders matching the query.
     */
    private void onResolveAll() {
        ConfirmationDialog.show(Messages.get("reporting.reminder.resolveall.prompt.title"),
                                Messages.get("reporting.reminder.resolveall.prompt.message"),
                                ConfirmationDialog.YES_NO, new WindowPaneListener() {
                    @Override
                    public void onClose(WindowPaneEvent event) {
                        onResolveAllConfirmed();
                    }
                });
    }


    /**
     * Resolves errors for all reminders items matching the query.
     */
    private void onResolveAllConfirmed() {
        ReminderItemQueryFactory factory = getQueryFactory();
        if (factory != null) {
            ResolveAllProgressBarProcessor processor = new ResolveAllProgressBarProcessor(factory);
            ReminderItemProgressBarDialog dialog = new ReminderItemProgressBarDialog(
                    Messages.get("reporting.reminder.resolveall.run.title"),
                    Messages.get("reporting.reminder.resolveall.run.message"), processor);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                @Override
                public void onClose(WindowPaneEvent event) {
                    onRefresh(getObject());
                }
            });
            dialog.show();
        } else {
            onRefresh(getObject());
        }
    }

    /**
     * Sets a reminder item status as {@code PENDING} and clears any error.
     *
     * @param item the reminder item
     */
    private void resolveError(Act item) {
        ActBean bean = new ActBean(item);
        bean.setValue("status", ReminderItemStatus.PENDING);
        bean.setValue("error", null);
        bean.save();
    }

    private class ResolveAllProgressBarProcessor extends ReminderItemProgressBarProcessor {

        /**
         * Constructs a {@link ResolveAllProgressBarProcessor}.
         */
        public ResolveAllProgressBarProcessor(ReminderItemQueryFactory factory) {
            super(factory);
        }

        /**
         * Processes a reminder item.
         *
         * @param item     the reminder item
         * @param reminder the reminder
         */
        @Override
        protected void process(Act item, Act reminder) {
            String status = item.getStatus();
            if (ReminderItemStatus.ERROR.equals(status)) {
                resolveError(item);
                updated();
            }
        }
    }

}
