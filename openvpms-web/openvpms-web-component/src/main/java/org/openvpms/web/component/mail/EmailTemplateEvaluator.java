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
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.macro.Macros;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportFactory;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.macro.MacroVariables;
import org.openvpms.web.system.ServiceHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Evaluates the content of an <em>entity.documentTemplateEmail</em> based on its <em>contentType</em>.
 *
 * @author Tim Anderson
 */
public class EmailTemplateEvaluator {

    /**
     * Text content type.
     */
    public static final String TEXT = "TEXT";

    /**
     * Macro content type.
     */
    public static final String MACRO = "MACRO";

    /**
     * XPath content type.
     */
    public static final String XPATH = "XPATH";

    /**
     * Document content type.
     */
    public static final String DOCUMENT = "DOCUMENT";

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
     * The report factory.
     */
    private final ReportFactory factory;

    /**
     * The document converter
     */
    private final Converter converter;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(EmailTemplateEvaluator.class);

    /**
     * Constructs an {@link EmailTemplateEvaluator}.
     *
     * @param service   the service
     * @param lookups   the lookups
     * @param macros    the macros
     * @param factory   the report factory
     * @param converter the document converter
     */
    public EmailTemplateEvaluator(IArchetypeService service, ILookupService lookups, Macros macros,
                                  ReportFactory factory, Converter converter) {
        this.service = service;
        this.lookups = lookups;
        this.macros = macros;
        this.factory = factory;
        this.converter = converter;
    }

    /**
     * Returns the email subject.
     *
     * @param template the template
     * @param object   the object to evaluate expressions against. May be {@code null}
     * @param context  the context, used to locate the object to evaluate
     * @return the email subject. May be {@code null}
     */
    public String getSubject(Entity template, Object object, Context context) {
        return evaluate(template, "subject", "subjectType", "subjectSource", object, context, false);
    }

    /**
     * Returns the email message.
     *
     * @param template the template
     * @param object   the object to evaluate expressions against. May be {@code null}
     * @param context  the context, used to locate the object to report on
     * @return the email message, as HTML or an HTML fragment, or {@code null} if no content was present
     */
    public String getMessage(Entity template, Object object, Context context) {
        return evaluate(template, "content", "contentType", "contentSource", object, context, true);
    }

