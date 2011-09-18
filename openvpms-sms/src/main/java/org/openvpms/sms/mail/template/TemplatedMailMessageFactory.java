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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.lang.StringUtils;
import org.openvpms.sms.SMSException;
import org.openvpms.sms.mail.MailMessageFactory;
import org.openvpms.sms.mail.MailMessage;
import org.openvpms.sms.i18n.SMSMessages;

import java.util.Map;


/**
 * A {@link MailMessageFactory} that generates email messages from an xpath email template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class TemplatedMailMessageFactory implements MailMessageFactory {

    /**
     * The template configuration.
     */
    private final MailTemplateConfig config;


    /**
     * Constructs a <tt>TemplatedMailMessageFactory</tt>.
     *
     * @param config the template source
     */
    public TemplatedMailMessageFactory(MailTemplateConfig config) {
        this.config = config;
    }

    /**
     * Creates a new email message to send to an SMS provider.
     *
     * @param phone the phone number to send the SMS to
     * @param text  the SMS text
     * @return a new email
     */
    public MailMessage createMessage(String phone, String text) {
        MailMessage result = new MailMessage();
        MailTemplate template = config.getTemplate();
        phone = getPhone(phone, template);

        JXPathContext context = JXPathContext.newContext(new Object());
        Variables variables = context.getVariables();
        variables.declareVariable("phone", phone);
        variables.declareVariable("message", text);
        for (Map.Entry<String, String> variable : template.getVariables().entrySet()) {
            variables.declareVariable(variable.getKey(), variable.getValue());
        }
        result.setFrom(template.getFrom());
        result.setTo(evaluate(template.getTo(), context));
        result.setSubject(evaluate(template.getSubject(), context));
        result.setText(evaluate(template.getText(), context));
        return result;
    }

    /**
     * Massages the phone number to:
     * <ul>
     * <li>remove any spaces
     * <li>remove any leading +
     * <li>add a country prefix if specified but not present in the number. Any trunk prefix will be removed
     * first
     * </ul>
     *
     * @param phone    the phone number
     * @param template the template
     * @return the updated number
     */
    protected String getPhone(String phone, MailTemplate template) {
        phone = StringUtils.remove(phone, ' ');
        if (phone.startsWith("+")) {
            phone = phone.substring(1);
        } else {
            String prefix = template.getCountry();
            if (prefix != null && !phone.startsWith(prefix)) {
                String trunkPrefix = template.getTrunkPrefix();
                if (trunkPrefix != null && phone.startsWith(trunkPrefix)) {
                    // strip off the trunk prefix before adding the country prefix
                    phone = phone.substring(trunkPrefix.length());
                }
                phone = prefix + phone;
            }
        }
        return phone;
    }


    /**
     * Evaluates an expression.
     *
     * @param context    the jxpath context
     * @param expression the xpath expression. May be <tt>null</tt>
     * @return the value of the expression
     */
    private String evaluate(String expression, JXPathContext context) {
        String result = null;
        if (!StringUtils.isEmpty(expression)) {
            try {
                Object value = context.getValue(expression, Object.class);
                result = value.toString();
            } catch (Throwable exception) {
                throw new SMSException(SMSMessages.failedToEvaluateExpression(expression), exception);
            }
        }
        return result;
    }

}