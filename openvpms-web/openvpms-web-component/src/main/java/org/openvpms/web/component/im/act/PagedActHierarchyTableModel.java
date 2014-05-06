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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.act;

import org.apache.commons.collections4.CollectionUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.PagedIMObjectTableModel;

import java.util.ArrayList;
import java.util.List;


/**
 * A paged table model that uses an {@link ActHierarchyIterator} to view both
 * parent and child acts in the one table.
 *
 * @author Tim Anderson
 */
public class PagedActHierarchyTableModel<T extends Act>
        extends PagedIMObjectTableModel<T> {

    /**
     * The archetype short names of the child acts to display.
     */
    private String[] shortNames;

    /**
     * The maximum depth in the hierarchy to display. Use {@code -1} to specify unlimited depth
     */
    private int maxDepth;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Constructs a {@code PagedActHierarchyTableModel}.
     *
     * @param model      the underlying table model
     * @param context    the context
     * @param shortNames the archetype short names of the child acts to display
     */
    public PagedActHierarchyTableModel(IMObjectTableModel<T> model, Context context, String... shortNames) {
        this(model, -1, context, shortNames);
    }

    /**
     * Construct a new {@code PagedActHierarchyTableModel}.
     *
     * @param model      the underlying table model
     * @param maxDepth   the maximum depth in the hierarchy to display. Use {@code -1} to specify unlimited depth
     * @param context    the context
     * @param shortNames the archetype short names of the child acts to display
     */
    public PagedActHierarchyTableModel(IMObjectTableModel<T> model, int maxDepth, Context context,
                                       String... shortNames) {
        super(model);
        this.shortNames = shortNames;
        this.maxDepth = maxDepth;
        this.context = context;
    }

    /**
     * Sets the archetype short names of the child acts to display.
     *
     * @param shortNames the archetype short names of the child acts to display
     */
    public void setShortNames(String[] shortNames) {
        this.shortNames = shortNames;
    }

    /**
     * Sets the objects for the current page.
     *
     * @param objects the objects to set
     */
    @Override
    protected void setPage(List<T> objects) {
        List<T> acts = flattenHierarchy(objects, shortNames, context);
        getModel().setObjects(acts);
    }

    /**
     * Flattens an act hierarchy, only including those acts matching the supplied short names.
     *
     * @param objects    the acts
     * @param shortNames the child archetype short names
     * @param context    the context
     * @return the acts
     */
    protected List<T> flattenHierarchy(List<T> objects, String[] shortNames, Context context) {
        List<T> list = new ArrayList<T>();
        CollectionUtils.addAll(list, new ActHierarchyIterator<T>(objects, shortNames, maxDepth));
        return list;
    }

}
