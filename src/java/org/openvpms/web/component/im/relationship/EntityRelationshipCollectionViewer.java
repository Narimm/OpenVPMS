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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.List;


/**
 * Viewer for collections of {@link EntityRelationship EntityRelationships}.
 * <p/>
 * If the relationships have a <em>sequence</em> node, the collection will
 * be ordered on it.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipCollectionViewer
        extends RelationshipCollectionViewer {

    /**
     * Determines if the collection has a sequence node.
     * If so, the collection is automatically ordered on the sequence.
     */
    private boolean sequenced;

    /**
     * Constructs a new <tt>EntityRelationshipCollectionViewer</tt>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param context  the layout context. May be <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public EntityRelationshipCollectionViewer(CollectionProperty property,
                                              Entity parent,
                                              LayoutContext context) {
        super(property, parent, context);

        sequenced = EntityRelationshipCollectionHelper.hasSequenceNode(
                property.getArchetypeRange());
    }

    /**
     * Creates a new result set.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<RelationshipState> createResultSet() {
        if (sequenced) {
            List<RelationshipState> states = getRelationshipStates();
            EntityRelationshipCollectionHelper.sort(states);
            return new ListResultSet<RelationshipState>(states, ROWS);
        }
        return super.createResultSet();
    }
}

