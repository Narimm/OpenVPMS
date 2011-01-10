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

import com.sun.star.bridge.UnoUrlResolver;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.NoConnectException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XCloseable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.FailedToStartService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;


/**
 * Service to bootstrap an OpenOffice instance on the local host.
 * The OpenOffice binaries must be in the path.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class OOBootstrapService {

    /**
     * The command path.
     */
    private String path;

    /**
     * The connection parameters.
     */
    private final String parameters;

    /**
     * Determines whether the service should be started headless or not.
     * In headless mode, the service can be reliably terminated, however
     * it prevents any viewing of OO documents by the local user.
     */
    private boolean headless = true;

    /**
     * The no. of times to attempt connection to the service, before giving
     * up.
     */
    private int retries = DEFAULT_RETRIES;

    /**
     * The time to wait after terminating the desktop, in milliseconds. Used to
     * ensure that subsequent startups don't fail due to socket errors.
     */
    private long terminateWait = 2000;

    /**
     * The current process.
     */
    private Process process;

    /**
     * Default no. of retries.
     */
    private static final int DEFAULT_RETRIES = 10;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(OOBootstrapService.class);


    /**
     * Constructs a new <code>OOBootstrapService</code>.
     *
     * @param parameters the connection parameters
     */
    public OOBootstrapService(String parameters) {
        this.parameters = parameters;
    }

    /**
     * Starts the OpenOffice service.
     *
     * @throws OpenOfficeException if the service cannot be started
     */
    public synchronized void start() {
        if (process != null) {
            throw new OpenOfficeException(FailedToStartService,
                                          "service already running");
        }
        log.info("Starting OpenOffice");
        try {
            // create default local component context
            XComponentContext localContext =
                    Bootstrap.createInitialComponentContext(null);
            if (localContext == null) {
                throw new OpenOfficeException(FailedToStartService,
                                              "no local component context");
            }

            String office;
            if (StringUtils.isEmpty(path)) {
                office = System.getProperty("os.name").startsWith("Windows") ?
                            "soffice.exe" : "soffice";
            } else {
                office = path;
            }

            // command line arguments
            String[] args;
            String accept = "-accept=" + parameters + ";urp;";
            if (headless) {
                args = new String[]{office, "-headless", accept};
            } else {
                // -nocrashreport not required in 2.1, added for compatibility
                // with 2.0.4
                args = new String[]{office, "-nologo", "-nodefault",
                                    "-norestore", "-nocrashreport",
                                    "-nolockcheck", accept};
            }

            if (log.isDebugEnabled()) {
                log.debug("Attempting to start soffice using: "
                        + StringUtils.join(args, " "));
            }

            // start the office process
            process = Runtime.getRuntime().exec(args);
            pipe(process.getInputStream(), System.out, "CO> ");
            pipe(process.getErrorStream(), System.err, "CE> ");

            // initial service manager
            XMultiComponentFactory localServiceManager
                    = localContext.getServiceManager();
            if (localServiceManager == null) {
                throw new OpenOfficeException(FailedToStartService,
                                              "no initial service manager");
            }

            // create a URL resolver
            XUnoUrlResolver urlResolver = UnoUrlResolver.create(localContext);

            // connection string
            String connect = "uno:" + parameters +
                    ";urp;StarOffice.ComponentContext";

            // wait until office is started
            boolean connected = false;
            for (int i = 0; i < retries && !connected; ++i) {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to connect, attempt "
                            + (i + 1) + " of " + retries);
                }
                connected = connect(urlResolver, connect);
                if (!connected) {
                    try {
                        // wait 500 ms, then try to connect again
                        Thread.sleep(500);
                    } catch (InterruptedException ignore) {
                        // no-op
                    }
                }
            }
            if (!connected) {
                throw new OpenOfficeException(
                        FailedToStartService, "failed to connect after "
                        + retries + " attempts");
            }
        } catch (OpenOfficeException exception) {
            if (process != null) {
                process.destroy();
            }
            throw exception;
        } catch (Throwable exception) {
            log.debug(exception, exception);
            if (process != null) {
                process.destroy();
            }
            throw new OpenOfficeException(exception, FailedToStartService,
                                          exception.getMessage());
        }
    }

    /**
     * Determines if the service is running and accepting requests.
     *
     * @return <code>true</code> if the service is running, otherwise
     *         <code>false</code>
     */
    public synchronized boolean isActive() {
        boolean running = false;
        if (process != null) {
            OOConnection connection = null;
            try {
                connection = getConnection();
                connection.getComponentLoader();
                running = true;
            } catch (OpenOfficeException exception) {
                log.debug("OpenOffice not responding", exception);
            } finally {
                OpenOfficeHelper.close(connection);
            }
        }
        return running;
    }

    /**
     * Stops the service.
     */
    public synchronized void stop() {
        if (process != null) {
            log.info("Stopping OpenOffice");
            try {
                OOConnection connection = getConnection();
                XDesktop desktop = (XDesktop) connection.getService(
                        "com.sun.star.frame.Desktop", XDesktop.class);
                if (desktop != null) {
                    close(desktop);
                }
                try {
                    connection.close();
                } catch (OpenOfficeException ignore) {
                    // no-op
                }
                Thread.sleep(terminateWait);
            } catch (Throwable exception) {
                log.debug(exception, exception);
            }
            process.destroy();
            process = null;
        }
    }

    /**
     * Returns a new {@link OOConnection} to the service.
     *
     * @return a new connection to the service
     * @throws OpenOfficeException if a connection cannot be established
     */
    public abstract OOConnection getConnection();

    /**
     * Sets the path of the command to execute.
     * If not specified defaults to <em>soffice.exe</em> on Windows or
     * <em>soffice</em> on other OSes.
     *
     * @param path the command path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Determines whether the service should be started headless or not.
     * In headless mode, the service can be reliably terminated, however
     * it prevents any viewing of OO documents by the local user.
     *
     * @param headless if <code>true</code> starts the server headless.
     *                 Defaults to <code>true</code>
     */
    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    /**
     * Sets the no. of times to retry connection to the service before giving
     * up.
     *
     * @param retries the no. of times to retry connection
     */
    public void setConnectionRetries(int retries) {
        this.retries = (retries > 0) ? retries : DEFAULT_RETRIES;
    }

    /**
     * Sets the amount of time to wait after terminating the desktop, when
     * stopping an OpenOffice service. This ensures that subsequent restarts
     * don't fail with socket errors.
     *
     * @param wait the time to wait, in milliseconds. Defaults to
     *             <code>1000</code>
     */
    public void setTerminateWait(long wait) {
        this.terminateWait = wait;
    }

    /**
     * Attempts connection to an OpenOffice service.
     *
     * @param urlResolver the URL resolver
     * @param connect     the connection string
     * @return <code>true</code> if connection is successful, otherwise
     *         <code>false</code>
     * @throws OpenOfficeException        if the context cannot be resolved
     * @throws com.sun.star.uno.Exception for any OpenOffice error
     */
    private boolean connect(XUnoUrlResolver urlResolver, String connect)
            throws com.sun.star.uno.Exception {
        boolean connected = false;
        try {
            // try to connect to office
            Object context = urlResolver.resolve(connect);
            if (context != null) {
                context = UnoRuntime.queryInterface(
                          XComponentContext.class, context);
                  if (context == null) {
                      throw new OpenOfficeException(FailedToStartService,
                                                    "no component context");
                  }
                  connected = true;
            }
        } catch (NoConnectException exception) {
            // see if the process is still running. Don't use
            // process exit to terminate the loop as soffice may
            // handle connections via soffice.bin?
            try {
                int exit = process.exitValue();
                log.debug("Process terminated with exit code=" + exit);
            } catch (IllegalThreadStateException ignore) {
                // process is running
            }
        }
        return connected;
    }

    /**
     * Cleans up the desktop, before terminating it.
     *
     * @param desktop the desktop
     */
    private void close(XDesktop desktop) {
        try {
            XEnumerationAccess iterAccess = desktop.getComponents();
            XEnumeration iter = iterAccess.createEnumeration();
            while (iter.hasMoreElements()) {
                Object object = iter.nextElement();
                XCloseable closeable = UnoRuntime.queryInterface(XCloseable.class, object);
                if (closeable != null) {
                    closeable.close(true);
                } else {
                    XComponent component = UnoRuntime.queryInterface(XComponent.class, object);
                    if (component != null) {
                        component.dispose();
                    }
                }
            }

            if (!desktop.terminate()) {
                log.debug("Failed to terminate desktop");
            }
        } catch (Throwable exception) {
            log.debug(exception, exception);
        }
    }

    /**
     * Pipes process output to a stream.
     *
     * @param in     the stream to read from
     * @param out    the stream to write to
     * @param prefix logging prefix
     */
    private static void pipe(final InputStream in, final PrintStream out,
                             final String prefix) {

        new Thread("Pipe: " + prefix) {
            public void run() {
                BufferedReader r = new BufferedReader(
                        new InputStreamReader(in));
                try {
                    for (; ;) {
                        String s = r.readLine();
                        if (s == null) {
                            break;
                        }
                        out.println(prefix + s);
                    }
                } catch (java.io.IOException exception) {
                    exception.printStackTrace(System.err);
                }
            }
        }.start();
    }

}
