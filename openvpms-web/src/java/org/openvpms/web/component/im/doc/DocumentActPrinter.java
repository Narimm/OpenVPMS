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

import java.util.Arrays;


/**
 * {@link DocumentAct} printer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActPrinter extends AbstractIMObjectPrinter {

    /**
     * Prints the object.
     *
     * @param object  the object to print
     * @param printer the printer
     */
    protected void doPrint(IMObject object, String printer) {
        try {
            DocumentAct act = (DocumentAct) object;
            Document doc = (Document) IMObjectHelper.getObject(
                    act.getDocReference());
            if (doc == null) {
                IMObjectReport report = createReport(object);
                report.print(Arrays.asList(object),
                             new PrintProperties(printer));
                printed(object);
            } else if (DocFormats.ODT_TYPE.equals(doc.getMimeType())) {
                OpenOfficeHelper.getPrintService().print(doc, printer);
            } else {
                doPrintPreview(object);
            }
            printed(object);
        } catch (OpenVPMSException exception) {
            if (isInteractive()) {
                ErrorHelper.show(exception);
            } else {
                failed(object, exception);
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
    protected IMObjectReport createReport(IMObject object) {
        DocumentAct act = (DocumentAct) object;
        ReportGenerator gen = new ReportGenerator(act);
        return gen.createReport();
    }

    /**
     * Returns a document for an object.
     *
     * @param object the object
     * @return a document
     * @throws DocumentException         if the document cannot be found
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Document getDocument(IMObject object) {
        DocumentAct act = (DocumentAct) object;
        Document doc = (Document) IMObjectHelper.getObject(
                act.getDocReference());
        if (doc == null) {
            ReportGenerator gen = new ReportGenerator(act);
            doc = gen.generate(act, DocFormats.PDF_TYPE);
        }
        return doc;
    }

}
