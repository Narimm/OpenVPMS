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

package org.openvpms.web.echo.servlet;

import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.WebRenderServlet;
import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.echo.spring.SpringApplicationInstance;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.WEAK;

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
     * The default period before screen-lock, in minutes.
     */
    public static final int DEFAULT_AUTO_LOCK_INTERVAL = 5;

    /**
     * The default session inactivity period before logout, in minutes.
     */
    public static final int DEFAULT_AUTO_LOGOUT_INTERVAL = 30;

    /**
     * The time in milliseconds before inactive sessions have their screens locked.
     */
    private volatile long autoLock = DEFAULT_AUTO_LOCK_INTERVAL * DateUtils.MILLIS_PER_MINUTE;

    /**
     * The time in milliseconds before inactive sessions are logged out.
     */
    private volatile long autoLogout = DEFAULT_AUTO_LOGOUT_INTERVAL * DateUtils.MILLIS_PER_MINUTE;

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
        log.info("Using default session auto-lock time=" + (autoLock / DateUtils.MILLIS_PER_MINUTE) + " minutes");
        log.info("Using default session auto-logout time=" + (autoLogout / DateUtils.MILLIS_PER_MINUTE) + " minutes");
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
            HttpServletRequest request = connection.getRequest();
            active(request, SecurityContextHolder.getContext().getAuthentication());
        }
    }

    /**
     * Marks a session as active.
     *
     * @param request        the request
     * @param authentication the user authentication
     */
    public void active(HttpServletRequest request, Authentication authentication) {
        Monitor monitor = monitors.get(request.getSession());
        if (monitor != null) {
            monitor.active(request, authentication);
        }
    }

    /**
     * Registers a new application.
     *
     * @param application the new application
     * @param session     the session that created the application
     */
    public void newApplication(SpringApplicationInstance application, HttpSession session) {
        Monitor monitor = monitors.get(session);
        if (monitor != null) {
            monitor.newApplication(application);
        }
    }

    /**
     * Returns the time that sessions may remain idle before they are locked.
     *
     * @return the timeout, in minutes. A value of {@code 0} indicates that sessions don't lock
     */
    public int getAutoLock() {
        return (int) (autoLock / DateUtils.MILLIS_PER_MINUTE);
    }

    /**
     * Sets the time that sessions may remain idle before they are locked.
     *
     * @param time the timeout, in minutes. A value of {@code 0} indicates that sessions don't lock
     */
    public void setAutoLock(int time) {
        long newTime = time * DateUtils.MILLIS_PER_MINUTE;
        if (newTime != autoLock) {
            autoLock = newTime;
            if (newTime == 0) {
                log.warn("Sessions configured to not auto-lock");
            } else {
                log.info("Using session auto-lock time=" + time + " minutes");
            }
            reschedule();
        }
    }

    /**
     * Unlocks the current session.
     */
    public void unlock() {
        Connection connection = WebRenderServlet.getActiveConnection();
        if (connection != null) {
            Monitor monitor = monitors.get(connection.getRequest().getSession());
            if (monitor != null) {
                monitor.unlock();
            }
        }
    }

    /**
     * Sets the time that sessions may remain idle before they are logged out.
     *
     * @param time the timeout, in minutes. A value of {@code 0} indicates that sessions don't expire
     */
    public void setAutoLogout(int time) {
        long newTime = time * DateUtils.MILLIS_PER_MINUTE;
        if (newTime != autoLogout) {
            autoLogout = newTime;
            if (newTime == 0) {
                log.warn("Sessions configured to not auto-logout");
            } else {
                log.info("Using session auto-logout time=" + time + " minutes");
            }

            reschedule();
        }
    }

    /**
     * Destroys this.
     */
    @Override
    public void destroy() {
        log.info("Shutting down SessionMonitor");
        executor.shutdown();
        monitors.clear();
    }

    /**
     * Reschedule existing sessions.
     */
    private void reschedule() {
        for (Object state : monitors.values().toArray()) {
            ((Monitor) state).reschedule();
        }
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
         * Determines if the applications are currently locked.
         */
        private volatile boolean locked;

        /**
         * The applications linked to the session.
         */
        private final ReferenceMap<SpringApplicationInstance, SpringApplicationInstance> apps
                = new ReferenceMap<SpringApplicationInstance, SpringApplicationInstance>(WEAK, WEAK);

        /**
         * The time the session was last accessed, in milliseconds.
         */
        private volatile long lastAccessedTime;

        /**
         * Used to cancel the scheduling.
         */
        private volatile ScheduledFuture<?> future;

        /**
         * The user, used for logging.
         */
        private volatile String user;

        /**
         * The user's IP address, used for logging.
         */
        private volatile String address;

        /**
         * Constructs a {@link Monitor}.
         *
         * @param session the session
         */
        public Monitor(HttpSession session) {
            lastAccessedTime = System.currentTimeMillis();
            this.session = new WeakReference<HttpSession>(session);
        }

        /**
         * Marks the session as active.
         *
         * @param request        the servlet request
         * @param authentication the user authentication
         */
        public void active(HttpServletRequest request, Authentication authentication) {
            lastAccessedTime = System.currentTimeMillis();
            boolean doLog = (user == null);
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                user = ((UserDetails) principal).getUsername();
            } else {
                user = principal.toString();
            }
            address = request.getRemoteAddr();
            if (doLog && log.isInfoEnabled()) {
                log.info("Active session, user=" + user + ", address=" + address);
            }
        }

        /**
         * Registers an application.
         *
         * @param application the application.
         */
        public synchronized void newApplication(SpringApplicationInstance application) {
            apps.put(application, application);
        }

        /**
         * Invoked by the executor. This:
         * <ul>
         * <li>invalidates the session if {@link #autoLogout} is non-zero and it hasn't been accessed for
         * {@link #autoLogout} milliseconds</li>
         * <li>locks the session if {@link #autoLock} is non-zero, and the session hasn't been accessed for
         * {@link #autoLock} milliseconds.</li>
         * </ul>
         * If it has been accessed, then it reschedules itself for another {@code autoLock} milliseconds.
         */
        @Override
        public synchronized void run() {
            long inactive = System.currentTimeMillis() - lastAccessedTime;
            long logout = autoLogout;
            long lock = autoLock;
            if (logout != 0 && inactive > logout) {
                // session is inactive, so kill it
                invalidate();
            } else {
                long reschedule = (logout != 0) ? logout - inactive : 0;
                if (lock != 0 && lock < logout) {
                    if (inactive > lock) {
                        if (!locked) {
                            lock();
                        }
                    } else {
                        reschedule = lock - inactive;
                    }
                }
                if (reschedule != 0) {
                    schedule(reschedule);
                }
            }
        }

        /**
         * Schedules the monitor.
         */
        public void schedule() {
            long time = autoLock;
            long logout = autoLogout;
            if (time == 0 || (logout != 0 && logout < time)) {
                time = logout;
            }
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
            long timeout = autoLock;
            if (timeout == 0) {
                timeout = autoLogout;
            }
            if (timeout != 0) {
                run(); // will either expire or schedule
            }
        }

        /**
         * Unlocks applications linked to the session.
         */
        public synchronized void unlock() {
            try {
                SpringApplicationInstance[] list = apps.values().toArray(new SpringApplicationInstance[apps.size()]);
                for (SpringApplicationInstance app : list) {
                    if (app != null) {
                        app.unlock();
                    }
                }
            } finally {
                locked = false;
                reschedule();
            }
            if (log.isInfoEnabled()) {
                log.info("Unlocked session for user=" + user + ", address=" + address);
            }
        }

        /**
         * Destroys the monitor.
         */
        public void destroy() {
            if (future != null) {
                future.cancel(true);
            }
        }

        /**
         * Schedules the monitor.
         *
         * @param delay the milliseconds to delay before invoking {@link #run}.
         */
        private void schedule(long delay) {
            future = executor.schedule(this, delay, TimeUnit.MILLISECONDS);
        }

        /**
         * Locks applications linked to the session.
         */
        private synchronized void lock() {
            locked = true;
            SpringApplicationInstance[] list = apps.values().toArray(new SpringApplicationInstance[apps.size()]);
            for (SpringApplicationInstance app : list) {
                if (app != null) {
                    app.lock();
                }
            }
            if (log.isInfoEnabled()) {
                log.info("Locked session for user=" + user + ", address=" + address);
            }
        }

        /**
         * Invalidates a session.
         */
        private void invalidate() {
            HttpSession httpSession = session.get();
            if (httpSession != null) {
                httpSession.invalidate();
                if (log.isInfoEnabled()) {
                    log.info("Invalidated session for user=" + user + ", address=" + address);
                }
            }
        }

    }

}
