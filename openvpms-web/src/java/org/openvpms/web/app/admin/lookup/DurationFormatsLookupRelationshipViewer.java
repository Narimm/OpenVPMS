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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.admin.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.relationship.IMObjectRelationshipCollectionViewer;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.List;


/**
 * Viewer for collections of <em>lookupRelationship.durationformats<em>.
 * <p/>
 * This displays the target <em>lookup.durationformat</em> ordered on ascending interval.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DurationFormatsLookupRelationshipViewer extends IMObjectRelationshipCollectionViewer {

    /**
     * Constructs a <tt>DurationFormatsLookupRelationshipViewer</tt>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param layout   the layout context. May be <tt>null</tt>
     */
    public DurationFormatsLookupRelationshipViewer(CollectionProperty property, IMObject parent, LayoutContext layout) {
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
     * <p/>
     * Note that this implementation returns a set containing the target lookups rather than the relationships.
     *
     * @return a new result set
     */
    @Override
    @SuppressWarnings("unchecked")
    protected ResultSet<IMObject> createResultSet() {
        List relationships = getObjects();
        List<IMObject> objects = RelationshipHelper.getTargets((List<IMObjectRelationship>) relationships);
        return new DurationFormatResultSet(objects, ROWS);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IMTableModel<IMObject> createTableModel(final LayoutContext context) {
        return new DurationFormatLookupTableModel(context);
    }
}
