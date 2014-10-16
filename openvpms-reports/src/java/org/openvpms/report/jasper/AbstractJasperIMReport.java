/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.fill.JREvaluator;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.WriterExporterOutput;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
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

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.Sides;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.openvpms.report.ReportException.ErrorCode.FailedToGenerateReport;
import static org.openvpms.report.ReportException.ErrorCode.FailedToGetParameters;
import static org.openvpms.report.ReportException.ErrorCode.NoPagesToPrint;
import static org.openvpms.report.ReportException.ErrorCode.UnsupportedMimeType;


/**
 * Abstract implementation of the {@link JasperIMReport} interface.
 *
 * @author Tim Anderson
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
                                                DocFormats.CSV_TYPE,
                                                DocFormats.TEXT_TYPE};

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractJasperIMReport.class);


    /**
     * Constructs an {@link AbstractJasperIMReport}.
     *
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public AbstractJasperIMReport(IArchetypeService service, DocumentHandlers handlers) {
        this.service = service;
        this.handlers = handlers;
    }

    /**
     * Returns the set of parameter types that may be supplied to the report.
     *
     * @return the parameter types
     * @throws ReportException if a parameter expression can't be evaluated
     */
    @Override
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
                        throw new ReportException(exception, FailedToGetParameters);
                    }
                }
                ParameterType type = new ParameterType(p.getName(), p.getValueClass(), p.getDescription(),
                                                       defaultValue);
                types.add(type);
            }
        }
        return types;
    }

    /**
     * Determines if the report accepts the named parameter.
     *
     * @param name the parameter name
     * @return {@code true} if the report accepts the parameter, otherwise {@code false}
     */
    @Override
    public boolean hasParameter(String name) {
        for (JRParameter p : getReport().getParameters()) {
            if (ObjectUtils.equals(p.getName(), name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the default mime type for report documents.
     *
     * @return the default mime type
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public String getDefaultMimeType() {
        return DocFormats.PDF_TYPE;
    }

    /**
     * Returns the supported mime types for report documents.
     *
     * @return the supported mime types
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public String[] getMimeTypes() {
        return MIME_TYPES;
    }

    /**
     * Generates a report.
     * <p/>
     * The default mime type will be used to select the output format.
     *
     * @param parameters a map of parameter names and their values, to pass to the report
     * @return a document containing the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Document generate(Map<String, Object> parameters) {
        return generate(parameters, getDefaultMimeType());
    }

    /**
     * Generates a report.
     *
     * @param parameters a map of parameter names and their values, to pass to the report
     * @param mimeType   the output format of the report
     * @return a document containing the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Document generate(Map<String, Object> parameters, String mimeType) {
        Document document;
        Map<String, Object> properties = getDefaultParameters();
        if (parameters != null) {
            properties.putAll(parameters);
        }
        if (mimeType.equals(DocFormats.CSV_TYPE) || mimeType.equals(DocFormats.XLS_TYPE)) {
            properties.put(JRParameter.IS_IGNORE_PAGINATION, true);
        }
        try {
            JasperPrint print = JasperFillManager.fillReport(getReport(), properties);
            document = export(print, properties, mimeType);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport, exception.getMessage());
        }
        return document;
    }

    /**
     * Generates a report for a collection of objects.
     * <p/>
     * The default mime type will be used to select the output format.
     *
     * @param objects the objects to report on
     * @return a document containing the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Document generate(Iterator<T> objects) {
        return generate(objects, getDefaultMimeType());
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects  the objects to report on
     * @param mimeType the output format of the report
     * @return a document containing the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
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
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @return a document containing the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Document generate(Iterator<T> objects, Map<String, Object> parameters) {
        return generate(objects, parameters, getDefaultMimeType());
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param mimeType   the output format of the report
     * @return a document containing the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Document generate(Iterator<T> objects, Map<String, Object> parameters, String mimeType) {
        Document document;
        try {
            if (mimeType.equals(DocFormats.CSV_TYPE) || mimeType.equals(DocFormats.XLS_TYPE)) {
                parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);
            }
            JasperPrint print = report(objects, parameters);
            document = export(print, parameters, mimeType);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport, exception.getMessage());
        }
        return document;
    }

    /**
     * Generates a report for a collection of objects to the specified stream.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param mimeType   the output format of the report
     * @param stream     the stream to write to
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public void generate(Iterator<T> objects, Map<String, Object> parameters, String mimeType, OutputStream stream) {
        try {
            if (mimeType.equals(DocFormats.CSV_TYPE) || mimeType.equals(DocFormats.XLS_TYPE)) {
                parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);
            }
            JasperPrint report = report(objects, parameters);
            export(report, stream, parameters, mimeType);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport, exception.getMessage());
        }
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report. May be {@code null}
     * @param properties the print properties
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public void print(Map<String, Object> parameters, PrintProperties properties) {
        Map<String, Object> params = getDefaultParameters();
        if (parameters != null) {
            params.putAll(parameters);
        }
        try {
            JasperPrint print = JasperFillManager.fillReport(getReport(), params);
            print(print, properties);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport, exception.getMessage());
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
    @Override
    public void print(Iterator<T> objects, PrintProperties properties) {
        Map<String, Object> empty = Collections.emptyMap();
        print(objects, empty, properties);
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report
     * @param properties the print properties
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void print(Iterator<T> objects, Map<String, Object> parameters, PrintProperties properties) {
        try {
            JasperPrint print = report(objects, parameters);
            print(print, properties);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport, exception.getMessage());
        }
    }

    /**
     * Generates a report.
     *
     * @param objects the objects to report on
     * @return the report the report
     * @throws JRException for any error
     */
    @Override
    public JasperPrint report(Iterator<T> objects) throws JRException {
        return report(objects, null);
    }

    /**
     * Generates a report.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report. May be {@code null}
     * @return the report
     * @throws JRException for any error
     */
    public JasperPrint report(Iterator<T> objects, Map<String, Object> parameters) throws JRException {
        JRDataSource source = createDataSource(objects);
        HashMap<String, Object> properties = new HashMap<String, Object>(getDefaultParameters());
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
     * @param report     the report to convert
     * @param parameters export parameters
     * @param mimeType   the mime-type of the document
     * @return a document containing the report
     * @throws ReportException           for any error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Document export(JasperPrint report, Map<String, Object> parameters, String mimeType) {
        Document document;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream(2048);
            String ext = export(report, output, parameters, mimeType);
            byte[] data = output.toByteArray();

            String name = report.getName() + "." + ext;
            DocumentHandler handler = handlers.get(name, DocumentArchetypes.DEFAULT_DOCUMENT, mimeType);
            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            document = handler.create(name, stream, mimeType, data.length);
        } catch (DocumentException exception) {
            throw new ReportException(exception, FailedToGenerateReport, exception.getMessage());
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport, exception.getMessage());
        } catch (Exception exception) {
            throw new ReportException(exception, FailedToGenerateReport, exception.getMessage());
        }
        return document;
    }

    /**
     * Exports a report to a stream as the specified mime type.
     *
     * @param report     the report to export
     * @param stream     the stream to write to
     * @param mimeType   the mime type to export as
     * @param parameters export parameters
     * @return the file name extension of the mime-type
     * @throws JRException if the export fails
     */
    protected String export(JasperPrint report, OutputStream stream, Map<String, Object> parameters,
                            String mimeType) throws JRException {
        String ext;
        if (DocFormats.PDF_TYPE.equals(mimeType)) {
            exportToPDF(report, stream);
            ext = DocFormats.PDF_EXT;
        } else if (DocFormats.RTF_TYPE.equals(mimeType)) {
            exportToRTF(report, stream);
            ext = DocFormats.RTF_EXT;
        } else if (DocFormats.XLS_TYPE.equals(mimeType)) {
            exportToXLS(report, stream);
            ext = DocFormats.XLS_EXT;
        } else if (DocFormats.CSV_TYPE.equals(mimeType)) {
            exportToCSV(report, stream);
            ext = DocFormats.CSV_EXT;
        } else if (DocFormats.XML_TYPE.equals(mimeType)) {
            exportToXML(report, stream);
            ext = DocFormats.XML_EXT;
        } else if (DocFormats.TEXT_TYPE.equals(mimeType)) {
            exportToText(report, stream, parameters);
            ext = DocFormats.TEXT_EXT;
        } else {
            throw new ReportException(UnsupportedMimeType, mimeType);
        }
        return ext;
    }

    /**
     * Exports a generated jasper report to PDF.
     *
     * @param report the report
     * @param stream the stream to write to
     * @throws JRException if the export fails
     */
    private void exportToPDF(JasperPrint report, OutputStream stream) throws JRException {
        exportStream(report, stream, new JRPdfExporter());
    }

    /**
     * Exports a generated jasper report to an RTF stream.
     *
     * @param report the report
     * @param stream the stream to write to
     * @throws JRException if the export fails
     */
    private void exportToRTF(JasperPrint report, OutputStream stream) throws JRException {
        exportWriter(report, stream, new JRRtfExporter());
    }

    /**
     * Exports a generated jasper report to a stream as XLS.
     *
     * @param report the report
     * @param stream the stream to write to
     * @throws JRException if the export fails
     */
    private void exportToXLS(JasperPrint report, OutputStream stream) throws JRException {
        JRXlsExporter exporter = new JRXlsExporter();
        SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
        configuration.setDetectCellType(true);
        configuration.setWhitePageBackground(false);
        configuration.setIgnorePageMargins(true);
        configuration.setCollapseRowSpan(true);
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setRemoveEmptySpaceBetweenColumns(true);
        exporter.setConfiguration(configuration);
        exportStream(report, stream, exporter);
    }

    /**
     * Exports a generated jasper report to a stream as CSV.
     *
     * @param report the report
     * @param stream the stream to write to
     * @throws JRException if the export fails
     */
    private void exportToCSV(JasperPrint report, OutputStream stream) throws JRException {
        JRCsvExporter exporter = new JRCsvExporter();
        SimpleCsvExporterConfiguration configuration = new SimpleCsvExporterConfiguration();
        configuration.setFieldDelimiter(",");
        configuration.setRecordDelimiter("\n");
        exporter.setConfiguration(configuration);
        exportWriter(report, stream, exporter);
    }

    /**
     * Exports a generated jasper report to a stream as XML.
     *
     * @param report the report
     * @param stream the stream to write to
     * @throws JRException if the export fails
     */
    private void exportToXML(JasperPrint report, OutputStream stream) throws JRException {
        exportWriter(report, stream, new JRXmlExporter());
    }

    /**
     * Exports a generated jasper report to a stream as plain text.
     *
     * @param report the report
     * @param stream the stream to write to
     * @throws JRException if the export fails
     */
    private void exportToText(JasperPrint report, OutputStream stream, Map<String, Object> parameters)
            throws JRException {
        JRTextExporter exporter = new JRTextExporter();
        ReportContext context = JasperReportHelper.createReportContext(parameters);
        exporter.setReportContext(context);
        exportWriter(report, stream, exporter);
    }

    /**
     * Exports a report using a stream.
     *
     * @param report   the report to export
     * @param stream   the stream to export to
     * @param exporter the exporter
     * @throws JRException if the export fails
     */
    private void exportStream(JasperPrint report, OutputStream stream,
                              Exporter<ExporterInput, ? extends ReportExportConfiguration,
                                      ? extends ExporterConfiguration, OutputStreamExporterOutput> exporter)
            throws JRException {
        exporter.setExporterInput(new SimpleExporterInput(report));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(stream));
        exporter.exportReport();
    }

    /**
     * Exports a report using a writer.
     *
     * @param report   the report to export
     * @param stream   the underlying stream to export to
     * @param exporter the exporter
     * @throws JRException if the export fails
     */
    private void exportWriter(JasperPrint report, OutputStream stream,
                              Exporter<ExporterInput, ? extends ReportExportConfiguration,
                                      ? extends ExporterConfiguration, WriterExporterOutput> exporter)
            throws JRException {
        exporter.setExporterInput(new SimpleExporterInput(report));
        exporter.setExporterOutput(new SimpleWriterExporterOutput(stream));
        exporter.exportReport();
    }

    /**
     * Prints a {@code JasperPrint} to a printer.
     *
     * @param print      the object to print
     * @param properties the print properties
     * @throws ReportException if {@code print} contains no pages
     * @throws JRException     for any error
     */
    private void print(JasperPrint print, PrintProperties properties) throws JRException {
        if (print.getPages().isEmpty()) {
            throw new ReportException(NoPagesToPrint);
        }
        if (log.isDebugEnabled()) {
            log.debug("PrinterName: " + properties.getPrinterName());
        }

        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(new Copies(properties.getCopies()));
        MediaSizeName mediaSize = properties.getMediaSize();
        OrientationRequested orientation = properties.getOrientation();
        MediaTray tray = properties.getMediaTray();
        Sides sides = properties.getSides();
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
        if (sides != null) {
            if (log.isDebugEnabled()) {
                log.debug("Sides: " + sides);
            }
            aset.add(sides);
        }
        SimplePrintServiceExporterConfiguration printConfiguration = new SimplePrintServiceExporterConfiguration();
        printConfiguration.setPrintRequestAttributeSet(aset);
        // set the printer name
        PrintServiceAttributeSet serviceAttributeSet = new HashPrintServiceAttributeSet();
        serviceAttributeSet.add(new PrinterName(properties.getPrinterName(), null));
        printConfiguration.setPrintServiceAttributeSet(serviceAttributeSet);
        exporter.setConfiguration(printConfiguration);
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
