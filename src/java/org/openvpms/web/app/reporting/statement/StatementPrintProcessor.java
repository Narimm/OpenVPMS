/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.statement;

import org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes;
import org.openvpms.archetype.rules.finance.statement.Statement;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.component.util.VetoListener;
import org.openvpms.web.resource.util.Messages;


/**
 * Prints statements.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class StatementPrintProcessor extends AbstractStatementProcessorListener {

    /**
     * The batch processor to invoke to process the next statement, when
     * printing interactively
     */
    private final StatementProgressBarProcessor processor;

    /**
     * The listener to cancel processing.
     */
    private final VetoListener cancelListener;

    /**
     * The name of the selected printer. Once a printer has been selected,
     * printing will occur in the background.
     */
    private String printerName;


    /**
     * Constructs a new <tt>StatementPrintProcessor</tt>.
     *
     * @param processor      the batch processor to invoke to process the next
     *                       statement, when printing interactively.
     * @param cancelListener the listener to cancel processing
     */
    public StatementPrintProcessor(StatementProgressBarProcessor processor,
                                   VetoListener cancelListener) {
        this.processor = processor;
        this.cancelListener = cancelListener;
    }

    /**
     * Process a statement.
     *
     * @param statement the event
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void process(final Statement statement) {
        IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<Act>(
                statement.getActs(), CustomerAccountActTypes.OPENING_BALANCE);
        printer.setParameters(getParameters(statement));

        String title = Messages.get("reporting.statements.print.customer");
        InteractiveIMPrinter<Act> iPrinter
                = new InteractiveIMPrinter<Act>(title, printer);
        if (printerName != null) {
            iPrinter.setInteractive(false);
        }
        iPrinter.setCancelListener(cancelListener);
        iPrinter.setListener(new PrinterListener() {
            public void printed(String printer) {
                try {
                    printerName = printer;
                    processor.processCompleted(statement.getCustomer());
                    processor.process(); // process the next statement
                } catch (OpenVPMSException exception) {
                    processor.notifyError(exception);
                }
            }

            public void cancelled() {
                processor.setStatus(null);
            }

            public void skipped() {
            }

            public void failed(Throwable cause) {
                processor.notifyError(cause);
            }
        });
        processor.setSuspend(true); // suspend generation while printing
        iPrinter.print(printerName);
    }

}
