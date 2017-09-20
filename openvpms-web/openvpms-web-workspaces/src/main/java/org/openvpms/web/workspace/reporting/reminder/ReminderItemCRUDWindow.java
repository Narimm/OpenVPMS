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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.patient.reminder.GroupingReminderIterator;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.Reminders;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.Selection;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.AbstractViewCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
     * Determines if this is used to resend reminders. If {@code true}, due dates are ignored and reminders are not
     * updated.
     */
    private final boolean resend;

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
     * Preview button identifier.
     */
    private static final String PREVIEW_ID = "button.preview";

    /**
     * Constructs a {@link ReminderItemCRUDWindow}.
     *
     * @param browser the browser
     * @param resend  determines if this is used to resend reminders. If {@code true}, due dates are ignored and
     *                reminders are not updated
     * @param context the context
     * @param help    the help context
     */
    public ReminderItemCRUDWindow(ReminderItemBrowser browser, boolean resend, Context context, HelpContext help) {
        super(Archetypes.create(ReminderArchetypes.REMINDER_ITEMS, Act.class), Actions.INSTANCE, context, help);
        this.browser = browser;
        this.resend = resend;
        this.rules = ServiceHelper.getBean(ReminderRules.class);
    }

    /**
     * Returns the reminder associated with the selected reminder item.
     *
     * @return the reminder, or {@code null} if no reminder item is selected
     */
    public Act getReminder() {
        Act item = getObject();
        return (item != null) ? getReminder(item) : null;
    }

    /**
     * Edits the current object.
     */
    @Override
    public void edit() {
        Act item = getObject();
        if (item != null) {
            Act reminder = getReminder(item);
            if (reminder == null) {
                ErrorDialog.show(Messages.format("imobject.noexist",
                                                 DescriptorHelper.getDisplayName(ReminderArchetypes.REMINDER)));
            } else {
                ActBean bean = new ActBean(reminder);
                ActRelationship relationship = bean.getRelationship(item);
                edit(reminder, Arrays.asList(new Selection("items", null), new Selection(null, relationship)));
            }
        }
    }

    /**
     * View an object.
     *
     * @param object the object to view. May be {@code null}
     */
    @Override
    protected void view(Act object) {
        Act reminder = (object != null) ? getReminder(object) : null;
        super.view(reminder);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act object, boolean isNew) {
        Act item = IMObjectHelper.reload(getObject());
        if (item != null) {
            super.onSaved(item, false);
        } else {
            onDeleted(getObject());  // item may have been deleted within the reminder editor
        }
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    @Override
    protected void onRefresh(Act object) {
        Act item = IMObjectHelper.reload(getObject());
        if (item != null) {
            super.onRefresh(item);
        }
    }

    protected Act getReminder(Act item) {
        return (Act) new ActBean(item).getNodeSourceObject("reminder");
    }

    /**
     * Creates a new {@link IMObjectViewer} for an object.
     *
     * @param object the object to view
     * @return a new viewer
     */
    @Override
    protected IMObjectViewer createViewer(IMObject object) {
        return super.createViewer(object);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button set
     */
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(createEditButton());
    }

    /**
     * Helper to create a new button with id {@link #SEND_ID} linked to {@link #onSend()}.
     *
     * @return a new button
     */
    protected Button createSendButton() {
        return ButtonFactory.create(SEND_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onSend();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #SEND_ALL_ID} linked to {@link #onSendAll()}.
     *
     * @return a new button
     */
    protected Button createSendAllButton() {
        return ButtonFactory.create(SEND_ALL_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onSendAll();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #COMPLETE_ID} linked to {@link #onComplete()} ()}.
     *
     * @return a new button
     */
    protected Button createCompleteButton() {
        return ButtonFactory.create(COMPLETE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onComplete();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #COMPLETE_ALL_ID} linked to {@link #onCompleteAll()}.
     *
     * @return a new button
     */
    protected Button createCompleteAllButton() {
        return ButtonFactory.create(COMPLETE_ALL_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onCompleteAll();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #PREVIEW_ID} linked to {@link #onPreview()}.
     *
     * @return a new button
     */
    protected Button createPreviewButton() {
        return ButtonFactory.create(PREVIEW_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onPreview();
            }
        });
    }

    /**
     * Previews the selected item.
     */
    @Override
    protected void onPreview() {
        ObjectSet selected = browser.getBrowser().getSelected();
        if (selected != null) {
            Act item = (Act) selected.get("item");
            Party customer = (Party) selected.get("customer");

            // use the GroupingReminderIterator to locate reminders that will be grouped with the selected item
            ReminderItemQueryFactory factory = browser.getFactory();
            String archetype = item.getArchetypeId().getShortName();
            ReminderItemQueryFactory copy = factory.copy(archetype);
            copy.setCustomer(customer);
            ReminderGeneratorFactory generators = ServiceHelper.getBean(ReminderGeneratorFactory.class);
            Context context = getContext();
            PatientReminderProcessorFactory processorFactory = generators.createFactory(
                    context.getLocation(), context.getPractice(), getHelpContext());
            ReminderConfiguration config = processorFactory.getConfiguration();
            GroupingReminderIterator iterator = new GroupingReminderIterator(
                    copy, processorFactory.getReminderTypes(), 100, config.getGroupByCustomerPolicy(),
                    config.getGroupByPatientPolicy(), ServiceHelper.getArchetypeService());
            Reminders found = null;
            while (iterator.hasNext()) {
                Reminders reminders = iterator.next();
                if (reminders.contains(item)) {
                    found = reminders;
                    break;
                }
            }
            if (found != null) {
                PatientReminderProcessor processor = processorFactory.create(archetype);
                PatientReminderPreviewer previewer = generators.createPreviewer(processor, getHelpContext());
                preview(item, found, previewer);
            }
        }
    }

    /**
     * Previews reminders.
     * <p>
     * This implementation treats reminders as having been sent previously
     *
     * @param item      the selected reminder item
     * @param reminders the reminders to preview
     * @param previewer the previewer to use
     */
    protected void preview(Act item, Reminders reminders, PatientReminderPreviewer previewer) {
        previewer.preview(item, reminders.getReminders(), reminders.getGroupBy(), new Date(), true);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.setEnabled(EDIT_ID, enable);
        buttons.setEnabled(SEND_ID, enable);
        buttons.setEnabled(COMPLETE_ID, enable);
    }

    /**
     * Returns the reminder item query factory.
     *
     * @return the query factory
     */
    protected ReminderItemQueryFactory getQueryFactory() {
        return browser.getFactory();
    }

    /**
     * Invoked when the 'Send' button is pressed. Sends the selected reminder item.
     */
    private void onSend() {
        Act object = getObject();
        final Act item = IMObjectHelper.reload(object);
        if (ReminderItemStatus.CANCELLED.equals(item.getStatus())) {
            ConfirmationDialog.show(Messages.get("reporting.reminder.send.title"),
                                    Messages.format("reporting.reminder.send.sendcancelled"),
                                    ConfirmationDialog.YES_NO, new PopupDialogListener() {
                        @Override
                        public void onYes() {
                            send(item);
                        }
                    });
        } else {
            send(item);
        }
    }

    /**
     * Sends a reminder item.
     *
     * @param item the reminder item to send
     */
    private void send(final Act item) {
        if (item != null) {
            try {
                HelpContext help = getHelpContext().subtopic("send");
                ReminderGeneratorFactory factory = ServiceHelper.getBean(ReminderGeneratorFactory.class);
                Context context = getContext();
                Party location = context.getLocation();
                Party practice = context.getPractice();
                final ReminderGenerator generator = factory.create(item, null, location, practice, help);
                generator.setResend(resend);
                generator.setListener(new BatchProcessorListener() {
                    public void completed() {
                        if (generator.getProcessed() == 0) {
                            // the reminder failed to be sent
                            String error = new ActBean(item).getString("error");
                            if (ReminderItemStatus.CANCELLED.equals(item.getStatus())) {
                                String message = !StringUtils.isEmpty(error)
                                                 ? error : Messages.get("reporting.reminder.send.cancelled");
                                ErrorDialog.show(Messages.get("reporting.reminder.send.title"), message);
                            } else if (ReminderItemStatus.ERROR.equals(item.getStatus())) {
                                ErrorDialog.show(Messages.get("reporting.reminder.send.title"), error);
                            }
                            onRefresh(getObject());
                        }
                    }

                    public void error(Throwable exception) {
                        ErrorHelper.show(exception);
                    }
                });
                generator.process();
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
        ReminderItemQueryFactory queryFactory = getQueryFactory();
        String[] statuses = queryFactory.getStatuses();
        if (statuses == null || statuses.length == 0 || ArrayUtils.contains(statuses, ReminderItemStatus.CANCELLED)) {
            InformationDialog.show(title, Messages.get("reporting.reminder.run.nocancelled"));
        } else {
            String message = Messages.get("reporting.reminder.run.message");
            HelpContext help = getHelpContext().subtopic("confirmsend");
            final ConfirmationDialog dialog = new ConfirmationDialog(title, message, ConfirmationDialog.YES_NO, help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onYes() {
                    generateReminders();
                }
            });
            dialog.show();
        }
    }

    /**
     * Invoked to complete a reminder.
     */
    private void onComplete() {
        ConfirmationDialog.show(Messages.get("reporting.reminder.complete.title"),
                                Messages.get("reporting.reminder.complete.message"),
                                ConfirmationDialog.YES_NO, new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        onCompleteConfirmed();
                    }
                });
    }

    /**
     * Completes the selected reminder item.
     */
    private void onCompleteConfirmed() {
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
     * Invoked to complete all reminders items matching the query, without sending them.
     */
    private void onCompleteAll() {
        ConfirmationDialog.show(Messages.get("reporting.reminder.completeall.prompt.title"),
                                Messages.get("reporting.reminder.completeall.prompt.message"),
                                ConfirmationDialog.YES_NO, new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        onCompleteAllConfirmed();
                    }
                });
    }

    /**
     * Completes all reminders items matching the query, without sending them.
     */
    private void onCompleteAllConfirmed() {
        ReminderItemQueryFactory factory = getQueryFactory();
        if (factory != null) {
            CompleteAllProgressBarProcessor processor = new CompleteAllProgressBarProcessor(factory);
            ReminderItemProgressBarDialog dialog = new ReminderItemProgressBarDialog(
                    Messages.get("reporting.reminder.completeall.run.title"),
                    Messages.get("reporting.reminder.completeall.run.message"), processor);
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
     * Sets the status of the reminder item to {@code COMPLETED}.
     * <br/>
     * If there are no other {@code PENDING} or {@code ERROR} reminder items linked to the reminder, the
     * reminder count will be incremented.
     *
     * @param item     the reminder item
     * @param reminder the reminder
     */
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
            ReminderItemQueryFactory queryFactory = getQueryFactory();
            Context context = getContext();
            Party location = context.getLocation();
            Party practice = context.getPractice();
            ReminderGenerator generator = factory.create(queryFactory, location, practice, help);
            generator.setResend(resend);
            generator.setListener(new BatchProcessorListener() {
                public void completed() {
                    onRefresh(getObject());
                }

                public void error(Throwable exception) {
                    ErrorHelper.show(exception);
                }
            });
            generator.process();
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
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

    private class CompleteAllProgressBarProcessor extends ReminderItemProgressBarProcessor {

        /**
         * Constructs a {@code CompleteAllProgressBarProcessor}.
         */
        public CompleteAllProgressBarProcessor(ReminderItemQueryFactory factory) {
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
            if (ReminderItemStatus.PENDING.equals(status) || ReminderItemStatus.ERROR.equals(status)) {
                complete(item, reminder);
                updated();
            }
        }
    }

}