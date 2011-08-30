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

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.im.doc.DocumentGenerator;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Document CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DocumentCRUDWindow extends ActCRUDWindow<DocumentAct> {


    /**
     * The refresh button.
     */
    private Button refresh;

    /**
     * Refresh button identifier.
     */
    private static final String REFRESH_ID = "refresh";


    /**
     * Create a new <tt>DocumentCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public DocumentCRUDWindow(Archetypes<DocumentAct> archetypes) {
        super(archetypes);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        refresh = ButtonFactory.create(REFRESH_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
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
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.removeAll();
        if (enable) {
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            buttons.add(getPrintButton());
            if (canRefresh()) {
                buttons.add(refresh);
            }
        } else {
            buttons.add(getCreateButton());
        }
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
                if (bean.hasNode("documentTemplate")) {
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
        final RefreshDialog dialog = new RefreshDialog(getObject());
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
     * @param print   if <tt>true</tt> print it
     * @param version if <tt>true</tt> version the document
     */
    private void refresh(final boolean print, boolean version) {
        final DocumentAct act = getObject();
        DocumentGenerator generator
                = new DocumentGenerator(act, version, new DocumentGenerator.Listener() {
            public void generated(Document document) {
                onSaved(act, false);
                if (print) {
                    print(act);
                }
            }
        });
        generator.generate(true);
    }

    /**
     * Determines if a document can be refreshed.
     *
     * @return <tt>true</tt> if the document can be refreshed, otherwise
     *         <tt>false</tt>
     */
    private boolean canRefresh() {
        boolean refresh = false;
        Act act = getObject();
        if (!ActStatus.POSTED.equals(act.getStatus())) {
            ActBean bean = new ActBean(getObject());
            if (bean.hasNode("documentTemplate") && bean.hasNode("document")) {
                refresh = true;
            }
        }
        return refresh;
    }

    private class RefreshDialog extends ConfirmationDialog {

        /**
         * Determines if the existing version of the document should be retained.
         */
        private CheckBox version;


        /**
         * Constructs a new <tt>RefreshDialog</tt>.
         *
         * @param act the document act
         */
        public RefreshDialog(DocumentAct act) {
            super(Messages.get("document.refresh.title"), Messages.get("document.refresh.message"));
            DocumentRules rules = new DocumentRules();
            if (act.getDocument() != null && rules.supportsVersions(act)) {
                version = CheckBoxFactory.create("document.refresh.version", true);
            }
        }

        /**
         * Determines if the existing version of the document should be retained.
         *
         * @return <tt>true</tt> if the existing version should be kept
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
