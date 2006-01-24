package org.openvpms.web.app.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.GridFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.list.LookupListModel;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Editor for {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class IMObjectEditor extends Column implements Editor {

    /**
     * The object to edit.
     */
    private IMObject _object;

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
     * The logger.
     */
    private final Log _log = LogFactory.getLog(IMObjectEditor.class);

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
        _object = object;
        _parent = parent;
        _descriptor = descriptor;
        doLayout();
    }

    /**
     * Returns a title for the editor.
     *
     * @return a title for the editor
     */
    public String getTitle() {
        ArchetypeId archId = _object.getArchetypeId();
        String conceptName;

        if (archId == null) {
            conceptName = _object.getClass().getName();
        } else {
            conceptName = archId.getConcept();
        }

        if (_object.isNew()) {
            return "New " + conceptName;
        } else {
            return "Edit " + conceptName;
        }
    }

    /**
     * Returns the editing component.
     *
     * @return the editing component
     */
    public Component getComponent() {
        return this;
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
        boolean saved = false;
        IArchetypeService service = ServiceHelper.getArchetypeService();
        if (doValidation()) {
            try {
                if (_parent == null) {
                    service.save(_object);
                    saved = true;
                } else {
                    if (_object.isNew()) {
                        _descriptor.addChildToCollection(_parent, _object);
                        saved = true;
                    } else if (_parent != null && !_parent.isNew()) {
                        service.save(_object);
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
        if (_parent != null) {
            _descriptor.removeChildFromCollection(_parent, _object);
            deleted = true;
        } else if (!_object.isNew()) {
            try {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                service.remove(_object);
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
        _object = null;
        _parent = null;
        _descriptor = null;
    }

    protected void doLayout() {
        setStyleName("Editor");
        ILookupService service = ServiceHelper.getLookupService();
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(_object);
        List<NodeDescriptor> descriptors = descriptor.getSimpleNodeDescriptors();
        if (!descriptors.isEmpty()) {
            Grid grid = GridFactory.create(2);
            for (NodeDescriptor nodeDesc : descriptors) {
                if (!nodeDesc.isHidden()) {
                    Label label = LabelFactory.create();
                    label.setText(nodeDesc.getDisplayName());
                    Component editor = NodeEditorFactory.create(_object, nodeDesc,
                            service);
                    if (editor instanceof SelectField) {
                        SelectField lookup = (SelectField) editor;
                        lookup.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent event) {
                                refreshLookups((SelectField) event.getSource());
                            }
                        });
                        _lookups.add(lookup);
                    }
                    grid.add(label);
                    grid.add(editor);
                }
            }
            add(grid);
        }
        descriptors = descriptor.getComplexNodeDescriptors();
        if (!descriptors.isEmpty()) {
            DefaultTabModel model = new DefaultTabModel();
            for (NodeDescriptor nodeDesc : descriptors) {
                Component editor = NodeEditorFactory.create(_object, nodeDesc,
                        service);
                model.addTab(nodeDesc.getDisplayName(), editor);
            }
            TabbedPane pane = new TabbedPane();
            pane.setModel(model);
            pane.setSelectedIndex(0);
            add(pane);
        }
    }

    /**
     * Returns the archetype descriptor for an object.
     * <p/>
     * TODO At the moment we have the logic to determine whether the descriptor
     * is an AssertionTypeDescriptor and then switch accordingly in this object.
     * This needs to be transparent
     *
     * @param object the object
     * @return the archetype descriptor for <code>object</code>
     */
    protected ArchetypeDescriptor getArchetypeDescriptor(IMObject object) {
        ArchetypeDescriptor descriptor;
        ArchetypeId archId = object.getArchetypeId();
        IArchetypeService service = ServiceHelper.getArchetypeService();

        //TODO This is a work around until we resolve the current
        // problem with archetyping and archetype. We need to
        // extend this page and create a new archetype specific
        // edit page.
        if (object instanceof AssertionDescriptor) {
            AssertionTypeDescriptor atDesc = service.getAssertionTypeDescriptor(
                    _object.getName());
            archId = new ArchetypeId(atDesc.getPropertyArchetype());
        }

        descriptor = service.getArchetypeDescriptor(archId);
        if (_log.isDebugEnabled()) {
            _log.debug("Returning archetypeDescriptor="
                    + (descriptor == null ? null : descriptor.getName())
                    + " for archId=" + archId
                    + " and object=" + object.getClass().getName());
        }

        if (descriptor == null) {
            descriptor = service.getArchetypeDescriptor(
                    _object.getArchetypeId().getShortName());
        }
        return descriptor;
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

        // check that we can retrieve a valid archetype for this object
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(_object);

        // if there are nodes attached to the archetype then validate the
        // associated assertions
        if (!descriptor.getNodeDescriptors().isEmpty()) {
            JXPathContext context = JXPathContext.newContext(_object);
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
