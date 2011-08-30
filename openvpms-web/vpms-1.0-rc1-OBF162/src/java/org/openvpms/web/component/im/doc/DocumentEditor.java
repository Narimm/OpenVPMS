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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.filetransfer.UploadEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.Cancellable;
import org.openvpms.web.component.edit.Deletable;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.io.InputStream;


/**
 * Editor for {@link IMObjectReference}s of type <em>document.*</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentEditor extends AbstractPropertyEditor
        implements Saveable, Cancellable, Deletable {

    /**
     * The document type label.
     */
    private final Label docType;

    /**
     * The component.
     */
    private final Row component;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup;

    /**
     * Manages old document references to avoid orphaned documents.
     */
    private final DocReferenceMgr refMgr;

    /**
     * Indicates if the object has been saved.
     */
    private boolean saved = false;


    /**
     * Construct a new <code>DocumentEditor</code>.
     *
     * @param property the property being edited
     */
    public DocumentEditor(Property property) {
        super(property);

        docType = LabelFactory.create();
        Button upload = ButtonFactory.create("upload", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onUpload();
            }
        });
        focusGroup = new FocusGroup("DocumentEditor");
        focusGroup.add(upload);
        component = RowFactory.create("CellSpacing", upload, docType);

        IMObjectReference original = (IMObjectReference) property.getValue();
        refMgr = new DocReferenceMgr(original);
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or <code>null</code> if the editor hasn't been
     *         rendered
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    public boolean save() {
        boolean result;
        try {
            refMgr.commit();
            saved = true;
            result = true;
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
            result = false;
        }
        return result;
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return <code>true</code> if edits have been saved.
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Cancel any edits.
     */
    public void cancel() {
        try {
            refMgr.rollback();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Perform deletion.
     *
     * @return <tt>true</tt> if deletion was successful
     */
    public boolean delete() {
        boolean result;
        try {
            refMgr.delete();
            result = true;
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
            result = false;
        }
        return result;
    }


    /**
     * Invoked when the upload button is pressed.
     */
    private void onUpload() {
        UploadListener listener = new UploadListener() {
            public void fileUpload(UploadEvent event) {
                String fileName = event.getFileName();
                InputStream stream = event.getInputStream();
                String contentType = event.getContentType();
                int size = event.getSize();
                upload(fileName, stream, contentType, size);
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
     * Uploads a file.
     *
     * @param fileName    the filename
     * @param stream      the file stream
     * @param contentType the mime type
     * @param size        the content length
     */
    private void upload(String fileName, InputStream stream, String contentType,
                        int size) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
        try {
            DocumentHandler handler = handlers.get(fileName, contentType);
            Document doc = handler.create(fileName, stream, contentType, size);
            service.save(doc);
            replaceDocReference(doc);
            String displayName = DescriptorHelper.getDisplayName(doc);
            docType.setText(displayName);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Replaces the existing document reference with that of a new document.
     * The existing document is queued for deletion.
     *
     * @param document the new document
     */
    private void replaceDocReference(Document document) {
        IMObjectReference ref = document.getObjectReference();
        getProperty().setValue(ref);
        refMgr.add(ref);
    }

}
