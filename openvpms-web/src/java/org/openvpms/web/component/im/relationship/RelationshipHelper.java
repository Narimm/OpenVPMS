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
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.util.IMObjectHelper;

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
     * Inactive relationships are those which are:
     * <ul>
     * <li>marked inactive</li>
     * <li>have an end date less than that specified.
     * <li>have an inactive target</li>
     * </ul>
     *
     * @param relationships the list of relationships
     * @param date          the date to filter on
     * @return the filtered relationships
     */
    public static List<IMObject> filterInactive(List<IMObject> relationships,
                                                Date date) {
        return filter(relationships, new Filter(date));
    }

    /**
     * Filters out patient relationships where the relationship or patient is
     * inactive or the patient is deceased.
     *
     * @param relationships the list of relationships
     * @param date          the date to filter on
     * @return the filter relationships
     */
    public static List<IMObject> filterPatients(List<IMObject> relationships,
                                                Date date) {
        return filter(relationships, new PatientFilter(date));
    }

    /**
     * Filter relationships using the supplied filter.
     *
     * @param relationships the relationships to filter
     * @param filter        the filter to use
     * @return the filtered relationships
     */
    private static List<IMObject> filter(List<IMObject> relationships,
                                         Filter filter) {
        List<IMObject> result = new ArrayList<IMObject>();
        for (IMObject object : relationships) {
            if (object instanceof EntityRelationship) {
                EntityRelationship relationship = (EntityRelationship) object;
                if (filter.include(relationship)) {
                    result.add(relationship);
                }
            }
        }
        return result;
    }

    private static class Filter {

        private final Date date;

        public Filter(Date date) {
            this.date = date;
        }

        public boolean include(EntityRelationship relationship) {
            boolean result = false;
            Date end = relationship.getActiveEndTime();
            if (end == null || end.getTime() >= date.getTime()) {
                IMObject target = IMObjectHelper.getObject(
                        relationship.getTarget());
                if (target == null) {
                    result = true;
                } else {
                    result = include(target);
                }
            }
            return result;
        }

        protected boolean include(IMObject target) {
            return target.isActive();
        }
    }

    private static class PatientFilter extends Filter {

        public PatientFilter(Date date) {
            super(date);
        }

        @Override
        protected boolean include(IMObject target) {
            if (super.include(target)) {
                IMObjectBean bean = new IMObjectBean(target);
                return !bean.getBoolean("deceased");
            }
            return false;
        }

    }

}
