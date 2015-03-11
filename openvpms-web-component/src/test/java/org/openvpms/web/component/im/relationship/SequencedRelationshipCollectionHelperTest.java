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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.junit.Test;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.SequencedRelationship;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link SequencedRelationshipCollectionHelper}.
 *
 * @author Tim Anderson
 */
public class SequencedRelationshipCollectionHelperTest {

    /**
     * Tests the {@link SequencedRelationshipCollectionHelper#sequence(List)} method.
     */
    @Test
    public void testSequence() {
        RelationshipState state1 = create(10);
        RelationshipState state2 = create(2);
        RelationshipState state3 = create(14);
        RelationshipState state4 = create(15);
        RelationshipState state5 = create(14);
        RelationshipState state6 = create(16);
        RelationshipState state7 = create(20);
        List<RelationshipState> states = Arrays.asList(state1, state2, state3, state4, state5, state6, state7);
        SequencedRelationshipCollectionHelper.sequence(states);

        check(states, 10, 11, 14, 15, 16, 17, 20);
    }

    /**
     * Verifies the states have the correct sequence.
     *
     * @param states   the relationship states
     * @param expected the expected sequences
     */
    private void check(List<RelationshipState> states, int... expected) {
        assertEquals(states.size(), expected.length);
        for (int i = 0; i < states.size(); ++i) {
            SequencedRelationship relationship = (SequencedRelationship) states.get(i).getRelationship();
            assertEquals(expected[i], relationship.getSequence());
        }
    }

    /**
     * Creates a relationship state with the specified sequence.
     *
     * @param sequence the sequence
     * @return a new state
     */
    private RelationshipState create(int sequence) {
        EntityRelationship result = new EntityRelationship();
        result.setSequence(sequence);
        return new RelationshipState(result, 0L, null, null, 0l, null, null, true);
    }
}
