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

package org.openvpms.web.component.im.edit;

import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


/**
 * {@link CollectionPropertyEditor} test.
 *
 * @author Tim Anderson
 */
public abstract class AbstractCollectionPropertyEditorTest extends AbstractAppTest {

    /**
     * Tests the behaviour of performing query operations on an empty
     * collection editor
     */
    @Test
    public void testEmpty() {
        IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        CollectionPropertyEditor editor = createEditor(property, parent);

        assertTrue("Collection should be empty", editor.getObjects().isEmpty());
        assertFalse("Collection shouldn't be modified", editor.isModified());
        assertSame(property, editor.getProperty());
        assertFalse("Collection not saved", editor.isSaved());
        if (property.getMinCardinality() > 0) {
            assertFalse("Collection should be invalid", editor.isValid());
        } else {
            assertTrue("Collection should be valid", editor.isValid());
        }
    }

    /**
     * Tests {@link CollectionPropertyEditor#save}.
     */
    @Test
    public void testSave() {
        final IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        assertTrue("Require collection with min cardinality >= 0", property.getMinCardinality() >= 0);
        final CollectionPropertyEditor editor = createEditor(property, parent);

        assertFalse(editor.isModified());

        IMObject element = createObject(parent);
        editor.add(element);
        assertEquals(1, editor.getObjects().size());
        assertSame(element, editor.getObjects().get(0));

        assertTrue("Collection should be valid", editor.isValid());
        assertTrue("Collection should be modified", editor.isModified());

        save(editor, parent);

        assertTrue(editor.isSaved());
        assertFalse(editor.isModified());

        // make sure the element has saved
        assertEquals("Retrieved element doesnt match that saved", element, get(element));

        // now retrieve parent and verify collection matches the original
        IMObject savedParent = get(parent);
        assertNotNull(savedParent);
        CollectionPropertyEditor saved = createEditor(
                getCollectionProperty(savedParent), savedParent);
        assertEquals(1, saved.getObjects().size());
        assertTrue(saved.getObjects().contains(element));

        assertFalse("Collection shouldn't be modified", saved.isModified());
        assertFalse("Collection not saved", saved.isSaved());
        assertTrue("Collection should be valid", saved.isValid());

        // make sure save can be executed a second time
        save(editor, parent);
    }

    /**
     * Tests {@link CollectionPropertyEditor#remove}.
     */
    @Test
    public void testRemove() {
        final IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        assertTrue("Require collection with min cardinality >= 0",
                   property.getMinCardinality() >= 0);
        final CollectionPropertyEditor editor = createEditor(property, parent);

        IMObject elt1 = createObject(parent);
        IMObject elt2 = createObject(parent);
        IMObject elt3 = createObject(parent);

        editor.add(elt1);
        editor.add(elt2);
        editor.add(elt3);

        assertEquals(3, editor.getObjects().size());
        assertTrue(editor.getObjects().contains(elt1));
        assertTrue(editor.getObjects().contains(elt2));
        assertTrue(editor.getObjects().contains(elt3));
        assertTrue(editor.isValid());
        assertTrue(editor.isModified());

        save(editor, parent);
        assertFalse(editor.isModified());

        // make sure the elements have saved
        assertEquals("Retrieved element1 doesnt match that saved", elt1, get(elt1));
        assertEquals("Retrieved element2 doesnt match that saved", elt2, get(elt2));
        assertEquals("Retrieved element3 doesnt match that saved", elt3, get(elt3));

        // now remove elt1, and elt2, save and verify that they are no longer
        // available
        editor.remove(elt1);
        assertTrue(editor.isModified());
        editor.remove(elt2);
        assertTrue(editor.isModified());
        assertEquals(1, editor.getObjects().size());
        assertFalse(editor.getObjects().contains(elt1));
        assertFalse(editor.getObjects().contains(elt2));
        assertTrue(editor.getObjects().contains(elt3));

        save(editor, parent);
        assertNull("element1 wasnt deleted", get(elt1));
        assertNull("element2 wasnt deleted", get(elt2));
        assertFalse(editor.isModified());

        // now retrieve parent and verify collection matches the original
        IMObject savedParent = get(parent);
        assertNotNull(savedParent);
        CollectionPropertyEditor saved = createEditor(
                getCollectionProperty(savedParent), savedParent);
        assertEquals(1, saved.getObjects().size());
        assertTrue(saved.getObjects().contains(elt3));

        assertFalse("Collection shouldn't be modified", saved.isModified());
        assertFalse("Collection not saved", saved.isSaved());
        assertTrue("Collection should be valid", saved.isValid());

        // make sure save can be executed a second time
        save(editor, parent);
    }

