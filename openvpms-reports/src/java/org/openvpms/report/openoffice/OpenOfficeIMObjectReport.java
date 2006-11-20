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

import org.apache.commons.io.FilenameUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ExpressionEvaluator;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportException;
import static org.openvpms.report.IMObjectReportException.ErrorCode.FailedToGenerateReport;
import static org.openvpms.report.IMObjectReportException.ErrorCode.UnsupportedMimeTypes;
import org.openvpms.report.PrintProperties;

import java.util.Collection;
import java.util.List;


/**
 * Generates a report for an <code>IMObject</code>, using an OpenOffice document
 * as the template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OpenOfficeIMObjectReport implements IMObjectReport {

    /**
     * The document template.
     */
    private final Document template;


    /**
     * Creates a new <code>OpenOfficeIMObjectReport</code>.
     *
     * @param template the document template
     * @throws IMObjectReportException if the mime-type is invalid
     */
    public OpenOfficeIMObjectReport(Document template) {
        this.template = template;
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects   the objects to report on
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @return a document containing the report
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document generate(Collection<IMObject> objects, String[] mimeTypes) {
        String mimeType = null;
        for (String type : mimeTypes) {
            if (DocFormats.ODT_TYPE.equals(type)
                    || DocFormats.PDF_TYPE.equals(type)) {
                mimeType = type;
                break;
            }
        }
        if (mimeType == null) {
            throw new IMObjectReportException(UnsupportedMimeTypes);
        }

        OpenOfficeDocument doc = null;
        try {
            doc = create(objects);
            return export(doc, mimeType);
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param objects    the objects to report on
     * @param properties the print properties
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void print(Collection<IMObject> objects,
                      PrintProperties properties) {
        OpenOfficeDocument doc = null;
        try {
            doc = create(objects);
            PrintService service = OpenOfficeHelper.getPrintService();
            service.print(doc, properties.getPrinterName());
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }

    /**
     * Creates an openoffice document from a collection of objects.
     * Note that the collection is limited to a single object.
     *
     * @param objects the objects to generate the document from
     * @return a new openoffice document
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    private OpenOfficeDocument create(Collection<IMObject> objects) {
        OpenOfficeDocument doc = null;
        if (objects.size() != 1) {
            throw new IMObjectReportException(
                    FailedToGenerateReport,
                    "Can only report on single objects");
        }
        IMObject object = objects.toArray(new IMObject[0])[0];
        ExpressionEvaluator eval = new ExpressionEvaluator(
                object, ArchetypeServiceHelper.getArchetypeService());

        try {
            doc = new OpenOfficeDocument(template,
                                         OpenOfficeHelper.getService());
            List<String> fieldNames = doc.getUserFieldNames();
            for (String name : fieldNames) {
                String value = doc.getUserField(name);
                if (value != null) {
                    value = eval.getFormattedValue(value);
                    doc.setUserField(name, value);
                }
            }
            // refresh the text fields
            doc.refresh();
        } catch (OpenVPMSException exception) {
            if (doc != null) {
                doc.close();
            }
            throw exception;
        }
        return doc;
    }

    /**
     * Exports a document, serializing to a {@link Document}.
     *
     * @param doc      the document to export
     * @param mimeType the mime-type of the document     *
     * @return a new document
     * @throws OpenOfficeException       for any error
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Document export(OpenOfficeDocument doc, String mimeType) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        String name = template.getName();
        if (!DocFormats.ODT_TYPE.equals(mimeType)) {
            name = FilenameUtils.removeExtension(name);
        }
        return doc.export(mimeType, name, service);
    }
}
