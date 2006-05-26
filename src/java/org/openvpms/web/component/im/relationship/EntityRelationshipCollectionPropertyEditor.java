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

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;

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
     * Determines if inactive relationships should be returned.
     */
    private boolean _exclude = true;


    /**
     * Construct a new <code>EntityRelationshipCollectionPropertyEditor</code>.
     *
     * @param property the collection property
     */
    public EntityRelationshipCollectionPropertyEditor(
            CollectionProperty property) {
        super(property);
    }

    /**
     * Indicates if inactive relationships should be returned.
     *
     * @param exclude if <code>true</code> exclude inactive relationships
     */
    public void setExcludeInactive(boolean exclude) {
        _exclude = exclude;
    }

    /**
     * Returns the objects in the collection.
     *
     * @return the objects in the collection
     */
    @Override
    public List<IMObject> getObjects() {
        List<IMObject> objects = super.getObjects();
        List<IMObject> result;
        if (_exclude) {
            result = RelationshipHelper.filterInactive(objects, new Date());
        } else {
            result = objects;
        }

        return result;
    }

}
