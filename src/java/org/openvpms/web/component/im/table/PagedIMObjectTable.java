package org.openvpms.web.component.im.table;

import nextapp.echo2.app.Column;

import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.table.TableNavigator;


/**
 * Paged IMObject table.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class PagedIMObjectTable extends Column {

    /**
     * The underlying table.
     */
    private final IMObjectTable _table;

    /**
     * The navigator.
     */
    private TableNavigator _navigator;


    /**
     * Construct a new <code>PagedIMObjectTable</code>.
     *
     * @param model the model to render results
     */
    public PagedIMObjectTable(IMObjectTableModel model) {
        setStyleName("CellSpacing");
        IMObjectTableModel paged = new PagedIMObjectTableModel(model);
        _table = new IMObjectTable(paged);
        add(_table);
    }

    /**
     * Construct a new <code>PagedIMObjectTable</code>.
     *
     * @param model the model to render results
     * @param set the result set
     */
    public PagedIMObjectTable(IMObjectTableModel model, ResultSet set) {
        this(model);
        setResultSet(set);
    }

    /**
     * Sets the result set.
     *
     * @param set the set
     */
    public void setResultSet(ResultSet set) {
        PagedIMObjectTableModel model
                = (PagedIMObjectTableModel) _table.getModel();
        model.setResultSet(set);
        if (model.getPages() > 1) {
            if (_navigator == null) {
                _navigator = new TableNavigator(_table);
            }
            if (indexOf(_navigator) == -1) {
                add(_navigator, 0);
            }
        } else {
            if (_navigator != null) {
                remove(_navigator);
            }
        }
    }

    /**
     * Returns the underlying table.
     *
     * @return the underlying table
     */
    public IMObjectTable getTable() {
        return _table;
    }


}
