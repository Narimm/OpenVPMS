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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.SimpleReportContext;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.io.IOUtils;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.io.InputStream;
import java.util.Map;

/**
 * Jasper Report helper.
 *
 * @author Tim Amderson
 */
public class JasperReportHelper {

    /**
     * Returns a jasper report template given its name.
     *
     * @param name     the report name
     * @param service  the archetype service
     * @param handlers the document handlers
     * @param context  the jasper reports context
     * @return the jasper report template or {@code null} if none can be found
     * @throws DocumentException for any document error
     * @throws JRException       if the report can't be deserialized
     */
    public static JasperDesign getReport(String name, IArchetypeService service, DocumentHandlers handlers,
                                         JasperReportsContext context)
            throws JRException {
        TemplateHelper helper = new TemplateHelper(service);
        Document document = helper.getDocument(name);
        if (document != null) {
            return getReport(document, handlers, context);
        }
        return null;
    }

    /**
     * Deserializes a jasper report from a {@link Document}.
     *
     * @param document the document
     * @param handlers the document handlers
     * @param context  the jasper reports context
     * @return a new jasper report
     * @throws DocumentException for any document error
     * @throws JRException       if the report can't be deserialized
     */
    public static JasperDesign getReport(Document document,  DocumentHandlers handlers, JasperReportsContext context)
            throws JRException {

        InputStream stream = null;
        try {
            DocumentHandler handler = handlers.get(document);
            stream = handler.getContent(document);
            return JRXmlLoader.load(context, stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Creates a new {@code ReportContext} from a map of parameters. Parameters can be null.
     *
     * @param parameters the parameters
     * @return a new report context
     */
    public static SimpleReportContext createReportContext(Map<String, Object> parameters) {
        SimpleReportContext result = new SimpleReportContext();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            result.setParameterValue(entry.getKey(), entry.getValue());
        }
        return result;

    }
}
