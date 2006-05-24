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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.spring.ServiceHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


/**
 * A {@link CollectionPropertyEditor} for collections of
 * {@link ActRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActRelationshipCollectionPropertyEditor
        extends AbstractCollectionPropertyEditor {

    /**
     * The parent act.
     */
    private final Act _act;

    /**
     * The set of acts being edited, and their associated relationships.
     */
    private Map<Act, ActRelationship> _acts;

    /**
     * The relationship short name.
     */
    private final String _relationshipType;

    /**
     * The set of removed objects.
     */
    private final Set<IMObject> _removed = new HashSet<IMObject>();


    /**
     * Construct a new <code>ActRelationshipCollectionPropertyEditor</code>.
     *
     * @param property the property to edit
     * @param act      the parent act
     */
    public ActRelationshipCollectionPropertyEditor(
            CollectionProperty property, Act act) {
        super(property);
        // @todo - no support for multiple relationship archetypes
        NodeDescriptor descriptor = property.getDescriptor();
        _act = act;
        _relationshipType = descriptor.getArchetypeRange()[0];
    }

    /**
     * Returns the relationship archetype short name.
     *
     * @return the relationship short name
     */
    public String getRelationshipShortName() {
        return _relationshipType;
    }

    /**
     * Returns the range of archetypes that the collection may contain.
     *
     * @return the range of archetypes
     */
    @Override
    public String[] getArchetypeRange() {
        ArchetypeDescriptor relationship
                = DescriptorHelper.getArchetypeDescriptor(_relationshipType);
        NodeDescriptor target = relationship.getNodeDescriptor("target");
        return target.getArchetypeRange();
    }

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     */
    @Override
    public boolean add(IMObject object) {
        boolean added = false;
        Act act = (Act) object;
        ActRelationship relationship = getActs().get(act);
        if (relationship == null) {
            try {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                relationship = (ActRelationship) service.create(
                        _relationshipType);
                relationship.setSource(_act.getObjectReference());
                relationship.setTarget(act.getObjectReference());

                getActs().put(act, relationship);
                getProperty().add(relationship);
                added = true;
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
        addEdited(object);
        return added;
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        Act act = (Act) object;
        ActRelationship relationship = getActs().remove(act);
        if (relationship != null) {
            getProperty().remove(relationship);
        }
        queueRemove(object);
    }

    /**
     * Returns the objects in the collection.
     *
     * @return the objects in the collection
     */
    @Override
    public List<IMObject> getObjects() {
        return new ArrayList<IMObject>(getActs().keySet());
    }

    /**
     * Saves the collection.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    protected boolean doSave() {
        if (super.doSave()) {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            IMObject[] removed = _removed.toArray(new IMObject[0]);
            for (IMObject object : removed) {
                if (SaveHelper.remove(object, service)) {
                    _removed.remove(object);
                    setSaved(true);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the child acts.
     *
     * @return the child acts
     */
    protected Map<Act, ActRelationship> getActs() {
        if (_acts == null) {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            List<IMObject> relationships = super.getObjects();
            _acts = new LinkedHashMap<Act, ActRelationship>();
            for (IMObject object : relationships) {
                ActRelationship relationship = (ActRelationship) object;
                Act item = (Act) ArchetypeQueryHelper.getByObjectReference(
                        service, relationship.getTarget());
                _acts.put(item, relationship);
            }
        }
        return _acts;
    }

    /**
      * Flags an object for removal when the collection is saved.
      *
      * @param object the object to remove
      */
     protected void queueRemove(IMObject object) {
        removeEdited(object);
         if (!object.isNew()) {
             _removed.add(object);
         }
     }

}
