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

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Listens for session events, passing them to the {@link SessionMonitor}.
 *
 * @author Tim Anderson
 */
public class SessionListener implements HttpSessionListener {

    /**
     * Invoked when a session is created.
     *
     * @param event the event
     */
    public void sessionCreated(HttpSessionEvent event) {
        SessionMonitor monitor = getSessionMonitor(event);
        monitor.addSession(event.getSession());
    }

    /**
     * Invoked when a session is destroyed.
     *
     * @param event the event
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        SessionMonitor monitor = getSessionMonitor(event);
        monitor.removeSession(event.getSession());
    }

    /**
     * Returns the session monitor.
     *
     * @param event the event
     * @return the session monitor
     */
    private SessionMonitor getSessionMonitor(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
        return ctx.getBean(SessionMonitor.class);
    }
}
