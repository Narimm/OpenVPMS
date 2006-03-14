package org.openvpms.web.component.im.table;

import java.util.Iterator;
import java.util.List;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;


/**
 * Table model created from an {@link ArchetypeDescriptor} or {@link
 * NodeDescriptor}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DescriptorTableModel extends IMObjectTableModel {

    /**
     * The layout context.
     */
    private final LayoutContext _context;


    /**
     * Construct a <code>DescriptorTableModel</code>.
     *
     * @param model   the table columne model
     * @param context the layout context
     */
    protected DescriptorTableModel(TableColumnModel model,
                                   LayoutContext context) {
        super(model);
        _context = context;
    }

    /**
     * Create a model.
     *
     * @param descriptors the column descriptors
     * @param context     the layout context
     * @return a new model
     */
    public static IMObjectTableModel create(List<NodeDescriptor> descriptors,
                                            LayoutContext context) {
        return new DescriptorTableModel(create(descriptors), context);
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
        IMObjectComponentFactory factory = _context.getComponentFactory();
        Object result = null;
        if (column instanceof DescriptorTableColumn) {
            DescriptorTableColumn col = (DescriptorTableColumn) column;
            NodeDescriptor descriptor = col.getDescriptor();
            result = factory.create(object, descriptor);
        } else {
            result = super.getValue(object, index, row);
        }
        return result;
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return _context;
    }

    /**
     * Returns the component factory.
     *
     * @return the component factory
     */
    protected IMObjectComponentFactory getFactory() {
        return _context.getComponentFactory();
    }
}
