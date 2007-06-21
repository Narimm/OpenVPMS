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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.property.PropertySet;


/**
 * Layout strategy for {@link EntityRelationship}s.
 * This returns a viewer for the "non-current" entity in a relationship.
 * This returns the "non-current" or target side of the relationship.
 * "Non-current" refers the object that is NOT currently being
 * viewed/edited. If the source and target entities don't refer to the
 * current object being viewed/edited, then the target entity of the
 * relationship is used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipLayoutStrategy
        implements IMObjectLayoutStrategy {

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <code>null</code>
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        EntityRelationship relationship = (EntityRelationship) object;
        IMObjectReference entity = getEntity(relationship, context);
        boolean hyperlink = !context.isEdit();
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(entity,
                                                                     hyperlink);
        return new ComponentState(viewer.getComponent());
    }

    /**
     * Helper to returns a reference to the entity in a relationship. This
     * returns the "non-current" or target side of the relationship.
     * "Non-current" refers the object that is NOT currently being
     * viewed/edited. If the source and target entities don't refer to the
     * current object being viewed/edited, then the target entity of the
     * relationship is used.
     *
     * @param relationship the relationship
     * @return the "non-current" entity of the relationship.
     *         May be <tt>null</tt>
     */
    public static IMObjectReference getEntity(EntityRelationship relationship,
                                              LayoutContext context) {
        IMObjectReference entity;
        IMObject current = context.getContext().getCurrent();
        if (current == null) {
            entity = relationship.getTarget();
        } else {
            IMObjectReference ref = current.getObjectReference();

            if (relationship.getSource() != null
                    && ref.equals(relationship.getSource())) {
                entity = relationship.getTarget();
            } else if (relationship.getTarget() != null
                    && ref.equals(relationship.getTarget())) {
                entity = relationship.getSource();
            } else {
                entity = relationship.getTarget();
            }
        }
        return entity;
    }

}
