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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.practice;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;

/**
 * Mail server settings from an <em>entity.mailServer</em>.
 *
 * @author Tim Anderson
 */
public class MailServer {

    /**
     * Connection security.
     */
    public enum Security {
        NONE,
        STARTTLS,
        SSL_TLS
    }

    /**
     * The host.
     */
    private final String host;

    /**
     * The port.
     */
    private final int port;

    /**
     * The username.
     */
    private final String username;

    /**
     * The password.
     */
    private final String password;

    /**
     * The connection security.
     */
    private final Security security;

    /**
     * The send timeout, in seconds, or 0 to use the default timeout.
     */
    private final int timeout;

    /**
     * The settings identifier.
     */
    private long id;

    /**
     * Constructs a {@link MailServer}.
     *
     * @param configuration the configuration
     * @param service       the archetype service
     */
    public MailServer(Entity configuration, IArchetypeService service) {
        IMObjectBean bean = service.getBean(configuration);
        id = configuration.getId();
        host = bean.getString("host");
        port = bean.getInt("port");
        username = StringUtils.trimToNull(bean.getString("username"));
        password = StringUtils.trimToNull(bean.getString("password"));
        security = getSecurity(bean.getString("security"));
        timeout = bean.getInt("timeout");
    }

    /**
     * Returns the configuration identifier.
     *
     * @return the configuration identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the mail server host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the mail server port.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the username for the account at the mail host.
     *
     * @return the user name. May be {@code null}
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password for the account at the mail host.
     *
     * @return the password. May be {@code null}
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the connection security.
     *
     * @return the connection security
     */
    public Security getSecurity() {
        return security;
    }

    /**
     * Returns the send timeout.
     *
     * @return the send timeout in seconds, or {@code 0} to use the default timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * Equality is determined by the {@code host}, {@code port}, {@code username}, {@code password},
     * {@code security} and {@code timeout}.<br/>
     * The {@code id} is not considered.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MailServer) {
            MailServer other = (MailServer) obj;
            return port == other.port && StringUtils.equals(host, other.host)
                   && StringUtils.equals(username, other.username)
                   && StringUtils.equals(password, other.password)
                   && security == other.security
                   && timeout == other.timeout;
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder().append(host).append(port).append(username).append(password)
                .append(security).append(timeout);
        return builder.toHashCode();
    }

    /**
     * Returns the connection security.
     *
     * @return the connection security
     */
    private Security getSecurity(String security) {
        if ("STARTTLS".equals(security)) {
            return Security.STARTTLS;
        } else if ("SSL_TLS".equals(security)) {
            return Security.SSL_TLS;
        }
        return Security.NONE;
    }

}
