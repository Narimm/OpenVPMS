package org.openvpms.web.component.im.table.act;

import java.math.BigDecimal;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.table.DefaultIMObjectTableModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.resource.util.Messages;


/**
 * Table model for {@link Act}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActTableModel extends DefaultIMObjectTableModel {

    /**
     * Date column index.
     */
    private static final int DATE_INDEX = NEXT_INDEX;

    /**
     * Type column index.
     */
    private static final int TYPE_INDEX = DATE_INDEX + 1;

    /**
     * Status column index.
     */
    private static final int STATUS_INDEX = TYPE_INDEX + 1;

    /**
     * Amount column index.
     */
    private static final int AMOUNT_INDEX = STATUS_INDEX + 1;


    /**
     * Construct a new <code>ActTableModel</code>.
     */
    public ActTableModel() {
        this(true, false);
    }

    /**
     * Construct a new <code>ActTableModel</code>.
     *
     * @param showStatus determines if the status colunn should be displayed
     * @param showAmount determines if the credit/debit amount should be
     *                   displayed
     */
    public ActTableModel(boolean showStatus, boolean showAmount) {
        super(createColumnModel(showStatus, showAmount));
    }

    /**
     * Returns the node name associated with a column.
     *
     * @param column the column
     * @return the name of the node associated with the column, or
     *         <code>null</code>
     */
    @Override
    public String getNode(int column) {
        String node = null;
        switch (column) {
            case DATE_INDEX:
                node = "startTime";
                break;
            case STATUS_INDEX:
                node = "status";
                break;
            case AMOUNT_INDEX:
                node = "credit";
                break;
            default:
                node = super.getNode(column);
                break;
        }
        return node;
    }

    /**
     * Helper to create a column model.
     *
     * @param showStatus determines if the status colunn should be displayed
     * @param showAmount determines if the credit/debit amount should be
     *                   displayed
     * @return a new column model
     */
    protected static TableColumnModel createColumnModel(boolean showStatus,
                                                        boolean showAmount) {
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(DATE_INDEX, "date"));
        model.addColumn(createTableColumn(TYPE_INDEX, "type"));
        if (showStatus) {
            model.addColumn(createTableColumn(STATUS_INDEX, "status"));
        }
        if (showAmount) {
            model.addColumn(createTableColumn(AMOUNT_INDEX, "amount"));
        }
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, "description"));
        return model;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object the object
     * @param column
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int column, int row) {
        Act act = (Act) object;
        Object result;
        switch (column) {
            case DATE_INDEX:
                result = act.getActivityStartTime();
                break;
            case TYPE_INDEX:
                result = DescriptorHelper.getArchetypeDescriptor(act).getDisplayName();
                break;
            case STATUS_INDEX:
                result = act.getStatus();
                break;
            case AMOUNT_INDEX:
                result = getAmount(act);
                break;
            default:
                result = super.getValue(object, column, row);
                break;
        }
        return result;
    }

    /**
     * Helper to create a table column.
     *
     * @param index the column model index
     * @param name  the column name
     * @return a new column
     */
    private static TableColumn createTableColumn(int index, String name) {
        TableColumn column = new TableColumn(index);
        String key = "table.act." + name;
        String label = Messages.get(key);
        column.setHeaderValue(label);
        return column;
    }

    /**
     * Returns an amount from an act.
     *
     * @param act the act
     * @return the stringified amount
     */
    private Label getAmount(Act act) {
        String result = "";
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(act);
        BigDecimal amount;
        Boolean credit;

        amount = (BigDecimal) IMObjectHelper.getValue(act, archetype, "amount");
        credit = (Boolean) IMObjectHelper.getValue(act, archetype, "credit");
        if (amount != null) {
            if (Boolean.TRUE.equals(credit)) {
                amount = amount.negate();
            }
            result = NumberFormatter.format(amount);
        }
        Label label = LabelFactory.create();
        label.setText(result);
        TableLayoutData layout = new TableLayoutData();
        Alignment right = new Alignment(Alignment.RIGHT,
                                        Alignment.DEFAULT);
        layout.setAlignment(right);
        label.setLayoutData(layout);
        return label;
    }

}
