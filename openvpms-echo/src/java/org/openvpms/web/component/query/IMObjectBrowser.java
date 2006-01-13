package org.openvpms.web.component.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import echopointng.GroupBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.GridFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class IMObjectBrowser extends Column {

    /**
     * The object to browse.
     */
    private final IMObject _object;

    /**
     * Construct a new <code>IMObjectBrowser</code>.
     *
     * @param object the object to browse.
     */
    public IMObjectBrowser(IMObject object) {
        _object = object;
        doLayout();
    }

    /**
     * Lay out the component.
     */
    protected void doLayout() {
        doLayout(_object, this);
    }

    /**
     * Lay out out the object in the specified container.
     */
    protected void doLayout(IMObject object, Component container) {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor(object);
        doSimpleLayout(object, descriptor.getSimpleNodeDescriptors(), container);
        doComplexLayout(object, descriptor.getComplexNodeDescriptors(), container);
    }


    private void doSimpleLayout(IMObject object,
                                List<NodeDescriptor> descriptors,
                                Component container) {
        Grid grid = GridFactory.create(2);
        for (NodeDescriptor node : descriptors) {
            if (!node.isHidden() && node.isRequired()) {
                Label label = LabelFactory.create();
                label.setText(node.getDisplayName());
                Component browser = NodeBrowserFactory.create(object, node);
                grid.add(label);
                grid.add(browser);
            }
        }
        container.add(grid);
    }

    private void doComplexLayout(IMObject object, List<NodeDescriptor> descriptors,
                                 Component container) {
        for (NodeDescriptor node : descriptors) {
            if (!node.isHidden() && node.isRequired()) {
                Collection values = null;
                GroupBox box = new GroupBox(node.getDisplayName());
                Object collection = node.getValue(object);
                if (collection instanceof Collection) {
                    values = (Collection) collection;
                } else if (collection instanceof Map) {
                    values = ((Map) collection).values();
                } else if (collection instanceof PropertyList) {
                    values = ((PropertyList) collection).values();
                }
                for (Object value : values) {
                    doLayout((IMObject) value, box);
                }
                container.add(box);
            }
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
        if (descriptor == null) {
            descriptor = service.getArchetypeDescriptor(
                    _object.getArchetypeId().getShortName());
        }
        return descriptor;
    }

}
