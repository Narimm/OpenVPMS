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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMObjectReportException;
import static org.openvpms.report.IMObjectReportException.ErrorCode.FailedToGenerateReport;
import static org.openvpms.report.IMObjectReportException.ErrorCode.UnsupportedMimeTypes;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Base class for jasper based reports.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractJasperReport {

    /**
     * The mime-type of the generated document.
     */
    private final String mimeType;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a new <code>AbstractJasperReport</code>.
     *
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @param service   the archetype service
     * @throws IMObjectReportException if no mime-type is supported
     */
    public AbstractJasperReport(String[] mimeTypes,
                                IArchetypeService service) {
        String type = null;
        for (String mimeType : mimeTypes) {
            if (DocFormats.PDF_TYPE.equals(mimeType)
                    || DocFormats.RTF_TYPE.equals(mimeType)) {
                type = mimeType;
                break;
            }
        }
        if (type == null) {
            throw new IMObjectReportException(UnsupportedMimeTypes);
        }
        mimeType = type;
        this.service = service;
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
     * Returns the preferred mime-type.
     *
     * @return the mime-type
     */
    protected String getMimeType() {
        return mimeType;
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
     * @param report the report to convert
     * @return a document containing the report
     * @throws IMObjectReportException   for any error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Document convert(JasperPrint report) {
        Document document = (Document) getArchetypeService().create(
                "document.other");
        try {
            byte[] data;
            String ext;
            if (DocFormats.PDF_TYPE.equals(getMimeType())) {
                data = JasperExportManager.exportReportToPdf(report);
                ext = DocFormats.PDF_EXT;
            } else {
                data = exportToRTF(report);
                ext = DocFormats.RTF_EXT;
            }
            document.setName(report.getName() + "." + ext);
            document.setContents(data);
            document.setMimeType(getMimeType());
            document.setDocSize(data.length);
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
