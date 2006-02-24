package org.openvpms.web.component.im.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import echopointng.table.DefaultPageableSortableTableModel;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;


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
     * Next unused model index.
     */
    public static final int NEXT_INDEX = 4;

    /**
     * Determines if the delete column is displayed.
     */
    private boolean _showDelete;

    /**
     * The objects.
     */
    private List<IMObject> _objects = new ArrayList<IMObject>();

    /**
     * Table column model.
     */
    private final TableColumnModel _model;

    /**
     * Check boxes to mark objects for deletion.
     */
    private List<CheckBox> _marks = new ArrayList<CheckBox>();

    /**
     * Table column identifiers.
     */
    protected static final String[] COLUMNS = {
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
        setRowsPerPage(15);
        _model = model;

        for (Iterator iter = model.getColumns(); iter.hasNext();) {
            TableColumn column = (TableColumn) iter.next();
            if (column.getModelIndex() == DELETE_INDEX) {
                _showDelete = true;
                break;
            }
        }
        setObjects(objects);
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<IMObject> objects) {
        _objects.clear();
        _marks.clear();
        getRows().clear();
        fireTableDataChanged();
        for (IMObject object : objects) {
            add(object);
        }
    }

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    public List<IMObject> getObjects() {
        return _objects;
    }

    /**
     * Add an object.
     *
     * @param object the object to add
     */
    public void add(IMObject object) {
        _objects.add(object);
        if (_showDelete) {
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                }
            };
            CheckBox box = new CheckBox();
            box.addActionListener(listener);
            TableLayoutData layout = new TableLayoutData();
            Alignment align = new Alignment(Alignment.CENTER,
                                            Alignment.DEFAULT);
            layout.setAlignment(align);
            box.setLayoutData(layout);
            _marks.add(box);
        }
        // @todo this doesn't work when the table is sorted....
        int row = _objects.size() - 1;
        for (int col = 0; col < getColumnCount(); ++col) {
            setValueAt(getValue(col, row), col, row);
        }
    }

    /**
     * Remove an object.
     *
     * @param object the object to remove
     */
    public void remove(IMObject object) {
        int index = _objects.indexOf(object);
        if (index != -1) {
            _objects.remove(index);
            deleteRow(index);
        }
    }

    /**
     * Deletes the specified row.
     *
     * @param row the row to delete
     */
    @Override
    public void deleteRow(int row) {
        getRows().remove(row);
        fireTableRowsDeleted(row, row);
    }

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    public TableColumnModel getColumnModel() {
        return _model;
    }

    /**
     * @see TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return _model.getColumnCount();
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
     * @see TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int column, int row) {
        // @todo echo2 table documentation incorrect
        return super.getValueAt(getColumnOffset(column), row);
    }

    /**
     * Helper to create a model.
     *
     * @param deletable if <code>true</code>, add a column to enable deletions
     * @return a new column model
     */
    public static IMObjectTableModel create(boolean deletable) {
        TableColumnModel model = new DefaultTableColumnModel();
        int i = (deletable) ? 0 : 1;
        for (; i < COLUMNS.length; ++i) {
            TableColumn column = new TableColumn(i);
            String key = "table.imobject." + COLUMNS[i];
            String label = Messages.get(key);

            column.setHeaderValue(label);
            model.addColumn(column);
        }
        return new IMObjectTableModel(model);
    }

    /**
     * Returns the value found at the given coordinate within the table. Column
     * and row values are 0-based.
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    protected Object getValue(int column, int row) {
        IMObject object = _objects.get(row);
        int index = _model.getColumn(column).getModelIndex();
        return getValue(object, index, row);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param index  the column model index
     * @param row    the table row
     */
    protected Object getValue(IMObject object, int index, int row) {
        Object result;
        switch (index) {
            case DELETE_INDEX:
                result = _marks.get(row);
                break;
            case ID_INDEX:
                result = new Long(object.getUid());
                break;
            case NAME_INDEX:
                if (object instanceof EntityRelationship) {
                    IArchetypeService service = ServiceHelper.getArchetypeService();
                    IMObject targetEntity = service.get(((EntityRelationship) object).getTarget());
                    IMObject sourceEntity = service.get(((EntityRelationship) object).getSource());
                    result = sourceEntity.getName() + " --> " + targetEntity.getName();
                } else
                    result = object.getName();
                break;
            case DESCRIPTION_INDEX:
                result = object.getDescription();
                break;
            default:
                throw new IllegalArgumentException("Illegal column index=" + index);
        }
        return result;
    }

    /**
     * Returns a column given its model index.
     *
     * @param index the model index
     * @return the column
     */
    protected TableColumn getColumn(int index) {
        TableColumn result = null;
        for (Iterator iter = _model.getColumns(); iter.hasNext();) {
            TableColumn column = (TableColumn) iter.next();
            if (column.getModelIndex() == index) {
                result = column;
                break;
            }
        }
        return result;
    }

    /**
     * Returns a column offset given its model index.
     *
     * @param index the model index
     * @return the column offset
     */
    protected int getColumnOffset(int index) {
        int result = 0;
        int offset = 0;
        for (Iterator iter = _model.getColumns(); iter.hasNext(); ++offset) {
            TableColumn column = (TableColumn) iter.next();
            if (column.getModelIndex() == index) {
                result = offset;
                break;
            }
        }
        return result;
    }
}
