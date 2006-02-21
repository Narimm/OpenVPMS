package org.openvpms.web.component.im.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import echopointng.table.DefaultPageableSortableTableModel;
import echopointng.table.PageableSortableTable;
import echopointng.table.SortableTableColumn;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.util.DescriptorHelper;


/**
 * Layout strategy that renders objects in a table.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class TableLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The collection descriptor.
     */
    private final NodeDescriptor _descriptor;


    /**
     * Construct a new <code>TableLayoutStrategy</code>.
     */
    public TableLayoutStrategy(NodeDescriptor descriptor) {
        this(descriptor, null);
    }

    /**
     * Construct a new <code>TableLayoutStrategy</code>.
     *
     * @param filter the node filter. May be <code>null</code>.
     */
    public TableLayoutStrategy(NodeDescriptor descriptor, NodeFilter filter) {
        super(filter);
        _descriptor = descriptor;
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param context the context
     * @param factory the component factory
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject context, IMObjectComponentFactory factory) {
        String[] range = _descriptor.getArchetypeRange();
        ArchetypeDescriptor descriptor
                = DescriptorHelper.getArchetypeDescriptor(range[0]);
        List<NodeDescriptor> simple
                = FilterHelper.filter(getNodeFilter(), descriptor.getSimpleNodeDescriptors());
        List<NodeDescriptor> complex
                = FilterHelper.filter(getNodeFilter(), descriptor.getComplexNodeDescriptors());
        List<NodeDescriptor> filtered = new ArrayList<NodeDescriptor>(simple);
        filtered.addAll(complex);

        TableColumnModel columns = new DefaultTableColumnModel();
        for (int i = 0; i < filtered.size(); ++i) {
            columns.addColumn(new SortableTableColumn(i));
        }
        DefaultPageableSortableTableModel model = new DefaultPageableSortableTableModel(columns);
        for (int i = 0; i < filtered.size(); ++i) {
            NodeDescriptor node = filtered.get(i);
            model.setColumnName(i, node.getDisplayName());
        }
        Collection values = (Collection) _descriptor.getValue(context);
        PageableSortableTable table = new PageableSortableTable(model, columns);
        populate(model, values, filtered, factory);
        return table;
    }

    protected void populate(DefaultPageableSortableTableModel table,
                            Collection values,
                            List<NodeDescriptor> descriptors,
                            IMObjectComponentFactory factory) {
        int row = 0;
        for (Object value : values) {
            IMObject object = (IMObject) value;
            int column = 0;
            for (NodeDescriptor descriptor : descriptors) {
                Component c = factory.create(object, descriptor);
                table.setValueAt(c, column, row);
                ++column;
            }
            ++row;
        }
    }
}
