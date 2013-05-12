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

package org.openvpms.web.app.reporting.statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.statement.Statement;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.echo.event.VetoListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Prints statements.
 *
 * @author Tim Anderson
 */
class StatementPrintProcessor extends AbstractStatementProcessorListener {

    /**
     * The batch processor to invoke to process the next statement, when printing interactively.
     */
    private final StatementProgressBarProcessor processor;

    /**
     * The listener to cancel processing.
     */
    private final VetoListener cancelListener;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The mail context.
     */
    private final MailContext mailContext;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The name of the selected printer. Once a printer has been selected, printing will occur in the background.
     */
    private String printerName;

    /**
     * Determines if statements should have their print flag updated.
     * Only applies to non-preview statements.
     */
    private boolean updatePrinted = true;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(StatementPrintProcessor.class);


    /**
     * Constructs a {@code StatementPrintProcessor}.
     *
     * @param processor      the batch processor to invoke to process the next
     *                       statement, when printing interactively.
     * @param cancelListener the listener to cancel processing
     * @param practice       the practice
     * @param mailContext    the mail context. May be {@code null}
     */
    public StatementPrintProcessor(StatementProgressBarProcessor processor, VetoListener cancelListener,
                                   Party practice, Context context, MailContext mailContext, HelpContext help) {
        super(practice);
        this.processor = processor;
        this.cancelListener = cancelListener;
        this.context = context;
        this.mailContext = mailContext;
        this.help = help;
    }

    /**
     * Determines if statements should have their print flag updated.
     * Only applies to non-preview statements. Defaults to {@code true}.
     *
     * @param update if {@code true} update the statements print flag
     */
    public void setUpdatePrinted(boolean update) {
        updatePrinted = update;
    }

    /**
     * Process a statement.
     *
     * @param statement the event
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void process(final Statement statement) {
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(CustomerAccountArchetypes.OPENING_BALANCE,
                                                                             context);
        IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<Act>(statement.getActs(), locator, context);
        printer.setParameters(getParameters(statement));

        print(printer, statement);
    }

    /**
     * Prints a statement.
     *
     * @param printer   the statement printer
     * @param statement the statement being printed
     */
    protected void print(IMObjectReportPrinter<Act> printer, final Statement statement) {
        String title = Messages.get("reporting.statements.print.customer");
        final InteractiveIMPrinter<Act> iPrinter = new InteractiveIMPrinter<Act>(title, printer, context, help);
        if (printerName != null) {
            iPrinter.setInteractive(false);
        }
        iPrinter.setCancelListener(cancelListener);
        iPrinter.setMailContext(mailContext);
        iPrinter.setListener(new PrinterListener() {
            public void printed(String printer) {
                try {
                    if (updatePrinted && !statement.isPreview() && !statement.isPrinted()) {
                        setPrinted(statement);
                    }
                    printerName = printer;
                    processor.processCompleted(statement.getCustomer());
                    if (iPrinter.getInteractive()) {
                        // Need to process the next statement. If non-interactive, the next statement will be processed
                        // automatically
                        processor.process();
                    }
                } catch (OpenVPMSException exception) {
                    log.error(exception, exception);
                    processor.processFailed(statement.getCustomer(), exception.getMessage(), exception);
                }
            }

            public void cancelled() {
                processor.setStatus(null);
            }

            public void skipped() {
            }

            public void failed(Throwable cause) {
                log.error(cause, cause);
                processor.processFailed(statement.getCustomer(), cause.getMessage(), cause);
            }
        });
        if (iPrinter.getInteractive()) {
            processor.setSuspend(true); // suspend generation while printing
        }
        iPrinter.print(printerName);
    }

}
