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

import nextapp.echo2.app.Component;
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
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.tabpane.TabPaneModel;

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
     * The tab model containing the customFields node.
     */
    private TabPaneModel tabModel;

    /**
     * The current custom field editor state.
     */
    private ComponentState customFieldState;

    /**
     * The index of the custom fields tab, or <tt>-1</tt> if it is not displayed.
     */
    private int customFieldsTab;

    /**
     * Determines if the custom fields node should be hidden.
     */
    private boolean hideCustomFields;


    /**
     * Constructs a <em>PatientLayoutStrategy</em> to view a patient.
     */
    public PatientLayoutStrategy() {
        this(null);
    }

    /**
     * Creates a new <em>PatientLayoutStrategy</em> to edit a patient.
     *
     * @param customFieldEditor the customField node editor
     */
    public PatientLayoutStrategy(RelationshipCollectionTargetEditor customFieldEditor) {
        super(true);
        this.customFieldEditor = customFieldEditor;
    }

    /**
     * Removes the custom fields tab.
     */
    public void removeCustomFields() {
        if (tabModel != null && customFieldsTab != -1) {
            tabModel.removeTabAt(customFieldsTab);
            customFieldsTab = -1;
        }
        if (customFieldState != null) {
            getFocusGroup().remove(customFieldState.getFocusGroup());
        }
        hideCustomFields = true;
    }

    /**
     * Adds the custom fields tab.
     */
    public void addCustomFields() {
        hideCustomFields = false;
        if (customFieldState != null) {
            // remove existing focus group
            getFocusGroup().remove(customFieldState.getFocusGroup());
        }
        addTab(tabModel, customFieldEditor.getProperty(), createCustomEditorComponent(), true);
    }

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
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        customFieldsTab = -1;
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns a node filter to filter nodes.
     *
     * @param object  the object to filter nodes for
     * @param context the context
     * @return a node filter to filter nodes, or <tt>null</tt> if no filterering is required
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        NodeFilter filter = super.getNodeFilter(object, context);
        if (hideCustomFields || !hasCustomFields(object)) {
            filter = FilterHelper.chain(new NamedNodeFilter("customFields"), filter);
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
    protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
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
     * Creates a new tab model.
     *
     * @param container the tab container. May be <tt>null</tt>
     * @return a new tab model
     */
    @Override
    protected TabPaneModel createTabModel(Component container) {
        tabModel = super.createTabModel(container);
        return tabModel;
    }

    /**
     * Adds a tab to a tab model.
     *
     * @param model       the tab  model
     * @param property    property
     * @param component   the component to add
     * @param addShortcut if <tt>true</tt> add a tab shortcut
     */
    @Override
    protected void addTab(TabPaneModel model, Property property, ComponentState component, boolean addShortcut) {
        super.addTab(model, property, component, addShortcut);
        if ("customFields".equals(property.getName())) {
            customFieldsTab = model.size() - 1;
        }
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
    private ComponentState createCustomViewComponent(Property property, IMObject parent, LayoutContext context) {
        ComponentState result = super.createComponent(property, parent, context);
        CollectionProperty collection = (CollectionProperty) property;
        Collection values = collection.getValues();
        if (!values.isEmpty()) {
            EntityRelationship relationship = (EntityRelationship) values.toArray(new Object[values.size()])[0];
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
        customFieldState = new ComponentState(customFieldEditor.getComponent(), customFieldEditor.getProperty(),
                                              customFieldEditor.getFocusGroup(), displayName);
        return customFieldState;
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
        List<EntityRelationship> relationships = bean.getNodeRelationships("customFields");
        return !relationships.isEmpty();
    }

}
