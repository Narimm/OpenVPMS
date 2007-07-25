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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Transformer;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * IterableSummary enables iteration over a patient medical record history,
 * flattening out the first level of the act heirarchy, and filtering child
 * acts.
 * e.g, given a heirarchy of:
 * <ul>
 * <li>event1</li>
 * <ul><li>note1</li><li>problem1</li><li>weight1</li></ul>
 * <li>event2</li>
 * <ul><li>note2</li><li>problem2</li></ul>
 * </ul>
 * and filtering out all child acts bar <em>act.patientNote</em> and
 * <em>act.patientClinicalProblem</em>, the following acts would be returned:
 * <tt>event1, note1, problem1, event2, note2, problem2</tt>.
 * <br/>
 * Note that the child acts are ordered on increasing start time.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class IterableSummary implements Iterable<Act> {

    /**
     * Collection of events.
     */
    private Iterable<Act> events;

    /**
     * The child short names to include.
     */
    private final String[] shortNames;


    /**
     * Creates a new <tt>IterableSummary</tt>.
     *
     * @param events     the collection of events
     * @param shortNames the child short names to include
     */
    public IterableSummary(Iterable<Act> events, String[] shortNames) {
        this.events = events;
        this.shortNames = shortNames;
    }

    /**
     * Returns an iterator over the summary.
     *
     * @return a new iterator
     */
    public Iterator<Act> iterator() {
        return new SummaryIterator();
    }

    private class SummaryIterator implements Iterator<Act> {

        /**
         * Iterator over the events.
         */
        private Iterator<Act> eventIterator;

        /**
         * Iterator over the current event's items.
         */
        private Iterator<Act> itemsIterator;


        /**
         * Creates a new <tt>SummaryIterator</tt>.
         */
        public SummaryIterator() {
            this.eventIterator = events.iterator();
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements.
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            if (itemsIterator == null || !itemsIterator.hasNext()) {
                advance();
            }
            return itemsIterator != null && itemsIterator.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException iteration has no more elements.
         */
        public Act next() {
            if (itemsIterator == null || !itemsIterator.hasNext()) {
                if (!advance()) {
                    throw new NoSuchElementException();
                }
            }
            return itemsIterator.next();
        }

        /**
         * Not supported.
         *
         * @throws UnsupportedOperationException if invoked
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Attempts to advance to the next event.
         *
         * @return <tt>true</tt> if the advance was successful.
         */
        private boolean advance() {
            if (eventIterator.hasNext()) {
                Act act = eventIterator.next();
                List<Act> result = new ArrayList<Act>();
                result.add(act);
                List<Act> items = new ArrayList<Act>();
                Set<ActRelationship> relationships
                        = act.getSourceActRelationships();
                for (ActRelationship relationship : relationships) {
                    IMObjectReference target = relationship.getTarget();
                    for (String shortName : shortNames) {
                        if (TypeHelper.isA(target, shortName)) {
                            Act item = (Act) IMObjectHelper.getObject(target);
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    }
                }
                sortItems(items);
                result.addAll(items);
                itemsIterator = result.iterator();
                return true;
            }
            return false;
        }

        /**
         * Sorts act on start time.
         *
         * @param acts the items to sort
         */
        @SuppressWarnings("unchecked")
        private void sortItems(List<Act> acts) {
            Transformer transformer = new Transformer() {
                public Object transform(Object input) {
                    Date date = ((Act) input).getActivityStartTime();
                    if (date instanceof Timestamp) {
                        // to avoid ClassCastException when doing compareTo
                        date = new Date(date.getTime());
                    }
                    return date;
                }
            };
            Comparator comparator = ComparatorUtils.transformedComparator(
                    ComparatorUtils.nullHighComparator(null), transformer);
            Collections.sort(acts, comparator);
        }
    }
}
