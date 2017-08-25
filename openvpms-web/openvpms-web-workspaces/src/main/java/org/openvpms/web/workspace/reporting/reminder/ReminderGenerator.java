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

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.component.processor.AbstractBatchProcessor;
import org.openvpms.archetype.component.processor.AsynchronousBatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessor;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.List;


/**
 * Reminder generator.
 *
 * @author Tim Anderson
 */
public class ReminderGenerator extends AbstractBatchProcessor {

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The reminder processors.
     */
    private List<ReminderBatchProcessor> processors = new ArrayList<>();

    /**
     * If {@code true}, multiple reminders are being processed.
     */
    private boolean multipleReminders = true;


    /**
     * Constructs a {@link ReminderGenerator} to process a single reminder item.
     *
     * @param item             the reminder item
     * @param reminder         the reminder
     * @param contact          the contact to send to. May be {@code null}
     * @param help             the help context
     * @param processorFactory the reminder processor factory
     */
    public ReminderGenerator(Act item, Act reminder, Contact contact, HelpContext help,
                             PatientReminderProcessorFactory processorFactory) {
        this.help = help;
        ReminderItemSource query = new SingleReminderItemSource(item, reminder, contact);
        ReminderBatchProcessor processor = processorFactory.createBatchProcessor(query);
        processors.add(processor);
        multipleReminders = false;
    }

    /**
     * Constructs a {@link ReminderGenerator} for reminders returned by a query.
     *
     * @param factory          the query factory
     * @param help             the help context
     * @param processorFactory the reminder processor factory
     */
    public ReminderGenerator(ReminderItemQueryFactory factory, HelpContext help,
                             PatientReminderProcessorFactory processorFactory) {
        this.help = help;
        ReminderTypes reminderTypes = processorFactory.getReminderTypes();
        ReminderConfiguration config = processorFactory.getConfiguration();
        for (String archetype : DescriptorHelper.getShortNames(factory.getArchetypes())) {
            ReminderItemQueryFactory clone = factory.copy(archetype);
            ReminderBatchProcessor processor = processorFactory.createBatchProcessor(
                    new ReminderItemQuerySource(clone, reminderTypes, config));
            processors.add(processor);
        }
    }

    /**
     * Processes the reminders.
     */
    public void process() {
        if (!processors.isEmpty()) {
            if (multipleReminders) {
                boolean popup = true;
                if (processors.size() == 1) {
                    // if there is only a single processor and it is synchronous, don't display the dialog
                    ReminderBatchProcessor processor = processors.get(0);
                    if (!(processor instanceof AsynchronousBatchProcessor)) {
                        popup = false;
                    }
                }
                if (popup) {
                    ReminderGenerationDialog dialog = new ReminderGenerationDialog(processors, help);
                    dialog.show();
                    dialog.addWindowPaneListener(new WindowPaneListener() {
                        @Override
                        public void onClose(WindowPaneEvent event) {
                            onCompletion();
                        }
                    });
                } else {
                    ReminderBatchProcessor processor = processors.get(0);
                    process(processor);
                }
            } else {
                // only processing a single reminder
                ReminderBatchProcessor processor = processors.get(0);
                process(processor);
            }
        } else {
            InformationDialog.show(Messages.get("reporting.reminder.none.title"),
                                   Messages.get("reporting.reminder.none.message"));
        }
    }

    /**
     * Indicates if reminders are being reprocessed.
     * <p>
     * If set:
     * <ul>
     * <li>due dates are ignored</li>
     * <li>the reminder last sent date is not updated</li>
     * </ul>
     * <p>
     * Defaults to {@code false}.
     *
     * @param resend if {@code true} reminders are being reprocessed
     */
    public void setResend(boolean resend) {
        for (ReminderBatchProcessor processor : processors) {
            processor.setResend(resend);
        }
    }

    /**
     * Processes reminders for a single processor.
     *
     * @param processor the processor
     */
    private void process(final ReminderBatchProcessor processor) {
        processor.setListener(new BatchProcessorListener() {
            @Override
            public void completed() {
                if (processor.hasMoreReminders()) {
                    InformationDialog.show(Messages.get("reporting.reminder.run.title"),
                                           Messages.format("reporting.reminder.run.rerun", processor.getTitle()),
                                           new WindowPaneListener() {
                                               @Override
                                               public void onClose(WindowPaneEvent event) {
                                                   onCompletion();
                                               }
                                           });
                } else {
                    onCompletion();
                }
            }

            @Override
            public void error(Throwable exception) {
                onError(exception);
            }
        });
        processor.process();
    }

    /**
     * Invoked when generation is complete.
     * Notifies any listener.
     */
    private void onCompletion() {
        updateProcessed();
        notifyCompleted();
    }

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    private void onError(Throwable exception) {
        updateProcessed();
        notifyError(exception);
    }

    /**
     * Updates the count of processed reminders.
     */
    private void updateProcessed() {
        int processed = 0;
        for (BatchProcessor processor : processors) {
            processed += processor.getProcessed();
        }
        setProcessed(processed);
    }

}

