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

package org.openvpms.web.component.print;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.doc.DocumentTemplatePrinter;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.system.ServiceHelper;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;


/**
 * Print helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PrintHelper {

    /**
     * Helper to return the default printer for a template for the context practice or location.
     * <p/>
     * The printer associated with the location will be returned if present, otherwise the practice relationship will
     * be returned.
     *
     * @param template the document template
     * @param context  the context
     * @return the default printer, or <tt>null</tt> if none is defined
     */
    public static String getDefaultPrinter(DocumentTemplate template, Context context) {
        String result;
        DocumentTemplatePrinter printer = getDocumentTemplatePrinter(template, context);
        if (printer != null) {
            result = printer.getPrinterName();
        } else {
            result = getDefaultLocationPrinter(context.getLocation());
        }
        return result;
    }

    /**
     * Returns the <em>entityRelationship.documentTemplatePrinter</em>
     * associated with an <em>entity.documentTemplate</em> for the context practice or location.
     * <p/>
     * The location relationship will be returned if present, otherwise the practice relationship will be returned.
     *
     * @param template an <em>entity.documentTemplate</em>
     * @param context  the context
     * @return the corresponding document template printer relationship, or <tt>null</tt> if none is found
     */
    public static DocumentTemplatePrinter getDocumentTemplatePrinter(Entity template, Context context) {
        return getDocumentTemplatePrinter(new DocumentTemplate(template, ServiceHelper.getArchetypeService()), context);
    }

    /**
     * Returns the <em>entityRelationship.documentTemplatePrinter</em>
     * associated with an <em>entity.documentTemplate</em> for the context practice or location.
     * <p/>
     * The location relationship will be returned if present, otherwise the practice relationship will be returned.
     *
     * @param template the document template
     * @param context  the context
     * @return the corresponding document template printer relationship, or <tt>null</tt> if none is found
     */
    public static DocumentTemplatePrinter getDocumentTemplatePrinter(DocumentTemplate template, Context context) {
        DocumentTemplatePrinter printer = null;
        Party location = context.getLocation();
        Party practice = context.getPractice();
        if (location != null) {
            printer = template.getPrinter(location);
        }
        if (printer == null && practice != null) {
            printer = template.getPrinter(practice);
        }
        return printer;
    }

    /**
     * Returns the default printer.
     *
     * @return the default printer, or <tt>null</tt> if none is defined
     */
    public static String getDefaultPrinter() {
        PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
        return (printer != null) ? printer.getName() : null;
    }

    /**
     * Returns a list of the available printers.
     *
     * @return a list of the available printers
     */
    public static String[] getPrinters() {
        PrintService[] printers = PrintServiceLookup.lookupPrintServices(
                null, null);
        String[] names = new String[printers.length];
        for (int i = 0; i < names.length; ++i) {
            names[i] = printers[i].getName();
        }
        return names;
    }

    /**
     * Helper to return the default printer for a location.
     * If no default printer set than returns system default printer.
     *
     * @param location the location. May be <tt>null</tt>
     * @return the printer name. May be <tt>null</tt> if none is defined
     */
    private static String getDefaultLocationPrinter(Party location) {
        if (location != null) {
            IMObjectBean bean = new IMObjectBean(location);
            if (bean.hasNode("defaultPrinter")) {
                return bean.getString("defaultPrinter", getDefaultPrinter());
            }
        }
        return PrintHelper.getDefaultPrinter();
    }

}
