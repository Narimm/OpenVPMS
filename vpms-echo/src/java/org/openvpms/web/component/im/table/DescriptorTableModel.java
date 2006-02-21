package org.openvpms.web.component.im.table;

import java.util.List;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NodeFilter;


/**
 * Table model created from an {@link ArchetypeDescriptor} or {@link
 * NodeDescriptor}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DescriptorTableModel extends IMObjectTableModel {

    private IMObjectComponentFactory _factory;


    /**
     * Construct a <code>DescriptorTableModel</code>.
     */
    protected DescriptorTableModel(TableColumnModel model,
                                   IMObjectComponentFactory factory) {
        super(model);
        _factory = factory;
    }

    /**
     * Create a model.
     *
     * @param descriptor the archetype descriptor
     * @param filter     the node filter. May be <code>null</code>
     * @param factory    the factory for creating components
     * @return a new model
     */
    public static IMObjectTableModel create(ArchetypeDescriptor descriptor,
                                            NodeFilter filter,
                                            IMObjectComponentFactory factory) {
        List<NodeDescriptor> descriptors
                = FilterHelper.filter(filter, descriptor);
        return create(descriptors, factory);
    }

    /**
     * Create a model.
     *
     * @param descriptors the column descriptors
     * @param factory     the factory for creating components
     * @return a new model
     */
    public static IMObjectTableModel create(List<NodeDescriptor> descriptors,
                                            IMObjectComponentFactory factory) {
        return new DescriptorTableModel(create(descriptors), factory);
    }

    /**
     * Create a column model.
     *
     * @param descriptors the column descriptors
     * @return a new column model
     */
    public static TableColumnModel create(List<NodeDescriptor> descriptors) {
        TableColumnModel columns = new DefaultTableColumnModel();

        int i = 0;
        for (NodeDescriptor descriptor : descriptors) {
            TableColumn column = new DescriptorTableColumn(i, descriptor);
            columns.addColumn(column);
            ++i;
        }
        return columns;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param index  the column model index
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int index, int row) {
        TableColumn column = getColumn(index);
        return getValue(object, column);
    }

    /**
     * Returns the value for the specified column.
     *
     * @param object the object
     * @param column the column
     */
    protected Object getValue(IMObject object, TableColumn column) {
        Object result = null;
        if (column instanceof DescriptorTableColumn) {
            DescriptorTableColumn col = (DescriptorTableColumn) column;
            result = _factory.create(object, col.getDescriptor());
        }
        return result;
    }

    /**
     * Returns the factory.
     *
     * @return the factory
     */
    protected IMObjectComponentFactory getFactory() {
        return _factory;
    }

}
