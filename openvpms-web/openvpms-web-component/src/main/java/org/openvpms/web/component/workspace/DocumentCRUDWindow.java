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

package org.openvpms.web.component.workspace;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.doc.DocumentGenerator;
import org.openvpms.web.component.im.doc.DocumentGeneratorFactory;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * Document CRUD window.
 *
 * @author Tim Anderson
 */
public class DocumentCRUDWindow extends ActCRUDWindow<DocumentAct> {

    /**
     * Refresh button identifier.
     */
    private static final String REFRESH_ID = "button.refresh";

    /**
     * External edit button identifier.
     */
    private static final String EXTERNAL_EDIT_ID = "button.externaledit";

    /**
     * Constructs a {@link DocumentCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public DocumentCRUDWindow(Archetypes<DocumentAct> archetypes, Context context, HelpContext help) {
        this(archetypes, new DocumentActActions(), context, help);
    }

    /**
     * Constructs a {@link DocumentCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param actions    determines the operations that may be performed on the selected object. If {@code null},
     *                   actions should be registered via {@link #setActions(IMObjectActions)}
     * @param help       the help context
     */
    public DocumentCRUDWindow(Archetypes<DocumentAct> archetypes, DocumentActActions actions, Context context,
                              HelpContext help) {
        super(archetypes, actions, context, help);
    }

    /**
     * Returns the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected DocumentActActions getActions() {
        return (DocumentActActions) super.getActions();
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createPrintButton());
        buttons.add(ButtonFactory.create(REFRESH_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onRefresh();
            }
        }));
        buttons.add(ButtonFactory.create(EXTERNAL_EDIT_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onExternalEdit();
            }
        }));
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
        enablePrintPreview(buttons, enable);
        boolean enableRefresh = enable && canRefresh();
        buttons.setEnabled(REFRESH_ID, enableRefresh);
        buttons.setEnabled(EXTERNAL_EDIT_ID, enable && getActions().canExternalEdit(getObject()));
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    @Override
    protected void onPrint() {
        DocumentAct act = IMObjectHelper.reload(getObject());
        if (act == null) {
            ErrorDialog.show(Messages.format("imobject.noexist", getArchetypes().getDisplayName()));
        } else if (act.getDocument() == null) {
            if (canRefresh()) {
                // regenerate the document, and print
                refresh(act, true, false);
            } else {
                ActBean bean = new ActBean(act);
                if (bean.hasNode("documentTemplate") || bean.hasNode("investigationType")) {
                    // document is generated on the fly
                    print(act);
                }
            }
        } else {
            print(act);
        }
    }

    /**
     * Invoked when the 'refresh' button is pressed.
     */
    private void onRefresh() {
        final DocumentAct act = IMObjectHelper.reload(getObject());
        if (act == null) {
            ErrorDialog.show(Messages.format("imobject.noexist", getArchetypes().getDisplayName()));
        } else {
            String name = act.getFileName();
            if (name == null) {
                ActBean bean = new ActBean(act);
                if (bean.hasNode("documentTemplate")) {
                    Entity documentTemplate = bean.getNodeParticipant("documentTemplate");
                    if (documentTemplate != null) {
                        name = documentTemplate.getName();
                    }
                }
            }
            if (name == null) {
                name = Messages.get("imobject.none");
            }
            final RefreshDialog dialog = new RefreshDialog(act, name, getHelpContext());
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    refresh(act, false, dialog.version());
                }
            });
            dialog.show();
        }
    }

    /**
     * Refreshes an act, optionally printing it.
     *
     * @param act     the act to refresh
     * @param print   if {@code true} print it
     * @param version if {@code true} version the document
     */
    private void refresh(final DocumentAct act, final boolean print, boolean version) {
        DocumentGeneratorFactory factory = ServiceHelper.getBean(DocumentGeneratorFactory.class);
        DocumentGenerator generator = factory.create(
                act, getContext(), getHelpContext(), new DocumentGenerator.AbstractListener() {
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
     * Launches an external editor to edit the selected document, if editing of the document is supported.
     */
    private void onExternalEdit() {
        final DocumentAct act = IMObjectHelper.reload(getObject());
        if (act == null) {
            ErrorDialog.show(Messages.format("imobject.noexist", getArchetypes().getDisplayName()));
        } else if (act.getDocument() != null) {
            getActions().externalEdit(act);
        } else {
            // the act has no document attached. Try and generate it first.
            DocumentGeneratorFactory factory = ServiceHelper.getBean(DocumentGeneratorFactory.class);
            DocumentGenerator generator = factory.create(act, getContext(), getHelpContext(),
                                                         new DocumentGenerator.AbstractListener() {
                                                             @Override
                                                             public void generated(Document document) {
                                                                 onSaved(act, false);
                                                                 DocumentActActions actions = getActions();
                                                                 if (actions.canExternalEdit(act)) {
                                                                     actions.externalEdit(act);
                                                                 }
                                                             }
                                                         });
            generator.generate(true, true);
        }
    }

    /**
     * Determines if a document can be refreshed.
     *
     * @return {@code true} if the document can be refreshed, otherwise
     * {@code false}
     */
    private boolean canRefresh() {
        DocumentAct act = getObject();
        return (act != null && getActions().canRefresh(act));
    }

    private class RefreshDialog extends ConfirmationDialog {

        /**
         * Determines if the existing version of the document should be retained.
         */
        private CheckBox version;


        /**
         * Constructs a {@link RefreshDialog}.
         *
         * @param act  the document act
         * @param name the name of the document
         * @param help the help context
         */
        public RefreshDialog(DocumentAct act, String name, HelpContext help) {
            super(Messages.get("document.refresh.title"), Messages.format("document.refresh.message", name),
                  help.subtopic("refresh"));
            DocumentRules rules = new DocumentRules(ServiceHelper.getArchetypeService());
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
                Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, content, version);
                Row row = RowFactory.create(Styles.LARGE_INSET, column);
                getLayout().add(row);
            } else {
                super.doLayout();
            }
        }

    }

}
