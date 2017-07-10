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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.print;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;

/**
 * A print dialog that enables documents to be printed, exported or mailed.
 *
 * @author Tim Anderson
 */
public abstract class ExportPrintDialog extends PrintDialog {

    /**
     * The export button identifier.
     */
    public static final String EXPORT_ID = "export";

    /**
     * The export mail button identifier.
     */
    public static final String EXPORT_MAIL_ID = "exportMail";


    /**
     * Constructs a {@link ExportPrintDialog}.
     *
     * @param title    the window title
     * @param location the current practice location. May be {@code null}
     * @param help     the help context. May be {@code null}
     */
    public ExportPrintDialog(String title, Party location, HelpContext help) {
        super(title, true, true, false, location, help);
        setStyleName("ExportPrintDialog");
    }

    /**
     * Invoked when the export button is pressed.
     */
    protected abstract void onExport();

    /**
     * Invoked when the export mail button is pressed.
     */
    protected abstract void onExportMail();

    /**
     * Lays out the dialog.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        addButton(EXPORT_ID, new ActionListener() {
            public void onAction(ActionEvent e) {
                onExport();
            }
        });

        addButton(EXPORT_MAIL_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onExportMail();
            }
        });
    }

}
