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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.select.IMObjectSelectorListener;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectCreatorListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.event.WindowPaneListener;


/**
 * Abstract implementation of the {@link IMObjectReferenceEditor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectReferenceEditor<T extends IMObject>
        extends AbstractPropertyEditor implements IMObjectReferenceEditor<T> {

    /**
     * The parent object. May be <tt>null</tt>
     */
    private final IMObject parent;

    /**
     * The selector.
     */
    private IMObjectSelector<T> selector;

    /**
     * Determines if the selector listener is currently being invoked
     * to avoid redundant updates.
     */
    private boolean inListener;

    /**
     * Listener for modifications to the property outside of this editor,
     * to refresh the UI.
     */
    private final ModifiableListener propertyListener;

    /**
     * The context.
     */
    private final Context context;


    /**
     * Constructs a new <tt>AbstractIMObjectReferenceEditor</tt>.
     *
     * @param property the reference property
     * @param parent   the parent object. May be <tt>null</tt>
     * @param context  the layout context
     */
    public AbstractIMObjectReferenceEditor(Property property,
                                           IMObject parent,
                                           LayoutContext context) {
        this(property, parent, context, false);
    }

    /**
     * Constructs a new <tt>AbstractIMObjectReferenceEditor</tt>.
     *
     * @param property    the reference property
     * @param parent      the parent object. May be <tt>null</tt>
     * @param context     the layout context
     * @param allowCreate determines if objects may be created
     */
    public AbstractIMObjectReferenceEditor(Property property,
                                           IMObject parent,
                                           LayoutContext context,
                                           boolean allowCreate) {
        super(property);
        this.parent = parent;
        selector = new IMObjectSelector<T>(property, allowCreate) {
            @Override
            protected Query<T> createQuery(String name) {
                return AbstractIMObjectReferenceEditor.this.createQuery(name);
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

            public void create() {
                onCreate();
            }
        });

        this.context = context.getContext();

        updateSelector();

        propertyListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onUpdate();
            }
        };
        addModifiableListener(propertyListener);
    }

    /**
     * Sets the value of the reference to the supplied object.
     *
     * @param object the object. May  be <tt>null</tt>
     */
    public void setObject(T object) {
        if (!inListener) {
            selector.setObject(object);
        }
        updateProperty(object);
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
     * Determines if the reference is null.
     * This treats an entered but incorrect name as being non-null.
     *
     * @return <tt>true</tt>  if the reference is null; otherwise
     *         <tt>false</tt>
     */
    public boolean isNull() {
        boolean result = false;
        if (getProperty().getValue() == null) {
            TextField text = selector.getText();
            if (text == null || StringUtils.isEmpty(text.getText())) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Determines if objects may be created.
     *
     * @param create if <tt>true</tt>, objects may be created
     */
    public void setAllowCreate(boolean create) {
        selector.setAllowCreate(create);
    }

    /**
     * Determines if objects may be created.
     *
     * @return <tt>true</tt> if objects may be created
     */
    public boolean allowCreate() {
        return selector.allowCreate();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid
     *         otherwise <tt>false</tt>
     */
    public boolean validate(Validator validator) {
        return (!selector.inSelect()) && super.validate(validator);
    }

    /**
     * Invoked when an object is selected.
     * <p/>
     * This implementation simply invokes {@link #setObject}.
     *
     * @param object the selected object. May be <tt>null</tt>
     */
    protected void onSelected(T object) {
        setObject(object);
    }

    /**
     * Invoked when the underlying property updates.
     * <p/>
     * This implementation is a no-op.
     *
     * @param object the updated object. May be <tt>null</tt>
     */
    protected void onUpdated(T object) {
    }

    /**
     * Invoked to create a new object.
     */
    protected void onCreate() {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated(object);
            }

            public void cancelled() {
                // ignore
            }
        };

        IMObjectCreator.create(getProperty().getDisplayName(),
                               getProperty().getArchetypeRange(),
                               listener);
    }

    /**
     * Invoked when an object is created. Pops up an editor to edit it.
     *
     * @param object the object to edit
     */
    protected void onCreated(IMObject object) {
        Context context = new LocalContext(this.context);
        context.setCurrent(object);
        LayoutContext layoutContext = new DefaultLayoutContext(true);
        layoutContext.setContext(context);
        final IMObjectEditor editor
                = IMObjectEditorFactory.create(object, parent, layoutContext);
        final EditDialog dialog = new EditDialog(editor);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                onEditCompleted(editor);
            }
        });

        dialog.show();
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     */
    @SuppressWarnings("unchecked")
    protected void onEditCompleted(IMObjectEditor editor) {
        if (!editor.isCancelled() && !editor.isDeleted()) {
            setObject((T) editor.getObject());
        }
    }

    /**
     * Creates a query to select objects.
     *
     * @param name the name to filter on. May be <tt>null</tt>
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    protected Query<T> createQuery(String name) {
        String[] shortNames = getProperty().getArchetypeRange();
        Query<T> query = QueryFactory.create(shortNames, context);
        query.setName(name);
        return query;
    }

    /**
     * Invoked when the property updates. Updates the selector and invokes
     * {@link #onUpdated}.
     */
    private void onUpdate() {
        T object = updateSelector();
        onUpdated(object);
    }

    /**
     * Updates the selector from the property.
     *
     * @return the current object, or <tt>null</tt> if there is none
     */
    @SuppressWarnings("unchecked")
    private T updateSelector() {
        Property property = getProperty();
        IMObjectReference reference = (IMObjectReference) property.getValue();
        T object = null;
        if (reference != null) {
            object = (T) IMObjectHelper.getObject(reference,
                                                  property.getArchetypeRange(),
                                                  context);
        }
        selector.setObject(object);
        return object;
    }

    /**
     * Updates the underlying property, notifying any registered listeners.
     *
     * @param object the object. May be <tt>null</tt>
     */
    private void updateProperty(IMObject object) {
        removeModifiableListener(propertyListener);
        try {
            Property property = getProperty();
            if (object != null) {
                property.setValue(object.getObjectReference());
            } else {
                property.setValue(null);
            }
        } finally {
            addModifiableListener(propertyListener);
        }
    }

}
