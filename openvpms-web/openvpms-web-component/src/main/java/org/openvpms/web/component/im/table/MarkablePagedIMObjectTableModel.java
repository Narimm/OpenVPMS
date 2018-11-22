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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.object.Reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A pageable table model that tracks marked rows across pages.
 * <p/>
 * This has the following restrictions:
 * <ul>
 *     <li>an object's reference is used to determine if a row is marked, so only result sets containing unique objects
 *     should be used</li>
 *     <li>it is best used with in-memory paged collections</li>
 * </ul>
 *
 *
 * @author Tim Anderson
 */
public class MarkablePagedIMObjectTableModel<T extends IMObject> extends PagedIMObjectTableModel<T> {

    /**
     * References of all marked objects.
     */
    private Set<Reference> marked = new HashSet<>();

    /**
     * Listener for mark/unmark events.
     */
    private final ListMarkModel.Listener listener;

    /**
     * Constructs a {@link MarkablePagedIMObjectTableModel}.
     *
     * @param model the underlying table model
     */
    public MarkablePagedIMObjectTableModel(IMObjectTableModel<T> model) {
        super(model);
        ListMarkModel rowMarks = model.getRowMarkModel();
        if (rowMarks != null) {
            listener = new ListMarkModel.Listener() {
                @Override
                public void changed(int index, boolean marked) {
                    setMarked(index, marked);
                }

                @Override
                public void cleared() {
                    clearMarks();
                }
            };
            rowMarks.addListener(listener);
        } else {
            listener = null;
        }
    }

    /**
     * Returns the marked objects.
     *
     * @param objects all objects in the collection
     * @return the marked objects
     */
    public List<T> getMarked(Collection<T> objects) {
        List<T> result = new ArrayList<>();
        for (T object : objects) {
            if (marked.contains(object.getObjectReference())) {
                result.add(object);
            }
        }
        return result;
    }

    /**
     * Unmark all rows.
     */
    public void unmarkAll() {
        ListMarkModel rowMarks = getModel().getRowMarkModel();
        if (rowMarks != null) {
            rowMarks.removeListener(listener);
            rowMarks.clear();
            clearMarks();
            rowMarks.addListener(listener);
        }
    }

    /**
     * Sets the objects for the current page.
     *
     * @param objects the objects to set
     */
    @Override
    protected void setPage(List<T> objects) {
        ListMarkModel rowMarks = getModel().getRowMarkModel();
        if (rowMarks != null) {
            rowMarks.removeListener(listener);
        }
        super.setPage(objects);
        if (rowMarks != null) {
            int i = 0;
            for (T object : objects) {
                if (marked.contains(object.getObjectReference())) {
                    rowMarks.setMarked(i, true);
                }
                i++;
            }
            rowMarks.addListener(listener);
        }
    }

    /**
     * Marks/unmarks an object.
     *
     * @param row the row of the object
     * @param marked if {@code true}, mark the object, otherwise unmark it
     */
    protected void setMarked(int row, boolean marked) {
        T object = getObjects().get(row);
        Reference reference = object.getObjectReference();
        if (marked) {
            this.marked.add(reference);
        } else {
            this.marked.remove(reference);
        }
    }

    /**
     * Clear all marks.
     */
    protected void clearMarks() {
        marked.clear();
    }
}
