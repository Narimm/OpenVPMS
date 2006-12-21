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
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.connection.NoConnectException;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
     * The connection parameters.
     */
    private final String parameters;

    /**
     * The current process.
     */
    private Process process;

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
        if (log.isDebugEnabled()) {
            log.debug("Attempting to start soffice using parameters="
                    + parameters);
        }
        try {
            // create default local component context
            XComponentContext localContext =
                    Bootstrap.createInitialComponentContext(null);
            if (localContext == null) {
                throw new BootstrapException("no local component context");
            }

            String office =
                    System.getProperty("os.name").startsWith("Windows") ?
                            "soffice.exe" : "soffice";

            // command line arguments
            String[] args = {
                    office, "-nologo", "-nodefault", "-norestore",
                    "-nolockcheck", "-accept="+ parameters + ";urp;"
            };

            // start the office process
            process = Runtime.getRuntime().exec(args);
            pipe(process.getInputStream(), System.out, "CO> ");
            pipe(process.getErrorStream(), System.err, "CE> ");

            // initial service manager
            XMultiComponentFactory localServiceManager
                    = localContext.getServiceManager();
            if (localServiceManager == null)
                throw new BootstrapException("no initial service manager");

            // create a URL resolver
            XUnoUrlResolver urlResolver = UnoUrlResolver.create(localContext);

            // connection string
            String connect = "uno:" + parameters +
                    ";urp;StarOffice.ComponentContext";

            // wait until office is started
            for (; ;) {
                try {
                    // try to connect to office
                    Object context = urlResolver.resolve(connect);
                    context = UnoRuntime.queryInterface(
                            XComponentContext.class, context);
                    if (context == null) {
                        throw new OpenOfficeException(FailedToStartService,
                                                      "no component context");
                    }
                    break;
                } catch (NoConnectException exception) {
                    // wait 500 ms, then try to connect again
                    Thread.sleep(500);
                }
            }
        } catch (OpenOfficeException exception) {
            if (process != null) {
                process.destroy();
            }
            throw exception;
        } catch (Throwable exception) {
            if (process != null) {
                process.destroy();
            }
            throw new OpenOfficeException(exception, FailedToStartService,
                                          exception.getMessage());
        }
    }

    /**
     * Determines if the service is running.
     *
     * @return <code>true</code> if the service is running, otherwise
     *         <code>false</code>
     */
    public synchronized boolean isRunning() {
        return (process != null);
    }

    /**
     * Stops the service.
     */
    public synchronized void stop() {
        if (process != null) {
            boolean terminated = false;
            try {
                OOConnection connection = getConnection();
                XDesktop desktop = (XDesktop) connection.getService(
                        "com.sun.star.frame.Desktop", XDesktop.class);
                if (desktop != null) {
                    if (desktop.terminate()) {
                        terminated = true;
                    }
                }
                try {
                    connection.close();
                } catch (OpenOfficeException ignore) {
                    log.error(ignore,  ignore);
                }
            } catch (Throwable exception) {
                log.debug(exception, exception);
            }
            if (!terminated) {
                log.debug("unable to terminate soffice process, destroying");
                process.destroy();
            }
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
