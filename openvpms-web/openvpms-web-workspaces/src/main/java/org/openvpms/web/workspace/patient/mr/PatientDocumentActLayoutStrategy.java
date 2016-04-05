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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.doc.DocumentActLayoutStrategy;
import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;

/**
 * Layout strategy for <em>act.patientDocument*</em> archetypes.
 *
 * @author benjamincharlton
 * @author Tim Anderson
 */
public class PatientDocumentActLayoutStrategy extends DocumentActLayoutStrategy {

    /**
     * Determines if the record is locked.
     */
    private final boolean locked;

    /**
     * The nodes to display.
     */
    protected static ArchetypeNodes EDIT_NODES = new ArchetypeNodes(NODES).excludeIfEmpty("invoiceItem");

    /**
     * Constructs an {@link PatientDocumentActLayoutStrategy}.
     */
    public PatientDocumentActLayoutStrategy() {
        this(null, null, false);
    }

    /**
     * Constructs an {@link PatientDocumentActLayoutStrategy}.
     *
     * @param editor         the editor. May be {@code null}
     * @param versionsEditor the versions editor. May be {@code null}
     * @param locked         determines if the record is locked
     */
    public PatientDocumentActLayoutStrategy(DocumentEditor editor, ActRelationshipCollectionEditor versionsEditor,
                                            boolean locked) {
        super(editor, versionsEditor);
        this.locked = locked;
        setArchetypeNodes(EDIT_NODES);
    }

    /**
     * Determines if the act is locked.
     *
     * @return {@code true} if the act is locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display {@code property}
     */
    @Override
    protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
        if (context.isEdit() && locked && makeReadOnly(property)) {
            property = createReadOnly(property);
        }
        return super.createComponent(property, parent, context);
    }

    /**
     * Determines if a property should be made read-only when the act is locked.
     *
     * @param property the property
     * @return {@code true} if the property should be made read-only
     */
    protected boolean makeReadOnly(Property property) {
        return !property.isReadOnly();
    }


}
