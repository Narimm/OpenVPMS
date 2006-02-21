package org.openvpms.web.component.im.table;

import nextapp.echo2.app.table.TableColumn;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Table column associated with a {@link NodeDescriptor}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 * @see DescriptorTableModel
 */
public class DescriptorTableColumn extends TableColumn {

    /**
     * The node descriptor.
     */

    private NodeDescriptor _descriptor;

    /**
     * Construct a new <code>DescriptorTableColumn</code> with the specified
     * model index,undefined width, and undefined cell and header renderers.
     *
     * @param modelIndex the column index of model data visualized by this
     *                   column
     * @param descriptor the node descriptor
     */
    public DescriptorTableColumn(int modelIndex, NodeDescriptor descriptor) {
        super(modelIndex);
        _descriptor = descriptor;
    }

    /**
     * Returns the header value for this column.  The header value is the object
     * that will be provided to the header renderer to produce a component that
     * will be used as the table header for this column.
     *
     * @return the header value for this column
     */
    @Override
    public Object getHeaderValue() {
        return _descriptor.getDisplayName();
    }

    /**
     * Returns the value of the cell.
     *
     * @param context the context
     * @return the value of the cell
     */
    public Object getValue(IMObject context) {
        return _descriptor.getValue(context);
    }

    /**
     * Returns the descriptor.
     *
     * @return the descriptor
     */
    public NodeDescriptor getDescriptor() {
        return _descriptor;
    }

}
