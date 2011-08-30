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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.act.DefaultActTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Editor for collections of {@link ActRelationship}s that displays
 * items in a configurable table model.
 * <p/>
 * This is a workaround for the inability to create alternative models
 * for an archetype via {@link IMObjectTableModelFactory}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AltModelActRelationshipCollectionEditor
        extends ActRelationshipCollectionEditor {

    /**
     * The table model, or <tt>null</tt> if an {@link DefaultActTableModel}
     * should be used.
     */
    private IMTableModel<IMObject> model;


    /**
     * Creates a new <tt>AltModelActRelationshipCollectionEditor</tt>.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public AltModelActRelationshipCollectionEditor(
            CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
    }

    /**
     * Sets the table model.
     * <p/>
     * Defaults to {@link DefaultActTableModel}.
     *
     * @param model the model. May be <tt>null</tt>
     */
    public void setTableModel(IMTableModel<IMObject> model) {
        this.model = model;
    }

    /**
     * Creates a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        IMTableModel result = model;
        if (result == null) {
            context = new DefaultLayoutContext(context);
            context.setComponentFactory(new TableComponentFactory(context));
            CollectionPropertyEditor editor = getCollectionPropertyEditor();
            result = new DefaultActTableModel(
                    editor.getArchetypeRange(), context);
        }
        return result;
    }
}
