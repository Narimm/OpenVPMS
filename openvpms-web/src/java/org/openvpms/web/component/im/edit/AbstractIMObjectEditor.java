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

import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.edit.Validator;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.view.AbstractIMObjectView;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.IMObjectView;
import org.openvpms.web.component.im.view.layout.DefaultLayoutStrategyFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link IMObjectEditor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectEditor
        implements IMObjectEditor {

    /**
     * The object being edited.
     */
    private final IMObject _object;

    /**
     * The parent object. May be <code>null</code>.
     */
    private final IMObject _parent;

    /**
     * The object's descriptor.
     */
    private final ArchetypeDescriptor _archetype;

    /**
     * The object viewer.
     */
    private IMObjectView _viewer;

    /**
     * The child editors.
     */
    private Editors _editors = new Editors();

    /**
     * The object properties.
     */
    private PropertySet _properties;

    /**
     * Lookup fields. These may beed to be refreshed.
     */
    private List<SelectField> _lookups = new ArrayList<SelectField>();

    /**
     * The layout strategy factory.
     */
    private IMObjectLayoutStrategyFactory _layoutFactory;

    /**
     * The layout context.
     */
    private LayoutContext _context;

    /**
     * Action listener for layout changes.
     */
    private ActionListener _layoutChangeListener;

    /**
     * Indicates if the object has been saved.
     */
    private boolean _saved = false;

    /**
     * Indicates if the object was deleted.
     */
    private boolean _deleted = false;

    /**
     * Indicates if editing was cancelled.
     */
    private boolean _cancelled = false;

    /**
     * Property change listener notifier.
     */
    private PropertyChangeSupport _propertyChangeNotifier;

    /**
     * Listener to update derived fields.
     */
    private ModifiableListener _derivedFieldRefresher;


    /**
     * Construct a new <code>AbstractIMObjectEditor</code>.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>.
     */
    public AbstractIMObjectEditor(IMObject object, IMObject parent,
                                  LayoutContext context) {
        _object = object;
        _parent = parent;

        if (context == null) {
            _context = new DefaultLayoutContext(true);
        } else {
            _context = new DefaultLayoutContext(context);
        }

        _archetype = DescriptorHelper.getArchetypeDescriptor(object);
        _properties = new PropertySet(object, _archetype);
        IMObjectComponentFactory factory = new ComponentFactory(_context);
        _context.setComponentFactory(factory);

        _layoutFactory = new DefaultLayoutStrategyFactory();
        _derivedFieldRefresher = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateDerivedFields(modifiable);
            }
        };
        _editors.addModifiableListener(_derivedFieldRefresher);
    }

    /**
     * Returns a title for the editor.
     *
     * @return a title for the editor
     */
    public String getTitle() {
        String title;
        if (_object.isNew()) {
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
        return _object;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object. May be <code>null</code>
     */
    public IMObject getParent() {
        return _parent;
    }

    /**
     * Returns the archetype descriptor of the object.
     *
     * @return the object's archetype descriptor
     */
    public ArchetypeDescriptor getArchetypeDescriptor() {
        return _archetype;
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    public boolean save() {
        if (_cancelled) {
            return false;
        }
        boolean saved = false;
        Validator validator = new Validator();
        validator.validate(this);
        if (validator.isValid()) {
            if (!isModified()) {
                saved = true;
            } else {
                saved = doSave();
                if (saved) {
                    _saved = true;
                    clearModified();
                }
            }
        } else {
            ValidationHelper.showError(validator);
        }
        return saved;
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return <code>true</code> if edits have been saved.
     */
    public boolean isSaved() {
        return _saved;
    }

    /**
     * Delete the current object.
     *
     * @return <code>true</code> if the object was deleted successfully
     */
    public boolean delete() {
        if (_cancelled) {
            return false;
        }
        boolean deleted = false;
        IMObject object = getObject();
        if (object.isNew()) {
            deleted = true;
        } else {
            try {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                service.remove(object);
                deleted = true;
            } catch (OpenVPMSException exception) {
                String title = Messages.get("imobject.delete.failed.title");
                ErrorHelper.show(title, exception);
            }
        }
        _deleted |= deleted;
        return deleted;
    }

    /**
     * Determines if the object has been deleted.
     *
     * @return <code>true</code> if the object has been deleted
     */
    public boolean isDeleted() {
        return _deleted;
    }

    /**
     * Determines if the object has been changed.
     *
     * @return <code>true</code> if the object has been changed
     */
    public boolean isModified() {
        return _editors.isModified() || getObject().isNew();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        _editors.clearModified();
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        _editors.addModifiableListener(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        _editors.removeModifiableListener(listener);
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        return _editors.isValid();
    }

    /**
     * Validates the object.
     *
     * @param validator thhe validator
     */
    public boolean validate(Validator validator) {
        return validator.validate(_editors);
    }

    /**
     * Cancel any edits. Once complete, query methods may be invoked, but the
     * behaviour of other methods is undefined..
     */
    public void cancel() {
        _cancelled = true;
    }

    /**
     * Determines if editing was cancelled.
     *
     * @return <code>true</code> if editing was cancelled
     */
    public boolean isCancelled() {
        return _cancelled;
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
     * @return the focus group
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
        if (_propertyChangeNotifier == null) {
            _propertyChangeNotifier = new PropertyChangeSupport(this);
        }
        _propertyChangeNotifier.addPropertyChangeListener(name, listener);
    }

    /**
     * Remove a property change listener.
     *
     * @param name     the property name to remove the listener for
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(String name,
                                             PropertyChangeListener listener) {
        if (_propertyChangeNotifier != null) {
            _propertyChangeNotifier.removePropertyChangeListener(
                    name, listener);
        }
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    protected boolean doSave() {
        boolean saved = false;
        if (saveObject()) {
            saved = saveChildren();
        }
        return saved;
    }

    /**
     * Save any modified child Saveable instances.
     *
     * @return <code>true</code> if the save was successful
     */
    protected boolean saveChildren() {
        for (Saveable saveable : _editors.getModifiedSaveable()) {
            if (!saveable.save()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Save the object.
     *
     * @return <code>true</code> if the save was successful
     */
    protected boolean saveObject() {
        IMObject object = getObject();
        return SaveHelper.save(object);
    }

    /**
     * Returns the child editors.
     *
     * @return the child editors
     */
    protected Editors getEditors() {
        return _editors;
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
        if (_propertyChangeNotifier != null) {
            _propertyChangeNotifier.firePropertyChange(name, oldValue,
                                                       newValue);
        }
    }

    /**
     * Returns the view, creating it if it doesn't exist.
     *
     * @return the view
     */
    protected IMObjectView getView() {
        if (_viewer == null) {
            _viewer = createView(_object);
        }
        return _viewer;
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
        view = new AbstractIMObjectView(object, _properties, layout) {
            @Override
            protected Component createComponent() {
                _lookups.clear();
                return super.createComponent();
            }

            protected LayoutContext getLayoutContext() {
                return _context;
            }
        };
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
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return _context;

    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return _layoutFactory.create(getObject());
    }

    /**
     * Change the layout.
     */
    protected void onLayout() {
        Component oldValue = getComponent();
        IMObjectLayoutStrategy layout = createLayoutStrategy();
        getView().setLayout(layout);
        if (layout instanceof ExpandableLayoutStrategy) {
            ExpandableLayoutStrategy exp = (ExpandableLayoutStrategy) layout;
            Button button = exp.getButton();
            if (button != null) {
                button.addActionListener(getLayoutChangeListener());
            }
        }
        Component newValue = getComponent();
        firePropertyChange(COMPONENT_CHANGED_PROPERTY, oldValue, newValue);
    }

    protected void updateDerivedFields(Modifiable modified) {
        if (modified instanceof Property) {
            _editors.removeModifiableListener(_derivedFieldRefresher);
            IArchetypeService service = ServiceHelper.getArchetypeService();
            service.deriveValues(getObject());

            for (Property property : _properties.getProperties()) {
                if (modified != property
                        && property.getDescriptor().isDerived()) {
                    property.refresh();
                }
            }
            _editors.addModifiableListener(_derivedFieldRefresher);
        }
    }

    protected void refreshLookups(SelectField source) {
        for (SelectField lookup : _lookups) {
            if (source != lookup) {
                LookupListModel model = (LookupListModel) lookup.getModel();
                model.refresh();
            }
        }
    }

    /**
     * Returns the layout change action listener.
     *
     * @return the layout change listener
     */
    protected ActionListener getLayoutChangeListener() {
        if (_layoutChangeListener == null) {
            _layoutChangeListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onLayout();
                }
            };
        }
        return _layoutChangeListener;
    }

    /**
     * Helper to return a node descriptor from the archetype, given its name.
     *
     * @param name the node descriptor's name
     * @return the corresponding node descriptor, or <code>null</code> if it
     *         doesn't exist
     */
    protected NodeDescriptor getDescriptor(String name) {
        return getArchetypeDescriptor().getNodeDescriptor(name);
    }

    /**
     * Helper to return a property, given its descriptor's name.
     *
     * @param name the descriptor's name
     * @return the property corresponding to <code>name</code> or
     *         <code>null</code> if none exists
     */
    protected Property getProperty(String name) {
        return _properties.get(name);
    }

    /**
     * Helper to return an editor associated with a property, given the property
     * name.
     *
     * @param name the property name
     * @return the editor corresponding to <code>name</code> or
     *         </code>null</code> if none exists
     */
    protected Editor getEditor(String name) {
        return _editors.getEditor(name);
    }


    private class ComponentFactory extends NodeEditorFactory {

        /**
         * Construct a new <code>ComponentFactory</code>.
         *
         * @param context the layout context
         */
        public ComponentFactory(LayoutContext context) {
            super(_editors, context);
        }

        /**
         * Returns a component to edit a lookup property.
         *
         * @param property the lookup property
         * @param context  the parent object
         * @return a component to edit the property
         */
        @Override
        protected Editor getSelectEditor(Property property,
                                         IMObject context) {
            Editor editor = super.getSelectEditor(property, context);
            SelectField lookup = (SelectField) editor.getComponent();
            lookup.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    refreshLookups((SelectField) event.getSource());
                }
            });
            _lookups.add(lookup);
            return editor;
        }
    }
}
