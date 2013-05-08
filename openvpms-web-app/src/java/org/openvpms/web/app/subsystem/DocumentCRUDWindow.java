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

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.im.doc.DocumentGenerator;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.i18n.Messages;


/**
 * Document CRUD window.
 *
 * @author Tim Anderson
 */
public class DocumentCRUDWindow extends ActCRUDWindow<DocumentAct> {

    /**
     * Refresh button identifier.
     */
    private static final String REFRESH_ID = "refresh";


    /**
     * Constructs a {@code DocumentCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public DocumentCRUDWindow(Archetypes<DocumentAct> archetypes, Context context, HelpContext help) {
        super(archetypes, new DocumentActActions(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        Button refresh = ButtonFactory.create(REFRESH_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onRefresh();
            }
        });
        buttons.add(createPrintButton());
        buttons.add(refresh);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(PRINT_ID, enable);
        boolean enableRefresh = enable && canRefresh();
        buttons.setEnabled(REFRESH_ID, enableRefresh);
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    @Override
    protected void onPrint() {
        DocumentAct act = getObject();
        if (act.getDocument() == null) {
            if (canRefresh()) {
                // regenerate the document, and print
                refresh(true, false);
            } else {
                ActBean bean = new ActBean(act);
                if (bean.hasNode("documentTemplate") || bean.hasNode("investigationType")) {
                    // document is generated on the fly
                    super.onPrint();
                }
            }
        } else {
            super.onPrint();
        }
    }

    /**
     * Invoked when the 'refresh' button is pressed.
     */
    private void onRefresh() {
        final RefreshDialog dialog = new RefreshDialog(getObject(), getHelpContext());
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                refresh(false, dialog.version());
            }
        });
        dialog.show();
    }

    /**
     * Refreshes the current document act, optionally printing it.
     *
     * @param print   if {@code true} print it
     * @param version if {@code true} version the document
     */
    private void refresh(final boolean print, boolean version) {
        final DocumentAct act = getObject();
        DocumentGenerator generator = new DocumentGenerator(
            act, getContext(), getHelpContext(), new DocumentGenerator.Listener() {
            public void generated(Document document) {
                onSaved(act, false);
                if (print) {
                    print(act);
                }
            }
        });
        generator.generate(true, version);
    }

    /**
     * Determines if a document can be refreshed.
     *
     * @return {@code true} if the document can be refreshed, otherwise
     *         {@code false}
     */
    private boolean canRefresh() {
        DocumentAct act = getObject();
        return (act != null && ((DocumentActActions) getActions()).canRefresh(act));
    }

    private class RefreshDialog extends ConfirmationDialog {

        /**
         * Determines if the existing version of the document should be retained.
         */
        private CheckBox version;


        /**
         * Constructs a new {@code RefreshDialog}.
         *
         * @param act  the document act
         * @param help the help context
         */
        public RefreshDialog(DocumentAct act, HelpContext help) {
            super(Messages.get("document.refresh.title"), Messages.get("document.refresh.message"),
                  help.subtopic("refresh"));
            DocumentRules rules = new DocumentRules();
            if (act.getDocument() != null && rules.supportsVersions(act)) {
                version = CheckBoxFactory.create("document.refresh.version", true);
            }
        }

        /**
         * Determines if the existing version of the document should be retained.
         *
         * @return {@code true} if the existing version should be kept
         */
        public boolean version() {
            return (version != null) && version.isSelected();
        }

        /**
         * Lays out the component prior to display.
         */
        @Override
        protected void doLayout() {
            if (version != null) {
                Label content = LabelFactory.create(true, true);
                content.setText(getMessage());
                Column column = ColumnFactory.create("WideCellSpacing", content, version);
                Row row = RowFactory.create("Inset", column);
                getLayout().add(row);
            } else {
                super.doLayout();
            }
        }

    }

}
