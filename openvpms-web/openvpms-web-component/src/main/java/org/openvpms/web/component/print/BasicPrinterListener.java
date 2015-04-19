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

package org.openvpms.web.component.print;

import org.openvpms.web.component.util.ErrorHelper;

/**
 * Basic implementation of the {@link PrinterListener} interface, for easy subclassing.
 * <p/>
 * This provides no-op implementations of the methods with the exception of {@link #failed(Throwable)} which displays
 * the error.
 *
 * @author Tim Anderson
 */
public class BasicPrinterListener implements PrinterListener {

    /**
     * Notifies of a successful print.
     *
     * @param printer the printer that was used. May be {@code null}
     */
    @Override
    public void printed(String printer) {
    }

    /**
     * Notifies that the print was cancelled.
     */
    @Override
    public void cancelled() {
    }

    /**
     * Notifies that the print was skipped.
     */
    @Override
    public void skipped() {
    }

    /**
     * Invoked when a print fails.
     *
     * @param cause the reason for the failure
     */
    @Override
    public void failed(Throwable cause) {
        ErrorHelper.show(cause);
    }
}