    /**
     * Returns an {@link Reporter} for the template message body, if the template has a document body.
     *
     * @param template the template
     * @param object   the object to evaluate expressions against. May be {@code null}
     * @param context  the context, used to locate the object to report on
     * @return a new reporter, or {@code null} if the content is not a supported document
     */
    public Reporter<IMObject> getMessageReporter(Entity template, Object object, Context context) {
        Reporter<IMObject> result = null;
        IMObjectBean bean = new IMObjectBean(template, service);
        String type = bean.getString("contentType");
        if (DOCUMENT.equals(type)) {
            String expression = bean.getString("contentSource");
            Object contextBean = getContextBean(expression, object, context);
            if (contextBean instanceof IMObject) {
                final Document document = new TemplateHelper(service).getDocumentFromTemplate(template);
                if (document != null) {
                    if (factory.isIMObjectReport(document)) {
                        result = createReporter((IMObject) contextBean, document, context);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns an {@link Reporter} for the template message body, if the template has a document body.
     *
     * @param template the template
     * @param objects  the objects to pass to the document
     * @param context  the context to pass to the document
     * @return a new reporter, or {@code null} if the content is not a supported document
     */
    public Reporter<ObjectSet> getMessageReporter(Entity template, List<ObjectSet> objects, Context context) {
        Reporter<ObjectSet> result = null;
        IMObjectBean bean = new IMObjectBean(template, service);
        String type = bean.getString("contentType");
        if (DOCUMENT.equals(type)) {
            final Document document = new TemplateHelper(service).getDocumentFromTemplate(template);
            if (document != null && factory.isObjectSetReport(document, objects.size())) {
                result = createReporter(objects, document, context);
            }
        }
        return result;
    }

    /**
     * Returns the email message.
     *
     * @param reporter the reporter used to generate the message
     * @return the email message, as HTML or an HTML fragment
     */
    public String getMessage(Reporter<?> reporter) {
        String result;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        reporter.generate(DocFormats.HTML_TYPE, true, bytes);
        result = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        return result;
    }

    /**
     * Creates a reporter for an object.
     *
     * @param object   the object to report on
     * @param document the report template
     * @param context  the context
     * @return a new reporter
     */
    private Reporter<IMObject> createReporter(IMObject object, final Document document, Context context) {
        Reporter<IMObject> result;
        result = new Reporter<IMObject>(object) {
            @Override
            protected IMReport<IMObject> getReport() {
                return factory.createIMObjectReport(document);
            }
        };
        result.setFields(ReportContextFactory.create(context));
        return result;
    }

    /**
     * Creates a reporter for a list of {@link ObjectSet}s.
     *
     * @param objects  the objects to report on
     * @param document the report template
     * @param context  the context
     * @return a new reporter
     */
    private Reporter<ObjectSet> createReporter(List<ObjectSet> objects, final Document document, Context context) {
        Reporter<ObjectSet> result;
        result = new Reporter<ObjectSet>(objects) {
            @Override
            protected IMReport<ObjectSet> getReport() {
                return factory.createObjectSetReport(document);
            }
        };
        result.setFields(ReportContextFactory.create(context));
        return result;
    }

    /**
     * Evaluates a node.
     *
     * @param template   the template
     * @param name       the content node name
     * @param typeName   the content type node name
     * @param sourceName the content source expression node name
     * @param object     the object to evaluate expressions against. May be {@code null}
     * @param context    the context, used to locate the object to report on
     * @param html       if {@code true}, the result is HTML
     * @return the result of the evaluation. May be {@code null}
     */
    private String evaluate(Entity template, String name, String typeName, String sourceName, Object object,
                            Context context, boolean html) {
        String result;
        IMObjectBean bean = new IMObjectBean(template, service);
        String expression = bean.getString(sourceName);
        String type = bean.getString(typeName, TEXT);
        switch (type) {
            case TEXT:
                result = evaluateText(bean, name);
                break;
            case MACRO:
                result = evaluateMacros(bean, name, expression, object, context);
                break;
            case XPATH:
                result = evaluateXPath(bean, name, expression, object, context);
                break;
            case DOCUMENT:
                result = evaluateDocument(template, expression, object, context);
                break;
            default:
                result = null;
        }
        if (result != null && html && !DOCUMENT.equals(type)) {
            result = toHTML(result);
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
     * @param template   the template
     * @param name       the content node name
     * @param object     the object to evaluate against. May be {@code null}
     * @param expression the expression used to locate the source object. May be {@code null}
     * @param context    the context
     * @return the expanded text. May be {@code null}
     */
    private String evaluateMacros(IMObjectBean template, String name, String expression, Object object,
                                  Context context) {
        String content = template.getString(name);
        Object contextBean = getContextBean(expression, object, context);
        MacroVariables variables = new MacroVariables(context, service, lookups);
        return macros.runAll(content, contextBean, variables, null, true);
    }

    /**
     * Evaluates the template content as a JXPath expression.
     *
     * @param template   the template
     * @param object     the object to evaluate expressions against. May be {@code null}
     * @param expression the expression used to locate the source object. May be {@code null}
     * @param context    the context
     * @return the result of the expression. May be {@code null}
     */
    private String evaluateXPath(IMObjectBean template, String name, String expression, Object object,
                                 Context context) {
        String content = template.getString(name);
        MacroVariables variables = new MacroVariables(context, service, lookups);
        variables.declareVariable("nl", "\n");     // to make expressions with newlines simpler
        Object contextBean = getContextBean(expression, object, context);
        JXPathContext jxPathContext = JXPathHelper.newContext(contextBean);
        jxPathContext.setVariables(variables);
        return (String) jxPathContext.getValue(content, String.class);
    }

    /**
     * Evaluates a document template, returning the content as HTML.
     *
     * @param expression the expression used to locate the source object. May be {@code null}
     * @param object     the object to evaluate expressions against. May be {@code null}
     * @param context    the context
     * @return the document as HTML
     */
    private String evaluateDocument(Entity template, String expression, Object object, Context context) {
        String result = null;
        Object contextBean = getContextBean(expression, object, context);
        Document document = new TemplateHelper(service).getDocumentFromTemplate(template);
        if (document != null) {
            if (DocFormats.HTML_TYPE.equals(document.getMimeType())) {
                DocumentHandlers handlers = ServiceHelper.getBean(DocumentHandlers.class);
                DocumentHandler handler = handlers.get(document);
                try {
                    int size = document.getDocSize();
                    if (size < 0) {
                        size = 1024;
                    }
                    ByteArrayOutputStream stream = new ByteArrayOutputStream(size);
                    IOUtils.copy(handler.getContent(document), stream);
                    result = new String(stream.toByteArray(), StandardCharsets.UTF_8);
                } catch (IOException exception) {
                    log.error("Failed to get HTML document, id=" + document.getId(), exception);
                }
            } else if (contextBean instanceof IMObject && factory.isIMObjectReport(document)) {
                Reporter<IMObject> reporter = createReporter((IMObject) contextBean, document, context);
                result = getMessage(reporter);
            } else if (converter.canConvert(document, DocFormats.HTML_TYPE)) {
                byte[] converted = converter.export(document, DocFormats.HTML_TYPE);
                result = new String(converted, StandardCharsets.UTF_8);
            }
        }
        return result;
    }

    /**
     * Converts plain text to HTML by escaping any HTML characters and replacing new lines with &lt;br/&gt;
     *
     * @param string the string
     * @return the corresponding HTML fragment
     */
    private String toHTML(String string) {
        string = StringEscapeUtils.escapeHtml(string);
        string = string.replaceAll("\n", "<br/>");
        return string;
    }

    /**
     * Returns the bean to supply to macros, xpath expressions and documents.
     * <p>
     * If the expression is non-null, it will be evaluated against the object and context, and the result
     * returned, otherwise the object will be returned.
     *
     * @param expression the expression used to locate the source object. May be {@code null}
     * @param object     the object to evaluate against. May be {@code null}
     * @param context    the context
     * @return the context bean
     */
    private Object getContextBean(String expression, Object object, Context context) {
        Object result;
        if (object == null) {
            object = new Object();
        }
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
