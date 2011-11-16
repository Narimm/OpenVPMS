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

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
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
     * Determines if a print dialog is being displayed.
     */
    private boolean interactive;

    /**
     * Determines if the print dialog should always be displayed.
     */
    private boolean alwaysInteractive;

    /**
     * The printer to fallback to, if none is specified by the document templates. This is selected once.
     */
    private String fallbackPrinter;

    /**
     * The listener for printer events.
     */
    private final PrinterListener listener;


    /**
     * Constructs a <tt>ReminderPrintProcessor</tt>.
     *
     * @param groupTemplate the grouped reminder document template
     * @param listener      the listener for printer events
     */
    public ReminderPrintProcessor(DocumentTemplate groupTemplate, PrinterListener listener) {
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
        return alwaysInteractive || interactive;
    }

    /**
     * Determines if reminders should always be printed interactively.
     *
     * @param interactive if <tt>true</tt>, reminders should always be printed interactively. If <tt>false</tt>,
     *                    reminders will only be printed interactively if a printer needs to be selected
     */
    public void setInteractiveAlways(boolean interactive) {
        alwaysInteractive = interactive;
    }

    /**
     * Processes a list of reminder events.
     *
     * @param events    the events
     * @param shortName the report archetype short name, used to select the document template if none specified
     * @param template  the document template to use. May be <tt>null</tt>
     */
    protected void process(List<ReminderEvent> events, String shortName, DocumentTemplate template) {
        // TODO - fix this so its not dependent on the global context
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(template, shortName,
                                                                             GlobalContext.getInstance());
        if (events.size() > 1) {
            List<ObjectSet> sets = createObjectSets(events);
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(sets, locator);
            print(printer);
        } else {
            List<Act> acts = new ArrayList<Act>();
            for (ReminderEvent event : events) {
                acts.add(event.getReminder());
            }
            IMPrinter<Act> printer = new IMObjectReportPrinter<Act>(acts, locator);
            print(printer);
        }
    }

    /**
     * Performs a print.
     * <p/>
     * If a printer is configured, the print will occur in the background, otherwise a print dialog will be popped up.
     *
     * @param printer the printer
     */
    private <T> void print(IMPrinter<T> printer) {
        final InteractiveIMPrinter<T> iPrinter = new InteractiveIMPrinter<T>(printer);
        String printerName = printer.getDefaultPrinter();
        if (printerName == null) {
            printerName = fallbackPrinter;
        }
        interactive = alwaysInteractive || printerName == null;
        iPrinter.setInteractive(interactive);

        if (interactive) {
            // register a listener to grab the selected printer, to avoid popping up a print dialog each time
            PrinterListener l = new DelegatingPrinterListener(listener) {
                public void printed(String printer) {
                    fallbackPrinter = printer;
                    super.printed(printer);
                }
            };
            iPrinter.setListener(l);
        } else {
            iPrinter.setListener(listener);
        }
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
