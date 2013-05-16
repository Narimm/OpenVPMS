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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.domain.im.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link Entity} class.
 *
 * @author Tim Anderson
 */
public class EntityTestCase {

    /**
     * Tests the {@link Entity#addEntityRelationship(EntityRelationship)} and
     * {@link Entity#removeEntityRelationship(EntityRelationship)} methods.
     */
    @Test
    public void testAddRemoveRelationship() {
        Entity entity1 = new Entity();
        Entity entity2 = new Entity();
        EntityRelationship relationship = new EntityRelationship();
        relationship.setSource(entity1.getObjectReference());
        relationship.setTarget(entity2.getObjectReference());

        // add the relationship to each entity
        entity1.addEntityRelationship(relationship);
        assertTrue(entity1.getSourceEntityRelationships().contains(relationship));
        assertTrue(entity1.getTargetEntityRelationships().isEmpty());

        entity2.addEntityRelationship(relationship);
        assertTrue(entity2.getTargetEntityRelationships().contains(relationship));
        assertTrue(entity2.getSourceEntityRelationships().isEmpty());

        // now remove it
        entity1.removeEntityRelationship(relationship);
        assertTrue(entity1.getSourceEntityRelationships().isEmpty());
        assertTrue(entity1.getTargetEntityRelationships().isEmpty());

        entity2.removeEntityRelationship(relationship);
        assertTrue(entity2.getSourceEntityRelationships().isEmpty());
        assertTrue(entity2.getTargetEntityRelationships().isEmpty());
    }


    /**
     * Verifies that {@link Entity#addEntityRelationship(EntityRelationship)} throws an exception if the relationship
     * doesn't have a source or target specified.
     */
    @Test
    public void testAddRelationshipForNullSourceOrTarget() {
        Entity entity = new Entity();
        EntityRelationship relationship = new EntityRelationship();
        try {
            entity.addEntityRelationship(relationship);
        } catch (EntityException expected) {
            assertEquals(EntityException.ErrorCode.FailedToAddEntityRelationship, expected.getErrorCode());
        }
    }

    /**
     * Tests the {@link Entity#addSourceEntityRelationship(EntityRelationship)}
     * and {@link Entity#removeSourceEntityRelationship(EntityRelationship)}.
     */
    @Test
    public void testAddRemoveSourceRelationship() {
        Entity entity = new Entity();
        EntityRelationship relationship = new EntityRelationship();
        entity.addSourceEntityRelationship(relationship);
        assertEquals(entity.getObjectReference(), relationship.getSource());
        assertNull(relationship.getTarget());

        assertTrue(entity.getSourceEntityRelationships().contains(relationship));
        assertTrue(entity.getTargetEntityRelationships().isEmpty());

        entity.removeSourceEntityRelationship(relationship);
        assertTrue(entity.getSourceEntityRelationships().isEmpty());
    }

    /**
     * Tests the {@link Entity#addTargetEntityRelationship(EntityRelationship)}
     * and {@link Entity#removeTargetEntityRelationship(EntityRelationship)}.
     */
    @Test
    public void testAddRemoveTargetRelationship() {
        Entity entity = new Entity();
        EntityRelationship relationship = new EntityRelationship();
        entity.addTargetEntityRelationship(relationship);
        assertEquals(entity.getObjectReference(), relationship.getTarget());
        assertNull(relationship.getSource());

        assertTrue(entity.getTargetEntityRelationships().contains(relationship));
        assertTrue(entity.getSourceEntityRelationships().isEmpty());

        entity.removeTargetEntityRelationship(relationship);
        assertTrue(entity.getTargetEntityRelationships().isEmpty());
    }
}
