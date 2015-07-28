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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.doc.DocumentUploadListener;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.im.edit.DefaultIMObjectActions;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.AbstractViewCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * CRUD window for HL7 mappings.
 *
 * @author Tim Anderson
 */
public class HL7MappingCRUDWindow extends AbstractViewCRUDWindow<Entity> {


    /**
     * The 'export lookup mapping' button.
     */
    private static final String EXPORT_ID = "button.exportMapping";

    /**
     * The 'import lookup mapping' button.
     */
    private static final String IMPORT_ID = "button.importMapping";

    /**
     * Constructs an {@link HL7MappingCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public HL7MappingCRUDWindow(Archetypes<Entity> archetypes, Context context, HelpContext help) {
        super(archetypes, DefaultIMObjectActions.<Entity>getInstance(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(EXPORT_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onExport();
            }
        });
        buttons.add(IMPORT_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onImport();
            }
        });
    }

    /**
     * Invoked when the 'export lookup mapping' button is pressed.
     */
    private void onExport() {
        HelpContext help = getHelpContext().subtopic("export");
        final LookupMappingExportDialog dialog = new LookupMappingExportDialog(help);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                String from = dialog.getMapFrom();
                String to = dialog.getMapTo();
                LookupMappingCSVWriter writer = new LookupMappingCSVWriter(ServiceHelper.getArchetypeService(),
                                                                           ServiceHelper.getLookupService(),
                                                                           ServiceHelper.getDocumentHandlers(),
                                                                           getSeparator());
                String name = from + "-to-" + to + "-mapping.csv";
                Document document = writer.write(name, from, to);
                DownloadServlet.startDownload(document);
            }
        });
        dialog.show();
    }

    /**
     * Invoked when the 'import lookup mapping' button is pressed.
     */
    private void onImport() {
        final HelpContext help = getHelpContext().subtopic("import");
        DocumentUploadListener listener = new DocumentUploadListener() {

            @Override
            protected void upload(Document document) {
                try {
                    importMapping(document, help);
                } catch (Throwable exception) {
                    ErrorHelper.show(exception);
                }
            }
        };
        UploadDialog dialog = new UploadDialog(listener, help.subtopic("upload"));
        dialog.show();
    }

    /**
     * Imports a mapping.
     *
     * @param document the document to import
     * @param help     the help context
     */
    private void importMapping(Document document, HelpContext help) {
        LookupMappingImporter importer = new LookupMappingImporter(ServiceHelper.getArchetypeService(),
                                                                   ServiceHelper.getLookupService(),
                                                                   ServiceHelper.getDocumentHandlers(),
                                                                   getSeparator());
        LookupMappings mappings = importer.load(document);
        if (!mappings.getErrors().isEmpty()) {
            List<LookupMapping> errors = mappings.getErrors();
            LookupMappingImportErrorDialog dialog = new LookupMappingImportErrorDialog(errors, help.subtopic("error"));
            dialog.show();
        } else {
            InformationDialog.show(Messages.get("admin.hl7.mapping.import.title"),
                                   Messages.get("admin.hl7.mapping.import.message"));
        }
    }

    /**
     * Returns the expected separator character.
     *
     * @return the separator character
     */
    private char getSeparator() {
        Context context = getContext();
        Party practice = context.getPractice();
        if (practice != null) {
            PracticeRules rules = ServiceHelper.getBean(PracticeRules.class);
            return rules.getExportFileFieldSeparator(practice);
        }
        return ',';
    }
}
