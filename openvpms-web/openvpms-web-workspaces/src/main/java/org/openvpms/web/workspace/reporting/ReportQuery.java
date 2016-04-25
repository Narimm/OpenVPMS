/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.query.AbstractIMObjectQuery;
import org.openvpms.web.component.im.query.EntityResultSet;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Report query.
 *
 * @author Tony De Keizer
 * @author Tim Anderson
 */
public class ReportQuery extends AbstractIMObjectQuery<Entity> {

    /**
     * The user to use to limit access to reports.
     */
    private final Entity user;

    /**
     * The selected report type. If {@code null} indicates to query using all matching types.
     */
    private String reportType;

    /**
     * The report type selector.
     */
    private LookupField typeSelector;

    /**
     * Type label id.
     */
    private static final String TYPE_ID = "query.type";

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT
            = new SortConstraint[]{new NodeSortConstraint("name", true)};


    /**
     * Constructs a {@link ReportQuery} that queries IMObjects with the specified criteria.
     *
     * @param user the user. May be {@code null}
     */
    public ReportQuery(Entity user) {
        super(new String[]{"entity.documentTemplate"}, Entity.class);
        this.user = user;
        setDefaultSortConstraint(DEFAULT_SORT);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set
     */
    @Override
    public ResultSet<Entity> query(SortConstraint[] sort) {
        ResultSet<Entity> templates;
        List<Entity> result = new ArrayList<>();
        int userReportLevel;
        int templateUserLevel;

        // Get the current user's level
        if (user == null) {
            userReportLevel = 0;
        } else {
            EntityBean userBean = new EntityBean(user);
            userReportLevel = userBean.getInt("userLevel", 0);
        }
        // Do the initial archetype query
        templates = new EntityResultSet<>(getArchetypes(), getValue(), false, getConstraints(), null, getMaxResults(),
                                          isDistinct());

        // Now filter for Reports, user Level and selected type
        while (templates.hasNext()) {
            IPage<Entity> page = templates.next();
            for (Entity object : page.getResults()) {
                EntityBean template = new EntityBean(object);
                String templateArchetype = template.getString("archetype", "");
                templateUserLevel = template.getInt("userLevel", 9);
                String reportType = template.getString("reportType", "");
                if (templateArchetype.equalsIgnoreCase("REPORT") && (templateUserLevel <= userReportLevel)) {
                    if (getReportType() == null || getReportType().equals("") ||
                        (reportType.equalsIgnoreCase(getReportType()))) {
                        result.add(object);
                    }
                }
            }
        }

        IMObjectListResultSet<Entity> set = new IMObjectListResultSet<>(result, getMaxResults());
        set.sort(sort);
        return set;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return {@code true} if the query should be run automaticaly; otherwie {@code false}
     */
    @Override
    public boolean isAuto() {
        return (user != null);
    }

    /**
     * Returns the report type.
     *
     * @return the report type
     */
    public String getReportType() {
        return reportType;
    }

    /**
     * Sets the report type.
     *
     * @param type the report type
     */
    public void setReportType(String type) {
        reportType = type;
    }

    /**
     * Lays out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        addSearchField(container);
        FocusHelper.setFocus(getSearchField());
        addReportTypeSelector(container);
    }

    /**
     * Adds the report Type selector to a container
     *
     * @param container the container
     */
    protected void addReportTypeSelector(Component container) {
        LookupQuery source = new NodeLookupQuery(DocumentArchetypes.DOCUMENT_TEMPLATE, "reportType");
        typeSelector = LookupFieldFactory.create(source, true);
        typeSelector.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onTypeChanged();
            }
        });
        typeSelector.setSelected((Lookup) null);

        Label typeLabel = LabelFactory.create(TYPE_ID);
        container.add(typeLabel);
        container.add(typeSelector);
        getFocusGroup().add(typeSelector);
    }

    /**
     * Invoked when a status is selected.
     */
    private void onTypeChanged() {
        setReportType(typeSelector.getSelectedCode());
    }

}
