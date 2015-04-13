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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.doc;

import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;


/**
 * Layout strategy for document acts.
 * <p/>
 * This implementation displays any 'document' node as a simple node.
 *
 * @author Tim Anderson
 */
public class DocumentActLayoutStrategy extends ActLayoutStrategy {

    /**
     * The document node.
     */
    public static final String DOCUMENT = "document";

    /**
     * The versions node name.
     */
    public static final String VERSIONS = "versions";

    /**
     * Treats the document node as a simple rather than complex node.
     */
    protected static ArchetypeNodes NODES = new ArchetypeNodes().simple(DOCUMENT);

    /**
     * Nodes to display when viewing document acts. This suppresses the versions node if its empty
     */
    protected static ArchetypeNodes VIEW_NODES = new ArchetypeNodes(NODES).excludeIfEmpty(VERSIONS);

    /**
     * Constructs a {@code DocumentActLayoutStrategy} for viewing document acts.
     */
    public DocumentActLayoutStrategy() {
        this(null, null);
    }

    /**
     * Constructs a {@code DocumentActEditLayoutStrategy} for editing document acts.
     *
     * @param editor         the document reference editor. May be {@code null}
     * @param versionsEditor the document version editor. May be {@code null}
     */
    public DocumentActLayoutStrategy(DocumentEditor editor, ActRelationshipCollectionEditor versionsEditor) {
        if (editor != null) {
            addComponent(new ComponentState(editor));
        }
        if (versionsEditor != null) {
            addComponent(new ComponentState(versionsEditor));
        }
    }

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return NODES;
    }
}
