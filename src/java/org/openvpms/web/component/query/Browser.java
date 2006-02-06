package org.openvpms.web.component.query;

import java.util.EventListener;
import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.IMObjectTable;
import org.openvpms.web.component.SplitPaneFactory;
import org.openvpms.web.component.TableNavigator;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.im.query.DefaultQuery;
import org.openvpms.web.component.im.query.Query;


/**
 * Browser of IMObject instances. In the left pane, a table displays IMObjects
 * matching the specified criteria. When an object is selected from the table, a
 * summary of it is displayed in the right pane.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class Browser extends SplitPane {

    /**
     * The selected action command.
     */
    public static final String SELECTED = "selected";

    /**
     * The query object.
     */
    private final Query _query;

    /**
     * The table to display results.
     */
    private IMObjectTable _table;

    /**
     * The selected object.
     */
    private IMObject _selected;

    /**
     * Split pane for laying out the table and navigation control.
     */
    private SplitPane _layout;

    /**
     * Style name for this.
     */
    private static final String STYLE = "Browser";

    /**
     * Query button id.
     */
    private static final String QUERY_ID = "query";


    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified short names.
     *
     * @param shortNames the short names
     */
    public Browser(String[] shortNames) {
        this(new DefaultQuery(shortNames));
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public Browser(String refModelName, String entityName,
                   String conceptName) {
        this(new DefaultQuery(refModelName, entityName, conceptName));
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects using
     * the specified query.
     *
     * @param query the query
     */
    public Browser(Query query) {
        super(ORIENTATION_VERTICAL);
        _query = query;
        doLayout();
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
     * Adds an <code>ActionListener</code> to receive notification of selection
     * actions.
     *
     * @param listener the listener to add
     */
    public void addActionListener(ActionListener listener) {
        getEventListenerList().addListener(ActionListener.class, listener);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        List<IMObject> result = _query.query();
        _table.setObjects(result);
        if (result != null && result.size() <= _table.getRowsPerPage()) {
            _layout.setSeparatorPosition(new Extent(0, Extent.PX));
        } else {
            _layout.setSeparatorPosition(new Extent(32, Extent.PX));
        }
    }

    /**
     * Layout this component.
     */
    protected void doLayout() {
        setStyleName(STYLE);

        // query component
        Component component = _query.getComponent();

        // query button
        Button query = ButtonFactory.create(QUERY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                query();
            }
        });

        _table = new IMObjectTable();
        _table.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSelect();
            }
        });

        Row row = RowFactory.create(component, query);
        add(row);

        TableNavigator navigator = new TableNavigator(_table);
        _layout = SplitPaneFactory.create(ORIENTATION_VERTICAL,
                navigator, _table);
        _layout.setSeparatorPosition(new Extent(0, Extent.PX));
        add(_layout);
    }

    /**
     * Notifies all listeners that have registered for this event type.
     *
     * @param event the <code>ActionEvent</code> to send
     */
    protected void fireActionPerformed(ActionEvent event) {
        EventListener[] listeners = getEventListenerList().getListeners(
                ActionListener.class);
        for (int index = 0; index < listeners.length; ++index) {
            ((ActionListener) listeners[index]).actionPerformed(event);
        }
    }

    /**
     * Updates the selected IMObject from the table, and notifies any
     * listeners.
     */
    private void onSelect() {
        _selected = _table.getSelected();
        fireActionPerformed(new ActionEvent(this, SELECTED));
    }

}
