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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.edit.Cancellable;
import org.openvpms.web.component.edit.Deletable;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.view.AbstractIMObjectView;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.IMObjectView;
import org.openvpms.web.component.im.view.layout.EditLayoutStrategyFactory;
import org.openvpms.web.component.im.view.layout.ViewLayoutStrategyFactory;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ModifiableListeners;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Abstract implementation of the {@link IMObjectEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectEditor extends AbstractModifiable
    implements IMObjectEditor {

    /**
     * The object being edited.
     */
    private final IMObject object;

    /**
     * The parent object. May be {@code null}.
     */
    private final IMObject parent;

    /**
     * The object's descriptor.
     */
    private final ArchetypeDescriptor archetype;

    /**
     * The object viewer.
     */
    private IMObjectView viewer;

    /**
     * The listeners.
     */
    private ModifiableListeners listeners = new ModifiableListeners();

    /**
     * The child editors.
     */
    private Editors editors;

    /**
     * The object properties.
     */
    private PropertySet properties;

    /**
     * Lookup editors. These may need to be refreshed.
     */
    private Map<PropertyEditor, ModifiableListener> lookups = new HashMap<PropertyEditor, ModifiableListener>();

    /**
     * The layout context.
     */
    private LayoutContext context;

    /**
     * Action listener for layout changes.
     */
    private ActionListener layoutChangeListener;

    /**
     * Indicates if the object has been saved.
     */
    private boolean saved = false;

    /**
     * Indicates if the object was deleted.
     */
    private boolean deleted = false;

    /**
     * Indicates if editing was cancelled.
     */
    private boolean cancelled = false;

    /**
     * Property change listener notifier.
     */
    private PropertyChangeSupport propertyChangeNotifier;


    /**
     * Constructs an {@code AbstractIMObjectEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public AbstractIMObjectEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        this.object = object;
        this.parent = parent;

        context = new DefaultLayoutContext(layoutContext);
        // don't increase the layout depth.
        context.setLayoutDepth(layoutContext.getLayoutDepth());

        archetype = context.getArchetypeDescriptor(object);
        properties = new PropertySet(object, archetype, context.getVariables());
        editors = new Editors(properties, listeners);

        IMObjectLayoutStrategyFactory strategyFactory = context.getLayoutStrategyFactory();
        if (strategyFactory == null || strategyFactory instanceof ViewLayoutStrategyFactory) {
            context.setLayoutStrategyFactory(new EditLayoutStrategyFactory());
        }

        IMObjectComponentFactory factory = new ComponentFactory(context);
        context.setComponentFactory(factory);

        editors.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onModified(modifiable);
            }
        });
    }


    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    public void dispose() {
        editors.dispose();
        disposeLookups();
        listeners.removeAll();

        if (viewer != null && viewer.hasComponent()) {
            Component component = viewer.getComponent();
            Component parent = component.getParent();
            if (parent != null) {
                parent.remove(component);
            }
        }
    }

    /**
     * Returns a title for the editor.
     *
     * @return a title for the editor
     */
    public String getTitle() {
        String title;
        if (object.isNew()) {
            title = Messages.get("editor.new.title", getDisplayName());
        } else {
            title = Messages.get("editor.edit.title", getDisplayName());
        }
        return title;
    }

    /**
     * Returns a display name for the object being edited.
     *
     * @return a display name for the object
     */
    public String getDisplayName() {
        return getArchetypeDescriptor().getDisplayName();
    }

    /**
     * Returns the object being edited.
     *
     * @return the object being edited
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object. May be {@code null}
     */
    public IMObject getParent() {
        return parent;
    }

    /**
     * Returns the archetype descriptor of the object.
     *
     * @return the object's archetype descriptor
     */
    public ArchetypeDescriptor getArchetypeDescriptor() {
        return archetype;
    }

    /**
     * Save any edits.
     *
     * @return {@code true} if the save was successful
     */
    public boolean save() {
        if (cancelled) {
            return false;
        }
        boolean result = false;
        Validator validator = new Validator();
        if (validator.validate(this)) {
            if (!isModified()) {
                result = true;
            } else {
                result = doSave();
                if (result) {
                    saved = true;
                    clearModified();
                }
            }
        } else {
            ValidationHelper.showError(validator);
        }
        return result;
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return {@code true} if edits have been saved.
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Delete the current object.
     *
     * @return {@code true} if the object was deleted successfully
     */
    public boolean delete() {
        if (cancelled) {
            return false;
        }
        boolean result = doDelete();
        deleted |= result;
        return result;
    }

    /**
     * Determines if the object has been deleted.
     *
     * @return {@code true} if the object has been deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Determines if the object has been changed.
     *
     * @return {@code true} if the object has been changed
     */
    public boolean isModified() {
        return object.isNew() || editors.isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        editors.clearModified();
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        editors.addModifiableListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        editors.addModifiableListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        editors.removeModifiableListener(listener);
    }

    /**
     * Cancel any edits. Once complete, query methods may be invoked, but the behaviour of other methods is undefined.
     */
    public void cancel() {
        try {
            cancelled = true;
            for (Cancellable cancellable : editors.getCancellable()) {
                cancellable.cancel();
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Determines if editing was cancelled.
     *
     * @return {@code true} if editing was cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    public Component getComponent() {
        return getView().getComponent();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been
     *         rendered
     */
    public FocusGroup getFocusGroup() {
        return getView().getFocusGroup();
    }

    /**
     * Add a property change listener.
     *
     * @param name     the property name to listen on
     * @param listener the listener
     */
    public void addPropertyChangeListener(String name,
                                          PropertyChangeListener listener) {
        if (propertyChangeNotifier == null) {
            propertyChangeNotifier = new PropertyChangeSupport(this);
        }
        propertyChangeNotifier.addPropertyChangeListener(name, listener);
    }

    /**
     * Remove a property change listener.
     *
     * @param name     the property name to remove the listener for
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(String name,
                                             PropertyChangeListener listener) {
        if (propertyChangeNotifier != null) {
            propertyChangeNotifier.removePropertyChangeListener(
                name, listener);
        }
    }

    /**
     * Returns the help context for the editor.
     *
     * @return the help context
     */
    public HelpContext getHelpContext() {
        return context.getHelpContext();
    }

    /**
     * Returns a property, given its node descriptor's name.
     *
     * @param name the descriptor's name
     * @return the property corresponding to {@code name} or {@code null} if
     *         none exists
     */
    public Property getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Resets the cached validity state of the object.
     *
     * @param elements if {@code true} reset the validity state of the elements as well
     */
    protected void resetValid(boolean elements) {
        super.resetValid(elements);
        if (elements) {
            editors.resetValid();
        }
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        boolean result;
        result = validator.validate(editors);
        if (result) {
            // run it by the archetype service to pick up validation issues
            // not detected by the app e.g expression assertions
            List<ValidatorError> errors = ValidationHelper.validate(object, ServiceHelper.getArchetypeService());
            if (errors != null) {
                validator.add(this, errors);
                result = false;
            }
        }
        return result;
    }

    /**
     * Save any edits.
     * <p/>
     * This uses {@link #saveChildren()} to save the children prior to
     * invoking {@link #saveObject()}.
     *
     * @return {@code true} if the save was successful
     */
    protected boolean doSave() {
        boolean saved = saveChildren();
        if (saved) {
            saved = saveObject();
        }
        return saved;
    }

    /**
     * Saves the object.
     *
     * @return {@code true} if the save was successful
     */
    protected boolean saveObject() {
        IMObject object = getObject();
        return SaveHelper.save(object);
    }

    /**
     * Save any modified child Saveable instances.
     *
     * @return {@code true} if the save was successful
     */
    protected boolean saveChildren() {
        for (Saveable saveable : editors.getModifiedSaveable()) {
            if (!saveable.save()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deletes the object.
     * <p/>
     * This uses {@link #deleteChildren()} to delete the children prior to
     * invoking {@link #deleteObject()}.
     *
     * @return {@code true} if the delete was successful
     */
    protected boolean doDelete() {
        boolean result = deleteChildren();
        if (result) {
            result = deleteObject();
        }
        return result;
    }

    /**
     * Deletes the object.
     *
     * @return {@code true} if the delete was successfule
     */
    protected boolean deleteObject() {
        IMObject object = getObject();
        return object.isNew() || SaveHelper.delete(object, getLayoutContext().getDeletionListener());
    }

    /**
     * Deletes any child Deletable instances.
     *
     * @return {@code true} if the delete was successful
     */
    protected boolean deleteChildren() {
        for (Deletable deletable : editors.getDeletable()) {
            if (!deletable.delete()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the child editors.
     *
     * @return the child editors
     */
    protected Editors getEditors() {
        return editors;
    }

    /**
     * Report a bound property update to any registered listeners. No event is
     * fired if old and new are equal and non-null.
     *
     * @param name     the name of the property that was changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    protected void firePropertyChange(String name, Object oldValue,
                                      Object newValue) {
        if (propertyChangeNotifier != null) {
            propertyChangeNotifier.firePropertyChange(name, oldValue,
                                                      newValue);
        }
    }

    /**
     * Returns the view, creating it if it doesn't exist.
     *
     * @return the view
     */
    protected IMObjectView getView() {
        if (viewer == null) {
            viewer = createView(object);
        }
        return viewer;
    }

    /**
     * Creates an {@link IMObjectView} to render the object.
     *
     * @param object the object to view
     * @return a new object view
     */
    protected IMObjectView createView(IMObject object) {
        IMObjectLayoutStrategy layout = createLayoutStrategy();
        IMObjectView view;
        view = new AbstractIMObjectView(object, properties, parent, layout) {
            @Override
            protected Component createComponent() {
                disposeLookups();
                return super.createComponent();
            }

            protected LayoutContext getLayoutContext() {
                return context;
            }
        };
        view.setLayoutListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                onLayoutCompleted();
            }
        });
        if (layout instanceof ExpandableLayoutStrategy) {
            view.getComponent(); // make sure the component is rendered.
            ExpandableLayoutStrategy exp = (ExpandableLayoutStrategy) layout;
            Button button = exp.getButton();
            if (button != null) {
                button.addActionListener(getLayoutChangeListener());
            }
        }
        return view;
    }

    /**
     * Removes listeners associated with lookup nodes.
     */
    private void disposeLookups() {
        for (Map.Entry<PropertyEditor, ModifiableListener> entry : lookups.entrySet()) {
            entry.getKey().removeModifiableListener(entry.getValue());
        }
        lookups.clear();
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
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategyFactory layoutStrategy
            = context.getLayoutStrategyFactory();
        return layoutStrategy.create(getObject(), getParent());
    }

    /**
     * Change the layout.
     */
    protected void onLayout() {
        Component oldValue = getComponent();
        disposeOnChangeLayout();
        if (getView().getLayout() instanceof ExpandableLayoutStrategy) {
            ExpandableLayoutStrategy expandable = (ExpandableLayoutStrategy) getView().getLayout();
            expandable.setShowOptional(!expandable.isShowOptional());
            getView().setLayout(expandable);
            Button button = expandable.getButton();
            if (button != null) {
                button.addActionListener(getLayoutChangeListener());
            }
        } else {
            IMObjectLayoutStrategy layout = createLayoutStrategy();
            getView().setLayout(layout);
            if (layout instanceof ExpandableLayoutStrategy) {
                ExpandableLayoutStrategy exp = (ExpandableLayoutStrategy) layout;
                Button button = exp.getButton();
                if (button != null) {
                    button.addActionListener(getLayoutChangeListener());
                }
            }
        }
        Component newValue = getComponent();
        firePropertyChange(COMPONENT_CHANGED_PROPERTY, oldValue, newValue);
    }

    /**
     * Invoked by {@link #onLayout} to dispose of existing editors.
     * <p/>
     * This implementation disposes each editor for which {@link #disposeOnChangeLayout(Editor)} returns {@code true}.
     */
    protected void disposeOnChangeLayout() {
        Set<Editor> set = editors.getEditors();
        for (Editor editor : set.toArray(new Editor[set.size()])) {
            if (disposeOnChangeLayout(editor)) {
                editors.remove(editor);
                editor.dispose();
            }
        }
    }

    /**
     * Determines if an editor should be disposed on layout change.
     * This implementation always returns true.
     *
     * @param editor the editor
     * @return {@code true}
     */
    protected boolean disposeOnChangeLayout(Editor editor) {
        return true;
    }

    /**
     * Invoked when layout has completed.
     * <p/>
     * This can be used to perform processing that requires all editors to be created.
     */
    protected void onLayoutCompleted() {
    }

    /**
     * Invoked when any of the child editors or properties update.
     * <p/>
     * This resets the cached valid state
     *
     * @param modifiable the updated object
     */
    protected void onModified(Modifiable modifiable) {
        resetValid(false);
    }

    /**
     * Refreshes lookups that may be linked to the specified source lookup.
     *
     * @param source the source lookup
     */
    protected void refreshLookups(LookupField source) {
        for (PropertyEditor editor : lookups.keySet()) {
            LookupField lookup = (LookupField) editor.getComponent();
            if (source != lookup) {
                lookup.refresh();
            }
        }
    }

    /**
     * Returns the layout change action listener.
     *
     * @return the layout change listener
     */
    protected ActionListener getLayoutChangeListener() {
        if (layoutChangeListener == null) {
            layoutChangeListener = new ActionListener() {
                public void onAction(ActionEvent event) {
                    onLayout();
                }
            };
        }
        return layoutChangeListener;
    }

    /**
     * Helper to return a node descriptor from the archetype, given its name.
     *
     * @param name the node descriptor's name
     * @return the corresponding node descriptor, or {@code null} if it
     *         doesn't exist
     */
    protected NodeDescriptor getDescriptor(String name) {
        return getArchetypeDescriptor().getNodeDescriptor(name);
    }

    /**
     * Helper to return an editor associated with a property, given the property
     * name.
     * <p/>
     * This performs a layout of the component if it hasn't already been done,
     * to ensure the editors are created
     *
     * @param name the property name
     * @return the editor corresponding to {@code name} or }null} if
     *         none exists
     */
    protected Editor getEditor(String name) {
        return getEditor(name, true);
    }

    /**
     * Helper to return an editor associated with a property, given the property
     * name.
     *
     * @param name   the property name
     * @param create if {@code true} force creation of the edit components if
     *               it hasn't already been done
     * @return the editor corresponding to {@code name} or }null} if
     *         none exists or hasn't been created
     */
    protected Editor getEditor(String name, boolean create) {
        if (create) {
            // make sure the component has been laid out to ensure
            // the editors are created
            getComponent();
        }
        return editors.getEditor(name);
    }

    /**
     * Returns the listeners.
     *
     * @return the listeners
     */
    protected ModifiableListeners getListeners() {
        return listeners;
    }

    /**
     * Helper to return an object given its reference.
     * <p/>
     * This implementation uses the cache associated with the layout context.
     *
     * @param reference the reference. May be {@code null}
     * @return the object corresponding to {@code reference} or {@code null} if none exists
     */
    protected IMObject getObject(IMObjectReference reference) {
        return getLayoutContext().getCache().get(reference);
    }

    private class ComponentFactory extends NodeEditorFactory {

        /**
         * Construct a new {@code ComponentFactory}.
         *
         * @param context the layout context
         */
        public ComponentFactory(LayoutContext context) {
            super(editors, context);
        }

        /**
         * Returns a component to edit a lookup property.
         *
         * @param property the lookup property
         * @param context  the parent object
         * @return a component to edit the property
         */
        @Override
        protected Editor createLookupEditor(Property property,
                                            IMObject context) {
            Editor editor = super.createLookupEditor(property, context);
            final LookupField lookup = (LookupField) editor.getComponent();
            ModifiableListener listener = new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    refreshLookups(lookup);
                }
            };
            property.addModifiableListener(listener);
            lookups.put((PropertyEditor) editor, listener);
            return editor;
        }
    }
}
