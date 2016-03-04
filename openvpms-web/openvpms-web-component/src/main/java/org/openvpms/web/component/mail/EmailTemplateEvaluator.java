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

package org.openvpms.web.component.mail;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.macro.Macros;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportFactory;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.doc.DocumentHelper;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.macro.MacroVariables;
import org.openvpms.web.system.ServiceHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Evaluates the content of an <em>entity.documentTemplateEmail</em> based on its <em>contentType</em>.
 *
 * @author Tim Anderson
 */
public class EmailTemplateEvaluator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The macros.
     */
    private final Macros macros;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(EmailTemplateEvaluator.class);

    /**
     * Constructs an {@link EmailTemplateEvaluator}.
     *
     * @param service the service
     * @param lookups the lookups
     * @param macros  the macros
     */
    public EmailTemplateEvaluator(IArchetypeService service, ILookupService lookups, Macros macros) {
        this.service = service;
        this.lookups = lookups;
        this.macros = macros;
    }

    /**
     * Returns the email subject.
     *
     * @param template the template
     * @param object   the object to evaluate expressions against
     * @param context  the context, used to locate the object to evaluate
     * @return the email subject. May be {@code null}
     */
    public String getSubject(Entity template, Object object, Context context) {
        return evaluate(template, "subject", "subjectType", object, context);
    }

    /**
     * Returns the email message.
     *
     * @param template the template
     * @param object   the object to evaluate expressions against
     * @param context  the context, used to locate the object to report on
     * @return the email message, as HTML or an HTML fragment, or {@code null} if no content was present
     */
    public String getMessage(Entity template, Object object, Context context) {
        String result = evaluate(template, "content", "contentType", object, context);
        if (result != null) {
            result = StringEscapeUtils.escapeHtml(result);
        }
        return result;
    }

    /**
     * Evaluates a node.
     *
     * @param template the template
     * @param name     the content node name
     * @param typeName the content type node name
     * @param object   the object to evaluate expressions against
     * @param context  the context, used to locate the object to report on
     * @return the result of the evaluation. May be {@code null}
     */
    private String evaluate(Entity template, String name, String typeName, Object object, Context context) {
        String result;
        IMObjectBean bean = new IMObjectBean(template, service);
        String type = bean.getString(typeName, "TEXT");
        switch (type) {
            case "TEXT":
                result = evaluateText(bean, name);
                break;
            case "MACRO":
                result = evaluateMacros(bean, name, object, context);
                break;
            case "XPATH":
                result = evaluateXPath(bean, name, object, context);
                break;
            case "DOCUMENT":
                result = evaluateDocument(bean, object, context);
                break;
            default:
                result = null;
        }
        return result;
    }

    /**
     * Evaluates the content as plain text.
     *
     * @param template the template
     * @param name     the content node name
     * @return the text. May be {@code null}
     */
    private String evaluateText(IMObjectBean template, String name) {
        return template.getString(name);
    }

    /**
     * Evaluates the template content as macros.
     *
     * @param template the template
     * @param name     the content node name
     * @param object   the object to evaluate against
     * @param context  the context
     * @return the expanded text. May be {@code null}
     */
    private String evaluateMacros(IMObjectBean template, String name, Object object, Context context) {
        String content = template.getString(name);
        MacroVariables variables = new MacroVariables(context, service, lookups);
        return macros.runAll(content, object, variables, null, true);
    }

    /**
     * Evaluates the template content as a JXPath expression.
     *
     * @param template the template
     * @param context  the context
     * @return the result of the expression, with any HTML characters escaped
     */
    private String evaluateXPath(IMObjectBean template, String name, Object object, Context context) {
        String content = template.getString(name);
        MacroVariables variables = new MacroVariables(context, service, lookups);
        variables.declareVariable("nl", "\n");     // to make expressions with newlines simpler
        Object contextBean = getContextBean(template, object, context);
        if (contextBean == null) {
            contextBean = new Object();
        }
        JXPathContext jxPathContext = JXPathHelper.newContext(contextBean);
        jxPathContext.setVariables(variables);
        String value = (String) jxPathContext.getValue(content, String.class);
        return StringEscapeUtils.escapeHtml(value);
    }

    /**
     * Evaluates a document template, returning the content as HTML.
     *
     * @param template the template
     * @param context  the context
     * @return the document as HTML
     */
    private String evaluateDocument(IMObjectBean template, Object object, Context context) {
        String result = null;
        Object contextBean = getContextBean(template, object, context);
        Document document = new TemplateHelper(service).getDocumentFromTemplate((Entity) template.getObject());
        if (document != null) {
            if (DocFormats.HTML_TYPE.equals(document.getMimeType())) {
                DocumentHandlers handlers = ServiceHelper.getBean(DocumentHandlers.class);
                DocumentHandler handler = handlers.get(document);
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream(document.getDocSize());
                    IOUtils.copy(handler.getContent(document), stream);
                    result = new String(stream.toByteArray(), StandardCharsets.UTF_8);
                } catch (IOException exception) {
                    log.error("Failed to get HTML document, id=" + document.getId(), exception);
                }
            } else if (contextBean instanceof IMObject) {
                ReportFactory factory = ServiceHelper.getBean(ReportFactory.class);
                IMReport<IMObject> report = factory.createIMObjectReport(document);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                report.generate(Collections.singletonList((IMObject) contextBean),
                                Collections.<String, Object>emptyMap(),
                                ReportContextFactory.create(context), DocFormats.HTML_TYPE, bytes);
                result = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            } else if (Converter.canConvert(document, DocFormats.HTML_TYPE)) {
                byte[] converted = DocumentHelper.export(document, DocFormats.HTML_TYPE);
                result = new String(converted, StandardCharsets.UTF_8);
            }
        }
        return result;
    }

    /**
     * Returns the bean to supply to macros, xpath expressions and documents.
     * <p/>
     * If the expression node is populated, it will be evaluated against the object and context, and the result
     * returned, otherwise the object will be returned.
     *
     * @param template the template
     * @param object   the object to evaluate against
     * @param context  the context
     * @return the context bean.
     */
    private Object getContextBean(IMObjectBean template, Object object, Context context) {
        Object result;
        String expression = template.getString("expression");
        if (!StringUtils.isBlank(expression)) {
            MacroVariables variables = new MacroVariables(context, service, lookups);
            JXPathContext xpathContext = JXPathHelper.newContext(object);
            xpathContext.setVariables(variables);
            result = xpathContext.getValue(expression);
        } else {
            result = object;
        }
        return result;
    }

}
