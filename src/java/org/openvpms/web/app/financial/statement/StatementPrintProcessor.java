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

package org.openvpms.web.app.financial.statement;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes;
import org.openvpms.archetype.rules.finance.statement.AbstractStatementProcessorListener;
import org.openvpms.archetype.rules.finance.statement.StatementEvent;
import org.openvpms.archetype.rules.finance.statement.StatementProcessor;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Prints statements.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class StatementPrintProcessor extends AbstractStatementProcessorListener {

    /**
     * The generator.
     */
    private final StatementGenerator generator;


    /**
     * Constructs a new <tt>StatementPrintProcessor</tt>.
     *
     * @param generator the statement generator
     */
    public StatementPrintProcessor(StatementGenerator generator) {
        super(ArchetypeServiceHelper.getArchetypeService());
        this.generator = generator;
    }

    /**
     * Process a statement.
     *
     * @param event the event
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void process(final StatementEvent event) {
        List<Act> acts = new ArrayList<Act>();
        ArchetypeQuery query = createQuery(event.getCustomer(),
                                           event.getDate());

        Iterator<Act> iterator = new IMObjectQueryIterator<Act>(query);
        while (iterator.hasNext()) {
            acts.add(iterator.next());
        }
        if (!acts.isEmpty()) {
            print(acts, event);
        } else {
            // nothing to print
            endPeriod(event);
        }
    }

    /**
     * Prints a statement.
     *
     * @param acts  the acts to print
     * @param event the statement event
     */
    private void print(List<Act> acts, final StatementEvent event) {
        IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<Act>(
                acts, CustomerAccountActTypes.OPENING_BALANCE);
        String title = Messages.get("financial.statements.print.customer");
        InteractiveIMPrinter<Act> iPrinter
                = new InteractiveIMPrinter<Act>(title, printer);
        iPrinter.setListener(new PrinterListener() {
            public void printed() {
                try {
                    endPeriod(event);
                    generator.process(); // process the next statement
                } catch (OpenVPMSException exception) {
                    ErrorHelper.show(exception);
                }
            }

            public void cancelled() {
            }

            public void skipped() {
            }

            public void failed(Throwable cause) {
                ErrorHelper.show(cause, new WindowPaneListener() {
                    public void windowPaneClosing(WindowPaneEvent event) {
                    }
                });
            }
        });
        generator.setSuspend(true); // suspend generation while printing
        iPrinter.print();
    }

    /**
     * Genenates account period end acts for the customer.
     *
     * @param event the statement event|
     * @throws OpenVPMSException for any error
     */
    private void endPeriod(StatementEvent event) {
        StatementProcessor processor = event.getProcessor();
        processor.end(event.getCustomer());
    }

}
