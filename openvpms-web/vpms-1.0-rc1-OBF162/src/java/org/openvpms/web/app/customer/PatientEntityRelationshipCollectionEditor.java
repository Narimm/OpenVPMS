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

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipCollectionEditor;
import org.openvpms.web.component.im.relationship.EntityRelationshipCollectionPropertyEditor;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.Date;
import java.util.List;


/**
 * Editor for collections of <em>entityRelationship.patientOwner</em> and
 * <em>entityRelationship.patientLocation</em> relationships.
 * Hides any inactive/deceased patients  if the 'hide inactive' checkbox is
 * selected.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientEntityRelationshipCollectionEditor
        extends EntityRelationshipCollectionEditor {

    /**
     * Construct a new <code>EntityRelationshipCollectionEditor</code>.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public PatientEntityRelationshipCollectionEditor(
            CollectionProperty property, IMObject object,
            LayoutContext context) {
        super(new Editor(property, (Party) object), object, context);
    }

    /**
     * An {@link EntityRelationshipCollectionPropertyEditor} that excludes
     * inactive/deceased patients if the 'hide inactive' checkbox is selected.
     */
    private static class Editor
            extends EntityRelationshipCollectionPropertyEditor {

        /**
         * Construct a new <code>Editor</code>.
         *
         * @param property the collection property
         * @param party    the party
         */
        public Editor(CollectionProperty property, Party party) {
            super(property, party);
        }

        /**
         * Filters objects.
         * This implementation filters inactive/deceased patients if
         * {@link #getExcludeInactive()} is <code>true</code>.
         *
         * @param objects the objects to filter
         * @return the filtered objects
         */
        @Override
        protected List<IMObject> filter(List<IMObject> objects) {
            List<IMObject> result;
            if (getExcludeInactive()) {
                Party party = (Party) getObject();
                result = RelationshipHelper.filterPatients(party, objects,
                                                           new Date());
            } else {
                result = objects;
            }

            return result;
        }
    }

}
