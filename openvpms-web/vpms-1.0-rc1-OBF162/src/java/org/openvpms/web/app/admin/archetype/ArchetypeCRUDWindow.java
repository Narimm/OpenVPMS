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
import nextapp.echo2.app.filetransfer.UploadEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.app.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.servlet.DownloadServlet;
import org.openvpms.web.system.ServiceHelper;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


/**
 * Add description here.
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
     * Archetype descriptor mapping.
     */
    private Mapping mapping;

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
     * The archetype descriptors castor mapping.
     */
    private static final String MAPPING
            = "org/openvpms/component/business/domain/im/archetype/descriptor/"
            + "archetype-mapping-file.xml";


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
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            Marshaller marshaller = new Marshaller(writer);
            marshaller.setMapping(getMapping());
            marshaller.setMarshalAsDocument(true);
            marshaller.marshal(descriptors);
            writer.close();
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
            Mapping mapping = getMapping();
            ArchetypeDescriptors descriptors = (ArchetypeDescriptors)
                    new Unmarshaller(mapping).unmarshal(
                            new InputStreamReader(stream));

            BatchLoader.Listener listener = new BatchLoader.Listener() {
                public void completed(ArchetypeDescriptor descriptor) {
                    if (descriptor != null) {
                        onSaved(descriptor, true);
                    }
                }
            };
            BatchLoader loader = new BatchLoader(descriptors, listener);
            loader.load();
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Returns the {@link ArchetypeDescriptors} mapping.
     *
     * @return the mapping
     * @throws MappingException if the mapping can't be loaded
     * @throws IOException      for any I/O error
     */
    private Mapping getMapping() throws MappingException, IOException {
        if (mapping == null) {
            mapping = new Mapping();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = loader.getResourceAsStream(MAPPING);
            mapping.loadMapping(new InputSource(new InputStreamReader(stream)));
        }
        return mapping;
    }
}
