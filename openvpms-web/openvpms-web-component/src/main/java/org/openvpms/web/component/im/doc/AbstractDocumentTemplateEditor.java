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
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.select.BasicSelector;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.system.ServiceHelper;

/**
 * Editor for document templates that have an associated mandatory/optional document act.
 *
 * @author Tim Anderson
 */
public class AbstractDocumentTemplateEditor extends AbstractIMObjectEditor {

    /**
     * The upload selector.
     */
    private BasicSelector<DocumentAct> selector;

    /**
     * The document act that has the template.
     */
    private DocumentAct act;

    /**
     * Determines if the document has changed.
     */
    private boolean docModified = false;

    /**
     * Helper to hook the document into the validation support.
     */
    private SimpleProperty content = new SimpleProperty("content", IMObjectReference.class);

    /**
     * The document handler.
     */
    private final DocumentHandler handler;

    /**
     * Manages old document references to avoid orphaned documents.
     */
    private final DocReferenceMgr refMgr;

    /**
     * Determines if the document should be deleted on save.
     */
    private boolean deleteDocOnSave;


    /**
     * Constructs a {@link DocumentTemplateEditor}.
     *
     * @param template    the object to edit
     * @param parent      the parent object. May be {@code null}
     * @param docRequired determines if the document template is required
     * @param handler     the document handler. May be {@code null}
     * @param context     the layout context. May be {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public AbstractDocumentTemplateEditor(Entity template, IMObject parent, boolean docRequired,
                                          DocumentHandler handler, LayoutContext context) {
        super(template, parent, context);
        setDocumentRequired(docRequired);
        this.handler = handler;
        refMgr = new DocReferenceMgr(context.getContext());
        getDocumentAct();
        content.setValue(act.getDocument());

        selector = new BasicSelector<>("button.upload");
        selector.getSelect().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelect();
            }
        });
        updateDisplay(act);
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
     * Cancel any edits. Once complete, query methods may be invoked, but the behaviour of other methods is undefined.
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
     * Returns the document act, creating it if none exists.
     *
     * @return the document act
     */
    protected DocumentAct getDocumentAct() {
        if (act == null) {
            TemplateHelper helper = new TemplateHelper(ServiceHelper.getArchetypeService());
            act = helper.getDocumentAct((Entity) getObject());
            if (act == null) {
                act = createDocumentAct();
                ActBean bean = new ActBean(act);
                bean.addNodeParticipation("template", (Entity) getObject());
            } else if (act.getDocument() != null){
                refMgr.add(act.getDocument());
            }
        }
        return act;
    }

    /**
     * Creates a new document act.
     *
     * @return a new document act
     */
    protected DocumentAct createDocumentAct() {
        return (DocumentAct) ServiceHelper.getArchetypeService().create(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && content.validate(validator);
    }

    /**
     * Determines if the document is required.
     *
     * @param required if {@code true}, the document is required for the editor to be valid
     */
    protected void setDocumentRequired(boolean required) {
        content.setRequired(required);
    }

    /**
     * Determines if the document is required.
     *
     * @return {@code true} if the document is required
     */
    protected boolean isDocumentRequired() {
        return content.isRequired();
    }

    /**
     * Determines if the document should be deleted on save.
     *
     * @param delete if {@code true}, delete the document on save
     */
    protected void setDeleteDocument(boolean delete) {
        deleteDocOnSave = delete;
    }

    /**
     * Save any modified child Saveable instances.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void saveChildren() {
        super.saveChildren();
        if (deleteDocOnSave) {
            if (isDocumentRequired()) {
                throw new IllegalStateException("Document is required but has been flagged for deletion");
            }
            if (act != null && !act.isNew()) {
                ServiceHelper.getArchetypeService().remove(act);
                act = null;
            }
            refMgr.delete();
        } else if (act != null) {
            if (docModified) {
                ServiceHelper.getArchetypeService().save(act);
                refMgr.commit();
                docModified = false;
            }
        }
    }

    /**
     * Deletes the object.
     *
     * @throws OpenVPMSException if the delete fails
     */
    @Override
    protected void doDelete() {
        ServiceHelper.getArchetypeService().remove(act);
        act = null;
        super.doDelete();
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
     * Returns the selector.
     *
     * @return the selector
     */
    protected ComponentState getSelector() {
        return new ComponentState(selector.getComponent(), content);
    }

    /**
     * Invoked to upload a document.
     */
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
        getDocumentAct(); // make sure there is a current act.
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
        content.setValue(ref);
    }
}
