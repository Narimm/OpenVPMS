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
 */

package org.openvpms.web.component.im.print;

import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.print.InteractivePrinter;
import org.openvpms.web.resource.util.Messages;


/**
 * {@link IMPrinter} implementation that provides interactive support if
 * the underlying implementation requires it. Pops up a dialog with options to
 * print, preview, or cancel.
 *
 * @author Tim Anderson
 */
public class InteractiveIMPrinter<T>
        extends InteractivePrinter implements IMPrinter<T> {


    /**
     * Constructs a new {@code InteractiveIMPrinter}.
     *
     * @param printer the printer to delegate to
     */
    public InteractiveIMPrinter(IMPrinter<T> printer, HelpContext help) {
        this(printer, false, help);
    }

    /**
     * Constructs a new {@code InteractiveIMPrinter}.
     *
     * @param printer the printer to delegate to
     * @param skip    if {@code true} display a 'skip' button that simply closes the dialog
     */
    public InteractiveIMPrinter(IMPrinter<T> printer, boolean skip, HelpContext help) {
        this(null, printer, skip, help);
    }

    /**
     * Constructs a new {@code InteractiveIMPrinter}.
     *
     * @param title   the dialog title. May be {@code null}
     * @param printer the printer to delegate to
     * @param help    the help context
     */
    public InteractiveIMPrinter(String title, IMPrinter<T> printer, HelpContext help) {
        this(title, printer, false, help);
    }

    /**
     * Constructs a new {@code InteractiveIMPrinter}.
     *
     * @param title   the dialog title. May be {@code null}
     * @param printer the printer to delegate to
     * @param skip    if {@code true} display a 'skip' button that simply closes the dialog
     * @param help    the help context
     */
    public InteractiveIMPrinter(String title, IMPrinter<T> printer, boolean skip, HelpContext help) {
        super(title, printer, skip, help);
    }

    /**
     * Returns the objects being printed.
     *
     * @return the objects being printed
     */
    public Iterable<T> getObjects() {
        return getPrinter().getObjects();
    }

    /**
     * Returns the underlying printer.
     *
     * @return the underlying printer
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IMPrinter<T> getPrinter() {
        return (IMPrinter<T>) super.getPrinter();
    }

    /**
     * Returns a title for the print dialog.
     *
     * @return a title for the print dialog
     */
    protected String getTitle() {
        String title = super.getTitle();
        if (title != null) {
            return title;
        }
        return Messages.get("imobject.print.title", getDisplayName());
    }
}
