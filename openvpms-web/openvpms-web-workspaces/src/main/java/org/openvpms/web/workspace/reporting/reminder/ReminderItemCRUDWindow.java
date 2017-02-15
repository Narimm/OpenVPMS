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
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.patient.reminder.PagedReminderIterator;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.AbstractViewCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Reminder item CRUD window.
 *
 * @author Tim Anderson
 * @see ReminderWorkspace
 */
class ReminderItemCRUDWindow extends AbstractViewCRUDWindow<Act> {

    /**
     * The browser.
     */
    private final ReminderItemBrowser browser;

    /**
     * The rules.
     */
    private final ReminderRules rules;

    /**
     * Send selected reminder button identifier.
     */
    private static final String SEND_ID = "button.send";

    /**
     * Send all reminders button identifier.
     */
    private static final String SEND_ALL_ID = "button.sendAll";

    /**
     * Complete selected reminder button identifier.
     */
    private static final String COMPLETE_ID = "button.complete";

    /**
     * Complete all reminders button identifier.
     */
    private static final String COMPLETE_ALL_ID = "button.completeAll";

    /**
     * Constructs a {@link ReminderItemCRUDWindow}.
     *
     * @param browser the browser
     * @param context the context
     * @param help    the help context
     */
    public ReminderItemCRUDWindow(ReminderItemBrowser browser, Context context, HelpContext help) {
        super(Archetypes.create(ReminderArchetypes.REMINDER_ITEMS, Act.class), Actions.INSTANCE, context, help);
        this.browser = browser;
        this.rules = ServiceHelper.getBean(ReminderRules.class);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button set
     */
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(SEND_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onSend();
            }
        });
        buttons.add(SEND_ALL_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onSendAll();
            }
        });
        buttons.add(COMPLETE_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onComplete();
            }
        });
        buttons.add(COMPLETE_ALL_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onCompleteAll();
            }
        });
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.setEnabled(SEND_ID, enable);
        buttons.setEnabled(COMPLETE_ID, enable);
    }

    /**
     * Invoked when the 'Send' button is pressed. Sends the selected reminder item.
     */
    private void onSend() {
        Act object = getObject();
        Act item = IMObjectHelper.reload(object);
        if (item != null) {
            try {
                HelpContext help = getHelpContext().subtopic("send");
                ReminderGeneratorFactory factory = ServiceHelper.getBean(ReminderGeneratorFactory.class);
                ReminderGenerator generator = factory.create(item, getContext(), getMailContext(), help);
                generateReminders(generator);
            } catch (Throwable exception) {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Invoked when the 'Send All' button is pressed. Runs the reminder generator for all reminders.
     */
    private void onSendAll() {
        String title = Messages.get("reporting.reminder.run.title");
        String message = Messages.get("reporting.reminder.run.message");
        HelpContext help = getHelpContext().subtopic("confirmsend");
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message, help);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                generateReminders();
            }
        });
        dialog.show();
    }

    /**
     * Invoked to complete a reminder.
     */
    private void onComplete() {
        Act object = getObject();
        Act item = IMObjectHelper.reload(object);
        if (item != null) {
            String status = item.getStatus();
            if (ReminderItemStatus.PENDING.equals(status) || ReminderItemStatus.ERROR.equals(status)) {
                ActBean bean = new ActBean(item);
                Act reminder = (Act) bean.getNodeSourceObject("reminder");
                if (reminder != null) {
                    complete(item, reminder);
                }
            }
        }
        onRefresh(object);
    }

    /**
     * Invoked to complete all reminders matching the query, without sending them.
     */
    private void onCompleteAll() {
        ReminderItemQueryFactory factory = browser.getFactory();
        if (factory != null) {
            PagedReminderIterator iterator = new PagedReminderIterator(factory, 1000,
                                                                       ServiceHelper.getArchetypeService());
            while (iterator.hasNext()) {
                ObjectSet set = iterator.next();
                Act item = (Act) set.get("item");
                String status = item.getStatus();
                if (ReminderItemStatus.PENDING.equals(status) || ReminderItemStatus.ERROR.equals(status)) {
                    Act reminder = (Act) set.get("reminder");
                    complete(item, reminder);
                    iterator.updated();
                }
            }
        }
        onRefresh(getObject());
    }

    private void complete(Act item, Act reminder) {
        item.setStatus(ReminderItemStatus.COMPLETED);
        ActBean itemBean = new ActBean(item);
        itemBean.setValue("error", null);
        List<Act> toSave = new ArrayList<>();
        toSave.add(item);
        if (rules.updateReminder(reminder, item)) {
            toSave.add(reminder);
        }
        SaveHelper.save(toSave);
    }

    /**
     * Generate the reminders.
     */
    private void generateReminders() {
        try {
            HelpContext help = getHelpContext().subtopic("send");
            ReminderGeneratorFactory factory = ServiceHelper.getBean(ReminderGeneratorFactory.class);
            ReminderItemQueryFactory queryFactory = browser.getFactory();
            ReminderGenerator generator = factory.create(queryFactory, getContext(), getMailContext(), help);
            generateReminders(generator);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Generates reminders using the specified generator.
     * Updates the browser on completion.
     *
     * @param generator the generator
     */
    private void generateReminders(ReminderGenerator generator) {
        generator.setListener(new BatchProcessorListener() {
            public void completed() {
                onRefresh(getObject());
            }

            public void error(Throwable exception) {
                ErrorHelper.show(exception);
            }
        });
        generator.process();
    }


    private static final class Actions extends ActActions<Act> {

        public static final Actions INSTANCE = new Actions();

        /**
         * Determines if objects can be created.
         *
         * @return {@code true}
         */
        @Override
        public boolean canCreate() {
            return false;
        }

        /**
         * Determines if an act can be deleted.
         *
         * @param act the act to check
         * @return {@code true} if the act isn't locked
         */
        @Override
        public boolean canDelete(Act act) {
            return true;
        }

        public boolean canEdit(Act act) {
            return true;
        }
    }
}
