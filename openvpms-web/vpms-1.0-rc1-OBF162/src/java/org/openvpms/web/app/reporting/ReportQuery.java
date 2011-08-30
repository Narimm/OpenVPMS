/**
 * 
 */
package org.openvpms.web.app.reporting;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.query.AbstractIMObjectQuery;
import org.openvpms.web.component.im.query.EntityResultSet;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.FastLookupHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tony
 */
public class ReportQuery extends AbstractIMObjectQuery<Entity> {

    /**
     * The user to use to limit acces to reports.
     */
    private final Entity _user;

    /**
     * The selected report type. If <code>null</code> indicates to
     * query using all matching types.
     */
    private String _reportType;

    /**
     * The status dropdown.
     */
    private SelectField _typeSelector;

    /**
     * Type label id.
     */
    private static final String TYPE_ID = "type";


    /**
     * Constructs a new <tt>ReportQuery</tt> that queries IMObjects
     * with the specified criteria.
     *
     * @param user the user. May be <tt>null</tt>
     */
    public ReportQuery(Entity user) {
        super(new String[]{"entity.documentTemplate"});
        _user = user;
    }

    /**
     * Lays out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        addReportTypeSelector(container);
    }

    /**
     * Adds the report Type selector to a container
     *
     * @param container the container
     */
    protected void addReportTypeSelector(Component container) {
        List<Lookup> lookups = FastLookupHelper.getLookups(
                "entity.documentTemplate", "reportType");
        LookupListModel model = new LookupListModel(lookups, true);
        _typeSelector = SelectFieldFactory.create(model);
        _typeSelector.setCellRenderer(new LookupListCellRenderer());
        _typeSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onTypeChanged();
            }
        });

        Label typeLabel = LabelFactory.create(TYPE_ID);
        container.add(typeLabel);
        container.add(_typeSelector);
        getFocusGroup().add(_typeSelector);
    }

    /**
     * Invoked when a status is selected.
     */
    private void onTypeChanged() {
        String value = (String) _typeSelector.getSelectedItem();
        setReportType(value);
    }


    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Entity> query(SortConstraint[] sort) {
        ResultSet<Entity> templates;
        List<Entity> result = new ArrayList<Entity>();
        int userReportLevel;
        int templateUserLevel;
        String type = getShortName();
        String name = null;
        boolean activeOnly = true;


        getComponent();  // ensure the component is rendered

        // Get the current users reportlevel
        if (_user == null) {
            userReportLevel = 0;
        } else {
            EntityBean userBean = new EntityBean(_user);
            userReportLevel = userBean.getInt("userLevel", 0);
        }
        // Do the initial archetype query
        ShortNameConstraint archetypes;
        if (type == null || type.equals(ShortNameListModel.ALL)) {
            archetypes = getArchetypes();
            archetypes.setActiveOnly(activeOnly);
        } else {
            archetypes = new ShortNameConstraint(type, true, activeOnly);
        }
        templates = new EntityResultSet<Entity>(archetypes, name, false,
                                                getConstraints(), sort,
                                                getMaxResults(), isDistinct());

        // Now filter for Reports, user Level and selected type
        while (templates.hasNext()) {
            IPage<Entity> page = templates.next();
            for (Entity object : page.getResults()) {
                EntityBean template = new EntityBean(object);
                String templateArchetype = template.getString("archetype", "");
                templateUserLevel = template.getInt("userLevel", 9);
                String reportType = template.getString("reportType", "");
                if (templateArchetype.equalsIgnoreCase(
                        "REPORT") && (templateUserLevel <= userReportLevel)) {
                    if (getReportType() == null || getReportType().equals("") ||
                            (reportType.equalsIgnoreCase(getReportType())))
                        result.add(object);
                }
            }
        }

        return new IMObjectListResultSet<Entity>(result, getMaxResults());
    }


    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwie <code>false</code>
     */
    @Override
    public boolean isAuto() {
        return (_user != null);
    }

    public String getReportType() {
        return _reportType;
    }

    public void setReportType(String type) {
        _reportType = type;
    }

}
