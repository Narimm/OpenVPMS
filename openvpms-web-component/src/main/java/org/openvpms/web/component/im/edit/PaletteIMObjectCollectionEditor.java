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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.bound.BoundPalette;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.palette.Palette;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * An {@link IMObjectCollectionEditor} where the available and selected objects are rendered in a palette.
 *
 * @author Tim Anderson
 */
public class PaletteIMObjectCollectionEditor extends AbstractIMObjectCollectionEditor {

    /**
     * The focus group.
     */
    private FocusGroup focusGroup;


    /**
     * Constructs a {@link PaletteIMObjectCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public PaletteIMObjectCollectionEditor(CollectionProperty property, IMObject object, LayoutContext context) {
        super(property, object, context);
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been rendered
     */
    @Override
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Lays out the component.
     *
     * @param context the layout context
     * @return the component
     */
    @Override
    protected Component doLayout(LayoutContext context) {
        CollectionProperty property = getCollection();
        // render as a palette when more than one object may be selected
        final String[] nodes = DescriptorHelper.getCommonNodeNames(property.getArchetypeRange(), DEFAULT_SORT_NODES,
                                                                   ServiceHelper.getArchetypeService());
        List<IMObject> objects = QueryHelper.query(property.getArchetypeRange(), nodes);
        Palette<IMObject> palette = new BoundPalette<IMObject>(objects, property) {
            @Override
            protected void sort(List<IMObject> values) {
                IMObjectSorter.sort(values, nodes);
            }
        };
        palette.setCellRenderer(IMObjectListCellRenderer.NAME);
        focusGroup = new FocusGroup(property.getDisplayName());
        focusGroup.add(palette);
        return palette;
    }


}
