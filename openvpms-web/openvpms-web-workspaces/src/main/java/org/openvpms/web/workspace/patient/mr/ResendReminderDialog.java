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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.SelectField;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderCount;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessor;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.reporting.reminder.ReminderGenerator;
import org.openvpms.web.workspace.reporting.reminder.ReminderGeneratorFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Reminder resend dialog.
 *
 * @author Tim Anderson
 */
class ResendReminderDialog extends PopupDialog {

    /**
     * The reminder.
     */
    private final Act reminder;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

    /**
     * The reminder processor.
     */
    private ReminderProcessor processor;

    /**
     * The reminder count selector.
     */
    private SelectField countSelector;

    /**
     * The email and location contact selector.
     */
    private SelectField contactSelector;

    /**
     * Error dialog title.
     */
    private static final String ERROR_TITLE = "patient.reminder.resend.error.title";


    /**
     * Constructs a {@link ResendReminderDialog}.
     *
     * @param reminder       the reminder
     * @param contacts       the customer's email, SMS and location contacts
     * @param reminderCounts the reminder counts that may be (re)sent
     * @param reminderCount  the current reminder count
     * @param processor      the reminder processor
     * @param context        the context
     * @param help           the help context
     */
    private ResendReminderDialog(Act reminder, List<Contact> contacts, List<Integer> reminderCounts, int reminderCount,
                                 ReminderProcessor processor, Context context, HelpContext help) {
        super(Messages.get("patient.reminder.resend.title"), OK_CANCEL, help);
        this.reminder = reminder;
        this.processor = processor;
        this.context = context;
        this.rules = ServiceHelper.getBean(ReminderRules.class);
        setModal(true);
        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("patient.reminder.resend.contacts"));
        IMObjectListModel model = new IMObjectListModel(contacts, false, false);
        contactSelector = SelectFieldFactory.create(model);
        contactSelector.setCellRenderer(IMObjectListCellRenderer.DESCRIPTION);
        contactSelector.setSelectedIndex(0);
        grid.add(contactSelector);

