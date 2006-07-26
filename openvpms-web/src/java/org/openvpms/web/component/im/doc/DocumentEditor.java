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
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.focus.FocusSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.io.InputStream;


/**
 * Editor for {@link IMObjectReference}s of type <em>document.*</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentEditor extends AbstractPropertyEditor {

    /**
     * The document type label.
     */
    private Label _docType;

    /**
     * The component.
     */
    private Row _component;


    /**
     * Construct a new <code>DocumentEditor</code>.
     *
     * @param property the property being edited
     * @param context  the layout context
     */
    public DocumentEditor(Property property, LayoutContext context) {
        super(property);

        _docType = LabelFactory.create();
        Button upload = ButtonFactory.create("upload", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onUpload();
            }
        });
        FocusSet set = new FocusSet("DocumentEditor");
        set.add(upload);
        context.getFocusTree().add(set);

        _component = RowFactory.create("CellSpacing", upload, _docType);
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    public Component getComponent() {
        return _component;
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
                Integer size = event.getSize();
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
     * @param fileName the filename
     * @param stream   the file stream
     */
    private void upload(String fileName, InputStream stream, String contentType, Integer size) {
        final IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        try {
            Document doc = DocumentFactory.create(fileName, stream, contentType, size);
            service.save(doc);
            IMObjectReference ref = doc.getObjectReference();
            getProperty().setValue(ref);
            String displayName = DescriptorHelper.getDisplayName(doc);
            _docType.setText(displayName);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

}
