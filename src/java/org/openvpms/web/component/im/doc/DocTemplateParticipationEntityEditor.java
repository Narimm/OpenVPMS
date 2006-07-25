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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.filetransfer.UploadEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.select.Selector;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.util.Messages;

import java.io.InputStream;


/**
 * Participation editor for document templates, where the parent object
 * is an {@link Entity}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocTemplateParticipationEntityEditor
        extends AbstractIMObjectEditor {

    /**
     * The upload selector.
     */
    private Selector _selector;

    /**
     * The document act.
     */
    private DocumentAct _act;

    /**
     * Determines if the document has changed.
     */
    private boolean _docModified = false;


    /**
     * Construct a new <code>DocTemplateParticipationEntityEditor</code>.
     *
     * @param participation the participation to edit
     * @param parent        the parent entity.
     * @param context       the layout context. May be <code>null</code>.
     */
    public DocTemplateParticipationEntityEditor(Participation participation,
                                                Entity parent,
                                                LayoutContext context) {
        super(participation, parent, context);
        Property entity = getProperty("entity");
        if (entity.getValue() == null) {
            entity.setValue(parent.getObjectReference());
        }
        _act = getDocumentAct();
        _selector = new Selector();
        _selector.getSelect().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelect();
            }
        });
        _selector.setObject(_act);
    }

    /**
     * Determines if the object has been changed.
     *
     * @return <code>true</code> if the object has been changed
     */
    @Override
    public boolean isModified() {
        return super.isModified() || _docModified;
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
        super.clearModified();
        _docModified = false;
    }

    /**
     * Save any modified child Saveable instances.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    protected boolean saveChildren() {
        boolean saved = super.saveChildren();
        if (saved && _docModified) {
            saved = SaveHelper.save(_act);
        }
        return saved;
    }

    /**
     * Returns the document act.
     *
     * @return the document act
     */
    protected DocumentAct getDocumentAct() {
        DocumentAct docAct;
        Property act = getProperty("act");
        IMObjectReference ref = (IMObjectReference) act.getValue();
        if (ref == null) {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            docAct = (DocumentAct) service.create("act.documentTemplate");
            Participation participation = (Participation) getObject();
            participation.setAct(docAct.getObjectReference());
            docAct.addParticipation(participation);
        } else {
            docAct = (DocumentAct) IMObjectHelper.getObject(ref);
        }
        return docAct;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new IMObjectLayoutStrategy() {
            public Component apply(IMObject object, PropertySet properties,
                                   LayoutContext context) {
                return _selector.getComponent();
            }
        };
    }

    private void onSelect() {
        final IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        UploadListener listener = new UploadListener() {
            public void fileUpload(UploadEvent event) {
                try {
                    String fileName = event.getFileName();
                    InputStream stream = event.getInputStream();
                    Document doc = DocumentFactory.create(fileName, stream);
                    service.save(doc);
                    _act.setName(doc.getName());
                    _act.setDescription(doc.getDescription());
                    _act.setDocReference(doc.getObjectReference());
                    _selector.setObject(_act);
                    _docModified = true;
                } catch (Exception exception) {
                    ErrorHelper.show(exception);
                }
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

}
