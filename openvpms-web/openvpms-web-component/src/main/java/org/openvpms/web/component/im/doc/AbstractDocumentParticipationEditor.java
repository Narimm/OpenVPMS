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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.select.BasicSelector;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.system.ServiceHelper;


/**
 * Editor for participation relationships where the target is a {@link DocumentAct}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractDocumentParticipationEditor extends AbstractIMObjectEditor {

    /**
     * The upload selector.
     */
    private BasicSelector<DocumentAct> selector;

    /**
     * The document act.
     */
    private DocumentAct act;

    /**
     * Determines if the document has changed.
     */
    private boolean docModified = false;

    /**
     * Manages old document references to avoid orphaned documents.
     */
    private final DocReferenceMgr refMgr;

    /**
     * Determines if the act should be deleted on delete().
     */
    private boolean deleteAct = false;

    /**
     * The handler to use when uploading documents. May be {@code null}
     */
    private DocumentHandler handler;


    /**
     * @param participation the participation to edit
     * @param parent        the parent entity
     * @param context       the layout context
     */
    public AbstractDocumentParticipationEditor(Participation participation, Entity parent, LayoutContext context) {
        super(participation, parent, context);
        Property entity = getProperty("entity");
        if (entity.getValue() == null) {
            entity.setValue(parent.getObjectReference());
        }
        getDocumentAct(); // get/create the document act
        selector = new BasicSelector<>("button.upload");
        selector.getSelect().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelect();
            }
        });
        updateDisplay(act);
        refMgr = new DocReferenceMgr(act.getDocument(), context.getContext());
    }

    /**
     * Determines if the associated act should be deleted when {@link #delete()} is invoked.
     * Defaults to {@code false}.
     *
     * @param delete if {@code true} delete the act
     */
    public void setDeleteAct(boolean delete) {
        this.deleteAct = delete;
    }

    /**
     * Sets the handler to use when uploading documents.
     *
     * @param handler the handler. May be {@code null}
     */
    public void setDocumentHandler(DocumentHandler handler) {
        this.handler = handler;
    }

    /**
     * Determines if the object has been changed.
     *
     * @return {@code true} if the object has been changed
     */
    @Override
    public boolean isModified() {
        return super.isModified() || docModified;
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
        super.clearModified();
        docModified = false;
    }

    /**
     * Sets the description of the document act.
     *
     * @param description the description of the document act. May be {@code null}
     */
    public void setDescription(String description) {
        act.setDescription(description);
        updateDisplay(act);
    }

    /**
     * Cancel any edits. Once complete, query methods may be invoked, but the
     * behaviour of other methods is undefined.
     */
    @Override
    public void cancel() {
        super.cancel();
        try {
            refMgr.rollback();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Save any modified child Saveable instances.
     */
    @Override
    protected void saveChildren() {
        super.saveChildren();
        if (docModified || act.isNew()) {
            if (!act.isNew()) {
                // need to reload the act as the participation has already
                // been saved by the parent Entity. Failing to do so will result in hibernate StaleObjectExceptions
                IMObjectReference ref = act.getDocument();
                String fileName = act.getFileName();
                String mimeType = act.getMimeType();
                String description = act.getDescription();
                act = reload(act);
                act.setDocument(ref);
                act.setFileName(fileName);
                act.setMimeType(mimeType);
                act.setDescription(description);
            }
            ServiceHelper.getArchetypeService().save(act);
            refMgr.commit();
        }
    }

    /**
     * Deletes the object.
     *
     * @throws OpenVPMSException if the delete fails
     */
    @Override
    protected void doDelete() {
        if (deleteAct) {
            ServiceHelper.getArchetypeService().remove(act);
            deleteChildren();
        } else {
            super.doDelete();
        }
    }

    /**
     * Deletes any child Deletable instances.
     *
     * @throws OpenVPMSException if the delete fails
     */
    @Override
    protected void deleteChildren() {
        super.deleteChildren();
        refMgr.delete();
    }

    /**
     * Returns the document act, creating it if none exists
     *
     * @return the document act
     */
    protected DocumentAct getDocumentAct() {
        if (act == null) {
            Property property = getProperty("act");
            IMObjectReference ref = (IMObjectReference) property.getValue();
            act = (DocumentAct) getObject(ref);
            if (act == null) {
                act = createDocumentAct();
                Participation participation = (Participation) getObject();
                participation.setAct(act.getObjectReference());
                act.addParticipation(participation);
            }
        }
        return act;
    }

    /**
     * Creates a new document act.
     *
     * @return a new document act
     */
    protected abstract DocumentAct createDocumentAct();

    /**
     * Helper to create a document act.
     *
     * @param shortName the document act short name
     * @return a new document act
     */
    protected DocumentAct createDocumentAct(String shortName) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        return (DocumentAct) service.create(shortName);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new IMObjectLayoutStrategy() {
            public void addComponent(ComponentState state) {
                // do nothing
            }

            public ComponentState apply(IMObject object, PropertySet properties, IMObject parent,
                                        LayoutContext context) {
                return new ComponentState(selector.getComponent());
            }
        };
    }

    /**
     * Returns the selector.
     *
     * @return the selector
     */
    protected BasicSelector<DocumentAct> getSelector() {
        return selector;
    }

    protected void onSelect() {
        UploadListener listener = new DocumentUploadListener(handler) {
            protected void upload(Document doc) {
                onUpload(doc);
            }
        };
        UploadDialog dialog = new UploadDialog(listener, getLayoutContext().getHelpContext());
        dialog.show();
    }

    /**
     * Invoked when a document is uploaded.
     *
     * @param document the uploaded document
     */
    protected void onUpload(Document document) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        service.save(document);
        populateDocumentAct(act, document);
        replaceDocReference(document);
        updateDisplay(act);
        docModified = true;
    }

    /**
     * Populates a document act with details from a document.
     *
     * @param act      the act to populate
     * @param document the document
     */
    protected void populateDocumentAct(DocumentAct act, Document document) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        act.setFileName(document.getName());
        service.deriveValue(act, "name");
        act.setMimeType(document.getMimeType());
        if (getParent() == null) {
            act.setDescription(document.getDescription());
        } else {
            act.setDescription(getParent().getName());
        }
    }

    /**
     * Updates the display with the selected act.
     *
     * @param act the act
     */
    protected void updateDisplay(DocumentAct act) {
        selector.setObject(act);
    }

    /**
     * Replaces the existing document reference with that of a new document.
     * The existing document is queued for deletion.
     *
     * @param document the new document
     */
    private void replaceDocReference(Document document) {
        IMObjectReference ref = document.getObjectReference();
        act.setDocument(ref);
        refMgr.add(ref);
    }
}
