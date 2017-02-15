/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.view.IMObjectTableCollectionViewer;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.List;


/**
 * Viewer for collections of {@link IMObjectRelationship}s.
 * <p/>
 * If the relationships have a sequence node, they will be sorted on this.
 *
 * @author Tim Anderson
 */
public class IMObjectRelationshipCollectionViewer extends IMObjectTableCollectionViewer {

    /**
     * Determines if the collection should be sorted on sequence.
     */
    private boolean sequenced;

    /**
     * Constructs a {@link IMObjectRelationshipCollectionViewer}.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param layout   the layout context. May be {@code null}
     */
    public IMObjectRelationshipCollectionViewer(CollectionProperty property, IMObject parent, LayoutContext layout) {
        super(property, parent, layout);
        sequenced = SequencedRelationshipCollectionHelper.hasSequenceNode(property.getArchetypeRange());
    }

    /**
     * Determines if the collection should be sorted on sequence.
     *
     * @return {@code true} if the collection should be sorted on sequence
     */
    protected boolean isSequenced() {
        return sequenced;
    }

    /**
     * Browse an object.
     *
     * @param object the object to browse.
     */
    @Override
    protected void browse(IMObject object) {
        IMObjectRelationship relationship = (IMObjectRelationship) object;
        IMObject target = getLayoutContext().getCache().get(relationship.getTarget());
        if (target != null) {
            browseTarget(target);
        }
    }

    /**
     * Browse the target of a relationship.
     *
     * @param target the object to browse
     */
    protected void browseTarget(IMObject target) {
        super.browse(target);
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<IMObject> createResultSet() {
        ResultSet<IMObject> result;
        if (isSequenced()) {
            result = new IMObjectListResultSet<>(getSorted(), ROWS);
        } else {
            result = super.createResultSet();
        }
        return result;
    }

    /**
     * Returns the objects sorted on sequence.
     *
     * @return the objects sorted on sequence
     */
    protected List<IMObject> getSorted() {
        return SequencedRelationshipCollectionHelper.sort(getObjects());
    }
}
