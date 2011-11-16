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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.service;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.GlobalContext;
import org.springframework.mail.javamail.JavaMailSenderImpl;


/**
 * Mail service that configures the SMTP details from <em>party.organisationLocation</em> from
 * {@link GlobalContext#getLocation()}, if available.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MailService extends JavaMailSenderImpl {

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
     * Returns the <em>party.organisationLocation</em> wrapped in a bean, if one is present in the global context.
     *
     * @return the location, or <tt>null</tt> if none is present.
     */
    private IMObjectBean getLocationBean() {
        Party location = GlobalContext.getInstance().getLocation();
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

}
