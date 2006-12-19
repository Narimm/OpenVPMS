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

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.openoffice.OpenOfficeHelper;
import org.openvpms.web.component.im.print.AbstractIMObjectPrinter;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * {@link DocumentAct} printer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActPrinter extends AbstractIMObjectPrinter<DocumentAct> {

    /**
     * Prints the object.
     *
     * @param act     the act to print
     * @param printer the printer name
     */
    @Override
    protected void doPrint(DocumentAct act, String printer) {
        try {
            Document doc = (Document) IMObjectHelper.getObject(
                    act.getDocReference());
            if (doc == null) {
                IMObjectReport report = createReport(act);
                List<IMObject> objects = new ArrayList<IMObject>();
                objects.add(act);
                report.print(objects, getProperties(act, printer));
                printed(act);
            } else if (DocFormats.ODT_TYPE.equals(doc.getMimeType())) {
                OpenOfficeHelper.getPrintService().print(
                        doc, printer);
            } else {
                doPrintPreview(act);
            }
            printed(act);
        } catch (OpenVPMSException exception) {
            if (isInteractive()) {
                ErrorHelper.show(exception);
            } else {
                failed(act, exception);
            }
        }
    }

    /**
     * Creates a new report.
     *
     * @param object the object to report on
     * @return a new report
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObjectReport createReport(DocumentAct object) {
        ReportGenerator gen = new ReportGenerator(object);
        return gen.createReport();
    }

    /**
     * Returns a document for an object.
     *
     * @param object the object
     * @return a document
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Document getDocument(DocumentAct object) {
        Document doc = (Document) IMObjectHelper.getObject(
                object.getDocReference());
        if (doc == null) {
            ReportGenerator gen = new ReportGenerator(object);
            doc = gen.generate(object, DocFormats.PDF_TYPE);
        }
        return doc;
    }

    /**
     * Returns the default printer for an object.
     *
     * @return the default printer, or <code>null</code> if there is
     *         none defined
     */
    protected String getDefaultPrinter(DocumentAct object) {
        ReportGenerator gen = new ReportGenerator(object);
        return gen.getDefaultPrinter();
    }

    /**
     * Returns the print properties for an object.
     *
     * @param object  the object to print
     * @param printer the printer
     * @return the print properties
     */
    @Override
    protected PrintProperties getProperties(DocumentAct object,
                                            String printer) {
        PrintProperties properties = super.getProperties(object, printer);
        ReportGenerator gen = new ReportGenerator(object);
        properties.setMediaSize(gen.getMediaSize());
        properties.setMediaTray(gen.getMediaTray(printer));
        return properties;
    }
}
