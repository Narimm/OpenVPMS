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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.template;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.DescriptorTableModel;

import java.util.Arrays;

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
     * The document template short names.
     */
    private static final String[] SHORT_NAMES = new String[]{DocumentArchetypes.DOCUMENT_TEMPLATE};

    /**
     * The nodes to display.
     */
    private static final String[] NODES = {"id", "name", "description", "archetype", "reportType", "userLevel", "active"};

    /**
     * The nodes, minus the active node. Used if active is false.
     */
    private static final String[] NODES_MINUS_ACTIVE = Arrays.copyOfRange(NODES, 0, NODES.length - 1);


    /**
     * Creates a {@code DocumentTemplateTableModel}.
     *
     * @param context the layout context
     */
    public DocumentTemplateTableModel(LayoutContext context) {
        super(context);
        active = true;
        setTableColumnModel(createColumnModel(SHORT_NAMES, context));
    }

    /**
     * Creates a {@code DocumentTemplateTableModel}.
     *
     * @param context the layout context
     * @param query   the query. If both active and inactive results are being queried, an Active column will be
     *                displayed
     */
    public DocumentTemplateTableModel(Query<Entity> query, LayoutContext context) {
        super(context);
        active = query.getActive() == BaseArchetypeConstraint.State.BOTH;
        setTableColumnModel(createColumnModel(SHORT_NAMES, context));
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
}
