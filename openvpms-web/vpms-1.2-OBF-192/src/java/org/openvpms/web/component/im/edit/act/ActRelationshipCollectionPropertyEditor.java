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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
    private final Act parent;

    /**
     * The set of acts being edited, and their associated relationships.
     */
    private Map<Act, ActRelationship> acts;

    /**
     * The relationship short name.
     */
    private final String relationshipType;

    /**
     * The set of removed objects.
     */
    private final Set<IMObject> removed = new HashSet<IMObject>();

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(ActRelationshipCollectionPropertyEditor.class);


    /**
     * Construct a new <tt>ActRelationshipCollectionPropertyEditor</tt>.
     *
     * @param property the property to edit
     * @param act      the parent act
     */
    public ActRelationshipCollectionPropertyEditor(
            CollectionProperty property, Act act) {
        super(property);
        // @todo - no support for multiple relationship archetypes
        relationshipType = property.getArchetypeRange()[0];
        parent = act;
    }

    /**
     * Returns the relationship archetype short name.
     *
     * @return the relationship short name
     */
    public String getRelationshipShortName() {
        return relationshipType;
    }

    /**
     * Returns the range of archetypes that the collection may contain.
     * Any wildcards are expanded.
     *
     * @return the range of archetypes
     */
    @Override
    public String[] getArchetypeRange() {
        ArchetypeDescriptor relationship
                = DescriptorHelper.getArchetypeDescriptor(relationshipType);
        NodeDescriptor target = relationship.getNodeDescriptor("target");
        return DescriptorHelper.getShortNames(target);
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
                ActBean bean = new ActBean(parent);
                relationship = bean.addRelationship(relationshipType, act);
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
    public boolean remove(IMObject object) {
        Act act = (Act) object;
        boolean removed = queueRemove(object);
        ActRelationship relationship = getActs().remove(act);
        if (relationship != null) {
            parent.removeActRelationship(relationship);

            // will generate events, so invoke last
            getProperty().remove(relationship);
        }
        return removed;
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
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    protected boolean doSave() {
        boolean saved = true;
        if (!removed.isEmpty()) {
            IMObject[] toRemove = removed.toArray(new IMObject[0]);
            boolean deleted;
            for (IMObject object : toRemove) {
                IMObjectEditor editor = getEditor(object);
                if (editor != null) {
                    deleted = editor.delete();
                    if (deleted) {
                        setEditor(object, null);
                    }
                } else {
                    deleted = SaveHelper.delete(object);
                }
                if (deleted) {
                    removed.remove(object);
                    setSaved(true);
                } else {
                    saved = false;
                    break;
                }
            }
        }
        if (saved) {
            saved = super.doSave();
        }
        return saved;
    }

    /**
     * Returns the child acts.
     *
     * @return the child acts
     */
    protected Map<Act, ActRelationship> getActs() {
        if (acts == null) {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            List<IMObject> relationships = super.getObjects();
            acts = new LinkedHashMap<Act, ActRelationship>();
            for (IMObject object : relationships) {
                ActRelationship relationship = (ActRelationship) object;
                Act item = (Act) service.get(relationship.getTarget());
                if (item != null) {
                    acts.put(item, relationship);
                } else {
                    log.warn("Target act=" + relationship.getTarget()
                            + " no longer exists. Referred to by relationship="
                            + relationship);
                    getProperty().remove(relationship);
                }
            }
        }
        return acts;
    }

    /**
     * Flags an object for removal when the collection is saved.
     *
     * @param object the object to remove
     * @return <tt>true</tt> if the object was removed
     */
    protected boolean queueRemove(IMObject object) {
        if (!object.isNew()) {
            removed.add(object);
        }
        return removeEdited(object);
    }

}
