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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.edit.SingleIMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for collections of {@link IMObjectRelationship}s with 0..1 or 1..1
 * cardinality.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class SingleRelationshipCollectionTargetEditor
        extends SingleIMObjectCollectionEditor {

    /**
     * Constructs a new <tt>SingleRelationshipCollectionTargetEditor</tt>.
     *
     * @param editor  the collection property editor
     * @param object  the parent object
     * @param context the layout context
     */
    public SingleRelationshipCollectionTargetEditor(
            RelationshipCollectionTargetPropertyEditor editor,
            IMObject object, LayoutContext context) {
        super(editor, object, context);
    }

    /**
     * Determines if the object being edited is empty.
     *
     * @return <tt>true</tt> if the object is empty
     */
    protected boolean isEmpty() {
        return false;
    }

}
