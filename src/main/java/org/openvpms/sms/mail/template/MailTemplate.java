/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.sms.mail.template;

import java.util.HashMap;
import java.util.Map;


/**
 * Template for email-to-SMS provider emails.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MailTemplate {

    /**
     * The country prefix.
     */
    private String country;

    /**
     * The trunk prefix to remove.
     */
    private String trunkPrefix;

    /**
     * The from address.
     */
    private String from;

    /**
     * The to address.
     */
    private String to;

    /**
     * The subject.
     */
    private String subject;

    /**
     * The text.
     */
    private String text;

    /**
     * Variables.
     */
    private Map<String, String> variables = new HashMap<String, String>();


    /**
     * Constructs a <tt>MailTemplate</tt>.
     */
    public MailTemplate() {

    }

    /**
     * Constructs a <tt>MailTemplate</tt>.
     *
     * @param country     the country prefix
     * @param trunkPrefix the trunk prefix
     * @param from        the 'from' address
     * @param to          the 'to' address
     * @param subject     the message subject
     * @param text        the message text
     */
    public MailTemplate(String country, String trunkPrefix, String from, String to, String subject, String text) {
        setCountry(country);
        setTrunkPrefix(trunkPrefix);
        setFrom(from);
        setTo(to);
        setSubject(subject);
        setText(text);
    }

    /**
     * Sets the country prefix.
     *
     * @param country the country prefix
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Returns the country prefix.
     *
     * @return the country prefix
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the trunk prefix.
     * <p/>
     * In Australia this is the '0' prior to the area code.
     * <p/>
     * If specified, this will be removed from the front of phone numbers, when the country prefix is provided.
     *
     * @param prefix the trunk prefix
     */
    public void setTrunkPrefix(String prefix) {
        trunkPrefix = prefix;
    }

    /**
     * Returns the trunk prefix.
     *
     * @return the trunk prefix
     */
    public String getTrunkPrefix() {
        return trunkPrefix;
    }

    /**
     * Sets the from address.
     *
     * @param from the from address
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Returns the 'from' address
     *
     * @return the 'from' address
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the 'to' address.
     *
     * @param to the 'to' address
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Returns the 'to' address.
     *
     * @return the 'to' address
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the subject.
     *
     * @param subject the subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Returns the email subject.
     *
     * @return the email subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the message text.
     *
     * @param text the message text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the email text.
     *
     * @return the email text
     */
    public String getText() {
        return text;
    }

    /**
     * Adds a variable.
     *
     * @param name  the variable name
     * @param value the variable value. May be <tt>null</tt>
     */
    public void addVariable(String name, String value) {
        variables.put(name, value);
    }

    /**
     * Returns the variables to use in the template.
     * <p/>
     * Each entry in the returned map is a variable name/value pair.
     *
     * @return the variables
     */
    public Map<String, String> getVariables() {
        return variables;
    }

}