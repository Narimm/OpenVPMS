package org.openvpms.web.component.im.layout;

import java.util.Collection;
import java.util.List;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.filter.BasicNodeFilter;


/**
 * {@link IMObjectLayoutStrategy} that lays out {@link IMObject} instances on a
 * single page.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class SinglePageLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param summary if <code>true</code>, only display a summary
     */
    public SinglePageLayoutStrategy(boolean summary) {
        super(new BasicNodeFilter(!summary, false));
    }

    /**
     * Lays out each child component in a group box.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param factory     the component factory
     */
    protected void doComplexLayout(IMObject object,
                                   List<NodeDescriptor> descriptors,
                                   Component container,
                                   IMObjectComponentFactory factory) {
        for (NodeDescriptor node : descriptors) {
            GroupBox box = new GroupBox(node.getDisplayName());
            Collection values = (Collection) node.getValue(object);
            for (Object value : values) {
                doLayout((IMObject) value, box, factory);
            }
            container.add(box);
        }
    }


}
