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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer.note;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.component.im.lookup.ArchetypeLookupQuery;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.factory.LabelFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Query for <em>act.customerNote</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NoteQuery extends DateRangeActQuery<Act> {

    /**
     * The customer note act.
     */
    public static final String CUSTOMER_NOTE = "act.customerNote";

    /**
     * The note categories.
     */
    private final LookupField categories;

    /**
     * The selected note category. If <tt>null</tt>, indicates to display
     * all notes.
     */
    private String category;


    /**
     * Constructs a new <tt>NoteQuery</tt>.
     *
     * @param customer the customer to query notes for
     */
    public NoteQuery(Party customer) {
        super(customer, "customer", "participation.customer", new String[]{CUSTOMER_NOTE}, Act.class);

        LookupQuery source
            = new ArchetypeLookupQuery("lookup.customerNoteCategory");
        categories = LookupFieldFactory.create(source, true);
        categories.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                onCategoryChanged();
            }
        });
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set. May be <tt>null</tt>
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Act> query(SortConstraint[] sort) {
        ResultSet<Act> result = super.query(sort);
        if (category != null) {
            result = filterOnCategory(result, sort);
        }
        return result;
    }

    /**
     * Lays out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        container.add(LabelFactory.create("customer.note.category"));
        container.add(categories);
        getFocusGroup().add(categories);
        super.doLayout(container);
        FocusHelper.setFocus(categories);
    }

    /**
     * Filters notes to include only those that have the selected category.
     *
     * @param set  the set to filter
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the filtered set
     */
    private ResultSet<Act> filterOnCategory(ResultSet<Act> set,
                                            SortConstraint[] sort) {
        List<Act> matches = new ArrayList<Act>();
        while (set.hasNext()) {
            IPage<Act> page = set.next();
            for (Act act : page.getResults()) {
                IMObjectBean bean = new IMObjectBean(act);
                if (category.equals(bean.getValue("category"))) {
                    matches.add(act);
                }
            }
        }
        ResultSet<Act> result = new IMObjectListResultSet<Act>(matches, getMaxResults());
        if (sort != null) {
            result.sort(sort);
        }
        return result;
    }

    /**
     * Invoked when the category changes.
     */
    private void onCategoryChanged() {
        category = categories.getSelectedCode();
        onQuery();
    }

}
