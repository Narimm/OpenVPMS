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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.doc.DocumentActLayoutStrategy;
import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.history.PatientHistoryDatingPolicy;

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
     * The policy for determining if the {@code startTime} node can be edited.
     */
    private final PatientHistoryDatingPolicy policy;

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
        policy = ServiceHelper.getBean(PatientHistoryDatingPolicy.class);
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
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        Property startTime = properties.get("startTime");
        if (context.isEdit() && !startTime.isReadOnly() && !policy.canEditStartTime(((Act) object))) {
            IMObjectComponentFactory factory = context.getComponentFactory();
            addComponent(factory.create(createReadOnly(startTime), object));
        }
        return super.apply(object, properties, parent, context);
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
