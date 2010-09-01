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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.bound;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Window;
import org.openvpms.web.component.property.Property;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


/**
 * Tests components that bind to properties.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractBoundFieldTest<T, V> {

    /**
     * The first test value.
     */
    private final V value1;

    /**
     * The second test value.
     */
    private final V value2;

    /**
     * Root window.
     */
    private Window window = new Window();

    /**
     * Component container.
     */
    private ContentPane container = new ContentPane();

    /**
     * Constructs a <tt>AbstractBoundFieldTest</tt> with two values.
     *
     * @param value1 the first test value
     * @param value2 the second test value
     */
    public AbstractBoundFieldTest(V value1, V value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    /**
     * Verifies that updates to the property are reflected in the field.
     */
    @Test
    public void testPropertyUpdate() {
        Property property = createProperty();
        T field = createField(property);
        property.setValue(value1);
        assertEquals(value1, getValue(field));
        property.setValue(value2);
        assertEquals(value2, getValue(field));
    }

    /**
     * Verifies that updates to the field are reflected in the property.
     */
    @Test
    public void testFieldUpdate() {
        Property property = createProperty();
        T field = createField(property);
        setValue(field, value1);
        assertEquals(value1, getValue(property));

        setValue(field, value2);
        assertEquals(value2, getValue(property));
    }

    /**
     * Verifies that the component doesn't get updates from the property when the component has been removed
     * from its container.
     * <p/>
     * When the component is removed, its <tt>dispose()</tt> method should be invoked which should unbind it from the
     * property. This ensures that components can be garbage collected.
     * <p/>
     * When the component is added, its <tt>init() should be invoked which should bind it to the property.
     * <p/>
     */
    @Test
    public void testInitDispose() {
        Property property = createProperty();
        T field = createField(property);
        property.setValue(value2);
        assertEquals(value2, getValue(field));

        // now register the field in a container. Should still receive updates
        addComponent(container, field);
        property.setValue(value1);
        assertEquals(value1, getValue(field));

        // now remove the field from the container. Should no longer receive updates
        removeComponent(container, field);
        property.setValue(value2);
        assertFalse(value2.equals(getValue(field)));

        // re-add the component to the container. Should get updates
        addComponent(container, field);
        assertEquals(value2, getValue(field));
        property.setValue(value1);
        assertEquals(value1, getValue(field));
    }

    /**
     * Sets up the fixture.
     */
    @Before
    public void setUp() {
        ApplicationInstance instance = new ApplicationInstance() {

            public Window init() {
                window.setContent(container);
                return window;
            }
        };
        ApplicationInstance.setActive(instance);
        instance.doInit();
    }

    /**
     * Returns the value of the field.
     *
     * @param field the field
     * @return the value of the field
     */
    protected abstract V getValue(T field);

    /**
     * Returns the value of the property.
     *
     * @param property the property
     * @return the value of the property
     */
    @SuppressWarnings("unchecked")
    protected V getValue(Property property) {
        return (V) property.getValue();
    }

    /**
     * Sets the value of the field.
     *
     * @param field the field
     * @param value the value to set
     */
    protected abstract void setValue(T field, V value);

    /**
     * Creates a new bound field.
     *
     * @param property the property to bind to
     * @return a new bound field
     */
    protected abstract T createField(Property property);

    /**
     * Creates a new property.
     *
     * @return a new property
     */
    protected abstract Property createProperty();

    /**
     * Adds a component to the container.
     *
     * @param container the container
     * @param field     the field to add
     */
    protected void addComponent(Component container, T field) {
        container.add((Component) field);
    }

    /**
     * Removes a component from the container.
     *
     * @param container the container
     * @param field     thhe field to remove
     */
    protected void removeComponent(Component container, T field) {
        container.remove((Component) field);
    }

}
