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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.document;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.patient.PatientActCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.print.IMObjectPrinter;
import org.openvpms.web.component.im.print.IMObjectPrinterListener;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Patient document CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DocumentCRUDWindow extends PatientActCRUDWindow {

    /**
     * The refresh button.
     */
    private Button _refresh;

    /**
     * Refresh button identifier.
     */
    private static final String REFRESH_ID = "refresh";


    /**
     * Create a new <code>DocumentCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create
     */
    public DocumentCRUDWindow(String type, String[] shortNames) {
        super(type, new ShortNameList(shortNames));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        _refresh = ButtonFactory.create(REFRESH_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onRefresh();
            }
        });

        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        Row buttons = getButtons();
        buttons.removeAll();
        if (enable) {
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            if (canPrint()) {
                buttons.add(getPrintButton());
            }
            if (canRefresh()) {
                buttons.add(_refresh);
            }
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Creates a new printer.
     *
     * @return a new printer.
     */
    @Override
    protected IMObjectPrinter createPrinter() {
        String type = getTypeDisplayName();
        IMObjectPrinter printer = new PatientDocumentPrinter(type);
        printer.setListener(new IMObjectPrinterListener() {
            public void printed(IMObject object) {
                DocumentCRUDWindow.this.printed(object);
            }
        });
        return printer;
    }

    /**
     * Invoked when the 'refresh' button is pressed.
     */
    private void onRefresh() {
        String title = Messages.get("patient.document.refresh.title");
        String message = Messages.get("patient.document.refresh.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    refresh();
                }
            }
        });
        dialog.show();
    }

    /**
     * Refreshes the current document act.
     */
    private void refresh() {
        DocumentAct act = (DocumentAct) getObject();
        PatientDocumentActEditor editor
                = new PatientDocumentActEditor(act, null, null);
        if (editor.refresh()) {
            editor.save();
            onSaved(act, false);
        }
    }

    /**
     * Determines if a document can be printed.
     *
     * @return <code>true</code> if the document can be refreshed, otherwise
     *         <code>false</code>
     */
    private boolean canPrint() {
        DocumentAct act = (DocumentAct) getObject();
        return (act.getDocReference() != null);
    }

    /**
     * Determines if a document can be refreshed.
     *
     * @return <code>true</code> if the document can be refreshed, otherwise
     *         <code>false</code>
     */
    private boolean canRefresh() {
        ActBean act = new ActBean((Act) getObject());
        return act.hasNode("documentTemplate") && act.hasNode("docReference");
    }

}
