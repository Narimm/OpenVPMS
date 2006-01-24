package org.openvpms.web.component.im;

import java.util.List;

import echopointng.GroupBox;
import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.GridFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.util.DescriptorHelper;


/**
 * Default implementation of the {@link IMObjectLayoutStrategy} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * Apply the layout strategy.
     *
     * @param object  the object to apply
     * @param factory the component factory
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject object, IMObjectComponentFactory factory) {
        Component component = new Column();
        ArchetypeDescriptor descriptor
                = DescriptorHelper.getArchetypeDescriptor(object);
        List<NodeDescriptor> descriptors = descriptor.getSimpleNodeDescriptors();
        if (!descriptors.isEmpty()) {
            Grid grid = GridFactory.create(2);
            for (NodeDescriptor nodeDesc : descriptors) {
                if (!nodeDesc.isHidden()) {
                    Label label = LabelFactory.create();
                    label.setText(nodeDesc.getDisplayName());
                    Component child = factory.create(object, nodeDesc);
                    grid.add(label);
                    grid.add(child);
                }
            }
            component.add(grid);
        }
        descriptors = descriptor.getComplexNodeDescriptors();
        if (!descriptors.isEmpty()) {
            DefaultTabModel model = new DefaultTabModel();
            for (NodeDescriptor nodeDesc : descriptors) {
                Component child = factory.create(object, nodeDesc);
                model.addTab(nodeDesc.getDisplayName(), child);
            }
            TabbedPane pane = new TabbedPane();
            pane.setModel(model);
            pane.setSelectedIndex(0);
            component.add(pane);
        }
        GroupBox box = new GroupBox();
        box.add(component);
        return box;
    }

}
