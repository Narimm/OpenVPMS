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

package org.openvpms.web.echo.servlet;

import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.springframework.beans.factory.DisposableBean;

/**
 * Configures an {@link SessionMonitor}.
 * <p/>
 * This listens for updates to the <em>party.organisationPractice</em> and updates the
 * {@link SessionMonitor#setAutoLogout(int) auto-logout time} according to the <em>autoLogout</em> node.
 *
 * @author Tim Anderson
 */
public class SessionMonitorConfigurer implements DisposableBean {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The session monitor.
     */
    private final SessionMonitor monitor;

    /**
     * The listener for practice updates.
     */
    private final IArchetypeServiceListener listener;

    /**
     * Constructs a {@link SessionMonitorConfigurer}.
     *
     * @param monitor the monitor to configure
     * @param service the archetype service
     * @param rules   the practice rules
     */
    public SessionMonitorConfigurer(SessionMonitor monitor, IArchetypeService service, PracticeRules rules) {
        this.monitor = monitor;
        this.service = service;
        Party practice = rules.getPractice();
        if (practice != null) {
            setAutoLogout(practice);
        }
        listener = new AbstractArchetypeServiceListener() {

            @Override
            public void saved(IMObject object) {
                setAutoLogout(object);
            }

        };
        service.addListener(PracticeArchetypes.PRACTICE, listener);
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        service.removeListener(PracticeArchetypes.PRACTICE, listener);
    }

    /**
     * Sets the auto logout period.
     *
     * @param practice the practice
     */
    private void setAutoLogout(IMObject practice) {
        IMObjectBean bean = new IMObjectBean(practice, service);
        int autoLogout = bean.getInt("autoLogout", SessionMonitor.DEFAULT_AUTO_LOGOUT_INTERVAL);
        monitor.setAutoLogout(autoLogout);
    }

}
