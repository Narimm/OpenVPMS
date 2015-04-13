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

package org.openvpms.sms.mail;

import org.openvpms.sms.Connection;
import org.openvpms.sms.ConnectionFactory;
import org.springframework.mail.javamail.JavaMailSender;


/**
 * Factory for {@link MailConnection} connections.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MailConnectionFactory implements ConnectionFactory {

    /**
     * The mail sender.
     */
    private final JavaMailSender sender;

    /**
     * The message factory.
     */
    private final MailMessageFactory factory;

    /**
     * Constructs a <tt>MailConnectionFactory</tt>.
     *
     * @param sender  the mail sender
     * @param factory the message factory
     */
    public MailConnectionFactory(JavaMailSender sender, MailMessageFactory factory) {
        this.sender = sender;
        this.factory = factory;
    }

    /**
     * Creates a new connection.
     *
     * @return a new connection
     */
    public Connection createConnection() {
        return new MailConnection(sender, factory);
    }
}
