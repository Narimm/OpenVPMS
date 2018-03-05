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
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.List;


/**
 * Viewer for collections of {@link IMObjectRelationship}s where the targets of the relationships is displayed
 * in the table.
 * <p>
 * If the relationships have a sequence node, they will be sorted on this.
 *
 * @author Tim Anderson
 */
public class IMObjectRelationshipCollectionTargetViewer extends IMObjectRelationshipCollectionViewer {

    /**
     * Constructs a {@link IMObjectRelationshipCollectionTargetViewer}.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param layout   the layout context. May be {@code null}
     */
    public IMObjectRelationshipCollectionTargetViewer(CollectionProperty property, IMObject parent,
                                                      LayoutContext layout) {
        super(property, parent, layout);
    }

    /**
     * Browse an object.
     *
     * @param object the object to browse.
     */
    @Override
    protected void browse(IMObject object) {
        browseTarget(object);
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    @SuppressWarnings("unchecked")
    protected ResultSet<IMObject> createResultSet() {
        List relationships = (isSequenced()) ? getSorted() : getObjects();
        List<IMObject> objects = RelationshipHelper.getTargets((List<IMObjectRelationship>) relationships);
        return new IMObjectListResultSet<>(objects, ROWS);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        String[] shortNames = RelationshipHelper.getTargetShortNames(getProperty().getArchetypeRange());
        return IMObjectTableModelFactory.create(shortNames, getObject(), context);
    }
}
