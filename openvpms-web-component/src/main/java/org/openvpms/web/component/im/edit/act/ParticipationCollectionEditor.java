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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.edit.AbstractIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.DefaultIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.DelegatingCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Editor for collections of {@link Participation}s.
 *
 * @author Tim Anderson
 */
public class ParticipationCollectionEditor extends DelegatingCollectionEditor {

    /**
     * Constructs a {@link ParticipationCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the parent object
     * @param context  the layout context
     */
    public ParticipationCollectionEditor(CollectionProperty property, IMObject object, LayoutContext context) {
        String[] shortNames = property.getArchetypeRange();
        int max = property.getMaxCardinality();
        AbstractIMObjectCollectionEditor editor;
        if (max == 1 && shortNames.length == 1) {
            editor = new SingleParticipationCollectionEditor(property, object, context);
        } else {
            editor = new DefaultIMObjectCollectionEditor(property, object, context);
        }
        setEditor(editor);
    }

}
