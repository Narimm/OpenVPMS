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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;


/**
 * An editor for {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public abstract class ActEditor extends AbstractIMObjectEditor {

    /**
     * The act item editor.
     */
    private ActRelationshipCollectionEditor _editor;


    /**
     * Construct a new <code>ActEditor</code>.
     *
     * @param act        the act to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param context    the layout context. May be <code>null</code>
     */
    protected ActEditor(Act act, IMObject parent, NodeDescriptor descriptor,
                        LayoutContext context) {
        super(act, parent, descriptor, context);
        NodeDescriptor items = getDescriptor("items");
        _editor = new ActRelationshipCollectionEditor(act, items,
                                                      getLayoutContext());
        getModifiableSet().add(act, _editor);
        _editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateTotals();
            }
        });
    }

    /**
     * Returns the act collection editor.
     *
     * @return the act colleciton editor
     */
    protected ActRelationshipCollectionEditor getEditor() {
        return _editor;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new ActLayoutStrategy(_editor);
    }

    /**
     * Update totals when an act item changes.
     *
     * @todo - workaround for OVPMS-211
     */
    protected abstract void updateTotals();

}
