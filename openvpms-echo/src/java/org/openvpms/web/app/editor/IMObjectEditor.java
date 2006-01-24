package org.openvpms.web.app.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.jxpath.JXPathContext;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.DefaultLayoutStrategy;
import org.openvpms.web.component.im.IMObjectComponentFactory;
import org.openvpms.web.component.im.IMObjectViewer;
import org.openvpms.web.component.list.LookupListModel;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.DescriptorHelper;


/**
 * Editor for {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class IMObjectEditor extends IMObjectViewer implements Editor {

    /**
     * The component factory.
     */
    private IMObjectComponentFactory _factory = new NodeEditorFactory();

    /**
     * The parent object. May be <code>null</code>.
     */
    private IMObject _parent;

    /**
     * The parent descriptor. May be <code>null</code>.
     */
    private NodeDescriptor _descriptor;

    /**
     * Lookup fields. These may beed to be refreshed.
     */
    private List<SelectField> _lookups = new ArrayList<SelectField>();

    /**
     * Indicates if the object was saved.
     */
    private boolean _saved = false;

    /**
     * Indicates if the object was deleted.
     */
    private boolean _deleted = false;


    /**
     * Construct a new <code>IMObjectEditor</code>.
     *
     * @param object the object to edit
     */
    public IMObjectEditor(IMObject object) {
        this(object, null, null);
    }

    /**
     * Construct a new <code>IMObjectEditor</code> for an object that belongs to
     * a collection.
     *
     * @param object     the object to edit
     * @param parent     the parent object.
     * @param descriptor the parent descriptor.
     */
    public IMObjectEditor(IMObject object, IMObject parent, NodeDescriptor descriptor) {
        super(object, new DefaultLayoutStrategy());
        _parent = parent;
        _descriptor = descriptor;
    }

    /**
     * Returns a title for the editor.
     *
     * @return a title for the editor
     */
    public String getTitle() {
        IMObject object = getObject();
        ArchetypeId archId = object.getArchetypeId();
        String conceptName;

        if (archId == null) {
            conceptName = object.getClass().getName();
        } else {
            conceptName = archId.getConcept();
        }

        if (object.isNew()) {
            return "New " + conceptName;
        } else {
            return "Edit " + conceptName;
        }
    }

    /**
     * Determines if the object has been changed.
     *
     * @return <code>true</code> if the object has been modified
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
     * Create a new object.
     */
    public void create() {

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
        _parent = null;
        _descriptor = null;
    }

    /**
     * Returns the factory for creating components for displaying the object.
     *
     * @return the component factory
     */
    protected IMObjectComponentFactory getComponentFactory() {
        return new IMObjectComponentFactory() {
            public Component create(IMObject object, NodeDescriptor descriptor) {
                return createComponent(object, descriptor);
            }

        };
    }

    private Component createComponent(IMObject object,
                                      NodeDescriptor descriptor) {
        Component editor = _factory.create(object, descriptor);
        if (editor instanceof SelectField) {
            SelectField lookup = (SelectField) editor;
            lookup.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    refreshLookups((SelectField) event.getSource());
                }
            });
            _lookups.add(lookup);
        }
        return editor;
    }

    protected void refreshLookups(SelectField source) {
        for (SelectField lookup : _lookups) {
            if (source != lookup) {
                LookupListModel model = (LookupListModel) lookup.getModel();
                model.refresh();
            }
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
