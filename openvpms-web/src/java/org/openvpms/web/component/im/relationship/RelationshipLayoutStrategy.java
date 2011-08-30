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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.LabelFactory;


/**
 * Layout strategy for {@link IMObjectRelationship}s.
 * This returns a viewer for the "non-current" object in a relationship.
 * This returns the "non-current" or target side of the relationship.
 * "Non-current" refers the object that is NOT currently being
 * viewed/edited. If the source and target object don't refer to the
 * current object being viewed/edited, then the target object of the
 * relationship is used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * If <tt>true</tt>, displays the "non-current" object in the relationship,
     * otherwise display its name, with an optional hyperlink.
     */
    private final boolean displayInine;


    /**
     * Creates a new <tt>RelationshipLayoutStrategy</tt>.
     * <p/>
     * Displays the name of the "non-current" object in the relationship.
     */
    public RelationshipLayoutStrategy() {
        this(false);
    }

    /**
     * Creates a new <tt>RelationshipLayoutStrategy</tt>.
     *
     * @param displayInline if <tt>true</tt>, displays the "non-current" object
     *                      in the relationship, otherwise display its name,
     *                      with an optional hyperlink
     */
    public RelationshipLayoutStrategy(boolean displayInline) {
        this.displayInine = displayInline;
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <tt>Component</tt>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null</tt>
     * @param context    the layout context
     * @return the component containing the rendered <tt>object</tt>
     */
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        ComponentState result;
        IMObjectRelationship relationship = (IMObjectRelationship) object;
        IMObjectReference ref = getObject(relationship, parent);
        if (!displayInine) {
            IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(ref, context.getContextSwitchListener());
            result = new ComponentState(viewer.getComponent());
        } else {
            IMObject entity = context.getCache().get(ref);
            if (entity != null) {
                IMObjectLayoutStrategyFactory factory = context.getLayoutStrategyFactory();
                IMObjectLayoutStrategy strategy = factory.create(entity, object);
                result = strategy.apply(entity, new PropertySet(entity, context), object, context);
            } else {
                result = new ComponentState(LabelFactory.create());
            }
        }
        return result;
    }

    /**
     * Returns a reference to the object in a relationship. This returns either:
     * <ul>
     * <li>the object that isn't the same as parent, if parent is supplied; or
     * <li>the target of the relationship
     * </ul>
     *
     * @param relationship the relationship
     * @param parent       the parent object. May be <tt>null</tt>
     * @return the object. May be <tt>null</tt>
     */
    protected IMObjectReference getObject(IMObjectRelationship relationship, IMObject parent) {
        IMObjectReference result;
        if (parent == null) {
            result = relationship.getTarget();
        } else {
            IMObjectReference ref = parent.getObjectReference();

            if (relationship.getSource() != null && ref.equals(relationship.getSource())) {
                result = relationship.getTarget();
            } else if (relationship.getTarget() != null && ref.equals(relationship.getTarget())) {
                result = relationship.getSource();
            } else {
                result = relationship.getTarget();
            }
        }
        return result;
    }

}
