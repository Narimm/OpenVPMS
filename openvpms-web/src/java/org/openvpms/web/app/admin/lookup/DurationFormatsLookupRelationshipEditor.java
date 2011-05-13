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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.admin.lookup;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.relationship.LookupRelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Editor for collections of <em>lookupRelationship.durationformats</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DurationFormatsLookupRelationshipEditor extends LookupRelationshipCollectionTargetEditor {

    /**
     * Creates a new <tt>LookupRelationshipCollectionEditor</tt>.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public DurationFormatsLookupRelationshipEditor(CollectionProperty property, Lookup object, LayoutContext context) {
        super(property, object, context);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit <tt>object</tt>
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        IMObjectEditor editor = super.createEditor(object, context);
        if (editor instanceof DurationFormatLookupEditor) {
            ((DurationFormatLookupEditor) editor).setShowName(false);
        }
        return editor;
    }

    /**
     * Creates a new result set for display.
     * <p/>
     * This implementation sorts the set on increasing interval.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<IMObject> createResultSet() {
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        List<IMObject> objects = new ArrayList<IMObject>(editor.getObjects());
        final Date now = new Date();
        Collections.sort(objects, new Comparator<IMObject>() {
            public int compare(IMObject o1, IMObject o2) {
                Date date1 = getTo(now, (Lookup) o1);
                Date date2 = getTo(now, (Lookup) o2);
                return date1.compareTo(date2);
            }

            /**
             * Returns the 'to' date of date format, based on a 'from' date
             * @param from the from date
             * @param format an <em>lookup.dateformat</em>
             * @return the 'to' date
             */
            private Date getTo(Date from, Lookup format) {
                IMObjectBean bean = new IMObjectBean(format);
                int interval = bean.getInt("interval");
                DateUnits unit = DateUnits.valueOf(bean.getString("units"));
                return DateRules.getDate(from, interval, unit);
            }
        });
        return new IMObjectListResultSet<IMObject>(objects, ROWS);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));
        return new DurationFormatModel(context);
    }

    /**
     * Table model for <em>lookup.durationformat</em> lookups that supresses the name node and disables sorting.
     * The latter is due to the objects being sorted in order of increasing interval.
     */
    private static class DurationFormatModel extends DescriptorTableModel<IMObject> {

        /**
         * Constructs a <tt>DurationFormatModel</tt>.
         *
         * @param context the layout context. May be <tt>null</tt>
         */
        public DurationFormatModel(LayoutContext context) {
            super(new String[]{"lookup.durationformat"}, context);
        }

        /**
         * Returns a list of node descriptor names to include in the table.
         *
         * @return the list of node descriptor names to include in the table
         */
        @Override
        protected String[] getNodeNames() {
            return new String[]{"interval", "units", "showYears", "showMonths", "showWeeks", "showDays"};
        }

        /**
         * Returns the sort criteria.
         *
         * @param column    the primary sort column
         * @param ascending if <tt>true</tt> sort in ascending order; otherwise sort in <tt>descending</tt> order
         * @return <tt>null</tt>
         */
        @Override
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            return null;
        }
    }
}
