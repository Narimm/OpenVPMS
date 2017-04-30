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

package org.openvpms.archetype.rules.patient.reminder;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * A rule that determines how a reminder is processed.
 * <p>
 * A rule contains the following options, one or more of which may be selected:
 * <ul>
 * <li><strong>contact</strong> - use the customer's contact(s) with REMINDER purpose. Can have multiple</li>
 * <li><strong>email</strong> - use the customer's preferred email contact (or first if no preferred), and email the
 * reminder to the customer
 * <li><strong>SMS</strong> - use the customer's preferred phone contact (or first if no preferred) with Allow SMS
 * enabled, send the reminder via SMS</li>
 * <li><strong>print</strong> - use the customer's preferred location contact (or first location if no preferred),
 * print the reminder for posting to the customer</li>
 * <li><strong>export</strong> - use the customer's preferred location contact (or first location if no preferred), and
 * export the reminder to CSV</li>
 * <li><strong>list</strong> - list the reminder in the report</li>
 * </ul>
 * The <strong>sendTo</strong> field may be one of:
 * <ul>
 * <li><strong>ALL</strong> - send to all selected options.<br/>
 * If a customer doesn't have a contact, then the rule isn't satisfied.<br/>
 * E.g. if both <em>email</em>  and <em>SMS</em> is selected, the rule is only satisfied if the customer has both email
 * and SMS contacts.<br/>
 * This is the default.</li>
 * <li><strong>FIRST</strong> - send to the first matching contact.<br/>
 * E.g. if both Email and SMS is selected, the rule is satisfied if the customer has either contact.
 * The precedence is email &gt; SMS &gt; print &gt; export &gt; list</li>
 * <li><strong>ANY</strong>- send to any matching contact. <br/>
 * If a customer doesn't have a contact, then the rule isn't satisfied.<br/>
 * E.g. if both Email and SMS is selected, the rule is satisfied if the customer has either contact, and the reminder
 * will be sent to each one</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class ReminderRule {

    /**
     * Constraint that determines how many options must apply for the rule to be satisfied.
     */
    public enum SendTo {
        ALL, FIRST, ANY
    }

    /**
     * Determines if any reminder contact should be used.
     */
    private final boolean contact;

    /**
     * Determines if the reminder should be emailed.
     */
    private final boolean email;

    /**
     * Determines if the reminder should be SMSed.
     */
    private final boolean sms;

    /**
     * Determines if the reminder should be printed.
     */
    private final boolean print;

    /**
     * Determines if the reminder should be exported.
     */
    private final boolean export;

    /**
     * Determines if the reminder should be listed.
     */
    private final boolean list;

    /**
     * Determines how many rules must apply.
     */
    private final SendTo sendTo;

    /**
     * Constructs a {@link ReminderRule}.
     *
     * @param rule    the <em>entity.reminderRule</em>
     * @param service the archetype service
     */
    public ReminderRule(IMObject rule, IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(rule, service);
        contact = bean.getBoolean("contact");
        email = bean.getBoolean("email");
        sms = bean.getBoolean("sms");
        print = bean.getBoolean("print");
        list = bean.getBoolean("list");
        export = bean.getBoolean("export");

        String send = bean.getString("sendTo");
        if (SendTo.FIRST.name().equals(send)) {
            sendTo = SendTo.FIRST;
        } else if (SendTo.ALL.name().equals(send)) {
            sendTo = SendTo.ALL;
        } else {
            sendTo = SendTo.ANY;
        }
    }

    /**
     * Determines if any of the customer's contacts with REMINDER purpose can be used.
     *
     * @return {@code true} if any of the customer REMINDER contacts can be used
     */
    public boolean isContact() {
        return contact;
    }

    /**
     * Determines if the reminder can be sent by email.
     *
     * @return {@code true} if the reminder can be sent by email
     */
    public boolean isEmail() {
        return email;
    }

    /**
     * Determines if the reminder can be sent via SMS.
     *
     * @return {@code true} if the reminder can be sent via SMS
     */
    public boolean isSMS() {
        return sms;
    }

    /**
     * Determines if the reminder can be printed for postal mail.
     *
     * @return {@code true} if the reminder can be printed
     */
    public boolean isPrint() {
        return print;
    }

    /**
     * Determines if the reminder can be exported.
     *
     * @return {@code true} if the reminder can be exported
     */
    public boolean isExport() {
        return export;
    }

    /**
     * Determines if the reminder can be listed.
     *
     * @return {@code true} if the reminder can be listed
     */
    public boolean isList() {
        return list;
    }

    /**
     * Determines if this rule specifies to email.
     *
     * @return {@code true} if {@link #isEmail} or {@link #isContact()} is set
     */
    public boolean canEmail() {
        return isEmail() || isContact();
    }

    /**
     * Determines if this rule specifies to SMS.
     *
     * @return {@code true} if {@link #isSMS()} or {@link #isContact()} is set
     */
    public boolean canSMS() {
        return isSMS() || isContact();
    }

    /**
     * Determines if this rule specifies to print.
     *
     * @return {@code true} if {@link #isPrint} or {@link #isContact()} is set
     */
    public boolean canPrint() {
        return isPrint() || isContact();
    }

    /**
     * Returns the contraint that determines how many options must apply for the rule to be satisfied.
     *
     * @return the send-to constraint
     */
    public SendTo getSendTo() {
        return sendTo;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ReminderRule) {
            ReminderRule other = (ReminderRule) obj;
            return contact == other.contact && email == other.email && sms == other.sms && print == other.print
                   && export == other.export && list == other.list && sendTo == other.sendTo;
        }
        return super.equals(obj);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(contact).append(email).append(sms).append(print).append(export)
                .append(list).append(sendTo).toHashCode();
    }

    /**
     * Returns a string representation of the rule.
     *
     * @return a string representation of the rule
     */
    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        append(builder, "contact", contact);
        append(builder, "email", email);
        append(builder, "sms", sms);
        append(builder, "print", print);
        append(builder, "export", export);
        append(builder, "list", list);
        builder.append("sendTo", sendTo);
        return builder.toString();
    }

    /**
     * Helper to appends a field value if it is true.
     *
     * @param builder the builder
     * @param name    the field name
     * @param value   the field value
     */
    private void append(ToStringBuilder builder, String name, boolean value) {
        if (value) {
            builder.append(name, true);
        }
    }
}
