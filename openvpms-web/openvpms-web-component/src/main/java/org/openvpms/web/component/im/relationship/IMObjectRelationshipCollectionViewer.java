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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectTableCollectionViewer;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Viewer for collections of {@link IMObjectRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class IMObjectRelationshipCollectionViewer extends IMObjectTableCollectionViewer {

    /**
     * Constructs a <tt>IMObjectRelationshipCollectionViewer</tt>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param layout   the layout context. May be <tt>null</tt>
     */
    public IMObjectRelationshipCollectionViewer(CollectionProperty property, IMObject parent, LayoutContext layout) {
        super(property, parent, layout);
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
}
