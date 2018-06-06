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

package org.openvpms.archetype.rules.patient.reminder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.Contacts;
import org.openvpms.archetype.rules.party.PurposeMatcher;
import org.openvpms.archetype.rules.party.SMSMatcher;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.party.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openvpms.archetype.rules.party.ContactArchetypes.EMAIL;
import static org.openvpms.archetype.rules.party.ContactArchetypes.LOCATION;
import static org.openvpms.archetype.rules.party.ContactArchetypes.PHONE;
import static org.openvpms.archetype.rules.party.ContactArchetypes.REMINDER_PURPOSE;
import static org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException.ErrorCode.NoContactsForRules;
import static org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException.ErrorCode.NoPatient;
import static org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException.ErrorCode.NoReminderCount;
import static org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException.ErrorCode.NoReminderType;


/**
 * Reminder processor.
 *
 * @author Tim Anderson
 */
public class ReminderProcessor {

    /**
     * The date to use to determine if reminders should be cancelled.
     */
    private final Date processingDate;

    /**
     * The reminder configuration.
     */
    private final ReminderConfiguration config;

    /**
     * If {@code true}, ignore any reminder templates with {@code sms = true}
     */
    private final boolean disableSMS;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Patient rules.
     */
    private final PatientRules patientRules;

