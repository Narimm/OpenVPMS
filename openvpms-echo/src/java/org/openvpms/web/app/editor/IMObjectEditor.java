package org.openvpms.web.app.editor;

import java.util.List;

import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.Editor;
import org.openvpms.web.component.GridFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.app.OpenVPMSApp;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
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
     * Construct a new <code>IMObjectEditor</code> for an object that belongs to a collection.
     *
     * @param object the object to edit
     * @param parent the parent object.
     * @param descriptor the parent descriptor.
     */
    public IMObjectEditor(IMObject object, IMObject parent, NodeDescriptor descriptor) {
        _object = object;
        _parent = parent;
        _descriptor = descriptor;
        setStyleName("Editor");
    }

    public void init() {
        super.init();
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
    public Object getObject() {
        return _object;
    }

    /**
     * Save any edits.
     */
    public void save() {
        IArchetypeService service = getArchetypeService();
        boolean isNew = _object.isNew();
        service.save(_object);
        if (isNew && _parent != null) {
            _descriptor.addChildToCollection(_parent, _object);
        }
    }

    /**
     * Delete the current object.
     */
    public void delete() {
        IArchetypeService service = getArchetypeService();
        service.remove(_object);
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
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(_object);
        List<NodeDescriptor> descriptors = descriptor.getSimpleNodeDescriptors();
        if (!descriptors.isEmpty()) {
            Grid grid = GridFactory.create(2);
            for (NodeDescriptor nodeDesc : descriptors) {
                if (!nodeDesc.isHidden()) {
                    Label label = LabelFactory.create();
                    label.setText(nodeDesc.getDisplayName());
                    Component editor = NodeEditorFactory.create(_object, nodeDesc,
                            getLookupService());
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
                        getLookupService());
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
     * TODO At the moment we have the logic to determine whether the descriptor is an AssertionTypeDescriptor and then
     * switch accordingly in this object. This needs to be transparent
     *
     * @param object the object
     * @return the archetype descriptor for <code>object</code>
     */
    protected ArchetypeDescriptor getArchetypeDescriptor(IMObject object) {
        ArchetypeDescriptor descriptor;
        ArchetypeId archId = object.getArchetypeId();
        IArchetypeService service = getArchetypeService();

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

    protected IArchetypeService getArchetypeService() {
        return (IArchetypeService) OpenVPMSApp.getInstance().getApplicationContext().getBean(
                "archetypeService");
    }

    protected ILookupService getLookupService() {
        return (ILookupService) OpenVPMSApp.getInstance().getApplicationContext().getBean(
                "lookupService");
    }

}
