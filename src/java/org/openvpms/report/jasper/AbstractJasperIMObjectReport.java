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

package org.openvpms.report.jasper;

import static org.openvpms.report.IMObjectReportException.ErrorCode.FailedToGenerateReport;
import static org.openvpms.report.IMObjectReportException.ErrorCode.UnsupportedMimeTypes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrinterName;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;

import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.report.PrintProperties;


/**
 * Abstract implementation of the {@link JasperIMObjectReport} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractJasperIMObjectReport
        implements JasperIMObjectReport {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;


    /**
     * Constructs a new <code>AbstractJasperIMObjectReport</code>.
     *
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public AbstractJasperIMObjectReport(IArchetypeService service,
                                        DocumentHandlers handlers) {
        this.service = service;
        this.handlers = handlers;
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
        Document document;
        String mimeType = null;
        for (String type : mimeTypes) {
            if (DocFormats.PDF_TYPE.equals(type)
                    || DocFormats.RTF_TYPE.equals(type)) {
                mimeType = type;
                break;
            }
        }
        if (mimeType == null) {
            throw new IMObjectReportException(UnsupportedMimeTypes);
        }
        try {
            JasperPrint print = report(objects);
            document = convert(print, mimeType);
        } catch (JRException exception) {
            throw new IMObjectReportException(exception, FailedToGenerateReport,
                                              exception.getMessage());
        }
        return document;
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
        try {
            JasperPrint print = report(objects);
            JRPrintServiceExporter exporter = new JRPrintServiceExporter();
            exporter.setParameter(JRPrintServiceExporterParameter.JASPER_PRINT,
                                  print);

            // print 1 copy
            PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
            aset.add(new Copies(1));
            aset.add(MediaSizeName.ISO_A4);
            exporter.setParameter(
                    JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET,
                    aset);

            // set the printer name
            PrintServiceAttributeSet serviceAttributeSet
                    = new HashPrintServiceAttributeSet();
            serviceAttributeSet.add(
                    new PrinterName(properties.getPrinterName(), null));
            exporter.setParameter(
                    JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET,
                    serviceAttributeSet);
            // print it
            exporter.exportReport();
        } catch (JRException exception) {
            throw new IMObjectReportException(exception, FailedToGenerateReport,
                                              exception.getMessage());
        }
    }

    /**
     * Generates a report for an object.
     *
     * @param objects
     * @return the report
     * @throws JRException for any error
     */
    public JasperPrint report(Collection<IMObject> objects) throws JRException {
        IMObjectCollectionDataSource source
                = new IMObjectCollectionDataSource(objects,
                                                   getArchetypeService());
        HashMap<String, Object> properties
                = new HashMap<String, Object>(getParameters());
        properties.put("dataSource", source);
        return JasperFillManager.fillReport(getReport(), properties, source);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the report parameters to use when filling the report.
     *
     * @return the report parameters
     */
    protected Map<String, Object> getParameters() {
        return new HashMap<String, Object>();
    }

    /**
     * Converts a report to a document.
     *
     * @param report   the report to convert
     * @param mimeType the mime-type of the document
     * @return a document containing the report
     * @throws IMObjectReportException   for any error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Document convert(JasperPrint report, String mimeType) {
        Document document;
        try {
            byte[] data;
            String ext;
            if (DocFormats.PDF_TYPE.equals(mimeType)) {
                data = JasperExportManager.exportReportToPdf(report);
                ext = DocFormats.PDF_EXT;
            } else {
                data = exportToRTF(report);
                ext = DocFormats.RTF_EXT;
            }
            String name = report.getName() + "." + ext;
            DocumentHandler handler = handlers.get(name, "document.other",
                                                   mimeType);
            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            document = handler.create(name, stream, mimeType, data.length);
        } catch (DocumentException exception) {
            throw new IMObjectReportException(exception, FailedToGenerateReport,
                                              exception.getMessage());
        } catch (JRException exception) {
            throw new IMObjectReportException(exception, FailedToGenerateReport,
                                              exception.getMessage());
        }
        return document;
    }

    /**
     * Exports a generated jasper report to an RTF stream.
     *
     * @param report the report
     * @return a new serialized RTF
     * @throws JRException if the export fails
     */
    protected byte[] exportToRTF(JasperPrint report) throws JRException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        JRRtfExporter exporter = new JRRtfExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, report);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
        exporter.exportReport();
        return stream.toByteArray();
    }
}
