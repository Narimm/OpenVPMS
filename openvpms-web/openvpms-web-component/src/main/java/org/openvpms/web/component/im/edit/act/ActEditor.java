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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
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
 * @author Tim Anderson
 */
public abstract class ActEditor extends AbstractActEditor {

    /**
     * Determines if items are being edited.
     */
    private final boolean editItems;

    /**
     * The act item editor.
     */
    private ActRelationshipCollectionEditor editor;


    /**
     * Constructs an {@code ActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    protected ActEditor(Act act, IMObject parent, LayoutContext context) {
        this(act, parent, true, context);
    }

    /**
     * Constructs an {@code ActEditor}.
     *
     * @param act       the act to edit
     * @param parent    the parent object. May be {@code null}
     * @param editItems if {@code true} create an editor for any items node
     * @param context   the layout context
     */
    protected ActEditor(Act act, IMObject parent, boolean editItems, LayoutContext context) {
        super(act, parent, context);
        this.editItems = editItems;
    }

    /**
     * Creates a collection editor for the items collection.
     *
     * @param act   the act
     * @param items the items collection
     * @return a new collection editor
     */
    protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
        return (ActRelationshipCollectionEditor) IMObjectCollectionEditorFactory.create(items, act, getLayoutContext());
    }

    /**
     * Returns the items collection editor.
     *
     * @return the items collection editor. May be {@code null}
     */
    protected ActRelationshipCollectionEditor getItems() {
        if (editor == null && editItems) {
            CollectionProperty items = (CollectionProperty) getProperty("items");
            if (items != null && !items.isHidden()) {
                editor = createItemsEditor(getObject(), items);
                editor.addModifiableListener(new ModifiableListener() {
                    public void modified(Modifiable modifiable) {
                        onItemsChanged();
                    }
                });
                addEditor(editor);
            }
        }
        return editor;
    }

    /**
     * Save any edits.
     * <p>
     * This uses {@link #saveObject()} to save the object prior to saving any children with {@link #saveChildren()}.
     * <p>
     * This is necessary to avoid stale object exceptions when related acts are deleted.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        saveObject();
        saveChildren();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        ActRelationshipCollectionEditor items = getItems();
        if (items != null) {
            return new ActLayoutStrategy(items);
        } else if (getProperty("items") != null) {
            return new ActLayoutStrategy(false);
        } else {
            return super.createLayoutStrategy();
        }
    }

    /**
     * Invoked when an act item changes.
     * <p>
     * This implementation is a no-op.
     */
    protected void onItemsChanged() {

    }

}
