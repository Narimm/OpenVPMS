package org.openvpms.web.component.im.table;

import java.util.List;
import java.util.Iterator;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;


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
     *
     * @param model   the table columne model
     * @param factory the component factory
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
        create(descriptors, columns);
        return columns;
    }

    /**
     * Create columns, adding to an existing column model.
     *
     * @param descriptors the column descriptors
     * @param columns     the columns to add to
     */
    public static void create(List<NodeDescriptor> descriptors,
                              TableColumnModel columns) {
        // determine a unique starting index for the columns
        int index = IMObjectTableModel.NEXT_INDEX;
        Iterator iterator = columns.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col.getModelIndex() >= index) {
                index = col.getModelIndex() + 1;
            }
        }

        for (NodeDescriptor descriptor : descriptors) {
            TableColumn column = new DescriptorTableColumn(index, descriptor);
            columns.addColumn(column);
            ++index;
        }
    }

    /**
     * @see TableModel#getColumnName
     */
    @Override
    public String getColumnName(int column) {
        String result;
        TableColumn col = getColumn(column);
        if (col instanceof DescriptorTableColumn) {
            NodeDescriptor descriptor
                    = ((DescriptorTableColumn) col).getDescriptor();
            result = descriptor.getDisplayName();
        } else {
            result = super.getColumnName(column);
        }
        return result;
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
        Object result = null;
        if (column instanceof DescriptorTableColumn) {
            DescriptorTableColumn col = (DescriptorTableColumn) column;
            NodeDescriptor descriptor = col.getDescriptor();
            Component component = _factory.create(object, descriptor);
            if (descriptor.isNumeric()) {
                TableLayoutData layout = new TableLayoutData();
                Alignment right = new Alignment(Alignment.RIGHT,
                        Alignment.DEFAULT);
                layout.setAlignment(right);
                component.setLayoutData(layout);
            }
            result = component;
        } else {
            result = super.getValue(object, index, row);
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
