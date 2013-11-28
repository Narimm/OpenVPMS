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
package org.openvpms.web.workspace.admin.lookup;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.DescriptorTableModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Table model for <em>lookup.*</em> objects.
 *
 * @author Tim Anderson
 */
public class LookupTableModel extends DescriptorTableModel<Lookup> {

    private boolean active = false;

    /**
     * Constructs a {@code LookupTableModel}.
     * <p/>
     * This displays the archetype column if the short names reference multiple archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public LookupTableModel(String[] shortNames, LayoutContext context) {
        this(shortNames, null, context);
    }

    /**
     * Constructs a {@code LookupTableModel}.
     * <p/>
     * This displays the archetype column if the short names reference multiple archetypes.
     *
     * @param shortNames the archetype short names
     * @param query      the query. If both active and inactive results are being queried, an Active column will be
     *                   displayed
     * @param context    the layout context
     */
    public LookupTableModel(String[] shortNames, Query<Lookup> query, LayoutContext context) {
        super(context);
        active = query == null || query.getActive() == BaseArchetypeConstraint.State.BOTH;
        setTableColumnModel(createColumnModel(shortNames, context));
    }

    /**
     * Returns the node names for a set of archetypes.
     * <p/>
     * This returns the all simple non-hidden node names common to all archetypes. If this doesn't provide enough
     * information, then the name and description nodes are included.
     *
     * @param archetypes the archetype descriptors
     * @param context    the layout context
     * @return the node names for the archetypes
     */
    @Override
    protected List<String> getNodeNames(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        List<String> result;
        List<String> names = super.getNodeNames(archetypes, context);
        if (archetypes.size() > 1 && names.size() < 2) {
            // more than one archetype, but little or no identifying information.
            result = new ArrayList<String>();
            include("id", result, names, false);
            include("code", result, names, false);
            include("name", result, names, true);
            include("description", result, names, true);
            if (active) {
                include("active", result, names, true);
            }
            result.addAll(names); // add any remaining node names
        } else {
            result = names;
        }
        return result;
    }

    /**
     * Helper to include a node name in a list of names.
     *
     * @param name     the name to include
     * @param target   the names to include {@code name} in
     * @param source   the list of names to check. If the name exists, it will be removed
     * @param required if {@code true}, add the name to {@code names} even if its not present in {@code source}.
     *                 If {@code false} only add it if its present.
     */
    private void include(String name, List<String> target, List<String> source, boolean required) {
        if (source.contains(name)) {
            target.add(name);
            source.remove(name);
        } else if (required) {
            target.add(name);
        }
    }

}
