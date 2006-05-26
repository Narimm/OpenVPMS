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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * {@link EntityRelationship} helper routines.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipHelper {

    /**
     * Filters out inactive relationships.
     * Inactive relationships are those which have an end date less than
     * that specified.
     *
     * @param relationships the list of relationships
     * @param date          the date to filter on
     * @return the relationships excluding
     */
    public static List<IMObject> filterInactive(List<IMObject> relationships,
                                                Date date) {
        List<IMObject> result = new ArrayList<IMObject>();
        for (IMObject object : relationships) {
            if (object instanceof EntityRelationship) {
                EntityRelationship relationship = (EntityRelationship) object;
                Date end = relationship.getActiveEndTime();
                if (end == null || end.compareTo(date) >= 0) {
                    result.add(relationship);
                }
            }
        }
        return result;
    }

}
