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

import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Editor for {@link DocumentAct}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActEditor extends AbstractActEditor {

    /**
     * The last document template.
     */
    private IMObjectReference lastTemplate;

    /**
     * The document editor. May be <tt>null</tt>.
     */
    private DocumentEditor docEditor;

    /**
     * The document versions editor. May be <tt>null</tt>.
     */
    private ActRelationshipCollectionEditor versionsEditor;

    /**
     * The document template node.
     */
    private static final String DOC_TEMPLATE = "documentTemplate";

    /**
     * The document node.
     */
    private static final String DOCUMENT = "document";

    /**
     * The legacy document reference node name.
     */
    private static final String DOC_REFERENCE = "docReference";

    /**
     * The versions node.
     */
    private static final String VERSIONS = "versions";


    /**
     * Construct a new <tt>DocumentActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>.
     */
    public DocumentActEditor(DocumentAct act, IMObject parent,
                             LayoutContext context) {
        super(act, parent, context);
        Property document = getProperty(DOCUMENT);
        if (document == null) {
            document = getProperty(DOC_REFERENCE);
        }
        if (document != null) {
            docEditor = new VersioningDocumentEditor(document);
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
        IMObjectCollectionEditor template = (IMObjectCollectionEditor) getEditor(DOC_TEMPLATE);
        if (template != null) {
            lastTemplate = getTemplate();
            template.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onTemplateUpdate();
                }
            });
        }
    }

    /**
     * Save any edits.
     *
     * @return <tt>true</tt> if the save was successful
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
     * @return <tt>true</tt> if the delete was successful
     */
    @Override
    protected boolean doDelete() {
        boolean deleted = deleteObject();
        if (deleted) {
            deleted = deleteChildren();
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
        return new LayoutStrategy();
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
     */
    private void onTemplateUpdate() {
        IMObjectReference template = getTemplate();
        if ((template != null && lastTemplate != null && !template.equals(lastTemplate))
            || (template != null && lastTemplate == null)) {
            lastTemplate = template;
            generateDoc(template);
        }
    }

    /**
     * Generates the document.
     *
     * @param template the document template
     */
    private void generateDoc(IMObjectReference template) {
        DocumentAct act = (DocumentAct) getObject();
        final DocumentGenerator generator = new DocumentGenerator(
                act, template, new DocumentGenerator.Listener() {
                    public void generated(Document document) {
                        updateDocument(document);
                    }
                });
        generator.generate(false);
    }

    /**
     * Updates the document.
     * <p/>
     * If the act supports versioning, any existing saved document will be copied to new version act.
     *
     * @param document the new document
     */
    private void updateDocument(Document document) {
        if (docEditor != null) {
            docEditor.setDocument(document);
        }
    }

    /**
     * Versions the existing document if necessary.
     *
     * @param reference the old document reference. May be <tt>null</tt>
     * @return <tt>true</tt> if it the document was versioned
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


    /**
     * Helper to return a reference to the current template, an instance of
     * <em>entity.documentTemplate</em>.
     *
     * @return a reference to the current template. May be <tt>null</tt>
     */
    private IMObjectReference getTemplate() {
        CollectionProperty property = (CollectionProperty) getProperty(DOC_TEMPLATE);
        Collection values = property.getValues();
        if (!values.isEmpty()) {
            Participation p = (Participation) values.toArray()[0];
            return p.getEntity();
        }
        return null;
    }

    /**
     * Layout strategy that treats the 'document' node as a simple node.
     */
    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Returns the 'simple' nodes.
         *
         * @param archetype the archetype
         * @return the simple nodes
         * @see ArchetypeDescriptor#getSimpleNodeDescriptors()
         */
        @Override
        protected List<NodeDescriptor> getSimpleNodes(ArchetypeDescriptor archetype) {
            List<NodeDescriptor> nodes = new ArrayList<NodeDescriptor>();
            nodes.addAll(super.getSimpleNodes(archetype));
            boolean found = false;
            for (NodeDescriptor node : nodes) {
                String name = node.getName();
                if (DOCUMENT.equals(name) || DOC_REFERENCE.equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                NodeDescriptor node = archetype.getNodeDescriptor(DOCUMENT);
                if (node != null) {
                    nodes.add(node);
                } else {
                    node = archetype.getNodeDescriptor(DOC_REFERENCE);
                    if (node != null) {
                        nodes.add(node);
                    }
                }
            }
            return nodes;
        }

        /**
         * Returns the 'complex' nodes.
         *
         * @param archetype the archetype
         * @return the complex nodes
         * @see ArchetypeDescriptor#getComplexNodeDescriptors()
         */
        @Override
        protected List<NodeDescriptor> getComplexNodes(
                ArchetypeDescriptor archetype) {
            return filter(getObject(), super.getComplexNodes(archetype),
                          new NamedNodeFilter(DOCUMENT, DOC_REFERENCE));
        }

        /**
         * Creates a component for a property.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display <code>property</code>
         */
        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
            String name = property.getName();
            if (DOCUMENT.equals(name) || DOC_REFERENCE.equals(name)) {
                return new ComponentState(docEditor.getComponent(), docEditor.getProperty());
            } else if (VERSIONS.equals(name)) {
                return new ComponentState(versionsEditor.getComponent(), versionsEditor.getProperty());
            }
            return super.createComponent(property, parent, context);
        }
    }

    private class VersioningDocumentEditor extends DocumentEditor {

        /**
         * Creates a new <tt>VersioningDocumentEditor</tt>.
         *
         * @param property the property being edited
         * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
         *          for any archetype service error
         */
        public VersioningDocumentEditor(Property property) {
            super(property);
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
