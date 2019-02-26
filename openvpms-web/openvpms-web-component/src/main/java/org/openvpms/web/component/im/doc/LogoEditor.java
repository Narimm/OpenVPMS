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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.ImageDocumentHandler;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.edit.Cancellable;
import org.openvpms.web.component.edit.Deletable;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.select.BasicSelector;
import org.openvpms.web.component.property.AbstractSaveableEditor;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * An editor for <em>participation.logo</em> participation relationships.
 *
 * @author Tim Anderson
 */
public class LogoEditor extends AbstractSaveableEditor implements Deletable, Cancellable {

    /**
     * The upload selector.
     */
    private final BasicSelector<DocumentAct> selector;

    /**
     * Manages old document references to avoid orphaned documents.
     */
    private final DocReferenceMgr refMgr;

    /**
     * The remove logo button.
     */
    private final Button remove;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The parent entity.
     */
    private final Entity parent;

    /**
     * The archetype service.
     */
    private IArchetypeRuleService service;

    /**
     * The document handler.
     */
    private ImageDocumentHandler handler;

    /**
     * The act to edit.
     */
    private DocumentAct act;

    /**
     * The component.
     */
    private Component component;

    /**
     * Constructs a {@link LogoEditor}.
     *
     * @param act     the logo act
     * @param parent  the parent entity
     * @param context the layout context
     */
    public LogoEditor(DocumentAct act, Entity parent, LayoutContext context) {
        this.act = act;
        service = ServiceHelper.getArchetypeService();
        if (act.isNew()) {
            IMObjectBean bean = service.getBean(act);
            bean.setTarget("owner", parent);
        }
        this.parent = parent;
        this.context = context;
        handler = ServiceHelper.getBean(ImageDocumentHandler.class);
        selector = new BasicSelector<>("button.upload");
        selector.getSelect().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelect();
            }
        });
        refMgr = new DocReferenceMgr(act.getDocument(), context.getContext());

        remove = ButtonFactory.create(null, "button.remove", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                removeDocument();
            }
        });
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    @Override
    public Component getComponent() {
        if (component == null) {
            updateDisplay();
            component = RowFactory.create(Styles.CELL_SPACING, selector.getComponent(), remove);
            FocusGroup focusGroup = getFocusGroup();
            focusGroup.add(selector.getComponent());
            focusGroup.add(remove);
        }
        return component;
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (component != null) {
            component.removeAll();
        }
    }

    /**
     * Perform deletion.
     *
     * @throws OpenVPMSException if the delete fails
     */
    @Override
    public void delete() {
        if (act != null && !act.isNew()) {
            service.remove(act);
            act = null;
        }
        refMgr.delete();
    }

    /**
     * Cancel any edits.
     */
    @Override
    public void cancel() {
        refMgr.rollback();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean result = true;
        List<ValidatorError> errors = ValidationHelper.validate(act, service);
        if (errors != null) {
            validator.add(this, errors);
            result = false;
        }
        return result;
    }

    /**
     * Save any edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        if (act != null) {
            if (act.getDocument() == null) {
                if (!act.isNew()) {
                    service.save(act);
                    refMgr.commit();
                    service.remove(act);
                    act = null;
                }
            } else {
                service.save(act);
                refMgr.commit();
                if (act.getDocument() == null) {
                    service.remove(act);
                    act = null;
                }
            }
        }
    }

    /**
     * Creates a new document act.
     *
     * @return a new document act
     */
    protected DocumentAct createAct() {
        DocumentAct act = (DocumentAct) service.create(DocumentArchetypes.LOGO_ACT);
        IMObjectBean bean = service.getBean(act);
        bean.setTarget("owner", parent);
        return act;
    }

    /**
     * Populates a document act with details from a document.
     *
     * @param act      the act to populate
     * @param document the document
     */
    protected void populateAct(DocumentAct act, Document document) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        act.setFileName(document.getName());
        service.deriveValue(act, "name");
        act.setMimeType(document.getMimeType());

        IMObjectBean bean = service.getBean(document);
        int width = bean.getInt("width");
        int height = bean.getInt("height");
        String description;
        if (width != -1 && height != -1) {
            description = Messages.format("admin.practice.logo.size", width, height);
        } else {
            description = Messages.get("admin.practice.logo.nosize");
        }
        act.setDescription(description);
    }

    /**
     * Displays the upload dialog to select a logo.
     */
    protected void onSelect() {
        UploadListener listener = new DocumentUploadListener(handler) {
            protected void upload(Document doc) {
                onUpload(doc);
            }
        };
        UploadDialog dialog = new UploadDialog(listener, context.getHelpContext());
        dialog.show();
    }

    /**
     * Invoked when a document is uploaded.
     *
     * @param document the uploaded document
     */
    protected void onUpload(Document document) {
        service.save(document);
        if (act == null) {
            act = createAct();
        }
        populateAct(act, document);
        replaceDocument(document);
        updateDisplay();
        setModified();
        notifyListeners();
    }

    /**
     * Removes the document.
     */
    protected void removeDocument() {
        replaceDocument(null);
        if (act != null) {
            act.setFileName(null);
            act.setName(null);
            act.setMimeType(null);
            act.setDescription(null);
        }
        updateDisplay();
        setModified();
        notifyListeners();
    }

    /**
     * Updates the display.
     */
    protected void updateDisplay() {
        selector.setObject(act);
        remove.setVisible(act != null && act.getDocument() != null);
    }

    /**
     * Replaces the existing document.
     * The existing document is queued for deletion.
     *
     * @param document the new document, or {@code null} if the document is being removed
     */
    private void replaceDocument(Document document) {
        if (document != null) {
            IMObjectReference ref = document.getObjectReference();
            act.setDocument(ref);
            refMgr.add(ref);
        } else {
            if (act != null) {
                act.setDocument(null);
            }
            refMgr.setDeleteAll(true);
        }
    }

}
