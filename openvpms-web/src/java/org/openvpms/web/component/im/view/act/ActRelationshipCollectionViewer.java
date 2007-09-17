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

package org.openvpms.web.component.im.view.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectTableCollectionViewer;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Viewer for collections of {@link ActRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActRelationshipCollectionViewer
        extends IMObjectTableCollectionViewer {


    /**
     * Constructs a new <tt>ActRelationshipCollectionViewer</tt>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param context  the layout context. May be <tt>null</tt>
     */
    public ActRelationshipCollectionViewer(CollectionProperty property,
                                           IMObject parent,
                                           LayoutContext context) {
        super(property, parent, context);
    }

    /**
     * Browse an object.
     *
     * @param object the object to browse.
     */
    @Override
    protected void browse(IMObject object) {
        ActRelationship relationship = (ActRelationship) object;
        Act act = (Act) IMObjectHelper.getObject(relationship.getTarget());
        if (act != null) {
            browse(act);
        }
    }

    /**
     * Browse an act.
     *
     * @param act the act to browse
     */
    protected void browse(Act act) {
        super.browse(act);
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<IMObject> createResultSet() {
        Act parent = (Act) getObject();
        return new ActRelationshipResultSet(parent, getObjects(),
                                            getProperty().getArchetypeRange(),
                                            ROWS);
    }
}
