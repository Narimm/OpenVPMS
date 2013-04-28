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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.macro.impl;

import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Runs report macros.
 * <p/>
 * This generates a report and exports it as text.
 *
 * @author Tim Anderson
 */
public class ReportMacroRunner extends MacroRunner {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers for deserialising documents.
     */
    private final DocumentHandlers handlers;

    /**
     * The default character encoding.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(ReportMacroRunner.class);

    /**
     * Constructs a {@link ReportMacroRunner}.
     *
     * @param context  the macro context
     * @param service  the archetype service
     * @param handlers the document handlers for deserialising documents
     */
    public ReportMacroRunner(MacroContext context, IArchetypeService service, DocumentHandlers handlers) {
        super(context);
        this.service = service;
        this.handlers = handlers;
    }

    /**
     * Runs a macro.
     *
     * @param macro  the macro to run
     * @param number a numeric expression, used to declare the <em>$number</em> variable. May be empty or {@code null}
     * @return the result of the macro
     */
    @Override
    public String run(Macro macro, String number) {
        ReportMacro reportMacro = (ReportMacro) macro;
        String result = null;
        Object object = getObject();
        if (object instanceof IMObject) {
            Document document = reportMacro.getDocument();
            if (document != null) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                if (document.getName().endsWith(DocFormats.JRXML_EXT)) {
                    parameters.put(JRTextExporterParameter.PROPERTY_PAGE_WIDTH, reportMacro.getWidth());
                    parameters.put(JRTextExporterParameter.PROPERTY_PAGE_HEIGHT, reportMacro.getHeight());
                    parameters.put(JRTextExporterParameter.PROPERTY_CHARACTER_ENCODING, ENCODING);
                }
                IMReport<IMObject> report = ReportFactory.createIMObjectReport(document, service, handlers);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                List<IMObject> objects = Arrays.asList((IMObject) object);
                report.generate(objects.iterator(), parameters, DocFormats.TEXT_TYPE, output);
                try {
                    result = new String(output.toByteArray(), ENCODING);
                } catch (UnsupportedEncodingException exception) {
                    log.error(exception, exception);
                }
            }
        }
        return result;
    }

}
