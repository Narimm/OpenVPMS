package org.openvpms.web.component.im.query;

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.table.TableNavigator;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Browser of IMObject instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class Browser {

    /**
     * The selected action command.
     */
    public static final String SELECTED = "selected";

    /**
     * The query object.
     */
    private final Query _query;

    /**
     * The browser component.
     */
    private Component _component;

    /**
     * The table to display results.
     */
    private IMObjectTable _table;

    /**
     * Table navigation control.
     */
    private TableNavigator _navigator;

    /**
     * The selected object.
     */
    private IMObject _selected;

    /**
     * The event listener list.
     */
    private List<QueryBrowserListener> _listeners
            = new ArrayList<QueryBrowserListener>();

    /**
     * Style name for this.
     */
    private static final String STYLE = "Browser";

    /**
     * Cell spacing row style.
     */
    private static final String CELLSPACING_STYLE = "CellSpacing";

    /**
     * Query button id.
     */
    private static final String QUERY_ID = "query";


    /**
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query the query
     */
    public Browser(Query query) {
        this(query, new IMObjectTable());
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query, displaying them in the table.
     *
     * @param query the query
     * @param table the table
     */
    public Browser(Query query, IMObjectTable table) {
        _query = query;
        _query.addQueryListener(new QueryListener() {
            public void query() {
                onQuery();
            }
        });
        _table = table;
    }


    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (_component == null) {
            doLayout();
        }
        return _component;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public IMObject getSelected() {
        return _selected;
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(IMObject object) {
        _table.setSelected(object);
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<IMObject> getObjects() {
        return _table.getObjects();
    }

    /**
     * Adds a listener to receive notification of selection and query actions.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryBrowserListener listener) {
        _listeners.add(listener);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        getComponent();  // ensure the component is rendered.

        List<IMObject> result = _query.query();
        _table.setObjects(result);
        if (result != null && result.size() > _table.getRowsPerPage()) {
            // need to show the navigator in order to page through results.
            // Place it before the table.
            if (_component.indexOf(_navigator) == -1) {
                int index = _component.indexOf(_table);
                _component.add(_navigator, index);
            }
        } else {
            _component.remove(_navigator);
        }
    }

    /**
     * Lay out this component.
     */
    protected void doLayout() {
        // query component
        Component component = _query.getComponent();

        // query button
        Button query = ButtonFactory.create(QUERY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onQuery();
            }
        });

        _table.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSelect();
            }
        });

        _navigator = new TableNavigator(_table);

        Row row = RowFactory.create(CELLSPACING_STYLE, component, query);
        _component = ColumnFactory.create(STYLE, row, _table);

        if (_query.isAuto()) {
            query();
        }
    }

    /**
     * Invoked when the query button is pressed. Performs the query and notifies
     * any listeners.
     */
    private void onQuery() {
        query();
        QueryBrowserListener[] listeners = _listeners.toArray(new QueryBrowserListener[0]);
        for (QueryBrowserListener listener : listeners) {
            listener.query();
        }
    }

    /**
     * Updates the selected IMObject from the table, and notifies any
     * listeners.
     */
    private void onSelect() {
        _selected = _table.getSelected();
        if (_selected != null) {
            QueryBrowserListener[] listeners
                    = _listeners.toArray(new QueryBrowserListener[0]);
            for (QueryBrowserListener listener : listeners) {
                listener.selected(_selected);
            }
        }
    }

}
