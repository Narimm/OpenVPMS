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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipCollectionPropertyEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionPropertyEditor;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.relationship.RelationshipStateQuery;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Editor for collections of <em>entityRelationship.patientOwner</em> and
 * <em>entityRelationship.patientLocation</em> relationships.
 * Hides any inactive/deceased patients if the 'hide inactive' checkbox is
 * selected.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientEntityRelationshipCollectionEditor
        extends RelationshipCollectionEditor {

    /**
     * Creates a new <tt>EntityRelationshipCollectionEditor</tt>.
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
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<RelationshipState> createTableModel(
            LayoutContext context) {
        RelationshipCollectionPropertyEditor editor
                = getCollectionPropertyEditor();
        return new PatientRelationshipStateTableModel(context,
                                                      editor.parentIsSource());
    }

    /**
     * An {@link RelationshipCollectionPropertyEditor} that excludes
     * inactive/deceased patients if the 'hide inactive' checkbox is selected.
     */
    private static class Editor
            extends EntityRelationshipCollectionPropertyEditor {

        /**
         * Construct a new <tt>Editor</tt>.
         *
         * @param property the collection property
         * @param party    the party
         */
        public Editor(CollectionProperty property, Party party) {
            super(property, party);
        }

        /**
         * Creates a new relationship state query.
         *
         * @param parent the parent entity
         * @return a new query
         */
        @Override
        protected RelationshipStateQuery createQuery(IMObject parent) {
            return new PatientRelationshipStateQuery(
                    (Entity) parent, getObjects(),
                    getProperty().getArchetypeRange());
        }
    }

}
