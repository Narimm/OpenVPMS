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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditorFactory;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;


/**
 * An editor for parent {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public abstract class ActEditor extends AbstractActEditor {

    /**
     * The act item editor.
     */
    private ActRelationshipCollectionEditor editor;


    /**
     * Construct a new <tt>ActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     */
    protected ActEditor(Act act, IMObject parent, LayoutContext context) {
        this(act, parent, true, context);
    }

    /**
     * Construct a new <tt>ActEditor</tt>.
     *
     * @param act       the act to edit
     * @param parent    the parent object. May be <tt>null</tt>
     * @param editItems if <tt>true</tt> create an editor for any items node
     * @param context   the layout context. May be <tt>null</tt>
     */
    protected ActEditor(Act act, IMObject parent, boolean editItems,
                        LayoutContext context) {
        super(act, parent, context);
        if (editItems) {
            CollectionProperty items
                    = (CollectionProperty) getProperty("items");
            if (items != null && !items.isHidden()) {
                editor = (ActRelationshipCollectionEditor)
                        IMObjectCollectionEditorFactory.create(
                                items, act, getLayoutContext());
                editor.addModifiableListener(new ModifiableListener() {
                    public void modified(Modifiable modifiable) {
                        onItemsChanged();
                    }
                });
                getEditors().add(editor);
            }
        }
    }

    /**
     * Returns the act collection editor.
     *
     * @return the act collection editor. May be <tt>null</tt>
     */
    protected ActRelationshipCollectionEditor getEditor() {
        return editor;
    }

    /**
     * Save any edits.
     * <p/>
     * This uses {@link #saveObject()} to save the object prior to saving
     * any children with {@link #saveChildren()}.
     * <p/>
     * This is necessary to avoid stale object exceptions when related acts
     * are deleted.
     *
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    protected boolean doSave() {
        boolean saved = saveObject();
        if (saved) {
            saved = saveChildren();
        }
        return saved;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        if (editor != null) {
            return new ActLayoutStrategy(editor);
        }
        return new ActLayoutStrategy(false);
    }

    /**
     * Invoked when an act item changes.
     * <p/>
     * This implementation is a no-op.
     */
    protected void onItemsChanged() {

    }

}
