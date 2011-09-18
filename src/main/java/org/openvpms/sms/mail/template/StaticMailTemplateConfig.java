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

/**
 * Implementation of {@link MailTemplateConfig} that returns the same {@link MailTemplate}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class StaticMailTemplateConfig implements MailTemplateConfig {

    /**
     * The template.
     */
    private final MailTemplate template;

    /**
     * Constructs a <tt>StaticMailTemplateConfig</tt> that returns the supplied template each time.
     *
     * @param template the template
     */
    public StaticMailTemplateConfig(MailTemplate template) {
        this.template = template;
    }

    /**
     * Constructs a <tt>StaticMailTemplateConfig</tt> that returns a template with the supplied details each time.
     *
     * @param country the country prefix
     * @param trunk   the trunk prefix
     * @param from    the 'from' address
     * @param to      the 'to' address
     * @param subject the message subject
     * @param text    the message text
     */
    public StaticMailTemplateConfig(String country, String trunkPrefix, String from, String to, String subject,
                                    String text) {
        this(new MailTemplate(country, trunkPrefix, from, to, subject, text));
    }

    /**
     * Returns the email template.
     *
     * @return the email template
     */
    public MailTemplate getTemplate() {
        return template;
    }
}
