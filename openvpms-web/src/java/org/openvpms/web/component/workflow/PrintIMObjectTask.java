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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.workflow;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.print.PrinterListener;


/**
 * Prints an {@link IMObject}.
 *
 * @author Tim Anderson
 */
public class PrintIMObjectTask extends AbstractTask {

    /**
     * The object to print.
     */
    private IMObject object;

    /**
     * The short name of the object to print.
     */
    private String shortName;

    /**
     * The mail context. May be {@code null}
     */
    private final MailContext mailContext;

    /**
     * Determines if objects should be printed interactively.
     * If {@code false}, indicates to try and print in the background unless
     * printing requires user intervention.
     */
    private final boolean interactive;

    /**
     * Determines if the skip button should be displayed if
     * {@link #isRequired()} is {@code false}.
     */
    private boolean enableSkip = true;


    /**
     * Creates a new {@code PrintIMObjectTask}.
     *
     * @param object  the object to print
     * @param context the mail context. May be {@code null}
     */
    public PrintIMObjectTask(IMObject object, MailContext context) {
        this(object, context, true);
    }

    /**
     * Creates a new {@code PrintIMObjectTask}.
     *
     * @param object      the object to print
     * @param context     the mail context. May be {@code null}
     * @param interactive if {@code true} print interactively, otherwise attempt to print in the background
     */
    public PrintIMObjectTask(IMObject object, MailContext context, boolean interactive) {
        this.object = object;
        this.mailContext = context;
        this.interactive = interactive;
    }

    /**
     * Creates a new {@code PrintIMObjectTask}.
     *
     * @param shortName the short name of the object to print
     * @param context   the mail context. May be {@code null}
     */
    public PrintIMObjectTask(String shortName, MailContext context) {
        this(shortName, context, true);
    }

    /**
     * Creates a new {@code PrintIMObjectTask}.
     *
     * @param shortName   the short name of the object to print
     * @param context     the mail context. May be {@code null}
     * @param interactive if {@code true} print interactively, otherwise attempt to print in the background
     */
    public PrintIMObjectTask(String shortName, MailContext context, boolean interactive) {
        this.shortName = shortName;
        this.mailContext = context;
        this.interactive = interactive;
    }

    /**
     * Determines if printing may be skipped. This only applies when the task
     * is not required. Defaults to {@code true}.
     *
     * @param skip if {@code true} and the task is not required, displays a
     *             skip button to skip printing.
     */
    public void setEnableSkip(boolean skip) {
        enableSkip = skip;
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
        if (object == null) {
            object = context.getObject(shortName);
        }
        if (object != null) {
            try {
                ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, context);
                IMPrinter<IMObject> printer = IMPrinterFactory.create(object, locator, context);
                boolean skip = !isRequired() && enableSkip;
                HelpContext help = context.getHelpContext().createTopic(object, "print");
                InteractiveIMPrinter<IMObject> iPrinter = new InteractiveIMPrinter<IMObject>(printer, skip, context, help);
                iPrinter.setInteractive(interactive);
                iPrinter.setMailContext(mailContext);

                iPrinter.setListener(new PrinterListener() {
                    public void printed(String printer) {
                        onPrinted(object);
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
            notifyCancelled();
        }
    }

    /**
     * Invoked when the object is successfully printed.
     * Notifies completion of the task.
     *
     * @param object the printed object
     */
    protected void onPrinted(IMObject object) {
        notifyCompleted();
    }

}
