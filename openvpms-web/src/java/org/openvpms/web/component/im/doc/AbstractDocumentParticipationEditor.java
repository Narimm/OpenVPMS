package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.select.BasicSelector;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ErrorHelper;


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
     * @param participation the participation to edit
     * @param parent        the parent entity
     * @param context       the layout context. May be <tt>null</tt>.
     */
    public AbstractDocumentParticipationEditor(Participation participation, Entity parent, LayoutContext context) {
        super(participation, parent, context);
        Property entity = getProperty("entity");
        if (entity.getValue() == null) {
            entity.setValue(parent.getObjectReference());
        }
        getDocumentAct(); // get/create the document act
        selector = new BasicSelector<DocumentAct>("button.upload");
        selector.getSelect().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelect();
            }
        });
        updateDisplay(act);
        refMgr = new DocReferenceMgr(act.getDocument());
    }

    /**
     * Determines if the associated act should be deleted when {@link #delete()} is invoked.
     * Defaults to <tt>false</tt>.
     *
     * @param delete if <tt>true</tt> delete the act
     */
    public void setDeleteAct(boolean delete) {
        this.deleteAct = delete;
    }

    /**
     * Determines if the object has been changed.
     *
     * @return <tt>true</tt> if the object has been changed
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
     * @param description the description of the document act. May be <tt>null</tt>
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
     *
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    protected boolean saveChildren() {
        boolean saved = super.saveChildren();
        if (saved && (docModified || act.isNew())) {
            if (!act.isNew()) {
                // need to reload the act as the participation has already
                // been saved by the parent Entity. Failing to do so will
                // result in hibernate StaleObjectExceptions
                IMObjectReference ref = act.getDocument();
                String fileName = act.getFileName();
                String mimeType = act.getMimeType();
                String description = act.getDescription();
                act = IMObjectHelper.reload(act);
                if (act == null) {
                    saved = false;
                } else {
                    act.setDocument(ref);
                    act.setFileName(fileName);
                    act.setMimeType(mimeType);
                    act.setDescription(description);
                }
            }
            if (saved) {
                saved = SaveHelper.save(act);
                if (saved) {
                    refMgr.commit();
                }
            }
        }
        return saved;
    }

    /**
     * Deletes the object.
     *
     * @return <tt>true</tt> if the delete was successful
     */
    @Override
    protected boolean doDelete() {
        boolean result;
        if (deleteAct) {
            result = super.deleteChildren();
            if (result) {
                result = SaveHelper.delete(act);
            }
        } else {
            result = super.doDelete();
        }
        return result;
    }

    /**
     * Deletes any child Deletable instances.
     *
     * @return <tt>true</tt> if the delete was successful
     */
    @Override
    protected boolean deleteChildren() {
        boolean result = super.deleteChildren();
        if (result) {
            try {
                refMgr.delete();
                result = true;
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
        return result;
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

            public ComponentState apply(IMObject object, PropertySet properties,
                                        IMObject parent,
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
        UploadListener listener = new DocumentUploadListener() {
            protected void upload(Document doc) {
                onUpload(doc);
            }
        };
        UploadDialog dialog = new UploadDialog(listener);
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
        act.setFileName(document.getName());
        service.deriveValue(act, "name");
        act.setMimeType(document.getMimeType());
        if (getParent() == null) {
            act.setDescription(document.getDescription());
        } else {
            act.setDescription(getParent().getName());
        }
        replaceDocReference(document);
        updateDisplay(act);
        docModified = true;
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
