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
 *
 *  $Id$
 */

package org.openvpms.web.app.admin.archetype;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import nextapp.echo2.app.filetransfer.UploadEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.tools.archetype.loader.Change;
import org.openvpms.web.app.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.servlet.DownloadServlet;
import org.openvpms.web.system.ServiceHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Archetype CRUD window, providing facilties to import and export archetype
 * descriptors.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ArchetypeCRUDWindow
        extends AbstractViewCRUDWindow<ArchetypeDescriptor> {

    /**
     * The import button.
     */
    private Button importButton;

    /**
     * The export button.
     */
    private Button exportButton;

    /**
     * The import button identifier.
     */
    private static final String IMPORT_ID = "import";

    /**
     * The export button identifier.
     */
    private static final String EXPORT_ID = "export";

    /**
     * The export mime type.
     */
    private static final String MIME_TYPE = "text/xml";


    /**
     * Constructs a new <tt>ArchetypeCRUDWindow</tt>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public ArchetypeCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(getImportButton());
        buttons.add(getExportButton());
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
        buttons.remove(getImportButton());
        buttons.remove(getExportButton());
        buttons.add(getImportButton());
        if (enable) {
            buttons.add(getExportButton());
        }
    }

    /**
     * Invoked when the object has been saved. If derived nodes have changed,
     * this prompts to update associated objects.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(ArchetypeDescriptor object, boolean isNew) {
        Change change = new Change(object, getObject());
        super.onSaved(object, isNew);
        if (change.hasChangedDerivedNodes()) {
            confirmUpdateDerivedNodes(Arrays.asList(object));
        }
    }

    /**
     * Returns the import button.
     *
     * @return the import button
     */
    private Button getImportButton() {
        if (importButton == null) {
            importButton = ButtonFactory.create(
                    IMPORT_ID, new ActionListener() {
                public void actionPerformed(
                        ActionEvent event) {
                    onImport();
                }
            });
        }
        return importButton;
    }

    /**
     * Returns the export button.
     *
     * @return the export button
     */
    private Button getExportButton() {
        if (exportButton == null) {
            exportButton = ButtonFactory.create(
                    EXPORT_ID, new ActionListener() {
                public void actionPerformed(
                        ActionEvent event) {
                    onExport();
                }
            });
        }
        return exportButton;
    }

    /**
     * Invoked when the import button is pressed.
     */
    private void onImport() {
        UploadListener listener = new UploadListener() {
            public void fileUpload(UploadEvent event) {
                InputStream stream = event.getInputStream();
                upload(stream);
            }

            public void invalidFileUpload(UploadEvent event) {
                String message = Messages.get("file.upload.failed",
                                              event.getFileName());
                ErrorDialog.show(message);
            }
        };
        UploadDialog dialog = new UploadDialog(listener);
        dialog.show();
    }

    /**
     * Invoked when the export button is pressed.
     */
    private void onExport() {
        ArchetypeDescriptor descriptor = getObject();
        try {
            ArchetypeDescriptors descriptors = new ArchetypeDescriptors();
            descriptors.setArchetypeDescriptorsAsArray(
                    new ArchetypeDescriptor[]{descriptor});
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ArchetypeDescriptors.write(descriptors, stream);
            String name = descriptor.getShortName() + ".adl";
            DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
            DocumentHandler handler = handlers.get(name, MIME_TYPE);
            byte[] buffer = stream.toByteArray();
            Document document = handler.create(
                    name, new ByteArrayInputStream(buffer), MIME_TYPE,
                    buffer.length);
            DownloadServlet.startDownload(document);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Uploads a file.
     *
     * @param stream the file stream
     */
    private void upload(InputStream stream) {
        try {
            ArchetypeDescriptors descriptors
                    = ArchetypeDescriptors.read(stream);

            final BatchArchetypeLoader loader
                    = new BatchArchetypeLoader(descriptors);
            loader.setListener(new BatchProcessorListener() {
                public void completed() {
                    uploaded(loader.getChanges());
                }

                public void error(Throwable exception) {
                    ErrorHelper.show(exception);
                }
            });

            loader.process();
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when uploading is completed.
     *
     * @param changes the changes
     */
    private void uploaded(List<Change> changes) {
        if (!changes.isEmpty()) {
            super.onSaved(changes.get(0).getNewVersion(), true);
            List<ArchetypeDescriptor> update
                    = new ArrayList<ArchetypeDescriptor>();
            for (Change change : changes) {
                if (change.hasChangedDerivedNodes()) {
                    update.add(change.getNewVersion());
                }
            }
            if (!update.isEmpty()) {
                confirmUpdateDerivedNodes(update);
            }
        }
    }

    /**
     * Prompts to update derived nodes of objects associated with the supplied
     * archetype descriptors.
     *
     * @param descriptors the archetype descriptors
     */
    private void confirmUpdateDerivedNodes(
            final List<ArchetypeDescriptor> descriptors) {
        StringBuffer names = new StringBuffer();
        for (ArchetypeDescriptor descriptor : descriptors) {
            if (names.length() != 0) {
                names.append(", ");
            }
            names.append(descriptor.getDisplayName());
        }

        String title = Messages.get("archetype.derived.update.title");
        String message = Messages.get(
                "archetype.derived.update.message",
                names);
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                String action = dialog.getAction();
                if (ConfirmationDialog.OK_ID.equals(action)) {
                    updateDerivedNodes(descriptors);
                }
            }
        });
        dialog.show();
    }

    /**
     * Updates derived nodes of objects associated with the supplied archetype
     * descriptors.
     *
     * @param descriptors the archetype descriptors
     */
    private void updateDerivedNodes(List<ArchetypeDescriptor> descriptors) {
        BatchArchetypeUpdater updater = new BatchArchetypeUpdater(descriptors);
        updater.setListener(new BatchProcessorListener() {
            public void completed() {
            }

            public void error(Throwable exception) {
                ErrorHelper.show(exception);
            }
        });
        updater.process();
    }

}
