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

package org.openvpms.web.workspace.admin.template;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Table model for <em>entity.documentTemplate</em> objects.
 *
 * @author Tim Anderson
 */

public class DocumentTemplateTableModel extends DescriptorTableModel<Entity> {

    /**
     * Determines if the active node should be displayed.
     */
    private final boolean active;

    /**
     * The template helper.
     */
    private final TemplateHelper templateHelper;

    /**
     * The content column index.
     */
    private int contentIndex;

    /**
     * The document template short names.
     */
    private static final String[] SHORT_NAMES = new String[]{DocumentArchetypes.DOCUMENT_TEMPLATE};

    /**
     * The nodes to display.
     */
    private static final String[] NODES = {"id", "name", "description", "archetype", "reportType", "userLevel",
                                           "active"};

    /**
     * The nodes, minus the active node. Used if active is false.
     */
    private static final String[] NODES_MINUS_ACTIVE = Arrays.copyOfRange(NODES, 0, NODES.length - 1);

    /**
     * Constructs a {@link DocumentTemplateTableModel}.
     *
     * @param context the layout context
     */
    public DocumentTemplateTableModel(LayoutContext context) {
        this(context, true);
    }

    /**
     * Constructs a {@link DocumentTemplateTableModel}.
     *
     * @param context the layout context
     * @param query   the query. If both active and inactive results are being queried, an Active column will be
     *                displayed
     */
    public DocumentTemplateTableModel(Query<Entity> query, LayoutContext context) {
        this(context, query.getActive() == BaseArchetypeConstraint.State.BOTH);
    }

    /**
     * Constructs a {@link DocumentTemplateTableModel}.
     *
     * @param context the layout context
     * @param active  determines if the active column should be displayed
     */
    protected DocumentTemplateTableModel(LayoutContext context, boolean active) {
        super(context);
        this.active = active;
        templateHelper = new TemplateHelper(ServiceHelper.getArchetypeService());
        setTableColumnModel(createColumnModel(SHORT_NAMES, context));
    }

    /**
     * Returns the sort constraints, given a primary sort column.
     * <p/>
     * If the column is not sortable, this implementation returns null.
     *
     * @param primary   the primary sort column
     * @param ascending whether to sort in ascending or descending order
     * @return the sort criteria, or {@code null} if the column isn't sortable
     */
    @Override
    protected List<SortConstraint> getSortConstraints(DescriptorTableColumn primary, boolean ascending) {
        String name = primary.getName();
        if ("archetype".equals(name) || "reportType".equals(name)) {
            List<SortConstraint> list = new ArrayList<>();
            list.add(new VirtualNodeSortConstraint(name, ascending));
            return list;
        }
        return super.getSortConstraints(primary, ascending);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return (active) ? NODES : NODES_MINUS_ACTIVE;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(Entity object, TableColumn column, int row) {
        if (column.getModelIndex() == contentIndex) {
            return getContentName(object);
        }
        return super.getValue(object, column, row);
    }

    /**
     * Creates a column model for a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(String[] shortNames, LayoutContext context) {
        DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(shortNames, context);
        contentIndex = getNextModelIndex(model);
        model.addColumn(createTableColumn(contentIndex, "document.template.content"));
        return model;
    }

    /**
     * Returns the name of the content associated with the template.
     *
     * @param object the template
     * @return the content name, or {@code null} if there is no content
     */
    private String getContentName(Entity object) {
        return templateHelper.getFileName(object);
    }

}
