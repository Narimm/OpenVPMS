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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Sends an email.
 *
 * @author Tim Anderson
 */
public interface Mailer {

    /**
     * Sets the from address.
     *
     * @param from the from address
     */
    void setFrom(String from);

    /**
     * Returns the from address.
     *
     * @return the from address
     */
    String getFrom();

    /**
     * Sets the from name.
     *
     * @param name the from name
     */
    void setFromName(String name);

    /**
     * Returns the from name.
     *
     * @return the from name
     */
    String getFromName();

    /**
     * Sets the to address.
     *
     * @param to the to address. May be {@code null}
     */
    void setTo(String to);

    /**
     * Returns the to address.
     *
     * @return the to address. May be {@code null}
     */
    String getTo();

    /**
     * Sets the CC address.
     *
     * @param cc the CC address. May be {@code null}
     */
    void setCc(String cc);

    /**
     * Returns the CC address.
     *
     * @return the CC address. May be {@code null}
     */
    String getCc();

    /**
     * Sets the BCC address.
     *
     * @param bcc the BCC address. May be {@code null}
     */
    void setBcc(String bcc);

    /**
     * Returns the BCC address.
     *
     * @return the BCC address. May be {@code null}
     */
    String getBcc();

    /**
     * Sets the subject.
     *
     * @param subject the subject
     */
    void setSubject(String subject);

    /**
     * Returns the subject.
     *
     * @return the subject
     */
    String getSubject();

    /**
     * Sets the body.
     *
     * @param body the body
     */
    void setBody(String body);

    /**
     * Returns the body.
     *
     * @return the body
     */
    String getBody();

    /**
     * Adds an attachment.
     *
     * @param document the document to attach
     */
    void addAttachment(Document document);

    /**
     * Sends the mail.
     *
     * @throws OpenVPMSException for any error
     */
    void send();

    /**
     * Sends the object to the specified email address.
     *
     * @param address the address to send to
     * @throws OpenVPMSException for any error
     */
    void send(String address);

}
