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
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.app.PracticeMailContext;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * Prints reminders, updating a progress bar as it goes.
 *
 * @author Tim Anderson
 */
public class ReminderPrintProgressBarProcessor extends ReminderProgressBarProcessor {

    /**
     * Customer contact identifiers collected during processing. Only the identifiers are collected to limit
     * memory use.
     */
    private final List<ContactIds> contactIds = new ArrayList<>();

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The mailing labels button.
     */
    private final Button mailingLabels;

    /**
     * The print component.
     */
    private final Component component;

    /**
     * Constructs a {@link ReminderPrintProgressBarProcessor}.
     *
     * @param query      the query
     * @param processor  the email processor
     * @param statistics the statistics
     * @param help       the help context
     */
    public ReminderPrintProgressBarProcessor(ReminderItemSource query, ReminderPrintProcessor processor,
                                             Statistics statistics, HelpContext help) {
        super(query, processor, statistics, Messages.get("reporting.reminder.run.print"));
        this.help = help;

        PrinterListener listener = new PrinterListener() {
            public void printed(String printer) {
                try {
                    setSuspend(false);
                    processCompleted();
                } catch (OpenVPMSException exception) {
                    processError(exception);
                }
            }

            public void cancelled() {
                notifyCompleted();
            }

            public void skipped() {
                setSuspend(false);
                skip();
            }

            public void failed(Throwable cause) {
                processError(cause);
            }
        };
        processor.setListener(listener);
        mailingLabels = ButtonFactory.create("button.mailinglabels", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                printMailingLabels();
            }
        });
        mailingLabels.setEnabled(false);
        component = RowFactory.create(Styles.CELL_SPACING, getProgressBar(), mailingLabels);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        return component;
    }

    /**
     * Invoked when batch processing has completed.
     */
    @Override
    protected void processingCompleted() {
        super.processingCompleted();
        if (!contactIds.isEmpty()) {
            mailingLabels.setEnabled(true);
        }
    }

    /**
     * Processes reminders.
     *
     * @param reminders the reminders
     */
    @Override
    protected void process(PatientReminders reminders) {
        super.process(reminders);
        List<ReminderEvent> events = reminders.getReminders();
        if (!events.isEmpty()) {
            // collect the contact information, to support mailing label printing on completion.
            Set<Party> patients = new HashSet<>();
            for (ReminderEvent event : events) {
                patients.add(event.getPatient());
            }
            Party patient = patients.size() == 1 ? patients.iterator().next() : null;
            ReminderEvent first = events.get(0);
            ContactIds ids = new ContactIds(first.getCustomer(), first.getContact(), patient, first.getLocation());
            contactIds.add(ids);
        }
    }

    /**
     * Prints the mailing labels.
     */
    private void printMailingLabels() {
        PatientReminderProcessor processor = getProcessor();
        Party practice = processor.getPractice();
        LocalContext context = new LocalContext();
        context.setPractice(practice);
        ObjectSetReportPrinter printer = new ObjectSetReportPrinter(new MailingLabelCollection(contactIds),
                                                                    "PATIENT_MAILING_LABELS", context);
        String title = Messages.format("imobject.print.title", Messages.get("reporting.reminder.mailinglabels"));
        InteractiveIMPrinter<ObjectSet> iPrinter = new InteractiveIMPrinter<>(
                title, printer, context, help.subtopic("print"));
        iPrinter.setMailContext(new PracticeMailContext(context));
        iPrinter.print();
    }

    private static class ContactIds {

        private final IMObjectReference customerId;
        private final IMObjectReference contactId;
        private final IMObjectReference patientId;
        private final IMObjectReference locationId;

        public ContactIds(Party customer, Contact contact, Party patient, Party location) {
            this.customerId = customer.getObjectReference();
            this.contactId = contact.getObjectReference();
            this.patientId = (patient != null) ? patient.getObjectReference() : null;
            this.locationId = (location != null) ? location.getObjectReference() : null;
        }
    }

    private static class MailingLabelCollection implements Iterable<ObjectSet> {

        private final List<ContactIds> ids;

        public MailingLabelCollection(List<ContactIds> contactIds) {
            this.ids = contactIds;
        }

        /**
         * Returns an iterator over the contact information.
         *
         * @return an Iterator.
         */
        @Override
        public Iterator<ObjectSet> iterator() {
            return new ContactIterator(ids);
        }

        private static class ContactIterator implements Iterator<ObjectSet> {

            private final Iterator<ContactIds> iterator;
            private ObjectSet current = null;

            public ContactIterator(List<ContactIds> ids) {
                this.iterator = ids.iterator();
            }

            /**
             * Returns {@code true} if the iteration has more elements.
             * (In other words, returns {@code true} if {@link #next} would
             * return an element rather than throwing an exception.)
             *
             * @return {@code true} if the iteration has more elements
             */
            @Override
            public boolean hasNext() {
                return current != null || advance();
            }

            /**
             * Returns the next element in the iteration.
             *
             * @return the next element in the iteration
             * @throws NoSuchElementException if the iteration has no more elements
             */
            @Override
            public ObjectSet next() {
                if (current == null) {
                    if (!advance()) {
                        throw new NoSuchElementException();
                    }
                }
                ObjectSet result = current;
                current = null;
                return result;
            }

            private boolean advance() {
                current = null;
                while (iterator.hasNext()) {
                    ContactIds ids = iterator.next();
                    Party customer = (Party) IMObjectHelper.getObject(ids.customerId);
                    if (customer != null) {
                        Contact contact = IMObjectHelper.getObject(ids.contactId, customer.getContacts());
                        if (contact != null) {
                            Party patient = (Party) IMObjectHelper.getObject(ids.patientId);
                            Party location = (Party) IMObjectHelper.getObject(ids.locationId);
                            current = new ObjectSet();
                            current.set("customer", customer);
                            current.set("contact", contact);
                            current.set("patient", patient);
                            current.set("location", location);
                            break;
                        }
                    }
                }
                return current != null;
            }
        }
    }

}
