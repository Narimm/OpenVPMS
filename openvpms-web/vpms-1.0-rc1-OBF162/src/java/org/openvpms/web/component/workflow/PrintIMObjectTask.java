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
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.print.PrinterListener;


/**
 * Prints an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PrintIMObjectTask extends AbstractTask {

    /**
     * The short name of the object to print.
     */
    private final String shortName;

    /**
     * Determines if objects should be printed interactively.
     * If <tt>false</tt>, indicates to try and print in the background unless
     * printing requires user intervention.
     */
    private final boolean interactive;


    /**
     * Creates a new <tt>PrintIMObjectTask</tt>.
     *
     * @param shortName the short name of the object to print
     */
    public PrintIMObjectTask(String shortName) {
        this(shortName, true);
    }

    /**
     * Creates a new <tt>PrintIMObjectTask</tt>.
     *
     * @param shortName   the short name of the object to print
     * @param interactive if <tt>true</tt> print interactively, otherwise
     *                    attempt to print in the background
     */
    public PrintIMObjectTask(String shortName, boolean interactive) {
        this.shortName = shortName;
        this.interactive = interactive;
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
        IMObject object = context.getObject(shortName);
        if (object != null) {
            try {
                IMPrinter<IMObject> printer = IMPrinterFactory.create(
                        object);
                boolean skip = !isRequired();
                InteractiveIMPrinter<IMObject> iPrinter
                        = new InteractiveIMPrinter<IMObject>(printer, skip);
                iPrinter.setInteractive(interactive);

                iPrinter.setListener(new PrinterListener() {
                    public void printed() {
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
            notifyCancelled();
        }
    }

}
