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
 * Mail service that configures the SMTP host from the <tt>mailHost</tt>
 * attribute of the <em>party.organisationLocation</em> from
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
        String host = super.getHost();
        if (StringUtils.isEmpty(host)) {
            Party location = GlobalContext.getInstance().getLocation();
            if (location != null) {
                IMObjectBean bean = new IMObjectBean(location);
                if (bean.hasNode("mailHost")) {
                    host = bean.getString("mailHost");
                }
            }
        }
        return host;
    }
}
