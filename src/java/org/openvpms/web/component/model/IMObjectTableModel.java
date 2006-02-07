package org.openvpms.web.component.model;

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.IMObjectTable;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.Messages;

import echopointng.table.DefaultPageableSortableTableModel;
import echopointng.table.SortableTableColumn;


/**
 * Table model for {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 * @see IMObjectTable
 */
public class IMObjectTableModel extends DefaultPageableSortableTableModel {

    /**
     * Delete column index.
     */
    public static final int DELETE_INDEX = 0;

    /**
     * Id column index.
     */
    public static final int ID_INDEX = 1;

    /**
     * Name column index.
     */
    public static final int NAME_INDEX = 2;

    /**
     * Description column index.
     */
    public static final int DESCRIPTION_INDEX = 3;

    /**
     * Determines if the delete column is displayed.
     */
    private boolean _showDelete;

    /**
     * The objects.
     */
    private List<IMObject> _objects;

    /**
     * Check boxes to mark objects for deletion. @todo should only need two, one
     * per state.
     */
    private List<CheckBox> _marks;

    /**
     * Table column identifiers.
     */
    private static final String[] COLUMNS = {
            "delete", "id", "name", "description"};

    /**
     * Construct an unpopulated  <code>IMObjectTableModel</code>.
     */
    public IMObjectTableModel(TableColumnModel model) {
        this(new ArrayList<IMObject>(), model);
    }

    /**
     * Construct a populated <code>IMObjectTableModel</code>.
     *
     * @param objects the objects to populate the model with
     */
    public IMObjectTableModel(List<IMObject> objects, TableColumnModel model) {
        super(model);
        _objects = objects;
        _showDelete = (model.getColumnCount() == COLUMNS.length);

        if (_showDelete) {
            _marks = new ArrayList<CheckBox>(_objects.size());
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                }
            };
            for (int i = 0; i < _objects.size(); ++i) {
                CheckBox box = new CheckBox();
                box.addActionListener(listener);
                _marks.add(box);
            }
        }

        for (int row = 0; row < _objects.size(); ++row) {
            for (int col = 0; col < getColumnCount(); ++col) {
                setValueAt(getValue(col, row), col, row);
            }
        }
    }

    /**
     * @see TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return (_showDelete) ? COLUMNS.length : COLUMNS.length - 1;
    }

    /**
     * @see TableModel#getColumnName
     */
    public String getColumnName(int column) {
        String key = "table.imobject." + COLUMNS[column];
        return Messages.get(key);
    }

    /**
     * Returns the absolute row, given one relative to the current page.
     *
     * @param row a row in the current page
     * @return the absolute row
     */
    public int getAbsRow(int row) {
        return (getCurrentPage() * getRowsPerPage()) + row;
    }

    /**
     * Returns the list of objects marked for deletion.
     *
     * @return the list of objects marked for deletion
     */
    public List<IMObject> getMarked() {
        List<IMObject> result = new ArrayList<IMObject>();
        if (_showDelete) {
            int rows = getTotalRows();
            for (int row = 0; row < rows; ++row) {
                if (_marks.get(row).isSelected()) {
                    result.add(_objects.get(row));
                }
            }
        }
        return result;
    }

    /**
     * Return the object at the given sbsolute row.
     *
     * @param row the row
     * @return the object at <code>row</code>
     */
    public IMObject getObject(int row) {
        return _objects.get(row);
    }

    /**
     * Helper to create a column model.
     *
     * @param deletable if <code>true</code>, add a column to enable deletions
     * @return a new column model
     */
    public static TableColumnModel createColumnModel(boolean deletable) {
        TableColumnModel model = new DefaultTableColumnModel();
        int index = 0;
        if (deletable) {
            model.addColumn(new TableColumn(index));
        }
        for (++index; index < COLUMNS.length; ++index) {
            model.addColumn(new SortableTableColumn(index));
        }
        return model;
    }

    /**
     * Returns the value found at the given coordinate within the table. Column
     * and row values are 0-based.
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    protected Object getValue(int column, int row) {
        Object result;
        IMObject object = _objects.get(row);
        int index = getIndex(column);
        switch (index) {
            case DELETE_INDEX:
                result = _marks.get(row);
                break;
            case ID_INDEX:
                result = new Long(object.getUid());
                break;
            case NAME_INDEX:
                if (object instanceof EntityRelationship){
                    IArchetypeService service = ServiceHelper.getArchetypeService();
                    IMObject targetEntity = service.get(((EntityRelationship)object).getTarget());
                    IMObject sourceEntity = service.get(((EntityRelationship)object).getSource());
                    result = sourceEntity.getName() + " --> " + targetEntity.getName();
                }
                else
                    result = object.getName();
                break;
            case DESCRIPTION_INDEX:
                result = object.getDescription();
                break;
            default:
                throw new IllegalArgumentException("Illegal column=" + column);
        }
        return result;
    }

    /**
     * Returns the index of a column.
     *
     * @param column the column
     * @return the index corresponding to <code>column</code<
     */
    private int getIndex(int column) {
        return (_showDelete) ? column : column + 1;
    }

}
