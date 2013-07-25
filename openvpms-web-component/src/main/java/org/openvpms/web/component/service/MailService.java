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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.service;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


/**
 * Mail service that configures the SMTP details from <em>party.organisationLocation</em> from
 * {@link Context#getLocation()}, if available.
 * <p/>
 * Note that instances of this must be per-session. It obtains the context from
 * {@code ContextApplicationInstance#getInstance().getContext()}. A user may have multiple application instances,
 * each with a different location selected, so mail sending is synchronized.
 *
 * @author Tim Anderson
 */
public class MailService extends JavaMailSenderImpl {

    /**
     * Property name for STARTTLS flag.
     */
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

    /**
     * Property name for authentication flag.
     */
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";


    /**
     * Return the mail server host.
     *
     * @return the mail server host
     */
    @Override
    public String getHost() {
        String host = getString("mailHost");
        return (host != null) ? host : super.getHost();
    }

    /**
     * Return the mail server port.
     */
    @Override
    public int getPort() {
        IMObjectBean bean = getLocationBean();
        int port = 0;
        if (bean != null) {
            port = bean.getInt("mailPort");
        }
        if (port == 0) {
            port = super.getPort();
        }
        return port;
    }

    /**
     * Return the username for the account at the mail host.
     */
    @Override
    public String getUsername() {
        String username = getString("mailUsername");
        return (username != null) ? username : super.getUsername();
    }

    /**
     * Return the password for the account at the mail host.
     */
    @Override
    public String getPassword() {
        String password = getString("mailPassword");
        return (password != null) ? password : super.getPassword();
    }

    /**
     * Return the mail protocol.
     */
    @Override
    public String getProtocol() {
        String security = getConnectionSecurity();
        if ("SSL_TLS".equals(security)) {
            return "smtps";
        }
        return super.getProtocol();
    }

    /**
     * Return the JavaMail <code>Session</code>,
     * lazily initializing it if hasn't been specified explicitly.
     */
    @Override
    public synchronized Session getSession() {
        String protocol = getConnectionSecurity();
        String username = getUsername();

        setProperty(MAIL_SMTP_AUTH, !StringUtils.isEmpty(username));
        setProperty(MAIL_SMTP_STARTTLS_ENABLE, "STARTTLS".equals(protocol));
        return super.getSession();
    }

    /**
     * Actually send the given array of MimeMessages via JavaMail.
     *
     * @param mimeMessages     MimeMessage objects to send
     * @param originalMessages corresponding original message objects
     *                         that the MimeMessages have been created from (with same array
     *                         length and indices as the "mimeMessages" array), if any
     * @throws MailAuthenticationException in case of authentication failure
     * @throws MailSendException           in case of failure when sending a message
     */
    @Override
    protected synchronized void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) throws MailException {
        super.doSend(mimeMessages, originalMessages);
    }

    /**
     * Returns the <em>party.organisationLocation</em> wrapped in a bean, if one is present in the global context.
     *
     * @return the location, or <tt>null</tt> if none is present.
     */
    private IMObjectBean getLocationBean() {
        // need to use the context associated with the current instance. Be nice if the context could be injected
        // by Spring, but the context is scoped to the application instance, not the session, as there may be
        // multiple application instances per session.
        Party location = null;
        ContextApplicationInstance instance = ContextApplicationInstance.getInstance();
        if (instance != null) {
            Context context = instance.getContext();
            location = context.getLocation();
        }
        return (location != null) ? new IMObjectBean(location) : null;
    }

    /**
     * Helper to return a string node value from an <em>party.organisationLocation</em>, if there is a location in
     * the global context.
     *
     * @param name the node name
     * @return the corresponding value. May be <tt>null</tt>
     */
    private String getString(String name) {
        IMObjectBean bean = getLocationBean();
        return (bean != null) ? StringUtils.trimToNull(bean.getString(name)) : null;
    }

    /**
     * Returns the mail connection security value.
     *
     * @return the mail connection security. May be {@code null}
     */
    private String getConnectionSecurity() {
        return getString("mailSecurity");
    }

    /**
     * Helper to set a configuration property.
     * This only sets the property if it is unset or different, to avoid closing the session unecessarily.
     *
     * @param name  the property name
     * @param value the property value
     */
    private void setProperty(String name, boolean value) {
        Properties properties = getJavaMailProperties();
        boolean update = false;
        if (properties == null) {
            properties = new Properties();
            update = true;
        } else {
            String current = properties.getProperty(name);
            if (current == null || Boolean.valueOf(current) != value) {
                update = true;
            }
        }
        if (update) {
            properties.setProperty(name, Boolean.toString(value));
            setJavaMailProperties(properties); // closes current session
        }
    }
}
