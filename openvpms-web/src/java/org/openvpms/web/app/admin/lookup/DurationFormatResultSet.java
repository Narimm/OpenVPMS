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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractListResultSet;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Result set for <em>lookup.durationformat</em> lookups.
 * <p/>
 * This sorts the set on increasing interval.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DurationFormatResultSet extends AbstractListResultSet<IMObject> {

    /**
     * Constructs a <tt>DurationFormatResultSet</tt>.
     *
     * @param objects  the objects
     * @param pageSize the maximum no. of results per page
     */
    public DurationFormatResultSet(List<IMObject> objects, int pageSize) {
        super(objects, pageSize);
        sort();
    }

    /**
     * Sorts the set.
     * <p/>
     * This implementation is a no-op.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    public void sort(SortConstraint[] sort) {
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <tt>true</tt>
     */
    public boolean isSortedAscending() {
        return true;
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    public SortConstraint[] getSortConstraints() {
        return new SortConstraint[0];
    }

    /**
     * Sorts the lookups on increasing interval.
     */
    protected void sort() {
        final Date now = new Date();
        Collections.sort(getObjects(), new Comparator<IMObject>() {
            public int compare(IMObject o1, IMObject o2) {
                Date date1 = getTo(now, (Lookup) o1);
                Date date2 = getTo(now, (Lookup) o2);
                return date1.compareTo(date2);
            }

            /**
             * Returns the 'to' date of date format, based on a 'from' date
             * @param from the from date
             * @param format an <em>lookup.dateformat</em>
             * @return the 'to' date
             */
            private Date getTo(Date from, Lookup format) {
                IMObjectBean bean = new IMObjectBean(format);
                int interval = bean.getInt("interval");
                DateUnits unit = DateUnits.valueOf(bean.getString("units"));
                return DateRules.getDate(from, interval, unit);
            }
        });
    }

}
