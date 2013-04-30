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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.supplier;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;


/**
 * Editor for <em>party.supplier*</em> archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SupplierEditor extends AbstractIMObjectEditor {

    /**
     * Editor for the "esci" node, if one is present.
     */
    private RelationshipCollectionTargetEditor esciEditor;

    /**
     * Constructs a <tt>SupplierEditor</tt>.
     *
     * @param supplier      the supplier to edit
     * @param parent        the parent object. May be <tt>null</tt>
     * @param layoutContext the layout context. May be <tt>null</tt>.
     */
    public SupplierEditor(Party supplier, IMObject parent, LayoutContext layoutContext) {
        super(supplier, parent, layoutContext);

        CollectionProperty esci
            = (CollectionProperty) getProperty("esci");
        if (esci != null) {
            esciEditor = new EntityRelationshipCollectionTargetEditor(esci, supplier, getLayoutContext());
            getEditors().add(esciEditor);
        }
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AbstractLayoutStrategy() {
            /**
             * Creates a component for a property.
             *
             * @param property the property
             * @param parent   the parent object
             * @param context  the layout context
             * @return a component to display <tt>property</tt>
             */
            @Override
            protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
                if ("esci".equals(property.getName())) {
                    return new ComponentState(esciEditor.getComponent(), esciEditor.getFocusGroup());
                }
                return super.createComponent(property, parent, context);
            }
        };
    }
}
