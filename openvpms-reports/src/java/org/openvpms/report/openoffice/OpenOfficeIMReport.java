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
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ExpressionEvaluator;
import org.openvpms.report.ExpressionEvaluatorFactory;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;
import static org.openvpms.report.ReportException.ErrorCode.*;
import org.openvpms.report.ParameterType;
import org.openvpms.report.PrintProperties;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Generates a report using an OpenOffice document as the template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OpenOfficeIMReport<T> implements IMReport<T> {

    /**
     * The document template.
     */
    private final Document template;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;


    /**
     * Creates a new <code>OpenOfficeIMReport</code>.
     *
     * @param template the document template
     * @param handlers the document handlers
     * @throws ReportException if the mime-type is invalid
     */
    public OpenOfficeIMReport(Document template,
                              DocumentHandlers handlers) {
        this.template = template;
        this.handlers = handlers;
    }

    /**
     * Returns the set of parameter types that may be supplied to the report.
     *
     * @return the parameter types
     */
    public Set<ParameterType> getParameterTypes() {
        return new HashSet<ParameterType>();
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException if invoked
     */
    public Document generate(Map<String, Object> parameters,
                             String[] mimeTypes) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException if invoked
     */
    public void print(Map<String, Object> parameters,
                      PrintProperties properties) {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects   the objects to report on
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @return a document containing the report
     * @throws ReportException for any report error
     */
    public Document generate(Iterator<T> objects, String[] mimeTypes) {
        String mimeType = null;
        for (String type : mimeTypes) {
            if (DocFormats.ODT_TYPE.equals(type)
                    || DocFormats.PDF_TYPE.equals(type)) {
                mimeType = type;
                break;
            }
        }
        if (mimeType == null) {
            throw new ReportException(UnsupportedMimeTypes);
        }


        OpenOfficeDocument doc = null;
        OOConnection connection = null;
        try {
            OOConnectionPool pool = OpenOfficeHelper.getConnectionPool();
            connection = pool.getConnection();
            doc = create(objects, connection);
            return export(doc, mimeType);
        } finally {
            if (doc != null) {
                doc.close();
            }
            OpenOfficeHelper.close(connection);
        }
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param objects    the objects to report on
     * @param properties the print properties
     * @throws ReportException         for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void print(Iterator<T> objects, PrintProperties properties) {
        OOConnection connection = null;
        try {
            PrintService service = OpenOfficeHelper.getPrintService();
            connection = OpenOfficeHelper.getConnectionPool().getConnection();
            OpenOfficeDocument doc = create(objects, connection);
            service.print(doc, properties.getPrinterName(), true);
        } catch (OpenOfficeException exception) {
            throw new ReportException(exception,
                                      FailedToPrintReport,
                                      exception.getMessage());
        } finally {
            OpenOfficeHelper.close(connection);
        }
    }

    /**
     * Creates an openoffice document from a collection of objects.
     * Note that the collection is limited to a single object.
     *
     * @param objects    the objects to generate the document from
     * @param connection a connection to the OpenOffice service
     * @return a new openoffice document
     * @throws ReportException         for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    private OpenOfficeDocument create(Iterator<T> objects,
                                      OOConnection connection) {
        OpenOfficeDocument doc = null;
        T object = null;
        if (objects.hasNext()) {
            object = objects.next();
        }
        if (object == null || objects.hasNext()) {
            throw new ReportException(
                    FailedToGenerateReport,
                    "Can only report on single objects");
        }
        ExpressionEvaluator eval = ExpressionEvaluatorFactory.create(
                object, ArchetypeServiceHelper.getArchetypeService());

        try {
            doc = new OpenOfficeDocument(template, connection, handlers);
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
     * @throws OpenOfficeException for any error
     */
    private Document export(OpenOfficeDocument doc, String mimeType) {
        String name = template.getName();
        if (!DocFormats.ODT_TYPE.equals(mimeType)) {
            name = FilenameUtils.removeExtension(name);
        }
        return doc.export(mimeType, name);
    }
}
