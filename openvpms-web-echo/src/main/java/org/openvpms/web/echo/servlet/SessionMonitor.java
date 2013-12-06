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

import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.WebRenderServlet;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

import javax.servlet.http.HttpSession;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Monitors HTTP sessions, forcing idle sessions to expire.
 * <p/>
 * This is required as echo2 asynchronous tasks keep sessions alive, such that web.xml {@code <session-timeout/>}
 * has no effect.
 *
 * @author Tim Anderson
 */
public class SessionMonitor implements DisposableBean {

    /**
     * The default session inactivity period before logout, in minutes.
     */
    public static int DEFAULT_AUTO_LOGOUT_INTERVAL = 30;

    /**
     * The time in minutes before inactive sessions are logged out.
     */
    private volatile int autoLogout = DEFAULT_AUTO_LOGOUT_INTERVAL;

    /**
     * The monitors, keyed on their sessions.
     */
    private Map<HttpSession, Monitor> monitors = Collections.synchronizedMap(new WeakHashMap<HttpSession, Monitor>());

    /**
     * The executor, used to expire sessions.
     */
    private final ScheduledExecutorService executor;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(SessionMonitor.class);

    /**
     * Constructs an {@link SessionMonitor}.
     */
    public SessionMonitor() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Adds a session to monitor.
     *
     * @param session the session
     */
    public void addSession(HttpSession session) {
        Monitor monitor = new Monitor(session);
        monitors.put(session, monitor);
        monitor.schedule();
    }

    /**
     * Stops monitoring a session.
     *
     * @param session the session to remove
     */
    public void removeSession(HttpSession session) {
        Monitor monitor = monitors.remove(session);
        if (monitor != null) {
            monitor.destroy();
        }
    }

    /**
     * Marks the current session as active.
     */
    public void active() {
        Connection connection = WebRenderServlet.getActiveConnection();
        if (connection != null) {
            active(connection.getRequest().getSession());
        }
    }

    /**
     * Marks a session as active.
     *
     * @param session the session
     */
    public void active(HttpSession session) {
        Monitor monitor = monitors.get(session);
        if (monitor != null) {
            monitor.active();
        }
    }

    /**
     * Sets the time that sessions may remain idle before they are logged out.
     *
     * @param time the timeout, in minutes. A value of {@code 0} indicates that sessions don't expire
     */
    public void setAutoLogout(int time) {
        if (time != autoLogout) {
            autoLogout = time;
            if (time == 0) {
                log.warn("Sessions configured to not auto-logout");
            } else {
                log.info("Using session auto-logout time=" + time + " minutes");
            }

            // reschedule existing session states
            for (Object state : monitors.values().toArray()) {
                ((Monitor) state).reschedule();
            }
        }
    }

    /**
     * Destroys this.
     */
    @Override
    public void destroy() {
        executor.shutdown();
        monitors.clear();
    }

    /**
     * Monitors session activity.
     */
    private class Monitor implements Runnable {

        /**
         * The session.
         */
        private WeakReference<HttpSession> session;

        /**
         * The time the session was last accessed, in milliseconds.
         */
        private volatile long lastAccessedTime;

        /**
         * Used to cancel the scheduling.
         */
        private volatile ScheduledFuture<?> future;

        /**
         * Constructs a {@link Monitor}.
         *
         * @param session the session
         */
        public Monitor(HttpSession session) {
            active();
            this.session = new WeakReference<HttpSession>(session);
        }

        /**
         * Marks the session as active.
         */
        public void active() {
            lastAccessedTime = System.currentTimeMillis();
        }

        /**
         * Invoked by the executor. This invalidates the session if it hasn't been accessed for {@link #autoLogout}
         * seconds. If it has been accessed, then it reschedules itself for another {@code sessionTimeout - inactive}
         * seconds.
         */
        @Override
        public void run() {
            int inactive = (int) ((System.currentTimeMillis() - lastAccessedTime) / DateUtils.MILLIS_PER_MINUTE);
            if (inactive >= autoLogout) {
                // session is inactive, so kill it
                HttpSession httpSession = session.get();
                if (httpSession != null) {
                    httpSession.invalidate();
                }
            } else {
                // the session is still active, so reschedule
                schedule(autoLogout - inactive);
            }
        }

        /**
         * Schedules the monitor.
         */
        public void schedule() {
            int time = autoLogout;
            if (time != 0) {
                schedule(time);
            }
        }

        /**
         * Reschedules the monitor.
         */
        public void reschedule() {
            ScheduledFuture<?> current = future;
            if (current != null) {
                current.cancel(false);
            }
            int timeout = autoLogout;
            if (timeout != 0) {
                run();
                // will either expire or schedule. Note that there is a possibility of a race condition where run() is
                // already in progress.
            }
        }

        /**
         * Destroys the monitor.
         */
        public void destroy() {
            future.cancel(true);
        }

        /**
         * Schedules the monitor.
         *
         * @param minutes the minutes to delay before invoking {@link #run}.
         */
        private void schedule(int minutes) {
            future = executor.schedule(this, minutes, TimeUnit.MINUTES);
        }

    }

}
