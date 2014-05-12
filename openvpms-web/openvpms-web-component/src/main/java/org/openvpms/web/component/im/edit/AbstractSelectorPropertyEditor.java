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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.select.IMObjectSelectorListener;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.focus.FocusGroup;

/**
 * An {@link PropertyEditor} that provides an {@link IMObjectSelector} to select objects to populate the property
 * with.
 *
 * @author Tim Anderson
 */
public abstract class AbstractSelectorPropertyEditor<T extends IMObject> extends AbstractPropertyEditor {

    /**
     * The selector.
     */
    private IMObjectSelector<T> selector;

    /**
     * Listener for modifications to the property outside of this editor,
     * to refresh the UI.
     */
    private final ModifiableListener propertyListener;

    /**
     * Determines if the selector listener is currently being invoked
     * to avoid redundant updates.
     */
    private boolean inListener;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * Constructs an {@link AbstractSelectorPropertyEditor}.
     *
     * @param property the property
     * @param context  the layout context
     */
    public AbstractSelectorPropertyEditor(Property property, LayoutContext context) {
        this(property, context, false);
    }

    /**
     * Constructs an {@link AbstractSelectorPropertyEditor}.
     * <p/>
     * Subclasses should invoke {@link #updateSelector()} to update the selector at the end of construction.
     *
     * @param property    the property
     * @param context     the layout context
     * @param allowCreate determines if objects may be created
     */
    public AbstractSelectorPropertyEditor(Property property, LayoutContext context, boolean allowCreate) {
        super(property);
        this.context = new DefaultLayoutContext(context, context.getHelpContext().subtopic("select"));

        selector = new IMObjectSelector<T>(property, allowCreate, this.context) {
            @Override
            protected Query<T> createQuery(String name) {
                return AbstractSelectorPropertyEditor.this.createQuery(name);
            }
        };
        selector.setListener(new IMObjectSelectorListener<T>() {
            public void selected(T object) {
                inListener = true;
                try {
                    onSelected(object);
                } finally {
                    inListener = false;
                }
            }

            public void selected(T object, Browser<T> browser) {
                inListener = true;
                try {
                    onSelected(object, browser);
                } finally {
                    inListener = false;
                }
            }

            public void create() {
                onCreate();
            }
        });

        // For OVPMS-967, don't allow focus traversal
        selector.getSelect().setFocusTraversalParticipant(false);

        propertyListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onUpdate();
            }
        };
        addModifiableListener(propertyListener);
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    public void dispose() {
        super.dispose();
        removeModifiableListener(propertyListener);
    }

    /**
     * Sets the value property to the supplied object.
     *
     * @param object the object. May  be {@code null}
     */
    public boolean setObject(T object) {
        if (!inListener) {
            selector.setObject(object);
        }
        return updateProperty(object);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return selector.getComponent();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return selector.getFocusGroup();
    }

    /**
     * Returns the selector.
     *
     * @return the selector
     */
    protected IMObjectSelector<T> getSelector() {
        return selector;
    }

    /**
     * Invoked when an object is selected.
     * <p/>
     * This implementation simply invokes {@link #setObject}.
     *
     * @param object the selected object. May be {@code null}
     */
    protected void onSelected(T object) {
        setObject(object);
    }

    /**
     * Invoked when an object is selected from a brwoser.
     * <p/>
     * This implementation delegates to {@link #onSelected(IMObject)}.
     *
     * @param object  the selected object. May be {@code null}
     * @param browser the browser
     */
    protected void onSelected(T object, Browser<T> browser) {
        onSelected(object);
    }

    /**
     * Invoked when the underlying property updates.
     * <p/>
     * This implementation is a no-op.
     *
     * @param object the updated object. May be {@code null}
     */
    protected void onUpdated(T object) {
    }

    /**
     * Invoked to create a new object.
     * <p/>
     * This implementation is a no-op.
     */
    protected void onCreate() {
    }

    /**
     * Updates the underlying property, notifying any registered listeners.
     *
     * @param object the object. May be {@code null}
     * @return {@code true} if the value was set, {@code false} if it cannot be set due to error, or is the same as
     *         the existing value
     */
    protected boolean updateProperty(T object) {
        boolean modified = false;
        removeModifiableListener(propertyListener);
        try {
            Property property = getProperty();
            modified = updateProperty(property, object);
            if (modified) {
                resetValid();
            }
        } finally {
            addModifiableListener(propertyListener);
        }
        return modified;
    }

    /**
     * Returns the object corresponding to the property.
     *
     * @return the object. May be {@code null}
     */
    protected abstract T getObject();

    /**
     * Updates the underlying property with the specified value.
     *
     * @param property the property
     * @param value    the value to update with. May be {@code null}
     * @return {@code true} if the property was modified
     */
    protected abstract boolean updateProperty(Property property, T value);

    /**
     * Creates a query to select objects.
     *
     * @param name the name to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    protected Query<T> createQuery(String name) {
        String[] shortNames = getProperty().getArchetypeRange();
        Query<T> query = QueryFactory.create(shortNames, context.getContext());
        query.setValue(name);
        return query;
    }

    /**
     * Updates the selector from the property.
     *
     * @return the current object, or {@code null} if there is none
     */
    protected T updateSelector() {
        T object = getObject();
        selector.setObject(object);
        return object;
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return context;
    }

    /**
     * Invoked when the property updates. Updates the selector and invokes {@link #onUpdated}.
     */
    private void onUpdate() {
        resetValid();
        T object = updateSelector();
        onUpdated(object);
    }

}