package org.openvpms.web.component.im.edit;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.view.AbstractIMObjectView;
import org.openvpms.web.component.im.view.DefaultLayoutStrategyFactory;
import org.openvpms.web.component.im.view.IMObjectView;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.edit.ModifiableSet;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of the {@link IMObjectEditor} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectEditor
        implements IMObjectEditor {

    /**
     * The object being edited.
     */
    private final IMObject _object;

    /**
     * The object's descriptor.
     */
    private final ArchetypeDescriptor _archetype;

    /**
     * The parent object. May be <code>null</code>.
     */
    private final IMObject _parent;

    /**
     * The parent descriptor. May be <code>null</code>.
     */
    private final NodeDescriptor _descriptor;

    /**
     * The object viewer.
     */
    private IMObjectView _viewer;

    /**
     * The change tracker.
     */
    private ModifiableSet _modifiable;

    /**
     * The component factory.
     */
    private NodeEditorFactory _factory;

    /**
     * Lookup fields. These may beed to be refreshed.
     */
    private List<SelectField> _lookups = new ArrayList<SelectField>();

    /**
     * The layout strategy factory.
     */
    private IMObjectLayoutStrategyFactory _layoutFactory;

    /**
     * If <code>true</code> show required and optional fields.
     */
    private boolean _showAll;

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
     * Property change listener notifier.
     */
    private PropertyChangeSupport _propertyChangeNotifier;


    /**
     * Construct a new <code>DefaultIMObjectEditor</code> for an object that
     * belongs to a collection.
     *
     * @param object     the object to edit
     * @param parent     the parent object
     * @param descriptor the parent descriptor
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     */
    public AbstractIMObjectEditor(IMObject object, IMObject parent,
                                  NodeDescriptor descriptor, boolean showAll) {
        _object = object;
        _parent = parent;
        _descriptor = descriptor;
        _showAll = showAll;
        _archetype = DescriptorHelper.getArchetypeDescriptor(object);
        _modifiable = new ModifiableSet();
        _factory = new ComponentFactory(_modifiable);
        _layoutFactory = new DefaultLayoutStrategyFactory();
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
        return _archetype.getDisplayName();
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
        boolean saved;
        if (!isModified()) {
            saved = true;
        } else {
            saved = doSave();
            if (saved) {
                _saved = true;
                clearModified();
            }
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
        boolean deleted = false;
        IMObject object = getObject();
        if (_parent != null) {
            try {
                _descriptor.removeChildFromCollection(_parent, object);
                deleted = true;
            } catch (DescriptorException exception) {
                ErrorDialog.show(exception);
            }
        } else if (!object.isNew()) {
            try {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                service.remove(object);
                deleted = true;
            } catch (ArchetypeServiceException exception) {
                ErrorDialog.show(exception);
            }
        }
        _deleted |= deleted;
        return deleted;
    }

    /**
     * Cancel any edits.
     */
    public void cancel() {
    }

    /**
     * Determines if the object has been changed.
     *
     * @return <code>true</code> if the object has been changed
     */
    public boolean isModified() {
        return _modifiable.isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        _modifiable.clearModified();
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
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    public Component getComponent() {
        return getView().getComponent();
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
        if (saveChildren()) {
            saved = saveObject();
        }
        return saved;
    }

    /**
     * Save any modified child Saveable instances.
     *
     * @return <code>true</code> if the save was successful
     */
    protected boolean saveChildren() {
        for (Saveable saveable : _modifiable.getModifiedSaveable()) {
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
        return SaveHelper.save(object, _parent, _descriptor);
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
        IMObjectLayoutStrategy layout = createLayoutStrategy(_showAll);
        IMObjectView view = new AbstractIMObjectView(object, layout) {
            @Override
            protected Component createComponent() {
                _lookups.clear();
                return super.createComponent();
            }

            protected IMObjectComponentFactory getComponentFactory() {
                return _factory;
            }
        };
        view.getComponent(); // make sure the component is rendered.
        if (layout instanceof ExpandableLayoutStrategy) {
            ExpandableLayoutStrategy exp = (ExpandableLayoutStrategy) layout;
            Button button = exp.getButton();
            if (button != null) {
                button.addActionListener(getLayoutChangeListener());
            }
        }
        return view;
    }

    /**
     * Creates the layout strategy.
     *
     * @param showAll if <code>true</code> show required and optional fields;
     *                otherwise show required fields.
     * @return a new layout strategy
     */
    protected IMObjectLayoutStrategy createLayoutStrategy(boolean showAll) {
        return _layoutFactory.create(getObject(), showAll);
    }

    /**
     * Change the layout.
     */
    protected void onLayout() {
        _showAll = !_showAll;
        Component oldValue = getComponent();
        IMObjectLayoutStrategy layout = createLayoutStrategy(_showAll);
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

    private class ComponentFactory extends NodeEditorFactory {

        /**
         * Construct a new <code>ComponentFactory</code>.
         *
         * @param modifiable the modification tracker
         */
        public ComponentFactory(ModifiableSet modifiable) {
            super(modifiable);
        }

        /**
         * Returns a component to edit a lookup node.
         *
         * @param object     the parent object
         * @param descriptor the node descriptor
         * @return a component to edit the node
         */
        @Override
        protected Component getSelectEditor(IMObject object,
                                            NodeDescriptor descriptor) {
            Component editor = super.getSelectEditor(object, descriptor);
            SelectField lookup = (SelectField) editor;
            lookup.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    refreshLookups((SelectField) event.getSource());
                }
            });
            _lookups.add(lookup);
            return lookup;
        }
    }
}
