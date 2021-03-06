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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workflow;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.system.ServiceHelper;

import java.util.Collection;


/**
 * Prints a collection of objects.
 *
 * @author Tim Anderson
 */
public class PrintIMObjectsTask<T extends IMObject> extends AbstractTask {

    /**
     * The collection to print.
     */
    private final Collection<T> objects;

    /**
     * The archetype short name to determine which template to use.
     */
    private final String shortName;


    /**
     * Creates a new <tt>PrintObjectsTask</tt>.
     *
     * @param objects   the objects to print
     * @param shortName the archetype short name to determine the template to
     *                  use
     */
    public PrintIMObjectsTask(Collection<T> objects, String shortName) {
        this.objects = objects;
        this.shortName = shortName;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     */
    public void start(final TaskContext context) {
        boolean skip = !isRequired();
        if (!objects.isEmpty()) {
            try {
                ReporterFactory factory = ServiceHelper.getBean(ReporterFactory.class);
                DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(shortName, context);
                IMObjectReportPrinter<T> printer = new IMObjectReportPrinter<T>(objects, locator, context, factory);
                InteractiveIMPrinter<T> iPrinter = new InteractiveIMPrinter<T>(printer, skip, context,
                                                                               context.getHelpContext());

                iPrinter.setListener(new PrinterListener() {
                    public void printed(String printer) {
                        notifyCompleted();
                    }

                    public void cancelled() {
                        notifyCancelled();
                    }

                    public void skipped() {
                        notifySkipped();
                    }

                    public void failed(Throwable cause) {
                        notifyCancelledOnError(cause);
                    }
                });
                iPrinter.print();
            } catch (OpenVPMSException exception) {
                notifyCancelledOnError(exception);
            }
        } else {
            notifyCompleted();
        }
    }

}
