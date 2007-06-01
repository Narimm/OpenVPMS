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

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanException;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
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
     * <li>have an inactive entity not matching that supplied</li>
     * </ul>
     *
     * @param entity        the entity
     * @param relationships the list of relationships
     * @param date          the date to filter on
     * @return the filtered relationships
     */
    public static List<IMObject> filterInactive(Entity entity,
                                                List<IMObject> relationships,
                                                Date date) {
        return filter(relationships, new Filter(entity, date));
    }

    /**
     * Filters out inactive patient relationships.
     * Inactive relationships are those which are:
     * <ul>
     * <li>marked inactive</li>
     * <li>have an end date less than that specified.
     * <li>have an inactive party not matching that supplied</li>
     * <li>the patient is deceased and not that same party as that supplied</li>
     * </ul>
     *
     * @param party         the party
     * @param relationships the list of relationships
     * @param date          the date to filter on
     * @return the filter relationships
     */
    public static List<IMObject> filterPatients(Party party,
                                                List<IMObject> relationships,
                                                Date date) {
        return filter(relationships, new PatientFilter(party, date));
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

    /**
     * Returns the target from the default entity relationship from the
     * specified relationship node.
     *
     * @param entity the parent entity
     * @param node   the relationship node
     * @return the default target, or the the first non-null target if the
     *         default target is <tt>null</tt>, or <tt>null</tt> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the node does't exist or an element
     *                                   is of the wrong type
     */
    public static IMObject getDefaultTarget(Entity entity, String node) {
        IMObjectBean bean = new IMObjectBean(entity);
        return getDefaultTarget(bean.getValues(node, EntityRelationship.class));
    }

    /**
     * Returns the target from the default entity relationship from the supplied
     * relationship list.
     *
     * @param relationships a list of relationship objects
     * @return the default target, or the the first non-null target if the
     *         default target is <tt>null</tt>, or <tt>null</tt> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMObject getDefaultTarget(
            List<EntityRelationship> relationships) {
        IMObject result = null;
        for (EntityRelationship relationship : relationships) {
            if (result == null) {
                result = IMObjectHelper.getObject(relationship.getTarget());
            } else {
                IMObjectBean bean = new IMObjectBean(relationship);
                if (bean.hasNode("default") && bean.getBoolean("default")) {
                    result = IMObjectHelper.getObject(relationship.getTarget());
                    if (result != null) {
                        break;
                    }
                }

            }
        }
        return result;
    }

    private static class Filter {

        private final Entity entity;

        private final Date date;

        public Filter(Entity entity, Date date) {
            this.entity = entity;
            this.date = date;
        }

        public boolean include(EntityRelationship relationship) {
            boolean result = false;
            Date end = relationship.getActiveEndTime();
            if (end == null || end.getTime() >= date.getTime()) {
                if (include(relationship.getSource())
                        && include(relationship.getTarget())) {
                    result = true;
                }
            }
            return result;
        }

        protected boolean include(IMObjectReference reference) {
            boolean result = true;
            if (reference != null
                    && !entity.getObjectReference().equals(reference)) {
                Entity other = (Entity) IMObjectHelper.getObject(reference);
                if (other != null) {
                    result = include(other);
                }
            }
            return result;
        }

        protected boolean include(IMObject object) {
            return object.isActive();
        }
    }

    private static class PatientFilter extends Filter {

        public PatientFilter(Party party, Date date) {
            super(party, date);
        }

        @Override
        protected boolean include(IMObject object) {
            boolean result = super.include(object);
            if (result && TypeHelper.isA(object, "party.patientpet")) {
                IMObjectBean bean = new IMObjectBean(object);
                result = !bean.getBoolean("deceased");
            }
            return result;
        }

    }

}
