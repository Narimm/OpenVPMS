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
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Random;


/**
 * Service to bootstrap an OpenOffice instance on the local host.
 * The OpenOffice binaries must be in the path.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BootstrapService {

    /**
     * The connection parameters.
     */
    private final String _connectParams;


    /**
     * Bootstraps the component context from a UNO installation.
     *
     * @throws BootstrapException if the component context can't be
     *                            bootstrapped
     */
    public BootstrapService() throws BootstrapException {
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
            // create random pipe name
            String pipeName = "uno" +
                    Long.toString(
                            (new Random()).nextLong() & 0x7fffffffffffffffL);

            // command line arguments
            String[] args = {
                    office, "-nologo", "-nodefault", "-norestore",
                    "-nolockcheck", "-accept=pipe,name=" + pipeName + ";urp;"
            };

            // start office process
            Process p = Runtime.getRuntime().exec(args);
            pipe(p.getInputStream(), System.out, "CO> ");
            pipe(p.getErrorStream(), System.err, "CE> ");

            // initial service manager
            XMultiComponentFactory localServiceManager
                    = localContext.getServiceManager();
            if (localServiceManager == null)
                throw new BootstrapException("no initial service manager");

            // create a URL resolver
            XUnoUrlResolver urlResolver = UnoUrlResolver.create(localContext);

            // connection string
            _connectParams = "pipe,name=" + pipeName;
            String connect = "uno:" + _connectParams
                    + ";urp;StarOffice.ComponentContext";

            // wait until office is started
            for (; ;) {
                try {
                    // try to connect to office
                    Object context = urlResolver.resolve(connect);
                    context = UnoRuntime.queryInterface(
                            XComponentContext.class, context);
                    if (context == null)
                        throw new BootstrapException("no component context!");
                    break;
                } catch (NoConnectException exception) {
                    // wait 500 ms, then try to connect again
                    Thread.sleep(500);
                }
            }
        } catch (BootstrapException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BootstrapException(exception.getMessage(), exception);
        }
    }

    /**
     * Returns the connection parameters.
     *
     * @return the connection parameters
     */
    public String getConnectionParameters() {
        return _connectParams;
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
