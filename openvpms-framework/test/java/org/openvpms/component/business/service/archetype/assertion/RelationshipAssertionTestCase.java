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

package org.openvpms.component.business.service.archetype.assertion;

import org.junit.Test;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ActionContext;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link RelationshipAssertions}.
 *
 * @author Tim Anderson
 */
public class RelationshipAssertionTestCase {

    /**
     * Tests the {@link RelationshipAssertions#validate(ActionContext)} method for an
     * {@link RelationshipAssertions#UNIQUE_RELATIONSHIP} assertion.
     */
    @Test
    public void testUniqueRelationship() {
        AssertionDescriptor descriptor = new AssertionDescriptor();
        descriptor.setName(RelationshipAssertions.UNIQUE_RELATIONSHIP);
        Lookup source = new Lookup();
        Lookup target = new Lookup();
        NodeDescriptor node = new NodeDescriptor();

        ActionContext context0 = new ActionContext(descriptor, source, node, source.getLookupRelationships());
        assertTrue(RelationshipAssertions.validate(context0));

        LookupRelationship relationship1 = new LookupRelationship(source, target);
        source.addSourceLookupRelationship(relationship1);
        ActionContext context1 = new ActionContext(descriptor, source, node, source.getLookupRelationships());
        assertTrue(RelationshipAssertions.validate(context1));

        LookupRelationship relationship2 = new LookupRelationship(source, target);
        source.addSourceLookupRelationship(relationship2);
        ActionContext context2 = new ActionContext(descriptor, source, node, source.getLookupRelationships());
        assertFalse(RelationshipAssertions.validate(context2));
    }

    /**
     * Tests the {@link RelationshipAssertions#validate(ActionContext)} method for an
     * {@link RelationshipAssertions#UNIQUE_RELATIONSHIP_TYPE} assertion.
     */
    @Test
    public void testUniqueRelationshipType() {
        AssertionDescriptor descriptor = new AssertionDescriptor();
        descriptor.setName(RelationshipAssertions.UNIQUE_RELATIONSHIP_TYPE);
        Lookup source = new Lookup();
        Lookup target = new Lookup();
        NodeDescriptor node = new NodeDescriptor();

        ActionContext context0 = new ActionContext(descriptor, source, node, source.getLookupRelationships());
        assertTrue(RelationshipAssertions.validate(context0));

        LookupRelationship relationship1 = new LookupRelationship(source, target);
        relationship1.setArchetypeId(new ArchetypeId("lookup.typeA"));
        source.addSourceLookupRelationship(relationship1);
        ActionContext context1 = new ActionContext(descriptor, source, node, source.getLookupRelationships());
        assertTrue(RelationshipAssertions.validate(context1));

        LookupRelationship relationship2 = new LookupRelationship(source, target);
        relationship2.setArchetypeId(new ArchetypeId("lookup.typeB"));
        source.addSourceLookupRelationship(relationship2);
        ActionContext context2 = new ActionContext(descriptor, source, node, source.getLookupRelationships());
        assertTrue(RelationshipAssertions.validate(context2));

        LookupRelationship relationship3 = new LookupRelationship(source, target);
        relationship3.setArchetypeId(new ArchetypeId("lookup.typeB"));
        source.addSourceLookupRelationship(relationship3);
        ActionContext context3 = new ActionContext(descriptor, source, node, source.getLookupRelationships());
        assertFalse(RelationshipAssertions.validate(context3));
    }

}
