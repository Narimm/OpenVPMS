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
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.util.List;


/**
 * Editor for collections of {@link IMObjectRelationship}s with 0..N
 * cardinality.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MultipleRelationshipCollectionTargetEditor
        extends IMObjectTableCollectionEditor {

    /**
     * Creates a new <tt>MultipleRelationshipCollectionTargetEditor</tt>.
     *
     * @param editor  the property editor
     * @param object  the parent object
     * @param context the layout context
     */
    public MultipleRelationshipCollectionTargetEditor(
            RelationshipCollectionTargetPropertyEditor editor,
            IMObject object, LayoutContext context) {
        super(editor, object, context);
    }

    /**
     * Adds an object to the collection.
     *
     * @param object the object to add
     */
    public void add(IMObject object) {
        getEditor().add(object);
    }

    /**
     * Returns an editor for an object, creating one if it doesn't exist.
     *
     * @param object the object to edit
     * @return an editor for the object
     */
    @Override
    public IMObjectEditor getEditor(IMObject object) {
        return super.getEditor(object);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit <code>object</code>
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        return super.createEditor(object, context);
    }

    /**
     * Returns the set of objects being edited.
     *
     * @return the set of objects being edited.
     */
    public List<IMObject> getObjects() {
        return getEditor().getObjects();
    }

    /**
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    protected RelationshipCollectionTargetPropertyEditor getEditor() {
        return (RelationshipCollectionTargetPropertyEditor)
                getCollectionPropertyEditor();
    }
}
