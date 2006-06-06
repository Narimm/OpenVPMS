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

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An editor for {@link Participation} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public abstract class AbstractParticipationEditor extends AbstractIMObjectEditor {

    /**
     * The entity editor.
     */
    private IMObjectReferenceEditor _editor;


    /**
     * Construct a new <code>AbstractParticipationEditor</code>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be <code>null</code>
     */
    public AbstractParticipationEditor(Participation participation, Act parent,
                                       LayoutContext context) {
        super(participation, parent, context);
        if (parent == null) {
            throw new IllegalArgumentException("Argument 'parent' is null");
        }
        Property entity = getProperty("entity");
        _editor = createObjectReferenceEditor(entity);
        getEditors().add(_editor, entity);
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
     * Returns the participation entity property.
     *
     * @return the participation entity property
     */
    public Property getEntity() {
        return _editor.getProperty();
    }

    /**
     * Returns the editor.
     *
     * @return the editor
     */
    protected IMObjectReferenceEditor getEditor() {
        return _editor;
    }
    
    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    protected IMObjectReferenceEditor createObjectReferenceEditor(
            Property property) {
        return new IMObjectReferenceEditor(property, getLayoutContext());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new IMObjectLayoutStrategy() {
            public Component apply(IMObject object,
                                   PropertySet properties, LayoutContext context) {
                return _editor.getComponent();
            }
        };
    }

}
