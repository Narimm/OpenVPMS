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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.ActCollectionResultSetFactory;
import org.openvpms.web.component.im.edit.CollectionResultSetFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.act.DefaultActTableModel;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Editor for collections of {@link ActRelationship}s that displays
 * items in a configurable table model.
 * <p/>
 * This is a workaround for the inability to create alternative models
 * for an archetype via {@link IMObjectTableModelFactory}.
 *
 * @author Tim Anderson
 */
public class AltModelActRelationshipCollectionEditor extends ActRelationshipCollectionEditor {

    /**
     * The table model, or {@code null} if an {@link DefaultActTableModel}
     * should be used.
     */
    private IMTableModel<IMObject> model;


    /**
     * Creates an {@link AltModelActRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public AltModelActRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        this(property, act, context, ActCollectionResultSetFactory.INSTANCE);
    }

    /**
     * Creates an {@link AltModelActRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     * @param factory  the result set factory
     */
    public AltModelActRelationshipCollectionEditor(
            CollectionProperty property, Act act, LayoutContext context, CollectionResultSetFactory factory) {
        super(property, act, context, factory);
    }

}
