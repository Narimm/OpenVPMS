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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.help.HelpContext;

import static org.openvpms.web.component.im.doc.DocumentActLayoutStrategy.DOCUMENT;
import static org.openvpms.web.component.im.doc.DocumentActLayoutStrategy.VERSIONS;


/**
 * Editor for {@link DocumentAct}s.
 *
 * @author Tim Anderson
 */
public class DocumentActEditor extends AbstractActEditor {

    /**
     * The last document template.
     */
    private IMObjectReference lastTemplate;

    /**
     * The document editor. May be {@code null}.
     */
    private DocumentEditor docEditor;

    /**
     * The document versions editor. May be {@code null}.
     */
    private ActRelationshipCollectionEditor versionsEditor;

    /**
     * The document template node.
     */
    private static final String DOC_TEMPLATE = "documentTemplate";


    /**
     * Constructs a {@link DocumentActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context. May be {@code null}.
     */
    public DocumentActEditor(DocumentAct act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        Property document = getProperty(DOCUMENT);
        if (document != null) {
            docEditor = new VersioningDocumentEditor(document, context.getContext(),
                                                     context.getHelpContext().topic("document"));
            ModifiableListener listener = new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onDocumentUpdate();
                }
            };
            docEditor.addModifiableListener(listener);
            getEditors().add(docEditor);
        }
        Property versions = getProperty(VERSIONS);
        if (versions != null) {
            versionsEditor = new ActRelationshipCollectionEditor((CollectionProperty) versions, act, context);
            getEditors().add(versionsEditor);
        }
        lastTemplate = getTemplateRef();
    }

    /**
     * Sets the document template, an instance of <em>entity.documentTemplate<em>.
     *
     * @param template the template. May be {@code null}
     */
    public void setTemplate(Entity template) {
        setParticipant(DOC_TEMPLATE, template);
    }

    /**
     * Returns the document template, an instance of <em>entity.documentTemplate</em>.
     *
     * @return the document template. May be {@code null}
     */
    public Entity getTemplate() {
        return (Entity) getParticipant(DOC_TEMPLATE);
    }

    /**
     * Sets a document template via its reference.
     *
     * @param template the template reference. May be {@code null}
     */
    public void setTemplateRef(IMObjectReference template) {
        setParticipant(DOC_TEMPLATE, template);
    }

    /**
     * Returns a reference to the current template, an instance of <em>entity.documentTemplate</em>.
     *
     * @return a reference to the current template. May be {@code null}
     */
    public IMObjectReference getTemplateRef() {
        return getParticipantRef(DOC_TEMPLATE);
    }

    /**
     * Sets the document.
     *
     * @param document the document. May be {@code null}
     * @throws IllegalStateException if the archetype doesn't support documents
     */
    public void setDocument(Document document) {
        if (docEditor == null) {
            throw new IllegalStateException("Documents are not supported by: " + getDisplayName());
        }
        docEditor.setDocument(document);
    }

    /**
     * Returns the document.
     *
     * @return the document. May be {@code null}
     * @throws IllegalStateException if the archetype doesn't support documents
     */
    public Document getDocument() {
        return (Document) getObject(getDocumentRef());
    }

    /**
     * Returns the document reference.
     *
     * @return the document reference. May be {@code null}
     * @throws IllegalStateException if the archetype doesn't support documents
     */
    public IMObjectReference getDocumentRef() {
        if (docEditor == null) {
            throw new IllegalStateException("Documents are not supported by: " + getDisplayName());
        }
        return docEditor.getReference();
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        DocumentTemplateParticipationEditor editor = getDocumentTemplateEditor();
        if (editor != null) {
            editor.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onTemplateUpdate();
                }
            });
        }
    }

    /**
     * Save any edits.
     *
     * @return {@code true} if the save was successful
     */
    @Override
    protected boolean doSave() {
        boolean saved = saveObject();
        if (saved) {
            saved = saveChildren();
        }
        return saved;
    }

    /**
     * Deletes the object.
     *
     * @return {@code true} if the delete was successful
     */
    @Override
    protected boolean doDelete() {
        boolean deleted = deleteObject();
        if (deleted) {
            deleted = deleteChildren();
        }
        if (deleted && versionsEditor != null) {
            // delete the prior versions. Need to jump through some hoops to do this to avoid stale object errors
            // TODO - ideally this would be done from within a delete rule
            for (Act act : versionsEditor.getActs()) {
                if (!act.isNew()) {
                    act = IMObjectHelper.reload(act);
                    if (act != null) {
                        DefaultLayoutContext context = new DefaultLayoutContext(getLayoutContext());
                        IMObjectEditor editor = versionsEditor.createEditor(act, context);
                        deleted = editor.delete();
                        if (!deleted) {
                            break;
                        }
                    }
                }
            }
        }
        return deleted;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new DocumentActLayoutStrategy(docEditor, versionsEditor);
    }

    /**
     * Returns the document editor.
     *
     * @return the document editor. May be {@code null}
     */
    protected DocumentEditor getDocumentEditor() {
        return docEditor;
    }

    /**
     * Returns the document versions editor.
     *
     * @return the document versions editor. May be {@code null}
     */
    protected ActRelationshipCollectionEditor getVersionsEditor() {
        return versionsEditor;
    }

    /**
     * Returns the document template participation editor.
     *
     * @return document template participation editor. May be {@code null}
     */
    protected DocumentTemplateParticipationEditor getDocumentTemplateEditor() {
        ParticipationEditor editor = getParticipationEditor(DOC_TEMPLATE, true);
        return (editor instanceof DocumentTemplateParticipationEditor) ?
               (DocumentTemplateParticipationEditor) editor : null;
    }

    /**
     * Invoked when the document reference is updated.
     * </p>
     * Updates the fileName and mimeType properties.
     */
    private void onDocumentUpdate() {
        getProperty("fileName").setValue(docEditor.getName());
        getProperty("mimeType").setValue(docEditor.getMimeType());
    }

    /**
     * Invoked when the document template updates.
     * <p/>
     * If the template is different to the prior instance, and there is a document node, the template will be used
     * to generate a new document.
     */
    private void onTemplateUpdate() {
        IMObjectReference template = getTemplateRef();
        if ((template != null && lastTemplate != null && !template.equals(lastTemplate))
            || (template != null && lastTemplate == null)) {
            lastTemplate = template;
            if (docEditor != null) {
                generateDoc();
            }
        }
    }

    /**
     * Generates the document.
     * <p/>
     * If the act supports versioning, any existing saved document will be copied to new version act.
     */
    private void generateDoc() {
        DocumentAct act = (DocumentAct) getObject();
        Context context = getLayoutContext().getContext();
        HelpContext help = getLayoutContext().getHelpContext();
        DocumentGenerator.Listener listener = new DocumentGenerator.AbstractListener() {
            public void generated(Document document) {
                docEditor.setDocument(document);
            }
        };
        final DocumentGenerator generator = new DocumentGenerator(act, context, help, listener);
        generator.generate();
    }

    /**
     * Versions the existing document if necessary.
     *
     * @param reference the old document reference. May be {@code null}
     * @return {@code true} if it the document was versioned
     */
    private boolean versionOldDocument(IMObjectReference reference) {
        boolean versioned = false;
        if (reference != null && !reference.isNew() && versionsEditor != null) {
            DocumentRules rules = new DocumentRules();
            DocumentAct version = rules.createVersion((DocumentAct) getObject());
            if (version != null) {
                versionsEditor.add(version);
                versionsEditor.refresh();
                versioned = true;
            }
        }
        return versioned;
    }

    private class VersioningDocumentEditor extends DocumentEditor {

        /**
         * Constructs a {@code VersioningDocumentEditor}.
         *
         * @param property the property being edited
         * @param context  the context
         * @param help     the help
         */
        public VersioningDocumentEditor(Property property, Context context, HelpContext help) {
            super(property, context, help);
        }

        /**
         * Sets the document.
         *
         * @param document the new document
         * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
         *          for any error
         */
        @Override
        public void setDocument(Document document) {
            boolean versioned = versionOldDocument(getReference());
            super.setDocument(document, versioned);
        }
    }

}
