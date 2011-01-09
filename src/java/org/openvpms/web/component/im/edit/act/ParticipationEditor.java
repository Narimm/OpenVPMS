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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditorFactory;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;


/**
 * An editor for {@link Participation} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public abstract class ParticipationEditor<T extends Entity>
        extends AbstractIMObjectEditor {

    /**
     * The entity editor.
     */
    private IMObjectReferenceEditor<T> editor;


    /**
     * Construct a new <tt>ParticipationEditor</tt>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be <tt>null</tt>
     */
    public ParticipationEditor(Participation participation, Act parent,
                               LayoutContext context) {
        super(participation, parent, context);
        if (parent == null) {
            throw new IllegalArgumentException("Argument 'parent' is null");
        }
        Property entity = getProperty("entity");
        editor = createObjectReferenceEditor(entity);
        getEditors().add(editor, entity);
        Property act = getProperty("act");
        if (act.getValue() == null) {
            act.setValue(new IMObjectReference(parent));
        }
    }

    /**
     * Returns the participation.
     *
     * @return the participation
     */
    public Participation getParticipation() {
        return (Participation) getObject();
    }

    /**
     * Returns the participation entity editor.
     *
     * @return the participation entity editor
     */
    public IMObjectReferenceEditor<T> getEditor() {
        return editor;
    }

    /**
     * Returns the participation entity reference.
     *
     * @return the participation entity reference. May be <tt>null</tt>
     */
    public IMObjectReference getEntityRef() {
        return (IMObjectReference) editor.getProperty().getValue();
    }

    /**
     * Sets the participation entity reference.
     *
     * @param reference the entity reference. May be <tt>null</tt>
     */
    public void setEntityRef(IMObjectReference reference) {
        editor.getProperty().setValue(reference);
    }

    /**
     * Returns the participation entity.
     *
     * @return the participation entity. May be <tt>null</tt>
     */
    @SuppressWarnings("unchecked")
    public T getEntity() {
        return (T) IMObjectHelper.getObject(getEntityRef());
    }

    /**
     * Sets the participation entity.
     *
     * @param entity the entity. May be <tt>null</tt>
     */
    public void setEntity(T entity) {
        editor.setObject(entity);
    }

    /**
     * Determines if the participation entity is null.
     * This takes into account if a name has been entered but does not match.
     *
     * @return <tt>true</tt> if the participation entity is null; otherwise <tt>false</tt>
     */
    public boolean isNull() {
        return editor.isNull();
    }

    /**
     * Deletes the object.
     * <p/>
     * This implementation always returns <tt>true</tt> if this is the child of an act, as the act manages its deletion.
     * If the participation is not the child of an act, deletion fails.
     *
     * @return <tt>true</tt> if the delete was successful
     */
    @Override
    protected boolean doDelete() {
        return getParent() != null;
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    protected IMObjectReferenceEditor<T> createObjectReferenceEditor(
            Property property) {
        return IMObjectReferenceEditorFactory.create(property, getObject(),
                                                     getLayoutContext());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new IMObjectLayoutStrategy() {
            public ComponentState apply(IMObject object,
                                        PropertySet properties, IMObject parent,
                                        LayoutContext context) {
                return new ComponentState(editor.getComponent(),
                                          editor.getFocusGroup());
            }

        };
    }

}
