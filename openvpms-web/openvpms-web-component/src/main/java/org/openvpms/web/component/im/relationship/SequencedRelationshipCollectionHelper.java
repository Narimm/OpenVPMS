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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.common.SequencedRelationship;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


/**
 * Helper for collections of sequenced relationships.
 *
 * @author Tim Anderson
 */
class SequencedRelationshipCollectionHelper {

    /**
     * Sorts objects on their sequence and id nodes.
     *
     * @param objects the objects. This list is modified
     * @return the objects
     */
    public static <T extends IMObject> List<T> sort(List<T> objects) {
        IMObjectSorter.sort(objects, "sequence", "id");
        return objects;
    }

    /**
     * Sorts a list of relationship states on sequence.
     *
     * @param states the states to sort
     */
    public static void sortStates(List<RelationshipState> states) {
        Collections.sort(states, SequenceComparator.INSTANCE);
    }

    /**
     * Determines if there is a sequence node in each of a set of relationship archetypes.
     *
     * @return <tt>true</tt> if all archetypes have a sequence node
     */
    public static boolean hasSequenceNode(String[] shortNames) {
        boolean hasSequence = true;
        for (String shortName : shortNames) {
            ArchetypeDescriptor descriptor = DescriptorHelper.getArchetypeDescriptor(shortName);
            if (descriptor != null && descriptor.getNodeDescriptor("sequence") == null) {
                hasSequence = false;
                break;
            }
        }
        return hasSequence;
    }

    /**
     * Sequences relationships, using the first relationship sequence as the starting point.
     * <p>
     * This preserves gaps in the sequence.
     *
     * @param states the states to sequence
     */
    public static void sequenceStates(List<RelationshipState> states) {
        int last = -1;
        for (RelationshipState state : states) {
            IMObjectRelationship object = state.getRelationship();
            last = sequence(object, last);
        }
    }

    /**
     * Sequences relationships, using the first relationship sequence as the starting point.
     * <p>
     * This preserves gaps in the sequence.
     *
     * @param objects the objects to sequence
     */
    public static <T extends IMObject> void sequence(Collection<T> objects) {
        int last = -1;
        for (IMObject object : objects) {
            last = sequence(object, last);
        }
    }

    /**
     * Sorts a map of targets to relationships on their relationship sequence.
     *
     * @param map the map to sort
     * @return the sorted map entries
     */
    public static <T extends SequencedRelationship> List<Map.Entry<IMObject, T>> sort(Map<IMObject, T> map) {
        List<Map.Entry<IMObject, T>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, (o1, o2) -> {
            T r1 = o1.getValue();
            T r2 = o2.getValue();
            int compare = Integer.compare(r1.getSequence(), r2.getSequence());
            if (compare == 0) {
                compare = Long.compare(r1.getId(), r2.getId());
                if (compare == 0) {
                    compare = r1.getLinkId().compareTo(r2.getLinkId());
                }
            }
            return compare;
        });
        return entries;
    }

    /**
     * Returns the next sequence.
     *
     * @param objects the objects to search. May be unordered.
     * @return the next sequence
     */
    public static <T extends IMObject> int getNextSequence(Collection<T> objects) {
        int last = -1;
        for (IMObject object : objects) {
            if (object instanceof SequencedRelationship) {
                int sequence = ((SequencedRelationship) object).getSequence();
                if (sequence > last) {
                    last = sequence;
                }
            }
        }
        return last + 1;
    }

    private static int sequence(IMObject object, int last) {
        if (object instanceof SequencedRelationship) {
            SequencedRelationship relationship = (SequencedRelationship) object;
            if (last != -1 && relationship.getSequence() <= last) {
                relationship.setSequence(++last);
            } else {
                last = relationship.getSequence();
            }
        }
        return last;
    }

    /**
     * Comparator to help order {@link RelationshipState} instances on sequence.
     */
    private static class SequenceComparator implements Comparator<RelationshipState> {

        /**
         * Singleton instance.
         */
        public static SequenceComparator INSTANCE = new SequenceComparator();

        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         * first argument is less than, equal to, or greater than the
         * second.
         * @throws ClassCastException if the arguments' types prevent them from
         *                            being compared by this Comparator.
         */
        public int compare(RelationshipState o1, RelationshipState o2) {
            SequencedRelationship r1 = (SequencedRelationship) o1.getRelationship();
            SequencedRelationship r2 = (SequencedRelationship) o2.getRelationship();
            return Integer.compare(r1.getSequence(), r2.getSequence());
        }
    }

}
