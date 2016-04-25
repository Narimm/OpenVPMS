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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.doc.ParameterDialog;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.macro.MacroVariables;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.Set;

/**
 * Evaluates email templates, prompting for parameters if required.
 *
 * @author Tim Anderson
 */
public class ParameterEmailTemplateEvaluator {

    public interface Listener {

        void generated(String subject, String message);
    }

    /**
     * The email template.
     */
    private final Entity template;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The template evaluator.
     */
    private final EmailTemplateEvaluator evaluator;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ParameterEmailTemplateEvaluator.class);

    /**
     * Constructs an {@link ParameterEmailTemplateEvaluator}.
     *
     * @param template the template
     * @param context  the context, used to locate the object to report on
     * @param help     the help context
     */
    public ParameterEmailTemplateEvaluator(Entity template, Context context, HelpContext help) {
        this.template = template;
        this.context = context;
        this.evaluator = ServiceHelper.getBean(EmailTemplateEvaluator.class);
        this.help = help;
    }

    /**
     * Evaluates the template, prompting for any parameters if required.
     *
     * @param object   the object to evaluate expressions against. May be {@code null}
     * @param prompt   if {@code true}, prompt for parameters
     * @param listener the listener to notify. This may be invoked immediately
     */
    public void evaluate(Object object, boolean prompt, Listener listener) {
        try {
            Reporter<IMObject> reporter = (prompt) ? evaluator.getMessageReporter(template, object, context) : null;
            if (reporter != null) {
                Set<ParameterType> parameters = reporter.getParameterTypes();
                if (!parameters.isEmpty()) {
                    promptParameters(reporter, object, listener);
                } else {
                    generate(reporter, object, listener);
                }
            } else {
                String subject = evaluator.getSubject(template, object, context);
                String message = evaluator.getMessage(template, object, context);
                listener.generated(subject, message);
            }
        } catch (Throwable exception) {
            error(exception);
        }
    }

    /**
     * Generates the subject and message. These will be passed to the supplied listener.
     *
     * @param reporter the reporter
     * @param object   the object to evaluate expressions against. May be {@code null}
     * @param listener the listener to notify
     */
    private void generate(Reporter<IMObject> reporter, Object object, Listener listener) {
        try {
            String subject = evaluator.getSubject(template, object, context);
            String message = evaluator.getMessage(reporter);
            listener.generated(subject, message);
        } catch (Throwable exception) {
            error(exception);
        }
    }

    /**
     * Pops up a dialog to prompt for report parameters.
     *
     * @param reporter the report
     */
    private void promptParameters(final Reporter<IMObject> reporter, final Object object, final Listener listener) {
        Set<ParameterType> parameters = reporter.getParameterTypes();
        String title = Messages.format("document.input.parameters", template.getName());
        MacroVariables variables = new MacroVariables(context, ServiceHelper.getArchetypeService(),
                                                      ServiceHelper.getLookupService());
        IMObject obj = (object instanceof IMObject) ? (IMObject) object : null;
        final ParameterDialog dialog = new ParameterDialog(title, parameters, obj, context, help.subtopic("parameters"),
                                                           variables, false, true);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                reporter.setParameters(dialog.getValues());
                generate(reporter, object, listener);
            }

        });
        dialog.show();
    }

    /**
     * Displays an error message when an email template fails to expand.
     *
     * @param exception the error
     */
    private void error(Throwable exception) {
        ErrorHelper.show(Messages.format("mail.template.error", template.getName(), exception.getMessage()));
        log.error("Failed to expand email template: " + template.getName(), exception);
    }

}
