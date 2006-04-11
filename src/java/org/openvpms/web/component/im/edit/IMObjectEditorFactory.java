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

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.DefaultParticipationEditor;
import org.openvpms.web.component.im.edit.estimation.EstimationEditor;
import org.openvpms.web.component.im.edit.estimation.EstimationItemEditor;
import org.openvpms.web.component.im.edit.invoice.InvoiceEditor;
import org.openvpms.web.component.im.edit.invoice.InvoiceItemEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * A factory for {@link IMObjectEditor} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectEditorFactory {

    /**
     * Prevent construction.
     */
    private IMObjectEditorFactory() {
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context. May be <code>null</code>
     * @return an editor for <code>object</code>
     */
    public static IMObjectEditor create(IMObject object,
                                        LayoutContext context) {
        return create(object, null, null, context);
    }

    /**
     * Creates a new editor.
     *
     * @param object     the object to edit
     * @param context    the parent object. May be <code>null</code>
     * @param descriptor the parent object's descriptor. May be
     *                   <code>null</code>
     * @param context    the layout context. May be <code>null</code>
     * @return an editor for <code>object</code>
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        NodeDescriptor descriptor,
                                        LayoutContext context) {
        IMObjectEditor result;
        result = RelationshipEditor.create(object, parent, descriptor, context);
        if (result == null) {
            result = EstimationEditor.create(object, parent, descriptor, context);
        }
        if (result == null) {
            result = EstimationItemEditor.create(object, parent, descriptor, context);
        }
        if (result == null) {
            result = InvoiceEditor.create(object, parent, descriptor, context);
        }
        if (result == null) {
            result = InvoiceItemEditor.create(object, parent, descriptor, context);
        }
        if (result == null) {
            result = DefaultParticipationEditor.create(object, parent, descriptor,
                                                       context);
        }
        if (result == null) {
            result = new DefaultIMObjectEditor(object, parent, descriptor, context);
        }
        return result;
    }
}
