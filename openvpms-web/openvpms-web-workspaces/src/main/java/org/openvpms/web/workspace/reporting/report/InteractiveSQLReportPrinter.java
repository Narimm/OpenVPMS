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

package org.openvpms.web.workspace.reporting.report;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.util.Variables;
import org.openvpms.report.ParameterType;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.MailDialog;
import org.openvpms.web.component.print.InteractiveExportPrinter;
import org.openvpms.web.component.print.PrintDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.Map;
import java.util.Set;


/**
 * Interactive printer for {@link SQLReportPrinter}. Pops up a dialog with
 * options to print, preview, or cancel, and supply the report with parameters.
 *
 * @author Tim Anderson
 */
public class InteractiveSQLReportPrinter extends InteractiveExportPrinter {

    /**
     * Variables for macro expansion.
     */
    private final Variables variables;

    /**
     * Constructs an {@link InteractiveSQLReportPrinter}.
     *
     * @param printer     the printer to delegate to
     * @param context     the context
     * @param mailContext the mail context
     * @param help        the help context
     * @param variables   variables for macro expansion
     */
    public InteractiveSQLReportPrinter(SQLReportPrinter printer, Context context, MailContext mailContext,
                                       HelpContext help, Variables variables) {
        super(printer, context, mailContext, help);
        this.variables = variables;
    }

    /**
     * Creates a new print dialog.
     *
     * @return a new print dialog
     */
    @Override
    protected PrintDialog createDialog() {
        final SQLReportPrinter printer = getPrinter();
        Set<ParameterType> parameterTypes = replaceVariables(printer.getParameterTypes());
        Party location = getContext().getLocation();
        return new SQLReportDialog(getTitle(), parameterTypes, variables, location, getHelpContext()) {

            @Override
            protected void doPrint() {
                printer.setParameters(getValues());
            }

            @Override
            protected void doPreview() {
                printer.setParameters(getValues());
                doPrintPreview();
            }

            @Override
            protected void doMail() {
                printer.setParameters(getValues());
                InteractiveSQLReportPrinter.this.mail(this);
            }

            @Override
            protected void doExport() {
                printer.setParameters(getValues());
                export();
            }

            @Override
            protected void doExportMail() {
                printer.setParameters(getValues());
                exportMail(this);
            }
        };
    }

    /**
     * Returns the underlying printer.
     *
     * @return the printer
     */
    protected SQLReportPrinter getPrinter() {
        return (SQLReportPrinter) super.getPrinter();
    }

    /**
     * Returns a title for the print dialog.
     *
     * @return a title for the print dialog
     */
    @Override
    protected String getTitle() {
        return Messages.format("reporting.run.title", getDisplayName());
    }

    /**
     * Shows the mail dialog.
     * <p/>
     * This implementation pre-fills the email with the template associated with the document being printed, if any.
     *
     * @param dialog the dialog
     */
    @Override
    protected void show(MailDialog dialog) {
        super.show(dialog);
        DocumentTemplate template = getPrinter().getTemplate();
        Entity emailTemplate = template.getEmailTemplate();
        if (emailTemplate != null) {
            dialog.getMailEditor().setContent(emailTemplate);
        }
    }

    /**
     * Replaces any "$OpenVPMS." default values with their actual values.
     *
     * @param parameterTypes the parameter types
     * @return the parameter types with default values replaced
     */
    private Set<ParameterType> replaceVariables(Set<ParameterType> parameterTypes) {
        ParameterEvaluator evaluator = new ParameterEvaluator(ServiceHelper.getArchetypeService(),
                                                              ServiceHelper.getLookupService());
        Map<String, Object> variables = ReportContextFactory.create(getContext());
        return evaluator.evaluate(parameterTypes, variables);
    }
}
