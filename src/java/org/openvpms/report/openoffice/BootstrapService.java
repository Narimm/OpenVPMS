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
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import org.openvpms.report.IMObjectReportException;

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
     * The component context.
     */
    private static XComponentContext _context;


    /**
     * Returns the component context.
     *
     * @return the component context
     * @throws IMObjectReportException if a the component context can't be
     *                                 bootstrapped
     */
    public static XComponentContext getComponentContext() {
        if (_context == null) {
            try {
                bootstrap();
            } catch (Throwable exception) {
                throw new IMObjectReportException(exception);
            }
        }
        return _context;
    }

    /**
     * Bootstraps the component context from a UNO installation.
     *
     * @throws BootstrapException if the component context can't be
     *                            bootstrapped
     */
    private static void bootstrap() throws BootstrapException {
        try {
            // create default local component context
            XComponentContext xLocalContext =
                    Bootstrap.createInitialComponentContext(null);
            if (xLocalContext == null)
                throw new BootstrapException("no local component context!");

            String sOffice =
                    System.getProperty("os.name").startsWith("Windows") ?
                            "soffice.exe" : "soffice";
            // create random pipe name
            String sPipeName = "uno" +
                    Long.toString(
                            (new Random()).nextLong() & 0x7fffffffffffffffL);

            // create call with arguments
            String[] cmdArray = new String[7];
            cmdArray[0] = sOffice;
            cmdArray[1] = "-nologo";
            cmdArray[2] = "-nodefault";
            cmdArray[3] = "-norestore";
            cmdArray[4] = "-nocrashreport";
            cmdArray[5] = "-nolockcheck";
            cmdArray[6] = "-accept=pipe,name=" + sPipeName + ";urp;";

            // start office process
            Process p = Runtime.getRuntime().exec(cmdArray);
            pipe(p.getInputStream(), System.out, "CO> ");
            pipe(p.getErrorStream(), System.err, "CE> ");

            // initial service manager
            XMultiComponentFactory xLocalServiceManager =
                    xLocalContext.getServiceManager();
            if (xLocalServiceManager == null)
                throw new BootstrapException("no initial service manager!");

            // create a URL resolver
            XUnoUrlResolver xUrlResolver =
                    UnoUrlResolver.create(xLocalContext);

            // connection string
            String sConnect = "uno:pipe,name=" + sPipeName +
                    ";urp;StarOffice.ComponentContext";

            // wait until office is started
            for (; ;) {
                try {
                    // try to connect to office
                    Object context = xUrlResolver.resolve(sConnect);
                    _context = (XComponentContext) UnoRuntime.queryInterface(
                            XComponentContext.class, context);
                    if (_context == null)
                        throw new BootstrapException("no component context!");
                    break;
                } catch (com.sun.star.connection.NoConnectException ex) {
                    // wait 500 ms, then try to connect again
                    Thread.sleep(500);
                }
            }
        } catch (BootstrapException e) {
            throw e;
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new BootstrapException(e.getMessage(), e);
        }
    }

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
                } catch (java.io.IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }.start();
    }

}
