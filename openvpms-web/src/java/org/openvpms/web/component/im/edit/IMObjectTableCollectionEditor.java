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

package org.openvpms.web.component.im.edit;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.table.TableNavigator;

import java.util.List;


/**
 * Editor for a collection of {@link IMObject}s. The collection is displayed
 * in a table. When an item is selected, an editor containing it is displayed
 * in a box beneath the table.
 * <p/>
 * This implementation renders {@link IMObject} instances, and creates the
 * table model using {@link IMObjectTableModelFactory}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class IMObjectTableCollectionEditor
        extends IMTableCollectionEditor<IMObject> {


    /**
     * Creates a new <tt>IMObjectTableCollectionEditor</tt>.
     *
     * @param editor  the collection property editor
     * @param object  the object being edited
     * @param context the layout context
     */
    protected IMObjectTableCollectionEditor(CollectionPropertyEditor editor,
                                            IMObject object,
                                            LayoutContext context) {
        super(editor, object, new DefaultLayoutContext(context));
    }

    /**
     * Creates a new <tt>IMObjectCollectionEditor</tt>.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public IMObjectTableCollectionEditor(CollectionProperty property,
                                         IMObject object,
                                         LayoutContext context) {
        this(new DefaultCollectionPropertyEditor(property), object, context);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        return IMObjectTableModelFactory.create(editor.getArchetypeRange(),
                                                context);
    }

    /**
     * Selects an object in the table.
     * <p/>
     * This implementation scans through the result set to find the object.
     *
     * @param object the object to select
     */
    protected void setSelected(IMObject object) {
        PagedIMTable<IMObject> table = getTable();
        IMObject current = table.getSelected();
        if (!ObjectUtils.equals(current, object)) {
            if (!table.getTable().getObjects().contains(object)) {
                ResultSet set = table.getResultSet();
                int index = 0;
                IPage page;
                while ((page = set.getPage(index)) != null) {
                    if (page.getResults().contains(object)) {
                        break;
                    }
                    ++index;
                }
                if (page != null) {
                    table.getModel().setPage(index);
                }
            }
            table.setSelected(object);
        }
        enableNavigation(table.getSelected() != null);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be <tt>null</tt>
     */
    protected IMObject getSelected() {
        return getTable().getSelected();
    }

    /**
     * Selects the object prior to the selected object, if one is available.
     *
     * @return the prior object. May be <tt>null</tt>
     */
    protected IMObject selectPrevious() {
        IMObject result = null;
        PagedIMTable<IMObject> table = getTable();
        TableNavigator navigator = table.getNavigator();
        if (navigator.selectPreviousRow()) {
            result = table.getSelected();
            setSelected(result);
        }
        return result;
    }

    /**
     * Selects the object after the selected object, if one is available.
     *
     * @return the next object. May be <tt>null</tt>
     */
    protected IMObject selectNext() {
        IMObject result = null;
        PagedIMTable<IMObject> table = getTable();
        TableNavigator navigator = table.getNavigator();
        if (navigator.selectNextRow()) {
            result = table.getSelected();
            setSelected(result);
        }
        return result;
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    protected ResultSet<IMObject> createResultSet() {
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        List<IMObject> objects = editor.getObjects();
        return new IMObjectListResultSet<IMObject>(objects, ROWS);
    }

}
