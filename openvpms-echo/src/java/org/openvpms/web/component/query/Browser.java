package org.openvpms.web.component.query;

import java.util.List;
import java.util.StringTokenizer;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.IMObjectTable;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.SelectFieldFactory;
import org.openvpms.web.component.SplitPaneFactory;
import org.openvpms.web.component.TableNavigator;
import org.openvpms.web.component.TextComponentFactory;
import org.openvpms.web.component.im.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.SinglePageLayoutStrategy;
import org.openvpms.web.component.model.ArchetypeShortNameListModel;
import org.openvpms.web.spring.ServiceHelper;


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
     * The archetype reference model name.
     */
    private final String _refModelName;

    /**
     * The archetype entity name. If <code>null</code>, indicates to to query
     * all entities.
     */
    private final String _entityName;

    /**
     * The archetype concept name. If <code>null</code>, indicates to query all
     * concepts.
     */
    private final String _conceptName;

    /**
     * The instance name. If the text is <code>null</code> or empty, indicates
     * to query all instances.
     */
    private TextField _instanceName;

    /**
     * The archetype short name. If <code>null</code>, or
     * <code>_selectAll</code>, indicates to query using all matching short
     * names.
     */
    private String _shortName;

    /**
     * The table to display results.
     */
    private IMObjectTable _table;

    /**
     * The selected object.
     */
    private IMObject _selected;

    /**
     * The selected object browser.
     */
    private IMObjectBrowser _browser;

    /**
     * Split pane for laying out the table and navigation control
     */
    private SplitPane _tableLayout;

    /**
     * Split pane for laying out the table and browser.
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
     * Type label id.
     */
    private static final String TYPE_ID = "type";

    /**
     * Name label id.
     */
    private static final String NAME_ID = "name";

    /**
     * Button row style name.
     */
    private static final String ROW_STYLE = "ControlRow";

    /**
     * Layout style name,
     */
    private static final String LAYOUT_STYLE = "Browser.Layout";


    /**
     * Construct a new <code>Browser</code> that queries IMObject's with the
     * specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public Browser(String refModelName, String entityName,
                   String conceptName) {
        super(ORIENTATION_VERTICAL);
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;
        doLayout();
    }

    /**
     * Sets the archetype instance name to query.
     *
     * @param name the archetype instance name. If <code>null</code> indicates
     *             to query all instances
     */
    public void setInstanceName(String name) {
        _instanceName.setText(name);
    }

    /**
     * Returns the archetype instance name.
     *
     * @return the archetype instance name. Nay be <code>null</code>
     */
    public String getInstanceName() {
        return _instanceName.getText();
    }

    /**
     * Set the archetype short name.
     *
     * @param name the archetype short name. If <code>null</code>, indicates to
     *             query using all matching short names.
     */
    public void setShortName(String name) {
        _shortName = name;
    }

    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name. May be <code>null</code>
     */
    public String getShortName() {
        return _shortName;
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
     * Layout this component.
     */
    protected void doLayout() {
        setStyleName(STYLE);

        Row row = RowFactory.create(ROW_STYLE);

        // set up the short names select field, iff there is more than
        // one matching short name.
        List<String> shortNames = getShortNames();
        if (shortNames.size() > 1) {
            ArchetypeShortNameListModel model
                    = new ArchetypeShortNameListModel(shortNames, true);
            final SelectField shortNameSelector = SelectFieldFactory.create(model);
            shortNameSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setShortName((String) shortNameSelector.getSelectedItem());
                }
            });
            int index = shortNames.indexOf(getShortName());
            if (index != -1) {
                shortNameSelector.setSelectedIndex(index);
            }

            Label typeLabel = LabelFactory.create(TYPE_ID);
            row.add(typeLabel);
            row.add(shortNameSelector);
        }

        // instance name text field
        _instanceName = TextComponentFactory.create();
        Label nameLabel = LabelFactory.create(NAME_ID);

        // query button
        Button query = ButtonFactory.create(QUERY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onQuery();
            }
        });

        row.add(nameLabel);
        row.add(_instanceName);
        row.add(query);

        _table = new IMObjectTable();
        _table.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSelect();
            }
        });

        add(row);

        TableNavigator navigator = new TableNavigator(_table);
        _tableLayout = SplitPaneFactory.create(ORIENTATION_VERTICAL,
                navigator, _table);
        _tableLayout.setSeparatorPosition(new Extent(0, Extent.PX));
        _layout = SplitPaneFactory.create(
                ORIENTATION_HORIZONTAL, LAYOUT_STYLE, _tableLayout);
        add(_layout);

        onQuery();
    }

    /**
     * Returns the archetype short names matching the specified reference model,
     * entity and concept names.
     *
     * @return the archetype short names
     */
    protected List<String> getShortNames() {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return service.getArchetypeShortNames(_refModelName, _entityName,
                _conceptName, true);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    protected void onQuery() {
        String type = getShortName();
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<IMObject> result = null;
        if (type == null || type.equals(ArchetypeShortNameListModel.ALL)) {
            result = service.get(_refModelName, _entityName,
                    _conceptName, getInstanceName(), true, true);
        } else {
            StringTokenizer tokens = new StringTokenizer(type, ".");
            if (tokens.countTokens() != 2) {
                throw new IllegalArgumentException(
                        "Invalid no. of tokens in archetype short name: "
                                + type);
            } else {
                String entityName = tokens.nextToken();
                String conceptName = tokens.nextToken();
                result = service.get(_refModelName, entityName, conceptName,
                        getInstanceName(), true, true);
            }
        }

        _table.setObjects(result);
        if (result.size() <= _table.getRowsPerPage()) {
            _tableLayout.setSeparatorPosition(new Extent(0, Extent.PX));
        } else {
            _tableLayout.setSeparatorPosition(new Extent(32, Extent.PX));
        }
    }

    /**
     * Create a new object and edit it.
     */
    protected void onNew() {

    }

    /**
     * Updates the selected IMObject from the table.
     */
    private void onSelect() {
        _selected = _table.getSelected();
        if (_browser != null) {
            _layout.remove(_browser.getComponent());
        }
        IMObjectLayoutStrategy layout = new SinglePageLayoutStrategy(true);
        _browser = new IMObjectBrowser(_selected, layout);
        _layout.add(_browser.getComponent());
    }

}