    /**
     * Reminder type cache.
     */
    private final ReminderTypes reminderTypes;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReminderProcessor.class);

    /**
     * Constructs a {@link ReminderProcessor}.
     *
     * @param date       the date to use to determine if reminders should be cancelled. Any reminder with a
     *                   due date + cancel interval <= this will be cancelled
     * @param config     the reminder configuration
     * @param disableSMS if {@code true}, ignore any reminder templates with {@code sms = true}
     * @param service    the archetype service
     */
    public ReminderProcessor(Date date, ReminderConfiguration config, boolean disableSMS, IArchetypeService service,
                             PatientRules patientRules) {
        this.processingDate = date;
        this.config = config;
        this.disableSMS = disableSMS;
        this.service = service;
        this.patientRules = patientRules;
        reminderTypes = new ReminderTypes(service);
    }

    /**
     * Process a reminder.
     *
     * @param reminder the reminder to process
     * @return the acts to save
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    public List<Act> process(Act reminder) {
        ActBean bean = new ActBean(reminder, service);
        int reminderCount = bean.getInt("reminderCount");
        return process(reminder, reminderCount, bean, false);
    }

    /**
     * Process a reminder for a particular reminder count.
     *
     * @param reminder      the reminder
     * @param reminderCount the reminder count
     * @return the acts to save
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    public List<Act> process(Act reminder, int reminderCount) {
        ActBean bean = new ActBean(reminder, service);
        return process(reminder, reminderCount, bean, true);
    }

    /**
     * Process a reminder for a particular reminder count and contact.
     *
     * @param reminder      the reminder
     * @param reminderCount the reminder count
     * @param contact       the contact
     * @return the reminder item, linked to the reminder. The caller is responsible for saving this
     */
    public Act process(Act reminder, int reminderCount, Contact contact) {
        ActBean bean = new ActBean(reminder, service);
        ReminderType reminderType = getReminderType(bean);
        ReminderCount count = reminderType.getReminderCount(reminderCount);
        if (count == null) {
            throw new ReminderProcessorException(NoReminderCount, reminderType.getName(), reminderCount);
        }
        ArrayList<Act> toSave = new ArrayList<>();
        Date dueDate = reminder.getActivityStartTime();
        Set<Contact> contacts = Collections.singleton(contact);
        if (TypeHelper.isA(contact, ContactArchetypes.EMAIL)) {
            if (!generateEmail(bean, dueDate, contacts, reminderCount, toSave)) {
                throw new IllegalStateException("Failed to generate email");
            }
        } else if (TypeHelper.isA(contact, ContactArchetypes.PHONE)) {
            if (disableSMS) {
                throw new IllegalStateException("Cannot process phone contacts. SMS is disabled");
            }
            IMObjectBean contactBean = new IMObjectBean(contact, service);
            if (!contactBean.getBoolean("sms")) {
                throw new IllegalArgumentException("Cannot use contact to SMS");
            }
            if (!generateSMS(bean, dueDate, contacts, reminderCount, toSave)) {
                throw new IllegalStateException("Failed to generate SMS");
            }
        } else if (TypeHelper.isA(contact, ContactArchetypes.LOCATION)) {
            if (!generatePrint(bean, dueDate, contacts, reminderCount, toSave)) {
                throw new IllegalStateException("Failed to generate print");
            }
        } else {
            throw new IllegalArgumentException("Invalid archetype for contact: " + contact.getArchetype());
        }
        if (toSave.size() != 1) {
            throw new IllegalStateException("Failed to generate reminder item");
        }
        return toSave.get(0);
    }

    /**
     * Returns the patient associated with a reminder.
     *
     * @param reminder the reminder
     * @return the patient, or {@code null} if none is found
     * @throws ReminderProcessorException if the patient cannot be found
     */
    public Party getPatient(Act reminder) {
        return getPatient(new ActBean(reminder, service));
    }

    /**
     * Returns the customer associated with a patient.
     *
     * @param patient the patient
     * @return the corresponding customer, or {@code null} if it cannot be found
     */
    public Party getCustomer(Party patient) {
        return patientRules.getOwner(patient);
    }

    /**
     * Returns the reminder type associated with a reminder.
     *
     * @param reminder the reminder
     * @return the reminder type
     * @throws ReminderProcessorException if the reminder type cannot be found
     */
    public ReminderType getReminderType(Act reminder) {
        return getReminderType(new ActBean(reminder, service));
    }

    protected Date now() {
        return new Date();
    }

    /**
     * Generates reminder items for a reminder based on the reminder count rules and available customer contacts.
     *
     * @param patient      the patient
     * @param reminder     the reminder
     * @param count        the reminder count
     * @param reminderType the reminder type
     * @return the acts to save
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    protected List<Act> generate(Party patient, Act reminder, ReminderCount count, ReminderType reminderType) {
        Party customer = getCustomer(patient);
        List<Contact> list = customer != null ? Contacts.sort(customer.getContacts())
                                              : Collections.<Contact>emptyList();
        Set<Contact> found = null;
        DocumentTemplate template = count.getTemplate();
        ReminderRule matchingRule = null;
        for (ReminderRule rule : count.getRules()) {
            Set<Contact> matches = new HashSet<>();
            if (getContacts(rule, list, matches, template, count, reminderType)) {
                found = matches;
                matchingRule = rule;
                break;
            }
        }
        ActBean bean = new ActBean(reminder, service);
        List<Act> toSave = new ArrayList<>();
        Date dueDate = reminder.getActivityStartTime();
        if (matchingRule != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found matching rule for customer=" + toString(customer) + ", patient=" + toString(patient)
                          + ", reminderType=" + reminderType.getName() + ", count=" + count.getCount() + ", rule="
                          + matchingRule + ", contacts=" + toString(found));
            }
            ReminderRule.SendTo sendTo = matchingRule.getSendTo();
            if (sendTo == ReminderRule.SendTo.ALL || sendTo == ReminderRule.SendTo.ANY) {
                if (matchingRule.canEmail()) {
                    generateEmail(bean, dueDate, found, count.getCount(), toSave);
                }
                if (!disableSMS && matchingRule.canSMS()) {
                    generateSMS(bean, dueDate, found, count.getCount(), toSave);
                }
                if (matchingRule.canPrint()) {
                    generatePrint(bean, dueDate, found, count.getCount(), toSave);
                }
            } else {
                boolean generated = matchingRule.canEmail() && generateEmail(bean, dueDate, found, count.getCount(),
                                                                             toSave);
                if (!generated) {
                    generated = !disableSMS && matchingRule.canSMS() && generateSMS(bean, dueDate, found,
                                                                                    count.getCount(), toSave);
                    if (!generated && matchingRule.canPrint()) {
                        generatePrint(bean, dueDate, found, count.getCount(), toSave);
                    }
                }
            }
            if (matchingRule.isExport()) {
                generateExport(bean, dueDate, found, count.getCount(), toSave);
            }
            if (matchingRule.isList()) {
                generateList(bean, dueDate, count.getCount(), null, toSave);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("NO matching rule for customer=" + toString(customer) + ", patient=" + toString(patient)
                          + ", reminderType=" + reminderType.getName() + ", count=" + count.getCount()
                          + ". Reminder will be Listed");
            }
            String message = new ReminderProcessorException(NoContactsForRules).getMessage();
            generateList(bean, dueDate, count.getCount(), message, toSave);
        }
        toSave.add(reminder);
        return toSave;
    }

    /**
     * Process a reminder for a particular reminder count.
     *
     * @param reminder      the reminder to process
     * @param reminderCount the reminder count
     * @param bean          contains the reminder
     * @param ignoreDueDate if {@code true}, ignore the reminder due date
     * @return the acts to save
     * @throws ArchetypeServiceException  for any archetype service error
     * @throws ReminderProcessorException if the reminder cannot be processed
     */
    private List<Act> process(Act reminder, int reminderCount, ActBean bean, boolean ignoreDueDate) {
        List<Act> result;
        ReminderType reminderType = getReminderType(bean);

        Date dueDate = reminder.getActivityStartTime();
        if (!ignoreDueDate && reminderType.shouldCancel(dueDate, processingDate)) {
            if (log.isDebugEnabled()) {
                log.debug("Cancelling reminder=" + reminder.getId() + ", patient=" + getPatientId(bean)
                          + ", reminderType=" + toString(reminderType) + ", firstDueDate=" + toString(dueDate)
                          + ", processingDate=" + toString(processingDate)
                          + ", cancelDate=" + toString(reminderType.getCancelDate(dueDate))
                          + ": firstDueDate <= cancelDate");
            }
            result = cancel(reminder);
        } else {
            Party patient = getPatient(bean);
            if (!patient.isActive() || patientRules.isDeceased(patient)) {
                if (log.isDebugEnabled()) {
                    if (!patient.isActive()) {
                        log.debug("Cancelling reminder=" + reminder.getId() + ", patient=" + toString(patient)
                                  + ", reminderType=" + toString(reminderType) + ": patient is inactive");
                    } else {
                        log.debug("Cancelling reminder=" + reminder.getId()
                                  + ", patient=" + toString(patient) + ", reminderType=" + toString(reminderType)
                                  + ": patient is deceased");
                    }
                }
                result = cancel(reminder);
            } else {
                ReminderCount count = reminderType.getReminderCount(reminderCount);
                if (count != null) {
                    result = generate(patient, reminder, count, reminderType);
                } else if (reminderCount == 0) {
                    // no reminder count, so list the reminder
                    result = new ArrayList<>();
                    String error = new ReminderProcessorException(NoReminderCount, reminderType.getName(),
                                                                  reminderCount).getMessage();
                    generateList(new ActBean(reminder, service), dueDate, reminderCount, error, result);
                } else {
                    // a reminderCount > 0 with no ReminderCount is valid - just skip the reminder
                    result = Collections.emptyList();
                    if (log.isDebugEnabled()) {
                        log.debug("Skipping reminder=" + reminder.getId() + ", patient=" + toString(patient)
                                  + ", reminderType=" + toString(reminderType)
                                  + ": no reminder count=" + reminderCount);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the patient associated with a reminder.
     *
     * @param bean the reminder
     * @return the patient
     */
    private Party getPatient(ActBean bean) {
        Party patient = (Party) bean.getNodeParticipant("patient");
        if (patient == null) {
            throw new ReminderProcessorException(NoPatient);
        }
        return patient;
    }

    /**
     * Generates an email reminder item, if there is an email contact.
     *
     * @param reminder the reminder
     * @param dueDate  the reminder due date
     * @param contacts the contacts
     * @param count    the reminder count
     * @param toSave   the list of acts to save
     * @return {@code true}, if an item was generated
     */
    private boolean generateEmail(ActBean reminder, Date dueDate, Set<Contact> contacts, int count, List<Act> toSave) {
        boolean result = false;
        if (hasContact(ContactArchetypes.EMAIL, contacts)) {
            createItem(ReminderArchetypes.EMAIL_REMINDER, config.getEmailSendDate(dueDate), reminder, count,
                       null, toSave);
            result = true;
        } else if (log.isDebugEnabled()) {
            log.debug("NOT generating email reminder for reminder=" + reminder.getAct().getId()
                      + ", patient=" + getPatientId(reminder) + ": customer has no email contact");
        }
        return result;
    }

    /**
     * Generates an SNS reminder item, if there is an SMS contact.
     *
     * @param reminder the reminder
     * @param dueDate  the reminder due date
     * @param contacts the contacts
     * @param count    the reminder count
     * @param toSave   the list of acts to save
     * @return {@code true}, if an item was generated
     */
    private boolean generateSMS(ActBean reminder, Date dueDate, Set<Contact> contacts, int count, List<Act> toSave) {
        boolean result = false;
        if (hasContact(ContactArchetypes.PHONE, contacts)) {
            createItem(ReminderArchetypes.SMS_REMINDER, config.getSMSSendDate(dueDate), reminder, count, null,
                       toSave);
            result = true;
        } else if (log.isDebugEnabled()) {
            log.debug("NOT generating SMS reminder for reminder=" + reminder.getAct().getId()
                      + ", patient=" + getPatientId(reminder) + ": customer has no SMS contact");
        }
        return result;
    }

    /**
     * Generates a print reminder item, if there is an location contact.
     *
     * @param reminder the reminder
     * @param dueDate  the reminder due date
     * @param contacts the contacts
     * @param count    the reminder count
     * @param toSave   the list of acts to save
     * @return {@code true}, if an item was generated
     */
    private boolean generatePrint(ActBean reminder, Date dueDate, Set<Contact> contacts, int count, List<Act> toSave) {
        boolean result = false;
        if (hasContact(ContactArchetypes.LOCATION, contacts)) {
            createItem(ReminderArchetypes.PRINT_REMINDER, config.getPrintSendDate(dueDate), reminder, count,
                       null, toSave);
            result = true;
        } else if (log.isDebugEnabled()) {
            log.debug("NOT generating print reminder for reminder=" + reminder.getAct().getId()
                      + ", patient=" + getPatientId(reminder) + ": customer has no location contact");
        }
        return result;
    }

    /**
     * Generates an export reminder item, if there is an location contact.
     *
     * @param reminder the reminder
     * @param dueDate  the reminder due date
     * @param contacts the contacts
     * @param count    the reminder count
     * @param toSave   the list of acts to save
     * @return {@code true}, if an item was generated
     */
    private boolean generateExport(ActBean reminder, Date dueDate, Set<Contact> contacts, int count, List<Act> toSave) {
        boolean result = false;
        if (hasContact(ContactArchetypes.LOCATION, contacts)) {
            createItem(ReminderArchetypes.EXPORT_REMINDER, config.getExportSendDate(dueDate), reminder, count,
                       null, toSave);
            result = true;
        } else if (log.isDebugEnabled()) {
            log.debug("NOT generating export reminder for reminder=" + reminder.getAct().getId()
                      + ", patient=" + getPatientId(reminder) + ": customer has no location contact");
        }
        return result;
    }

    /**
     * Generates a list reminder item.
     *
     * @param reminder the reminder
     * @param dueDate  the reminder due date
     * @param count    the reminder count
     * @param toSave   the list of acts to save
     */
    private Act generateList(ActBean reminder, Date dueDate, int count, String error, List<Act> toSave) {
        return createItem(ReminderArchetypes.LIST_REMINDER, config.getListSendDate(dueDate), reminder, count, error,
                          toSave);
    }

    /**
     * Determines if a contact type exists.
     *
     * @param shortName the contact archetype short name
     * @param contacts  the available contacts
     * @return {@code true} if the contact exists
     */
    private boolean hasContact(String shortName, Set<Contact> contacts) {
        for (Contact contact : contacts) {
            if (TypeHelper.isA(contact, shortName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a reminder item.
     *
     * @param shortName the reminder item archetype
     * @param startTime the start time (aka send from date)
     * @param reminder  the parent reminder
     * @param count     the reminder count
     * @param error     the error message. May be {@code null}
     * @param toSave    the list to add the changed acts to
     * @return the new reminder item
     */
    private Act createItem(String shortName, Date startTime, ActBean reminder, int count, String error,
                           List<Act> toSave) {
        Act act = (Act) service.create(shortName);
        act.setActivityStartTime(DateRules.getDate(startTime));  // set the send date
        act.setActivityEndTime(reminder.getDate("endTime"));     // set the due date to that of the reminder
        ActBean bean = new ActBean(act, service);
        bean.setValue("count", count);
        if (error != null) {
            bean.setValue("error", error);
            act.setStatus(ReminderItemStatus.ERROR);
        } else {
            act.setStatus(ReminderItemStatus.PENDING);
        }

        reminder.addNodeRelationship("items", act);
        toSave.add(act);
        return act;
    }

    /**
     * Adds contacts matching the rule.
     *
     * @param rule         the rule
     * @param contacts     the available contacts
     * @param matches      the matches to add to
     * @param template     the document template
     * @param count        the reminder count
     * @param reminderType the reminder type
     * @return {@code true} if the rule matched, otherwise {@code false}
     */
    private boolean getContacts(ReminderRule rule, List<Contact> contacts, Set<Contact> matches,
                                DocumentTemplate template, ReminderCount count, ReminderType reminderType) {
        ReminderRule.SendTo sendTo = rule.getSendTo();
        boolean isAll = sendTo == ReminderRule.SendTo.ALL;
        if (!contacts.isEmpty()) {
            if (rule.isContact()) {
                if (!addReminderContacts(contacts, matches, template, count, reminderType) && isAll) {
                    if (log.isDebugEnabled()) {
                        log.debug("Rule not matched. There are no REMINDER contacts for reminderType="
                                  + toString(reminderType) + ", count=" + count.getCount()
                                  + ", rule=" + rule);
                    }
                    return false;
                }
            }
            if (rule.isEmail()) {
                if (!addEmailContact(contacts, matches, template, count, reminderType) && isAll) {
                    if (log.isDebugEnabled()) {
                        log.debug("Rule not matched. There are no email contacts for reminderType="
                                  + toString(reminderType) + ", count=" + count.getCount() + ", rule="
                                  + rule);
                    }
                    return false;
                }
            }
            if (rule.isSMS()) {
                if (isAll && disableSMS) {
                    if (log.isDebugEnabled()) {
                        log.debug("Rule not matched. SMS has been disabled for reminderType="
                                  + toString(reminderType) + ", count=" + count.getCount()
                                  + ", rule=" + rule);
                    }
                    return false;
                }
                if (!addSMSContact(contacts, matches, template, count, reminderType) && isAll) {
                    if (log.isDebugEnabled()) {
                        log.debug("Rule not matched. There are no SMS contacts for reminderType="
                                  + toString(reminderType) + ", count=" + count.getCount()
                                  + ", rule=" + rule);
                    }
                    return false;
                }
            }
            if (rule.isPrint()) {
                if (!addLocationContact(contacts, matches, template, count, reminderType) && isAll) {
                    if (log.isDebugEnabled()) {
                        log.debug("Rule not matched. There are no location contacts for reminderType="
                                  + toString(reminderType) + ", count=" + count.getCount()
                                  + ", rule=" + rule);
                    }
                    return false;
                }
            }
        }
        if (rule.isExport()) {
            addContact(LOCATION, contacts, matches);
        } else if (rule.isList()) {
            addContact(PHONE, contacts, matches);
        }

        boolean result = !matches.isEmpty();
        if (!result && log.isDebugEnabled()) {
            log.debug("Rule not matched. No contacts match reminderType="
                      + toString(reminderType) + ", count=" + count.getCount() + ", rule=" + rule);
        }
        return result;
    }

    /**
     * Returns the reminder type associated with a reminder.
     *
     * @param bean the reminder
     * @return the reminder type
     * @throws ReminderProcessorException if the reminder type cannot be found
     */
    private ReminderType getReminderType(ActBean bean) {
        IMObjectReference ref = bean.getNodeParticipantRef("reminderType");
        ReminderType reminderType = reminderTypes.get(ref);
        if (reminderType == null) {
            throw new ReminderProcessorException(NoReminderType);
        }
        return reminderType;
    }

    /**
     * Cancels a reminder.
     *
     * @param reminder the reminder to cancel
     * @return the lists of acts requiring saving
     */
    private List<Act> cancel(Act reminder) {
        reminder.setStatus(ActStatus.CANCELLED);
        List<Act> toSave = new ArrayList<>();
        toSave.add(reminder);
        ActBean bean = new ActBean(reminder, service);
        for (Act item : bean.getNodeActs("items")) {
            if (!ActStatus.COMPLETED.equals(item.getStatus())) {
                item.setStatus(ActStatus.CANCELLED);
                toSave.add(item);
            }
        }
        return toSave;
    }

    /**
     * Adds contacts with <em>REMINDER</em> purpose.
     *
     * @param contacts     the customer's contacts
     * @param matches      the matches to add to
     * @param template     the reminder template. If {@code null}, no contacts will be added
     * @param count        the reminder count
     * @param reminderType the reminder type
     * @return {@code true} if any contacts were added
     */
    private boolean addReminderContacts(List<Contact> contacts, Set<Contact> matches, DocumentTemplate template,
                                        ReminderCount count, ReminderType reminderType) {
        int size = matches.size();
        if (template != null) {
            if (!disableSMS && template.getSMSTemplate() != null) {
                matches.addAll(Contacts.findAll(contacts, new SMSMatcher(REMINDER_PURPOSE, true, service)));
            } else {
                if (log.isDebugEnabled()) {
                    if (disableSMS) {
                        log.debug("NOT adding SMS contacts for reminderType=" + toString(reminderType)
                                  + ", reminderCount=" + count.getCount() + ". SMS is disabled");
                    } else {
                        log.debug("NOT adding SMS contacts for reminderType=" + toString(reminderType)
                                  + ", reminderCount=" + count.getCount() + ". Template=" + template.getName()
                                  + " has no SMS template");
                    }
                }
            }
            matches.addAll(Contacts.findAll(contacts, new PurposeMatcher(LOCATION, REMINDER_PURPOSE, true, service)));
            if (template.getEmailTemplate() != null) {
                matches.addAll(Contacts.findAll(contacts, new PurposeMatcher(EMAIL, REMINDER_PURPOSE, true, service)));
            } else {
                log.debug("NOT adding email contacts for reminderType=" + toString(reminderType)
                          + ", reminderCount=" + count.getCount() + ". Template=" + template.getName()
                          + " has no email template");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("NOT adding REMINDER contacts for reminderType=" + toString(reminderType)
                          + ", reminderCount=" + count.getCount() + ". ReminderCount has no template");
            }
        }
        return size != matches.size();
    }

    /**
     * Adds the customer's email contact, if any.
     *
     * @param contacts     the customer's contacts
     * @param matches      the matches to add to
     * @param template     the reminder template. If {@code null} no contact will be added
     * @param count        the reminder count
     * @param reminderType the reminder type
     * @return {@code true} if any contacts were added
     */
    private boolean addEmailContact(List<Contact> contacts, Set<Contact> matches, DocumentTemplate template,
                                    ReminderCount count, ReminderType reminderType) {
        boolean result = false;
        if (template != null) {
            if (template.getEmailTemplate() != null) {
                result = addContact(ContactArchetypes.EMAIL, contacts, matches);
            } else if (log.isDebugEnabled()) {
                log.debug("NOT adding email contacts for reminderType=" + toString(reminderType)
                          + ", reminderCount=" + count.getCount() + ". Template=" + template.getName()
                          + " has no email template");
            }
        } else if (log.isDebugEnabled()) {
            log.debug("NOT adding email contacts for reminderType=" + toString(reminderType)
                      + ", reminderCount=" + count.getCount() + ". ReminderCount has no template");

        }
        return result;
    }

    /**
     * Adds the customer's SMS contact, if any.
     *
     * @param contacts     the customer's contacts
     * @param matches      the matches to add to
     * @param template     the reminder template. If {@code null} no contact will be added
     * @param count        the reminder count
     * @param reminderType the reminder type
     * @return {@code true} if any contacts were added
     */
    private boolean addSMSContact(List<Contact> contacts, Set<Contact> matches, DocumentTemplate template,
                                  ReminderCount count, ReminderType reminderType) {
        boolean result = false;
        if (template != null) {
            if (template.getSMSTemplate() != null) {
                result = addContact(contacts, matches, new SMSMatcher(REMINDER_PURPOSE, false, service));
            } else if (log.isDebugEnabled()) {
                log.debug("NOT adding SMS contacts for reminderType=" + toString(reminderType)
                          + ", reminderCount=" + count.getCount() + ". Template=" + template.getName()
                          + " has no SMS template");
            }
        } else if (log.isDebugEnabled()) {
            log.debug("NOT adding SMS contacts for reminderType=" + toString(reminderType)
                      + ", reminderCount=" + count.getCount() + ". ReminderCount has no template");
        }
        return result;
    }

    /**
     * Adds the customer's location contact, if any.
     *
     * @param contacts     the customer's contacts
     * @param matches      the matches to add to
     * @param template     the reminder template. If {@code null} no contact will be added
     * @param count        the reminder count
     * @param reminderType the reminder type
     * @return {@code true} if any contacts were added
     */
    private boolean addLocationContact(List<Contact> contacts, Set<Contact> matches, DocumentTemplate template,
                                       ReminderCount count, ReminderType reminderType) {
        boolean result = false;
        if (template != null) {
            result = addContact(LOCATION, contacts, matches);
        } else if (log.isDebugEnabled()) {
            log.debug("NOT adding location contacts for reminderType=" + toString(reminderType)
                      + ", reminderCount=" + count.getCount() + ". ReminderCount has no template");
        }
        return result;
    }

    /**
     * Adds a contact of the specified type if one is present.
     *
     * @param shortName the contact archetype short name
     * @param contacts  the available contacts
     * @param matches   the matches to add to
     * @return {@code true} if a contact was matched, otherwise {@code false}
     */
    private boolean addContact(String shortName, List<Contact> contacts, Set<Contact> matches) {
        PurposeMatcher matcher = new PurposeMatcher(shortName, REMINDER_PURPOSE, false, service);
        return addContact(contacts, matches, matcher);
    }

    /**
     * Adds a contact matching the criteria, if one is present.
     *
     * @param contacts the available contacts
     * @param matches  the matches to add to
     * @param matcher  the criteria
     * @return {@code true} if a contact was matched, otherwise {@code false}
     */
    private boolean addContact(List<Contact> contacts, Set<Contact> matches, PurposeMatcher matcher) {
        Contact contact = Contacts.find(contacts, matcher);
        if (contact != null) {
            matches.add(contact);
            return true;
        }
        return false;
    }

    /**
     * Helper to generate a debug string for a reminder type.
     *
     * @param reminderType the reminder type. May be {@code null}
     * @return a string, or {@code null} if the reminder type is null
     */
    private String toString(ReminderType reminderType) {
        return (reminderType != null) ? toString(reminderType.getEntity()) : null;
    }

    /**
     * Helper to generate a debug string for an entity.
     *
     * @param entity the party. May be {@code null}
     * @return a string, or {@code null} if the party is null
     */
    private String toString(Entity entity) {
        return (entity != null) ? entity.getName() + " (" + entity.getId() + ")" : null;
    }

    /**
     * Helper to generate a debug string for a date.
     *
     * @param date the date
     * @return a string
     */
    private String toString(Date date) {
        return new java.sql.Timestamp(date.getTime()).toString();
    }

    /**
     * Helper to generate a debug string for a patient associated with a reminder.
     *
     * @param reminder the reminder
     * @return a string, or {@code null} if the patient is null
     */
    private String getPatientId(ActBean reminder) {
        IMObjectReference patient = reminder.getNodeParticipantRef("patient");
        return patient != null ? Long.toString(patient.getId()) : null;
    }

    /**
     * Helper to generate a debug string for a set of contacts.
     *
     * @param contacts the contacts
     * @return a string
     */
    private String toString(Set<Contact> contacts) {
        StringBuilder result = new StringBuilder();
        for (Contact contact : contacts) {
            if (result.length() != 0) {
                result.append(", ");
            }
            result.append(contact.getDescription());
        }
        return result.toString();
    }
}
