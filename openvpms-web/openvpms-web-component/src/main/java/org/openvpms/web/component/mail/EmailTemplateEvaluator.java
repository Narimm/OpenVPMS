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
import org.apache.commons.lang.StringUtils;
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

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;

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
     * @return the email subject
     */
    public String getSubject(Entity template) {
        IMObjectBean bean = new IMObjectBean(template, service);
        return bean.getString("subject");
    }

    /**
     * Returns the email message.
     *
     * @param template the template
     * @param context the context, used to locate the object to report on
     * @return thhe email message
     */
    public String getMessage(Entity template, Context context) {
        String result;
        IMObjectBean bean = new IMObjectBean(template, service);
        String type = bean.getString("contentType", "TEXT");
        switch (type) {
            case "TEXT":
                result = bean.getString("content");
                break;
            case "MACRO":
                result = evaluateMacros(bean, context);
                break;
            case "XPATH":
                result = evaluateXPath(bean, context);
                break;
            default:
                result = evaluateDocument(bean, context);
        }
        return result;
    }

    private String evaluateMacros(IMObjectBean template, Context context) {
        String content = template.getString("content");
        MacroVariables variables = new MacroVariables(context, service, lookups);
        return macros.runAll(content, null, variables, null, true);
    }

    private String evaluateXPath(IMObjectBean template, Context context) {
        String content = template.getString("content");
        MacroVariables variables = new MacroVariables(context, service, lookups);
        variables.declareVariable("nl", "\n");     // to make expressions with newlines simpler
        Object contextBean = getContextBean(template, context);
        if (contextBean == null) {
            contextBean = new Object();
        }
        JXPathContext jxPathContext = JXPathHelper.newContext(contextBean);
        jxPathContext.setVariables(variables);
        return (String) jxPathContext.getValue(content, String.class);
    }

    /**
     * Evaluates a document template, returning the content as HTML.
     *
     * @param template the template
     * @param context the context
     * @return the document as HTML
     */
    private String evaluateDocument(IMObjectBean template, Context context) {
        String result = null;
        Object contextBean = getContextBean(template, context);
        Document document = new TemplateHelper(service).getDocumentFromTemplate((Entity) template.getObject());
        if (document != null) {
            if (DocFormats.HTML_TYPE.equals(document.getMimeType())) {
                DocumentHandlers handlers = ServiceHelper.getBean(DocumentHandlers.class);
                DocumentHandler handler = handlers.get(document);
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream(document.getDocSize());
                    String html = new String(stream.toByteArray(), StandardCharsets.UTF_8);
                    IOUtils.copy(handler.getContent(document), stream);
                    result = filter(html);
                } catch (IOException ignore) {

                }
            } else if (contextBean instanceof IMObject) {
                ReportFactory factory = ServiceHelper.getBean(ReportFactory.class);
                IMObject object = (IMObject) contextBean;
                IMReport<IMObject> report = factory.createIMObjectReport(document);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                report.generate(Collections.singletonList(object), Collections.<String, Object>emptyMap(),
                                ReportContextFactory.create(context), DocFormats.HTML_TYPE, bytes);
                result = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            } else if (Converter.canConvert(document, DocFormats.HTML_TYPE)) {
                byte[] converted = DocumentHelper.export(document, DocFormats.HTML_TYPE);
                String html = new String(converted, StandardCharsets.UTF_8);
                result = filter(html);
            }
        }
        return result;
    }

    /**
     * Returns the bean to supply to xpath expressions and documents.
     *
     * @param template the template
     * @param context  the context
     * @return the context bean
     */
    private Object getContextBean(IMObjectBean template, Context context) {
        Object result = null;
        String expression = template.getString("expression");
        if (!StringUtils.isBlank(expression)) {
            MacroVariables variables = new MacroVariables(context, service, lookups);
            JXPathContext jxPathContext = JXPathHelper.newContext(new Object());
            jxPathContext.setVariables(variables);
            result = jxPathContext.getValue(expression);
        }
        if (result == null) {
            result = new Object();
        }
        return result;
    }

    /**
     * Filters html to extract the inner html of the body tag. This is required by the rich text area editor.
     *
     * @param html the html to filter
     * @return the filtered html
     */
    private String filter(String html) {
        String result;
        // also see http://stackoverflow.com/questions/9022140/using-xpath-contains-against-html-in-java
        // for an xpath version
        ParserDelegator delegator = new ParserDelegator();
        final StringBuilder buffer = new StringBuilder();
        try {
            delegator.parse(new StringReader(html), new HTMLEditorKit.ParserCallback() {
                @Override
                public void handleStartTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
                    if (!filter(tag)) {
                        buffer.append('<').append(tag.toString()).append('>');
                    }
                }

                @Override
                public void handleText(char[] data, int pos) {
                    buffer.append(new String(data));
                }

                @Override
                public void handleEndTag(HTML.Tag tag, int pos) {
                    if (!filter(tag)) {
                        buffer.append("</").append(tag.toString()).append(">");
                    }
                }

                @Override
                public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
                    if (!filter(tag)) {
                        buffer.append("<").append(tag.toString());
                        append(a);
                        buffer.append("/>");
                    }
                }

                protected boolean filter(HTML.Tag tag) {
                    return tag == HTML.Tag.HTML || tag == HTML.Tag.HEAD || tag == HTML.Tag.BODY
                           || tag == HTML.Tag.META || tag == HTML.Tag.STYLE || tag == HTML.Tag.TITLE
                           || tag == HTML.Tag.SCRIPT;
                }

                private void append(MutableAttributeSet a) {
                    Enumeration<?> names = a.getAttributeNames();
                    if (names.hasMoreElements()) {
                        buffer.append(' ');
                        while (names.hasMoreElements()) {
                            String name = names.nextElement().toString();
                            buffer.append(name).append('=').append(a.getAttribute(name));
                        }
                    }
                }
            }, true);
            result = buffer.toString();
        } catch (IOException exception) {
            // do nothing
            result = null;
        }
        return result;
    }
}
