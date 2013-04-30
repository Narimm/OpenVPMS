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

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.macro.MacroException;
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
public class ReportMacroRunner extends AbstractExpressionMacroRunner {

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
     * @throws MacroException for any macro error
     */
    @Override
    public String run(Macro macro, String number) {
        ReportMacro reportMacro = (ReportMacro) macro;
        String result;
        Object object = evaluate(reportMacro, number);
        if (object instanceof IMObject) {
            Document document = getTemplate(reportMacro);
            Map<String, Object> parameters = new HashMap<String, Object>();
            IMReport<IMObject> report = ReportFactory.createIMObjectReport(document, service, handlers);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            List<IMObject> objects = Arrays.asList((IMObject) object);
            report.generate(objects.iterator(), parameters, DocFormats.TEXT_TYPE, output);
            try {
                result = new String(output.toByteArray(), ENCODING);
            } catch (UnsupportedEncodingException exception) {
                throw new MacroException("Failed to encode report output", exception);
            }
            if (document.getName().endsWith(DocFormats.JRXML_EXT)) {
                // Jasper Reports generates an array characters to fill with text, much of which ends up as whitespace.
                // Strip trailing whitespace.
                result = result.replaceAll("[\\s&&[^\\n]]+\n", "\n"); // strip whitespace up to each newline
                result = result.replaceAll("\\n+$", "");              // strip empty lines at the end
            }
        } else {
            throw new MacroException("Expression='" + reportMacro.getExpression()
                                     + "' did not return an object for macro=" + reportMacro.getCode());
        }
        return result;
    }

    /**
     * Returns the report document template associated with the macro.
     *
     * @param macro the macro
     * @return the report document template
     * @throws MacroException for any macro error
     */
    private Document getTemplate(ReportMacro macro) {
        Document document;
        try {
            document = macro.getDocument();
        } catch (ArchetypeServiceException exception) {
            throw new MacroException("Failed to retrieve document template for macro=" + macro.getCode(), exception);
        }
        if (document == null) {
            throw new MacroException("No document template for macro=" + macro.getCode());
        }
        return document;
    }

}
