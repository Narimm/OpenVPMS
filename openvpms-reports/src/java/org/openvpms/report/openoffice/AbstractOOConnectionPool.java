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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.openoffice;

import com.sun.star.frame.XComponentLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToConnect;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Abstract implementation of the {@link OOConnectionPool} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractOOConnectionPool implements OOConnectionPool {

    /**
     * The set of available collections.
     */
    private final ArrayBlockingQueue<State> connections;

    /**
     * Reentrant lock to avoid allocating and destroying connections
     * simulatenously.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * The pool capacity.
     */
    private final int capacity;

    /**
     * The no. of connections.
     */
    private int count = 0;

    /**
     * The maximum no. of times a connection may be used before being
     * closed. A value <tt>&lt;= 0</tt> indicates it should never be closed.
     */
    private int uses = 0;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(
            AbstractOOConnectionPool.class);


    /**
     * Constructs a new <tt>AbstractOOConnectionPool</tt>.
     *
     * @param capacity the pool capacity
     */
    public AbstractOOConnectionPool(int capacity) {
        connections = new ArrayBlockingQueue<State>(capacity);
        this.capacity = capacity;
    }

    /**
     * Gets a connection, blocking for up to <tt>timeout</tt> milliseconds
     * until one becomes available.
     *
     * @param timeout the maximum no. of milliseconds to wait for a connection
     * @return a connection or <tt>null</tt> if none was available before
     *         the timeout expired
     * @throws OpenOfficeException if a connection error occurs
     */
    public OOConnection getConnection(long timeout) {
        lock.lock();
        try {
            allocate();
        } finally {
            lock.unlock();
        }
        try {
            State state = connections.poll(timeout, TimeUnit.MILLISECONDS);
            return (state != null) ? new OOConnectionHandle(state) : null;
        } catch (InterruptedException exception) {
            throw new OpenOfficeException(FailedToConnect, exception);
        }
    }

    /**
     * Gets a connection, blocking until one becomes available.
     *
     * @throws OpenOfficeException if a connection error occurs
     */
    public OOConnection getConnection() {
        OOConnection result = null;
        while (result == null) {
            lock.lock();
            try {
                allocate();
            } finally {
                lock.unlock();
            }
            try {
                // find an active connection
                State state = connections.take();
                lock.lock();
                try {
                    if (isResponsive(state)) {
                        result = new OOConnectionHandle(state);
                    } else {
                        destroy(state);
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException exception) {
                throw new OpenOfficeException(FailedToConnect, exception);
            }
        }
        return result;
    }

    /**
     * Returns the pool capacity.
     *
     * @return the pool capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Determines when connections should be closed.
     *
     * @param uses the number of uses after which connections should be closed.
     *             A value <tt>&lt;= 0</tt> indicates it should never
     *             be closed
     */
    public synchronized void setReuseCount(int uses) {
        this.uses = uses;
    }

    /**
     * Creates a new connection.
     *
     * @return a new connection
     * @throws OpenOfficeException if the connection cannot be created
     */
    protected abstract OOConnection create();

    /**
     * Destroys a connection.
     *
     * @param state the connection state
     */
    protected void destroy(State state) {
        try {
            state.getConnection().close();
        } catch (Throwable exception) {
            log.warn(exception, exception);
        } finally {
            --count;
        }
    }

    /**
     * Allocates connections to the pool.
     *
     * @throws OpenOfficeException if a connection cannot be created
     */
    private void allocate() {
        while (count < capacity) {
            State state = new State(create());
            ++count;
            if (!connections.offer(state)) {
                destroy(state);
                // shouldn't occur
                throw new IllegalStateException(
                        "Failed to add a new connection to the pool");
            }
        }
    }

    /**
     * Releases a connection back to the pool.
     *
     * @param state the connection state
     */
    private void release(State state) {
        lock.lock();
        try {
            if (!canRelease(state)) {
                destroy(state);
            } else {
                if (!connections.offer(state)) {
                    log.error("Failed to release connection to the pool");
                    destroy(state);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Determines if a connection can be released back into the pool.
     *
     * @param state the connection state
     * @return <tt>true</tt> if the connection can be released,
     *         otherwise <tt>false</tt> to indicate it should be destroyed
     */
    private boolean canRelease(State state) {
        return ((uses <= 0 || state.getUses() < uses) && isResponsive(state));
    }

    /**
     * Determines if a connection is responsive.
     *
     * @param state the connection state
     * @return <tt>true</tt> if the connection is responsive, otherwise
     *         <tt>false</tt>
     */
    private boolean isResponsive(State state) {
        boolean responsive = false;
        try {
            state.getConnection().getComponentLoader();
            responsive = true;
        } catch (OpenOfficeException exception) {
            log.debug("Connection not responding", exception);
        }
        return responsive;
    }

    protected class State {

        private final OOConnection connection;

        private int uses;

        public State(OOConnection connection) {
            this.connection = connection;
        }

        public OOConnection getConnection() {
            return connection;
        }

        public synchronized void incUses() {
            ++uses;
        }

        public synchronized int getUses() {
            return uses;
        }

    }

    protected class OOConnectionHandle implements OOConnection,
                                                  OOConnectionListener {

        /**
         * The connection state.
         */
        private State state;

        /**
         * The listener.
         */
        private OOConnectionListener listener;

        /**
         * The thread that created the connection.
         */
        private String thread;

        /**
         * The call stack where the connection was created.
         */
        private Throwable stack;


        /**
         * Constructs a new <tt>OOConnectionHandle</tt>
         *
         * @param state the connection state
         */
        public OOConnectionHandle(State state) {
            this.state = state;
            state.incUses();
            stack = new Throwable();
            stack.fillInStackTrace();
            thread = Thread.currentThread().getName();
        }

        /**
         * Returns the component loader.
         *
         * @return the component loader
         * @throws OpenOfficeException for any error
         */
        public XComponentLoader getComponentLoader() {
            return state.getConnection().getComponentLoader();
        }

        /**
         * Returns a service of the specified type.
         *
         * @param name the service name
         * @param type the service type
         * @throws OpenOfficeException if the service can't be created
         */
        public Object getService(String name, Class type) {
            return state.getConnection().getService(name, type);
        }

        /**
         * Sets the listener for this connection.
         *
         * @param listener the listener. May be <tt>null</tt>
         */
        public void setListener(OOConnectionListener listener) {
            this.listener = listener;
        }

        /**
         * Puts the connection back in the pool.
         */
        public void close() {
            if (state != null) {
                release(state);
                state = null;

                if (listener != null) {
                    listener.closed(this);
                    listener = null;
                }
            }
        }

        /**
         * Invoked when the connection is closed.
         *
         * @param connection the connection
         */
        public void closed(OOConnection connection) {
            this.state = null;
            if (listener != null) {
                listener.closed(this);
            }
        }

        /**
         * Called by the garbage collector on an object when garbage collection
         * determines that there are no more references to the object.
         *
         * @throws Throwable the <tt>Exception</tt> raised by this method
         */
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (state != null) {
                log.warn("OOConnection allocated by thread=" + thread
                        + " not closed: releasing", stack);
                release(state);
                state = null;
            }
        }
    }
}