        grid.add(LabelFactory.create("patient.reminder.resend.reminderCount"));
        countSelector = SelectFieldFactory.create(reminderCounts);
        countSelector.setSelectedItem(reminderCount);
        if (countSelector.getSelectedIndex() < 0) {
            countSelector.setSelectedIndex(0);
        }
        grid.add(countSelector);
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, grid));
    }

    /**
     * Creates a new {@link ResendReminderDialog} for the supplied reminder.
     *
     * @param reminder the reminder
     * @param context  the context
     * @param help     the help context
     * @return a new resend dialog, or {@code null} if the reminder can't be resent
     */
    public static ResendReminderDialog create(Act reminder, Context context, HelpContext help) {
        ResendReminderDialog result = null;
        IMObjectBean bean = new IMObjectBean(reminder);
        int reminderCount = bean.getInt("reminderCount");
        PatientRules rules = ServiceHelper.getBean(PatientRules.class);
        boolean disableSMS = !SMSHelper.isSMSEnabled(context.getPractice());

        try {
            ReminderProcessor processor = new ReminderProcessor(reminder.getActivityStartTime(),
                                                                getReminderConfig(context), disableSMS,
                                                                ServiceHelper.getArchetypeService(), rules);
            Party patient = processor.getPatient(reminder);
            if (patient == null) {
                throw new IllegalStateException("Patient not found");
            }
            Party customer = processor.getCustomer(patient);
            if (customer != null) {
                List<Contact> contacts = getContacts(customer);
                if (contacts != null && !contacts.isEmpty()) {
                    ReminderType reminderType = processor.getReminderType(reminder);
                    List<Integer> counts = getReminderCounts(reminderType, reminderCount);
                    if (!counts.isEmpty()) {
                        result = new ResendReminderDialog(reminder, contacts, counts, reminderCount,
                                                          processor, context, help);
                    } else {
                        ErrorHelper.show(Messages.get(ERROR_TITLE), Messages.format(
                                "patient.reminder.resend.notemplates",
                                reminderType.getName(), reminderCount));
                    }
                } else {
                    ErrorHelper.show(Messages.get(ERROR_TITLE), Messages.get("patient.reminder.resend.nocontacts"));
                }
            } else {
                ErrorHelper.show(Messages.get(ERROR_TITLE), Messages.get("patient.reminder.resend.nocustomer"));
            }

        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }

    /**
     * Returns the reminder configuration.
     *
     * @return the reminder configuration
     */
    protected static ReminderConfiguration getReminderConfig(Context context) {
        IMObjectBean bean = new IMObjectBean(context.getPractice());
        IMObject config = bean.getNodeTargetObject("reminderConfiguration");
        if (config == null) {
            throw new IllegalStateException("Patient reminders have not been configured");
        }
        return new ReminderConfiguration(config, ServiceHelper.getArchetypeService());
    }

    /**
     * Invoked when the OK button is pressed.
     * <p>
     * If a reminder count and contact have been selected, generates the reminder.
     */
    @Override
    protected void onOK() {
        Integer count = (Integer) countSelector.getSelectedItem();
        Contact contact = (Contact) contactSelector.getSelectedItem();
        if (count != null && contact != null) {
            generate(count, contact);
        }
    }

    /**
     * Returns the location, email and SMS contacts for a customer.
     *
     * @param customer the customer
     * @return a list of location and email contacts
     */
    private static List<Contact> getContacts(Party customer) {
        List<Contact> result = new ArrayList<>();
        for (org.openvpms.component.model.party.Contact c : customer.getContacts()) {
            Contact contact = (Contact) c;
            if (TypeHelper.isA(contact, ContactArchetypes.LOCATION, ContactArchetypes.EMAIL)) {
                result.add(contact);
            } else if (TypeHelper.isA(contact, ContactArchetypes.PHONE)) {
                IMObjectBean bean = new IMObjectBean(contact);
                if (bean.getBoolean("sms")) {
                    result.add(contact);
                }
            }
        }
        return result;
    }

    /**
     * Returns an ordered list of reminder counts up to but not including the current reminder count, that have an
     * associated document template.
     *
     * @param reminderType  the reminder type
     * @param reminderCount the current reminder count
     * @return the reminder counts
     */
    private static List<Integer> getReminderCounts(ReminderType reminderType, int reminderCount) {
        List<Integer> result = new ArrayList<>();
        for (ReminderCount count : reminderType.getReminderCounts()) {
            int value = count.getCount();
            if (value < reminderCount) {
                if (count.getTemplate() != null) {
                    result.add(value);
                }
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Generates a reminder.
     *
     * @param reminderCount the reminder count to use
     * @param contact       the contact to use
     */
    private void generate(final int reminderCount, Contact contact) {
        Act item = rules.getReminderItem(reminder, reminderCount, contact);
        if (item == null) {
            // no reminder item was created for the contact type. Create one now.
            item = processor.process(reminder, reminderCount, contact);
            // set the date to today, rather than the date when it should have been sent
            item.setActivityStartTime(new Date());
        }
        generate(item, contact);
    }

    /**
     * Generates a reminder for an item.
     *
     * @param item    the reminder item
     * @param contact the contact to use
     */
    private void generate(final Act item, Contact contact) {
        ReminderGeneratorFactory factory = ServiceHelper.getBean(ReminderGeneratorFactory.class);
        Party location = context.getLocation();
        Party practice = context.getPractice();
        ReminderGenerator generator = factory.create(item, reminder, contact, location, practice, getHelpContext());
        generator.setResend(true);
        generator.setListener(new BatchProcessorListener() {
            public void completed() {
                close();
            }

            public void error(Throwable exception) {
                ErrorHelper.show(exception);
            }
        });
        generator.process();
    }

}
