package org.openvpms.web.component.edit;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.DescriptorHelper;
import org.openvpms.web.util.Messages;


/**
 * Abstract implementation of the {@link IMObjectEditor} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectEditor implements IMObjectEditor {

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
     * Indicates if the object was saved.
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
     */
    public AbstractIMObjectEditor(IMObject object, IMObject parent,
                                  NodeDescriptor descriptor) {
        _object = object;
        _parent = parent;
        _descriptor = descriptor;
        _archetype = ServiceHelper.getArchetypeService().getArchetypeDescriptor(
                object.getArchetypeId());
    }

    /**
     * Returns a title for the editor.
     *
     * @return a title for the editor
     */
    public String getTitle() {
        String title;
        String name = _archetype.getDisplayName();

        if (_object.isNew()) {
            title = Messages.get("editor.new.title", name);
        } else {
            title = Messages.get("editor.edit.title", name);
        }
        return title;
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
        IMObject object = getObject();
        boolean saved = false;
        IArchetypeService service = ServiceHelper.getArchetypeService();
        if (doValidation()) {
            try {
                if (_parent == null) {
                    service.save(object);
                    saved = true;
                } else {
                    if (object.isNew()) {
                        _descriptor.addChildToCollection(_parent, object);
                        saved = true;
                    } else if (_parent != null && !_parent.isNew()) {
                        service.save(object);
                        saved = true;
                    } else {
                        // new parent, new child. Parent must be saved first.
                        // Not a failure, so return true.
                        saved = true;
                    }
                }
            } catch (RuntimeException exception) {
                ErrorDialog.show(exception);
            }
        }
        _saved |= saved;
        return saved;
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
            _descriptor.removeChildFromCollection(_parent, object);
            deleted = true;
        } else if (!object.isNew()) {
            try {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                service.remove(object);
                deleted = true;
            } catch (RuntimeException exception) {
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
        return _saved;
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
     * Add a property change listener.
     *
     * @param name     the property name to listen on
     * @param listener the listener
     */
    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        if (_propertyChangeNotifier == null) {
            _propertyChangeNotifier = new PropertyChangeSupport(this);
        }
        _propertyChangeNotifier.addPropertyChangeListener(name, listener);
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

    protected boolean doValidation() {
        List<ValidationError> errors = new ArrayList<ValidationError>();
        IMObject object = getObject();

        // check that we can retrieve a valid archetype for this object
        ArchetypeDescriptor descriptor
                = DescriptorHelper.getArchetypeDescriptor(object);

        // if there are nodes attached to the archetype then validate the
        // associated assertions
        if (!descriptor.getNodeDescriptors().isEmpty()) {
            JXPathContext context = JXPathContext.newContext(object);
            context.setLenient(true);
            validateObject(context, descriptor.getNodeDescriptors(), errors);
        }
        boolean valid = errors.isEmpty();
        if (!valid) {
            ValidationError error = errors.get(0);
            ErrorDialog.show("Error: " + error.getNodeName(),
                    errors.get(0).getErrorMessage());
        }
        return valid;
    }

    /**
     * Iterate through all the nodes and ensure that the object meets all the
     * specified assertions. The assertions are defined in the node and can be
     * hierarchical, which means that this method is re-entrant.
     *
     * @param context holds the object to be validated
     * @param nodes   assertions are managed by the nodes object
     * @param errors  the errors are collected in this object
     */
    private void validateObject(JXPathContext context, Map<String, NodeDescriptor> nodes,
                                List<ValidationError> errors) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        for (NodeDescriptor node : nodes.values()) {
            Object value = null;
            try {
                value = context.getValue(node.getPath());
            } catch (Exception ignore) {
                // ignore since context.setLenient doesn't
                // seem to be working.
                // TODO Need to sort out a better way since this
                // can also cause problems
            }

            // first check whether the value for this node is derived and if it
            // is then derive the value
            if (node.isDerived()) {
                try {
                    value = context.getValue(node.getDerivedValue());
                    context.getPointer(node.getPath()).setValue(value);
                } catch (Exception exception) {
                    value = null;
                    errors.add(new ValidationError(node.getName(),
                            "Cannot derive value"));
                }
            }

            // check the cardinality
            int minCardinality = node.getMinCardinality();
            int maxCardinality = node.getMaxCardinality();
            if ((minCardinality == 1) && (value == null)) {
                errors.add(new ValidationError(node.getName(),
                        "value is required"));
            }

            // if the node is a collection and there are cardinality
            // constraints then check them
            if (node.isCollection()) {
                if ((minCardinality > 0) && (getCollectionSize(value) < minCardinality))
                {
                    errors.add(new ValidationError(node.getName(),
                            " must supply at least " + minCardinality + " "
                                    + node.getBaseName()));

                }

                if ((maxCardinality > 0)
                        && (maxCardinality != NodeDescriptor.UNBOUNDED)
                        && (getCollectionSize(value) > maxCardinality)) {
                    errors.add(new ValidationError(node.getName(),
                            " cannot supply more than " + maxCardinality + " "
                                    + node.getBaseName()));
                }
            }

            if ((value != null)
                    && (node.getAssertionDescriptorsAsArray().length > 0)) {
                // only check the assertions for non-null values
                for (AssertionDescriptor assertion : node
                        .getAssertionDescriptorsAsArray()) {
                    AssertionTypeDescriptor assertionType =
                            service.getAssertionTypeDescriptor(assertion.getName());

                    // TODO
                    // no validation required where the type is not specified.
                    // This is currently a work around since we need to deal
                    // with assertions and some other type of declaration...
                    // which I don't have a name for.
                    if (assertionType.getActionType("assert") == null) {
                        continue;
                    }

                    try {
                        if (!assertionType.assertTrue(value, node, assertion)) {
                            errors.add(new ValidationError(node.getName(),
                                    assertion.getErrorMessage()));
                        }
                    } catch (Exception exception) {
                        // log the error
                        errors.add(new ValidationError(node.getName(),
                                assertion.getErrorMessage()));
                    }
                }
            }

            // if this node has other nodes then re-enter this method
            if (node.getNodeDescriptors().size() > 0) {
                validateObject(context, node.getNodeDescriptors(), errors);
            }
        }
    }

    /**
     * Determine the number of entries in the collection. If the collection is
     * null then return 0. The node descriptor defines the type of the
     * collection
     *
     * @param collection the collection item
     * @return the collection size
     */
    private int getCollectionSize(Object collection) {
        int size;
        if (collection instanceof Map) {
            size = ((Map) collection).size();
        } else if (collection instanceof Collection) {
            size = ((Collection) collection).size();
        } else {
            size = 0;
        }
        return size;
    }
}
