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

package org.openvpms.web.workspace.admin.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.relationship.LookupRelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.ArrayList;
import java.util.List;


/**
 * Editor for collections of <em>lookupRelationship.durationformats</em>.
 * <p/>
 * This displays the target of the relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DurationFormatsLookupRelationshipEditor extends LookupRelationshipCollectionTargetEditor {

    /**
     * Constructs a <tt>DurationFormatsLookupRelationshipEditor</tt>.
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
        return new DurationFormatResultSet(objects, ROWS);
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
        return new DurationFormatLookupTableModel(context);
    }

}
