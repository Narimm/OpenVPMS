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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Editor for collections of {@link EntityRelationship}s with cardinality > 1, or that have multiple archetypes.
 * <p/>
 * If the relationships have a <em>sequence</em> node, the collection will be ordered on it, and controls displayed to
 * move relationships up or down within the collection.
 *
 * @author Tim Anderson
 */
public class MultipleEntityRelationshipCollectionEditor
        extends MultipleSequencedRelationshipCollectionEditor {


    /**
     * Constructs a {@link MultipleEntityRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public MultipleEntityRelationshipCollectionEditor(CollectionProperty property, Entity object,
                                                      LayoutContext context) {
        super(new EntityRelationshipCollectionPropertyEditor(property, object), object, context);
    }

}