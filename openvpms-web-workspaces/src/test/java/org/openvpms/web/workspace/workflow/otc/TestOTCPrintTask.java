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

package org.openvpms.web.workspace.workflow.otc;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.PracticeMailContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.print.PrintDialog;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Test implementation of the {@link PrintIMObjectTask}.
 *
 * @author Tim Anderson
 */
class TestOTCPrintTask extends PrintIMObjectTask {

    /**
     * The printer.
     */
    private InteractiveIMPrinter<IMObject> printer;

    /**
     * Constructs an {@link TestOTCPrintTask}.
     *
     * @param parent the parent context
     */
    public TestOTCPrintTask(Context parent) {
        super(CustomerAccountArchetypes.COUNTER, new PracticeMailContext(parent));
    }

    /**
     * Returns the print dialog.
     *
     * @return the print dialog, or {@code null} if none is being displayed
     */
    public PrintDialog getPrintDialog() {
        return (printer != null) ? printer.getPrintDialog() : null;
    }

    /**
     * Creates a printer.
     *
     * @param printer the printer to delegate to
     * @param skip    if {@code true} display a 'skip' button that simply closes the dialog
     * @param context the context
     * @param help    the help context
     */
    @Override
    protected InteractiveIMPrinter<IMObject> createPrinter(IMPrinter<IMObject> printer, boolean skip,
                                                           TaskContext context, HelpContext help) {
        this.printer = super.createPrinter(printer, skip, context, help);
        return this.printer;
    }
}
