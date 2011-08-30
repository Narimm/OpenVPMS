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

package org.openvpms.web.app.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.print.PrinterListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Prints reminders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderPrintProcessor extends AbstractReminderProcessor {

    /**
     * The name of the selected printer. Once a printer has been selected,
     * printing will occur in the background.
     */
    private String printerName;

    /**
     * The listener for printer events.
     */
    private final PrinterListener listener;


    /**
     * Creates a new <tt>ReminderPrintProcessor</tt>.
     *
     * @param groupTemplate the grouped reminder document template
     * @param listener      the listener for printer events
     */
    public ReminderPrintProcessor(Entity groupTemplate, PrinterListener listener) {
        super(groupTemplate);
        this.listener = listener;
    }

    /**
     * Determines if reminders are being printed interactively, or in the background.
     *
     * @return <tt>true</tt> if reminders are being printed interactively, or <tt>false</tt> if they are being
     *         printed in the background
     */
    public boolean isInteractive() {
        return (printerName == null);
    }

    /**
     * Processes a list of reminder events.
     *
     * @param events           the events
     * @param shortName        the report archetype short name, used to select the document template if none specified
     * @param documentTemplate the document template to use. May be <tt>null</tt>
     */
    protected void process(List<ReminderEvent> events, String shortName, Entity documentTemplate) {
        if (events.size() > 1) {
            List<ObjectSet> sets = createObjectSets(events);
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(sets, shortName, documentTemplate);
            print(printer);
        } else {
            List<Act> acts = new ArrayList<Act>();
            for (ReminderEvent event : events) {
                acts.add(event.getReminder());
            }
            IMPrinter<Act> printer = new IMObjectReportPrinter<Act>(acts, shortName, documentTemplate);
            print(printer);
        }
    }

    /**
     * Performs a print.
     * <p/>
     * If a printer has been selected, the print will occur in the background, otherwise a print dialog will be popped
     * up.
     *
     * @param printer the printer
     */
    private <T> void print(IMPrinter<T> printer) {
        final InteractiveIMPrinter<T> iPrinter = new InteractiveIMPrinter<T>(printer);
        if (printerName != null) {
            iPrinter.setInteractive(false);
        }

        // create a delegating listener to keep track of printer name selections
        PrinterListener l = new DelegatingPrinterListener(listener) {
            public void printed(String printer) {
                printerName = printer;
                super.printed(printer);
            }
        };
        iPrinter.setListener(l);
        iPrinter.print(printerName);
    }

    private static class DelegatingPrinterListener implements PrinterListener {

        /**
         * The listener to delegate to.
         */
        private final PrinterListener listener;

        /**
         * Creates a new <tt>DelegatingPrinterListener</tt>.
         *
         * @param listener the listener to delegate to
         */
        public DelegatingPrinterListener(PrinterListener listener) {
            this.listener = listener;
        }

        /**
         * Notifies of a successful print.
         *
         * @param printer the printer that was used. May be <tt>null</tt>
         */
        public void printed(String printer) {
            listener.printed(printer);
        }

        /**
         * Notifies that the print was cancelled.
         */
        public void cancelled() {
            listener.cancelled();
        }

        /**
         * Notifies that the print was skipped.
         */
        public void skipped() {
            listener.skipped();
        }

        /**
         * Invoked when a print fails.
         *
         * @param cause the reason for the failure
         */
        public void failed(Throwable cause) {
            listener.failed(cause);
        }
    }
}
