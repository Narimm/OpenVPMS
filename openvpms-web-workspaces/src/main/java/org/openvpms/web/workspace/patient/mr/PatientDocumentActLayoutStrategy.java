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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.web.component.im.doc.DocumentActLayoutStrategy;
import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;

/**
 * Layout strategy for <em>act.patientDocument*</em> archetypes.
 *
 * @author benjamincharlton
 */
public class PatientDocumentActLayoutStrategy extends DocumentActLayoutStrategy {

    /**
     * The nodes to display.
     */
    protected static ArchetypeNodes EDIT_NODES = new ArchetypeNodes(NODES).excludeIfEmpty("invoiceItem");

    /**
     * Constructs an {@link PatientDocumentActLayoutStrategy}.
     */
    public PatientDocumentActLayoutStrategy() {
        this(null, null);
    }

    /**
     * Constructs an {@link PatientDocumentActLayoutStrategy}.
     *
     * @param editor         the editor. May be {@code null}
     * @param versionsEditor the versions editor. May be {@code null}
     */
    public PatientDocumentActLayoutStrategy(DocumentEditor editor, ActRelationshipCollectionEditor versionsEditor) {
        super(editor, versionsEditor);
    }

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return EDIT_NODES;
    }
}