    /**
     * Tests {@link CollectionPropertyEditor#remove}.
     */
    @Test
    public void testRemoveAndAdd() {
        final IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        assertTrue("Require collection with min cardinality >= 0",
                   property.getMinCardinality() >= 0);
        final CollectionPropertyEditor editor = createEditor(property, parent);

        IMObject elt1 = createObject(parent);
        IMObject elt2 = createObject(parent);
        IMObject elt3 = createObject(parent);
        editor.add(elt1);

        assertEquals(1, editor.getObjects().size());
        assertTrue(editor.getObjects().contains(elt1));
        assertTrue(editor.isValid());
        assertTrue(editor.isModified());

        save(editor, parent);

        // make sure the element has saved
        assertEquals("Retrieved element1 doesnt match that saved", elt1, get(elt1));

        // now remove elt1, and add elt2
        // save and verify that it is no longer available
        editor.remove(elt1);
        editor.add(elt2);
        assertEquals(1, editor.getObjects().size());
        assertFalse(editor.getObjects().contains(elt1));
        assertTrue(editor.getObjects().contains(elt2));

        save(editor, parent);
        assertNull("element1 wasnt deleted", get(elt1));

        // now retrieve parent and verify collection matches the original
        IMObject savedParent = get(parent);
        assertNotNull(savedParent);
        CollectionPropertyEditor saved = createEditor(
                getCollectionProperty(savedParent), savedParent);
        assertEquals(1, saved.getObjects().size());
        assertTrue(saved.getObjects().contains(elt2));

        assertFalse("Collection shouldn't be modified", saved.isModified());
        assertFalse("Collection not saved", saved.isSaved());
        assertTrue("Collection should be valid", saved.isValid());

        // make sure save can be executed a second time
        save(editor, parent);

        // add an object and remove it without saving
        editor.add(elt3);
        editor.getEditor(elt3);
        editor.remove(elt3);

        save(editor, parent);
    }

    /**
     * Tests {@link CollectionPropertyEditor#remove} on a collection that has
     * been saved and reloaded.
     */
    @Test
    public void testRemoveAfterReload() {
        final IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        assertTrue("Require collection with min cardinality >= 0",
                   property.getMinCardinality() >= 0);
        final CollectionPropertyEditor editor = createEditor(property, parent);

        IMObject elt1 = createObject(parent);
        IMObject elt2 = createObject(parent);

        editor.add(elt1);
        editor.add(elt2);

        assertEquals(2, editor.getObjects().size());
        assertTrue(editor.getObjects().contains(elt1));
        assertTrue(editor.getObjects().contains(elt2));
        assertTrue(editor.isValid());
        assertTrue(editor.isModified());

        save(editor, parent);

        // make sure the elements have saved
        assertEquals("Retrieved element1 doesnt match that saved", elt1, get(elt1));
        assertEquals("Retrieved element2 doesnt match that saved", elt2, get(elt2));

        // reload parent and collection
        final IMObject parent2 = get(parent);
        final CollectionPropertyEditor editor2 = createEditor(
                getCollectionProperty(parent2), parent2);
        assertEquals(2, editor2.getObjects().size());
        elt1 = get(elt1);
        elt2 = get(elt2);

        // now remove elt1, save and verify that it is no longer available
        editor2.remove(elt1);
        assertEquals(1, editor2.getObjects().size());
        assertFalse(editor2.getObjects().contains(elt1));
        assertTrue(editor2.getObjects().contains(elt2));

        save(editor2, parent2);
        assertNull("element1 wasnt deleted", get(elt1));

        // now retrieve parent and verify collection matches the original
        IMObject savedParent = get(parent2);
        assertNotNull(savedParent);
        CollectionPropertyEditor saved = createEditor(
                getCollectionProperty(savedParent), savedParent);
        assertEquals(1, saved.getObjects().size());
        assertTrue(saved.getObjects().contains(elt2));

        assertFalse("Collection shouldn't be modified", saved.isModified());
        assertFalse("Collection not saved", saved.isSaved());
        assertTrue("Collection should be valid", saved.isValid());

        // make sure save can be executed a second time
        save(editor2, parent2);
    }

