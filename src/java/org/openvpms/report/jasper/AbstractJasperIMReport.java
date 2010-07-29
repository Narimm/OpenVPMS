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

import static org.openvpms.report.ReportException.ErrorCode.UnsupportedMimeType;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.fill.JREvaluator;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ParameterType;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.ReportException;
import static org.openvpms.report.ReportException.ErrorCode.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrinterName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Abstract implementation of the {@link JasperIMReport} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractJasperIMReport<T> implements JasperIMReport<T> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Cached expression evaluator.
     */
    private JREvaluator evaluator;

    /**
     * The supported mime types.
     */
    private static final String[] MIME_TYPES = {DocFormats.PDF_TYPE,
                                                DocFormats.RTF_TYPE,
                                                DocFormats.XLS_TYPE,
                                                DocFormats.CSV_TYPE};

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(
            AbstractJasperIMReport.class);


    /**
     * Constructs a new <tt>AbstractJasperIMReport</tt>.
     *
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public AbstractJasperIMReport(IArchetypeService service,
                                  DocumentHandlers handlers) {
        this.service = service;
        this.handlers = handlers;
    }

    /**
     * Returns the set of parameter types that may be supplied to the report.
     *
     * @return the parameter types
     * @throws ReportException if a parameter expression can't be evaluated
     */
    public Set<ParameterType> getParameterTypes() {
        Set<ParameterType> types = new LinkedHashSet<ParameterType>();
        JasperReport report = getReport();
        for (JRParameter p : report.getParameters()) {
            if (!p.isSystemDefined() && p.isForPrompting()) {
                JRExpression expression = p.getDefaultValueExpression();
                Object defaultValue = null;
                if (expression != null) {
                    try {
                        defaultValue = getEvaluator().evaluate(expression);
                    } catch (JRException exception) {
                        throw new ReportException(FailedToGetParameters,
                                                  exception);
                    }
                }
                ParameterType type = new ParameterType(p.getName(),
                                                       p.getValueClass(),
                                                       p.getDescription(),
                                                       defaultValue);
                types.add(type);
            }
        }
        return types;
    }

    /**
     * Returns the default mime type for report documents.
     *
     * @return the default mime type
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     */
    public String getDefaultMimeType() {
        return DocFormats.PDF_TYPE;
    }

    /**
     * Returns the supported mime types for report documents.
     *
     * @return the supported mime types
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     */
    public String[] getMimeTypes() {
        return MIME_TYPES;
    }

    /**
     * Generates a report.
     * <p/>
     * The default mime type will be used to select the output format.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     */
    public Document generate(Map<String, Object> parameters) {
        return generate(parameters, getDefaultMimeType());
    }

    /**
     * Generates a report.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report
     * @param mimeType   the output format of the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     */
    public Document generate(Map<String, Object> parameters, String mimeType) {
        Document document;
        Map<String, Object> properties = getDefaultParameters();
        if (parameters != null) {
            properties.putAll(parameters);
        }
        if(mimeType.equals(DocFormats.CSV_TYPE) || mimeType.equals(DocFormats.XLS_TYPE)) {
            properties.put(JRParameter.IS_IGNORE_PAGINATION, true);
        }
        try {
            JasperPrint print = JasperFillManager.fillReport(getReport(),
                                                             properties);
            document = convert(print, mimeType);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());
        }
        return document;
    }

    /**
     * Generates a report.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report. May be <tt>null</tt>
     * @param mimeTypes  a list of mime-types, used to select the preferred
     *                   output format of the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     */
    @Deprecated
    public Document generate(Map<String, Object> parameters,
                             String[] mimeTypes) {
        return generate(parameters, getMimeType(mimeTypes));
    }

    /**
     * Generates a report for a collection of objects.
     * <p/>
     * The default mime type will be used to select the output format.
     *
     * @param objects the objects to report on
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     */
    public Document generate(Iterator<T> objects) {
        return generate(objects, getDefaultMimeType());
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects  the objects to report on
     * @param mimeType the output format of the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     */
    public Document generate(Iterator<T> objects, String mimeType) {
        Map<String, Object> empty = Collections.emptyMap();
        return generate(objects, empty, mimeType);
    }

    /**
     * Generates a report for a collection of objects.
     * <p/>
     * The default mime type will be used to select the output format.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report. May be <tt>null</tt>
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     */
    public Document generate(Iterator<T> objects,
                             Map<String, Object> parameters) {
        return generate(objects, parameters, getDefaultMimeType());
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report. May be <tt>null</tt>
     * @param mimeType   the output format of the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     */
    public Document generate(Iterator<T> objects,
                             Map<String, Object> parameters, String mimeType) {
        Document document;
        try {
            if(mimeType.equals(DocFormats.CSV_TYPE) || mimeType.equals(DocFormats.XLS_TYPE)) {
                parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);            	
            }
            JasperPrint print = report(objects, parameters);
            document = convert(print, mimeType);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());
        }
        return document;
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects   the objects to report on
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @return a document containing the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Deprecated
    public Document generate(Iterator<T> objects, String[] mimeTypes) {
        return generate(objects, getMimeType(mimeTypes));
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report
     * @param mimeTypes  a list of mime-types, used to select the preferred
     *                   output format of the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    @Deprecated
    public Document generate(Iterator<T> objects,
                             Map<String, Object> parameters,
                             String[] mimeTypes) {
        return generate(objects, parameters, getMimeType(mimeTypes));
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report. May be <tt>null</tt>
     * @param properties the print properties
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void print(Map<String, Object> parameters,
                      PrintProperties properties) {
        Map<String, Object> params = getDefaultParameters();
        if (parameters != null) {
            params.putAll(parameters);
        }
        try {
            JasperPrint print = JasperFillManager.fillReport(getReport(),
                                                             params);
            print(print, properties);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());

        }
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param objects    the objects to report on
     * @param properties the print properties
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void print(Iterator<T> objects, PrintProperties properties) {
        Map<String, Object> empty = Collections.emptyMap();
        print(objects, empty, properties);
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report
     * @param properties the print properties
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    public void print(Iterator<T> objects, Map<String, Object> parameters,
                      PrintProperties properties) {
        try {
            JasperPrint print = report(objects, parameters);
            print(print, properties);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());
        }
    }

    /**
     * Generates a report.
     *
     * @param objects the objects to report on
     * @return the report the report
     * @throws JRException for any error
     */
    public JasperPrint report(Iterator<T> objects) throws JRException {
        return report(objects, null);
    }

    /**
     * Generates a report.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report. May be <tt>null</tt>
     * @return the report
     * @throws JRException for any error
     */
    public JasperPrint report(Iterator<T> objects,
                              Map<String, Object> parameters)
            throws JRException {
        JRDataSource source = createDataSource(objects);
        HashMap<String, Object> properties
                = new HashMap<String, Object>(getDefaultParameters());
        if (parameters != null) {
            properties.putAll(parameters);
        }
        properties.put("dataSource", source);
        return JasperFillManager.fillReport(getReport(), properties, source);
    }

    /**
     * Creates a data source for a collection of objects.
     *
     * @param objects an iterator over the collection of objects
     * @return a new datas ource
     */
    protected abstract JRDataSource createDataSource(Iterator<T> objects);

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the document handlers.
     *
     * @return the document handlers
     */
    protected DocumentHandlers getDocumentHandlers() {
        return handlers;
    }

    /**
     * Returns the default report parameters to use when filling the report.
     *
     * @return the report parameters
     */
    protected Map<String, Object> getDefaultParameters() {
        return new HashMap<String, Object>();
    }

    /**
     * Converts a report to a document.
     *
     * @param report   the report to convert
     * @param mimeType the mime-type of the document
     * @return a document containing the report
     * @throws ReportException           for any error
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
            } else if (DocFormats.RTF_TYPE.equals(mimeType)) {
                data = exportToRTF(report);
                ext = DocFormats.RTF_EXT;
            } else if (DocFormats.XLS_TYPE.equals(mimeType)) {
            	data = exportToXLS(report);
            	ext = DocFormats.XLS_EXT;
            } else if (DocFormats.CSV_TYPE.equals(mimeType)) {
            	data = exportToCSV(report);
            	ext = DocFormats.CSV_EXT;
            } else if (DocFormats.XML_TYPE.equals(mimeType)) {
            	data = exportToXML(report);
            	ext = DocFormats.XML_EXT;
            } else {
                throw new ReportException(UnsupportedMimeType, mimeType);
            }
            String name = report.getName() + "." + ext;
            DocumentHandler handler = handlers.get(name, "document.other",
                                                   mimeType);
            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            document = handler.create(name, stream, mimeType, data.length);
        } catch (DocumentException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());
        } catch (Exception exception) {
            throw new ReportException(exception, FailedToGenerateReport,
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

    /**
     * Exports a generated jasper report to an XLS stream.
     *
     * @param report the report
     * @return a new serialized XLS
     * @throws JRException if the export fails
     */
    protected byte[] exportToXLS(JasperPrint report) throws JRException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        JRXlsExporter exporter = new JRXlsExporter();
        exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
        exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
        exporter.setParameter(JRXlsExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
        exporter.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, Boolean.TRUE);
        exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
        exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, report);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
        exporter.exportReport();
        return stream.toByteArray();
    }

    /**
     * Exports a generated jasper report to a CSV stream.
     *
     * @param report the report
     * @return a new serialized CSV
     * @throws JRException if the export fails
     */
    protected byte[] exportToCSV(JasperPrint report) throws JRException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER,",");
        exporter.setParameter(JRCsvExporterParameter.RECORD_DELIMITER,"\n");
        exporter.setParameter(JRCsvExporterParameter.IGNORE_PAGE_MARGINS,Boolean.TRUE);
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, report);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
        exporter.exportReport();
        return stream.toByteArray();
    }

    /**
     * Exports a generated jasper report to a XML stream.
     *
     * @param report the report
     * @return a new serialized XML
     * @throws JRException if the export fails
     */
    protected byte[] exportToXML(JasperPrint report) throws JRException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        JRXmlExporter exporter = new JRXmlExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, report);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
        exporter.exportReport();
        return stream.toByteArray();
    }

    /**
     * Returns the preferred mime-type from a list of mime types.
     *
     * @param mimeTypes the mime types
     * @return the preferred mime type
     * @throws ReportException if none of the mime types are supported
     */
    private String getMimeType(String[] mimeTypes) {
        if (mimeTypes.length == 0) {
            throw new ReportException(UnsupportedMimeType);
        }
        String mimeType = null;
        for (String type : mimeTypes) {
            if (DocFormats.PDF_TYPE.equals(type)
                    || DocFormats.RTF_TYPE.equals(type)) {
                mimeType = type;
                break;
            }
        }
        if (mimeType == null) {
            throw new ReportException(UnsupportedMimeType, mimeTypes[0]);
        }
        return mimeType;
    }

    /**
     * Prints a <tt>JasperPrint</tt> to a printer.
     *
     * @param print      the object to print
     * @param properties the print properties
     * @throws JRException for any error
     */
    private void print(JasperPrint print, PrintProperties properties)
            throws JRException {
        if (log.isDebugEnabled()) {
            log.debug("PrinterName: " + properties.getPrinterName());
        }

        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        exporter.setParameter(JRPrintServiceExporterParameter.JASPER_PRINT,
                              print);

        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(new Copies(properties.getCopies()));
        MediaSizeName mediaSize = properties.getMediaSize();
        OrientationRequested orientation = properties.getOrientation();
        MediaTray tray = properties.getMediaTray();
        if (mediaSize != null) {
            if (log.isDebugEnabled()) {
                log.debug("MediaSizeName: " + mediaSize);
            }
            aset.add(mediaSize);
        }
        if (orientation != null) {
            if (log.isDebugEnabled()) {
                log.debug("Orientation: " + orientation);
            }
            aset.add(orientation);
        }
        if (tray != null) {
            if (log.isDebugEnabled()) {
                log.debug("MediaTray: " + tray);
            }
            aset.add(tray);
        }
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
    }

    /**
     * Returns the expression evaluator.
     *
     * @return the expression evaluator
     * @throws JRException if the evaluator can't be loaded
     */
    private JREvaluator getEvaluator() throws JRException {
        if (evaluator == null) {
            evaluator = JasperCompileManager.loadEvaluator(getReport());
        }
        return evaluator;
    }
}
