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

package org.openvpms.web.app.patient;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.RelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;

import java.util.Collection;
import java.util.List;


/**
 * Layout strategy for <em>party.patientpet</em>.
 * <p/>
 * Renders the <em>customField</em> node inline if there is an
 * <em>entity.customPatient*</em> associated with it.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The customField node editor, if the object is being edited.
     */
    private final RelationshipCollectionTargetEditor customFieldEditor;


    /**
     * Creates a new <em>PatientLayoutStrategy</em> to view a patient.
     */
    public PatientLayoutStrategy() {
        this.customFieldEditor = null;
    }

    /**
     * Creates a new <em>PatientLayoutStrategy</em> to edit a patient.
     *
     * @param customFieldEditor the customField node editor
     */
    public PatientLayoutStrategy(
            RelationshipCollectionTargetEditor customFieldEditor) {
        this.customFieldEditor = customFieldEditor;
    }

    /**
     * Returns a node filter to filter nodes.
     *
     * @param object
     * @param context the context
     * @return a node filter to filter nodes, or <tt>null</tt> if no
     *         filterering is required
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object,
                                       LayoutContext context) {
        NodeFilter filter = super.getNodeFilter(object, context);
        if (!hasCustomFields(object)) {
            filter = FilterHelper.chain(new NamedNodeFilter("customFields"),
                                        filter);
        }
        return filter;
    }

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <tt>property</tt>
     */
    @Override
    protected ComponentState createComponent(Property property,
                                             IMObject parent,
                                             LayoutContext context) {
        ComponentState result;
        if ("customFields".equals(property.getName())) {
            if (customFieldEditor != null) {
                result = createCustomEditorComponent();
            } else {
                result = createCustomViewComponent(property, parent, context);
            }
        } else {
            result = super.createComponent(property, parent, context);
        }
        return result;
    }

    /**
     * Creates a component to view the custom fields node.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a new component
     */
    @SuppressWarnings("unchecked")
    private ComponentState createCustomViewComponent(Property property,
                                                     IMObject parent,
                                                     LayoutContext context) {
        ComponentState result = super.createComponent(property, parent,
                                                      context);
        CollectionProperty collection = (CollectionProperty) property;
        Collection values = collection.getValues();
        if (!values.isEmpty()) {
            EntityRelationship relationship
                    = (EntityRelationship) values.toArray(new Object[0])[0];
            IMObjectReference ref = relationship.getTarget();
            if (ref != null) {
                String displayName = DescriptorHelper.getDisplayName(
                        ref.getArchetypeId().getShortName());
                result.setDisplayName(displayName);
            }
        }
        return result;
    }

    /**
     * Creates a component to edit the custom fields node.
     *
     * @return a new component
     */
    private ComponentState createCustomEditorComponent() {
        String displayName = null;
        List<IMObject> objects = customFieldEditor.getObjects();
        Entity fields = !objects.isEmpty() ? (Entity) objects.get(0) : null;
        if (fields != null) {
            displayName = DescriptorHelper.getDisplayName(fields);
        }
        return new ComponentState(customFieldEditor.getComponent(),
                                  customFieldEditor.getProperty(),
                                  customFieldEditor.getFocusGroup(),
                                  displayName);
    }

    /**
     * Determines if the object has an <em>entity.customPatient*</em> associated
     * with it.
     *
     * @param object the object. Must be an {@link Entity}.
     * @return <tt>true</em> if there is any <em>entity.customPatient*</em>
     *         associated with the object
     */
    private boolean hasCustomFields(IMObject object) {
        EntityBean bean = new EntityBean((Entity) object);
        List<EntityRelationship> relationships = bean.getNodeRelationships(
                "customFields");
        return !relationships.isEmpty();
    }

}