    /**
     * Tests the {@link CollectionPropertyEditor#clearModified()} method.
     */
    @Test
    public void testClearModified() {
        final IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        final CollectionPropertyEditor editor = createEditor(property, parent);

        assertFalse(editor.isModified());

        IMObject element = createObject(parent);
        editor.add(element);
        assertEquals(1, editor.getObjects().size());
        assertTrue(editor.isModified());
        editor.clearModified();
        assertFalse(editor.isModified());
    }

    /**
     * Verifies that if one item in the collection is invalid, the collection is invalid.
     */
    @Test
    public void testValidation() {
        final IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        final CollectionPropertyEditor editor = createEditor(property, parent);

        IMObject element1 = createObject(parent);
        IMObject element2 = createObject(parent);
        IMObject element3 = createObject(parent);
        editor.add(element1);
        editor.add(element2);
        editor.add(element3);
        assertTrue(editor.isValid());

        makeValid(element1, false);
        editor.resetValid();
        assertFalse(editor.isValid());

        makeValid(element1, true);
        editor.resetValid();
        assertTrue(editor.isValid());

        makeValid(element2, false);
        editor.resetValid();
        assertFalse(editor.isValid());

        makeValid(element2, true);
        editor.resetValid();
        assertTrue(editor.isValid());

        makeValid(element3, false);
        editor.resetValid();
        assertFalse(editor.isValid());

        makeValid(element3, true);
        editor.resetValid();
        assertTrue(editor.isValid());
    }

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    protected abstract IMObject createParent();

    /**
     * Returns the name of the collection node.
     *
     * @return the node name
     */
    protected abstract String getCollectionNode();

    /**
     * Returns the collection property.
     *
     * @param parent the parent of the collection
     * @return the collection property
     */
    protected CollectionProperty getCollectionProperty(
            IMObject parent) {
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(
                parent);
        assertNotNull(archetype);
        NodeDescriptor node = archetype.getNodeDescriptor(getCollectionNode());
        assertNotNull(node);
        return new IMObjectProperty(parent, node);
    }

    /**
     * Returns an editor for a collection property.
     *
     * @param property the collection property
     * @param parent   the parent of the collection
     * @return a new editor for the property
     */
    protected abstract CollectionPropertyEditor createEditor(
            CollectionProperty property, IMObject parent);

    /**
     * Returns an object to add to the collection.
     *
     * @param parent the parent of the collection
     * @return a new object to add to the collection
     */
    protected abstract IMObject createObject(IMObject parent);

    /**
     * Makes an object valid or invalid.
     *
     * @param object the object
     * @param valid  if {@code true}, make it valid, otherwise make it invalid
     */
    protected abstract void makeValid(IMObject object, boolean valid);

    /**
     * Executes a callback in a transaction.
     *
     * @param callback the callback
     */
    protected void execute(TransactionCallback<Object> callback) {
        TransactionTemplate template = new TransactionTemplate(
                ServiceHelper.getTransactionManager());
        template.execute(callback);
    }

    /**
     * Helper to save a collection and its parent.
     *
     * @param editor the collection editor
     * @param parent the parent object
     */
    protected void save(final CollectionPropertyEditor editor, final IMObject parent) {
        execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                editor.save();
                assertTrue("Failed to save parent", SaveHelper.save(parent));
            }
        });
    }

}
