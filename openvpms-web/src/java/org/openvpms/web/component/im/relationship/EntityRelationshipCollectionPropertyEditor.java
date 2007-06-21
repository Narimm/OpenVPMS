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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.Date;
import java.util.List;


/**
 * A {@link CollectionPropertyEditor} for collections of
 * {@link EntityRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipCollectionPropertyEditor
        extends AbstractCollectionPropertyEditor {

    /**
     * The parent object.
     */
    private final Entity object;

    /**
     * Determines if inactive relationships should be excluded.
     */
    private boolean exclude = true;


    /**
     * Construct a new <code>EntityRelationshipCollectionPropertyEditor</code>.
     *
     * @param property the collection property
     * @param object   the parent object
     */
    public EntityRelationshipCollectionPropertyEditor(
            CollectionProperty property, Entity object) {
        super(property);
        this.object = object;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object
     */
    public Entity getObject() {
        return object;
    }

    /**
     * Indicates if inactive relationships should be excluded.
     *
     * @param exclude if <code>true</code> exclude inactive relationships
     */
    public void setExcludeInactive(boolean exclude) {
        this.exclude = exclude;
    }

    /**
     * Determines if inactive relationships should be excluded.
     *
     * @return <code>true</code> if inactive relationships should be excluded
     */
    public boolean getExcludeInactive() {
        return exclude;
    }

    /**
     * Returns the objects in the collection.
     *
     * @return the objects in the collection
     */
    @Override
    public List<IMObject> getObjects() {
        return filter(super.getObjects());
    }

    /**
     * Filters objects.
     * This implementation filters inactive objects,
     * if {@link #getExcludeInactive()} is <code>true</code>.
     *
     * @param objects the objects to filter
     * @return the filtered objects
     */
    protected List<IMObject> filter(List<IMObject> objects) {
        List<IMObject> result;
        if (exclude) {
            result = RelationshipHelper.filterInactive(object, objects,
                                                       new Date());
        } else {
            result = objects;
        }

        return result;
    }

}
