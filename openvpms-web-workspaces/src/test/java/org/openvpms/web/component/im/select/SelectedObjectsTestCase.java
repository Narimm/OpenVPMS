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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.select;

import org.junit.Test;
import org.openvpms.component.business.domain.im.common.Entity;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link SelectedObjects} class.
 *
 * @author Tim Anderson
 */
public class SelectedObjectsTestCase {

    /**
     * The objects.
     */
    private SelectedObjects<Entity> objects;

    /**
     * Test entity 'a'.
     */
    private final Entity a;

    /**
     * Test entity 'b'.
     */
    private final Entity b;

    /**
     * Test entity 'c'.
     */
    private final Entity c;

    /**
     * Test entity 'd'.
     */
    private final Entity d;


    /**
     * Default constructor.
     */
    public SelectedObjectsTestCase() {
        objects = new SelectedObjects<Entity>();
        a = createEntity("A");
        b = createEntity("B");
        c = createEntity("C");
        d = createEntity("D");
    }

    /**
     * Test addition.
     */
    @Test
    public void testAdd() {
        objects.addObject(a);
        checkObjects(a);
        assertEquals("A; ", objects.getText());

        objects.addObject(b);
        checkObjects(a, b);
        assertEquals("A; B; ", objects.getText());

        objects.addObject(c);
        checkObjects(a, b, c);
        assertEquals("A; B; C; ", objects.getText());
    }

    /**
     * Test removal.
     */
    @Test
    public void testRemove() {
        objects.addObjects(a, b, c, d);
        checkObjects(a, b, c, d);
        assertEquals("A; B; C; D; ", objects.getText());

        // now remove B and C
        objects.setNames("A", "D");
        checkObjects(a, d);
        assertEquals("A; D; ", objects.getText());

        // now remove A
        objects.setNames("D");
        checkObjects(d);
        assertEquals("D; ", objects.getText());

        // now remove D
        objects.setNames();
        checkObjects();
        assertEquals("", objects.getText());
    }

    /**
     * Test replacement.
     */
    @Test
    public void testReplace() {
        objects.addObjects(a, b, c);
        checkObjects(a, b, c);
        assertEquals("A; B; C; ", objects.getText());

        objects.parseNames("D; B; C; ");
        assertEquals("D; B; C; ", objects.getText());
        assertFalse(objects.isValid());
        assertFalse(objects.haveMatch(0));

        objects.setObject(0, d);
        assertTrue(objects.isValid());
        assertTrue(objects.haveMatch(0));
        assertEquals("D; B; C; ", objects.getText());
    }

    /**
     * Tests the {@link SelectedObjects#setNames(String...)} method.
     */
    @Test
    public void testSetNames() {
        objects.setNames("A", "B", "C");
        assertEquals("A; B; C; ", objects.getText());
    }

    /**
     * Verifies that the expected objects are selected.
     *
     * @param expected the expected objects
     */
    private void checkObjects(Entity... expected) {
        List<Entity> entities = objects.getObjects();
        assertEquals(expected.length, entities.size());
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], entities.get(i));
        }
    }

    /**
     * Helper to create an entity with the specified name.
     *
     * @param name the name
     * @return a new entity
     */
    private Entity createEntity(String name) {
        Entity result = new Entity();
        result.setName(name);
        return result;
    }

}
