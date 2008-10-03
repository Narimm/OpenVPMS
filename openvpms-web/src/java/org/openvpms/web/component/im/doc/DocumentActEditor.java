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

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Editor for <em>act.patientDocument</em> acts.
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
     * The document template node.
     */
    private static final String DOC_TEMPLATE = "documentTemplate";

    /**
     * The document reference node.
     */
    private static final String DOC_REFERENCE = "docReference";

    /**
     * The document listener.
     */
    private ModifiableListener listener;


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
        Editor editor = getDocumentEditor();
        if (editor != null) {
            listener = new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onDocumentUpdate();
                }
            };
            editor.addModifiableListener(listener);
        }
        IMObjectCollectionEditor template
                = (IMObjectCollectionEditor) getEditor(DOC_TEMPLATE);
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
     * Invokes {@link #updateFileProperties} with the related document.
     */
    private void onDocumentUpdate() {
        DocumentAct act = (DocumentAct) getObject();
        IMObjectReference docRef = act.getDocument();
        Document document = (Document) IMObjectHelper.getObject(docRef);
        updateFileProperties(document);
    }

    /**
     * Invoked when the document template updates.
     */
    private void onTemplateUpdate() {
        IMObjectReference template = getTemplate();
        if ((template != null && lastTemplate != null
                && !template.equals(lastTemplate))
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
     * Updates the document reference.
     *
     * @param document the new document
     */
    private void updateDocument(Document document) {
        DocumentEditor editor = getDocumentEditor();
        editor.removeModifiableListener(listener);
        try {
            editor.setDocument(document);
            getProperty("fileName").refresh();
            getProperty("mimeType").refresh();
        } finally {
            editor.addModifiableListener(listener);
        }
    }

    /**
     * Duplicates the document's name and mime-type to the fileName and mimeType
     * properties respectively to the {@link DocumentAct}.
     *
     * @param document the document
     */
    private void updateFileProperties(Document document) {
        String name = null;
        String mimeType = null;
        if (document != null) {
            name = document.getName();
            mimeType = document.getMimeType();
        }
        getProperty("fileName").setValue(name);
        getProperty("mimeType").setValue(mimeType);
    }

    /**
     * Returns the document editor.
     *
     * @return the document editor or <tt>null</tt> if there is no
     *         <tt>docReference</tt> node
     */
    private DocumentEditor getDocumentEditor() {
        return (DocumentEditor) getEditor(DOC_REFERENCE);
    }

    /**
     * Helper to return a reference to the current template, an instance of
     * <em>entity.documentTemplate</em>.
     *
     * @return a reference to the current template. May be <tt>null</tt>
     */
    private IMObjectReference getTemplate() {
        CollectionProperty property
                = (CollectionProperty) getProperty(DOC_TEMPLATE);
        Collection values = property.getValues();
        if (!values.isEmpty()) {
            Participation p = (Participation) values.toArray()[0];
            return p.getEntity();
        }
        return null;
    }

    /**
     * Layout strategy that treats the 'docReference' node as a simple node.
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
        protected List<NodeDescriptor> getSimpleNodes(
                ArchetypeDescriptor archetype) {
            List<NodeDescriptor> nodes = new ArrayList<NodeDescriptor>();
            nodes.addAll(super.getSimpleNodes(archetype));
            boolean found = false;
            for (NodeDescriptor node : nodes) {
                if (node.getName().equals(DOC_REFERENCE)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                NodeDescriptor node = archetype.getNodeDescriptor(
                        DOC_REFERENCE);
                if (node != null) {
                    nodes.add(archetype.getNodeDescriptor(DOC_REFERENCE));
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
                          new NamedNodeFilter(DOC_REFERENCE));
        }

    }

}
