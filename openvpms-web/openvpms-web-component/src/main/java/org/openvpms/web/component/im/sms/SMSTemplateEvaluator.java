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

package org.openvpms.web.component.im.sms;

import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.macro.Macros;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.macro.MacroVariables;

/**
 * Evaluates an SMS template.
 * <p/>
 * This supports <em>entity.documentTemplateSMS*</em> templates, which may be macros, xpath expressions, or plain text.
 * <br/>
 * For macros and xpath expressions, a {@link Context} may be provided to make objects available as variables.
 * <p/>
 * For xpath expressions, a {@code $nl} variable containing a new line is defined to make multi-line SMS generation
 * simpler.
 *
 * @author Tim Anderson
 */
public class SMSTemplateEvaluator {

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
     * Constructs an {@link SMSTemplateEvaluator}.
     *
     * @param service the service
     * @param lookups the lookups
     * @param macros  the macros
     */
    public SMSTemplateEvaluator(IArchetypeService service, ILookupService lookups, Macros macros) {
        this.service = service;
        this.lookups = lookups;
        this.macros = macros;
    }

    /**
     * Evaluates an SMS template.
     *
     * @param template the template. An entity.documentTemplateSMS*
     * @param object   the object to evaluate against. May be {@code null}
     * @param context  the context. Each object will be declared as a variable using {@link MacroVariables}
     * @return the SMS text. May be {@code null} or exceed the maximum SMS text length
     */
    public String evaluate(Entity template, Object object, Context context) {
        MacroVariables variables = new MacroVariables(context, service, lookups);
        return evaluate(template, object, variables);
    }

    /**
     * Evaluates an SMS template.
     *
     * @param template  the template. An entity.documentTemplateSMS*
     * @param object    the object to evaluate against. May be {@code null}
     * @param variables the variables available to the expression
     * @return the SMS text. May be {@code null} or exceed the maximum SMS text length
     */
    public String evaluate(Entity template, Object object, MacroVariables variables) {
        IMObjectBean templateBean = new IMObjectBean(template, service);
        String type = templateBean.getString("contentType");
        String content = templateBean.getString("content");
        String result;
        if (object == null) {
            object = new Object();
        }
        if ("XPATH".equals(type)) {
            variables.declareVariable("nl", "\n");     // to make expressions with newlines simpler
            JXPathContext jxPathContext = JXPathHelper.newContext(object);
            jxPathContext.setVariables(variables);
            result = (String) jxPathContext.getValue(content, String.class);
        } else if ("MACRO".equals(type)) {
            result = macros.runAll(content, object, variables, null, true);
        } else {
            result = content;
        }
        return result;
    }
}
