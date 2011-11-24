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

package org.openvpms.web.component.im.print;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.util.ArchetypeHandler;
import org.openvpms.web.component.im.util.ArchetypeHandlers;


/**
 * A factory for {@link IMPrinter} instances. The factory is configured to return
 * specific {@link IMPrinter} implementations based on the supplied criteria, with
 * {@link IMObjectReportPrinter} returned if no implementation matches.
 * <p/>
 * The factory is configured using a <em>IMObjectPrinterFactory.properties</em> file,
 * located in the class path. The file contains pairs of archetype short names
 * and their corresponding printer implementations. Short names may be wildcarded
 * e.g:
 * <p/>
 * <table>
 * <tr><td>party.*</td><td>org.openvpms.web.component.im.print.APrinter</td></tr>
 * <tr><td>lookup.*</td><td>org.openvpms.web.component.im.print.BPrinter</td></tr>
 * <tr><td>act.customerAccountChargesInvoice.*</td><td>org.openvpms.web.component.im.print.CPrinter</td></tr>
 * </table>
 * <p/>
 * Multiple <em>IMPrinterFactory.properties</em> may be used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-17 06:51:11Z $
 */
public final class IMPrinterFactory {

    /**
     * IMPrinter implementations.
     */
    private static ArchetypeHandlers<IMPrinter> printers;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMPrinterFactory.class);


    /**
     * Prevent construction.
     */
    private IMPrinterFactory() {
    }

    /**
     * Construct a new {@link IMPrinter}.
     * <p/>
     * IMPrinter implementations must provide a public constructor accepting the object to print, and optionally a
     * document locator.
     *
     * @param object  the object to print
     * @param locator the document template locator
     * @return a new printer
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> IMPrinter<T> create(T object, DocumentTemplateLocator locator) {
        String[] shortNames = {object.getArchetypeId().getShortName()};
        shortNames = DescriptorHelper.getShortNames(shortNames);
        ArchetypeHandler<IMPrinter> handler = getPrinters().getHandler(shortNames);
        IMPrinter<T> result = null;
        if (handler != null) {
            try {
                try {
                    result = handler.create(new Object[]{object, locator});
                } catch (NoSuchMethodException throwable) {
                    result = handler.create(new Object[]{object});
                }
            } catch (Throwable exception) {
                log.error(exception, exception);
            }
        }
        if (result == null) {
            result = new IMObjectReportPrinter<T>(object, locator);
        }
        return result;
    }

    /**
     * Returns the printer implementations.
     *
     * @return the printers
     */
    private static ArchetypeHandlers<IMPrinter> getPrinters() {
        if (printers == null) {
            printers = new ArchetypeHandlers<IMPrinter>("IMPrinterFactory.properties", IMPrinter.class);
        }
        return printers;
    }

}
