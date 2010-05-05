/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.List;


/**
 * Table model for domain objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface IMTableModel<T> extends TableModel {

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    List<T> getObjects();

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    void setObjects(List<T> objects);

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    TableColumnModel getColumnModel();

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <tt>true</tt> sort in ascending order; otherwise
     *                  sort in <tt>descending</tt> order
     * @return the sort criteria, or <tt>null</tt> if the column isn't
     *         sortable
     */
    SortConstraint[] getSortConstraints(int column, boolean ascending);

    /**
     * Determines if selection should be enabled.
     *
     * @return <tt>true</tt> if selection should be enabled; otherwise
     *         <tt>false</tt>
     */
    boolean getEnableSelection();

    /**
     * Determines if selection should be enabled.
     *
     * @param enable if <tt>true</tt> selection should be enabled; otherwise
     *               it should be disabled
     */
    void setEnableSelection(boolean enable);

    /**
     * Notfies the table to refresh.
     * <p/>
     * This can be used to refresh the table if properties of objects held by the model have changed.
     */
    void refresh();
}
