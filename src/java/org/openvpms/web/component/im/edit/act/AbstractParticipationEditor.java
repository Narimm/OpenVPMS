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

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.edit.ModifiableProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.ObjectReferenceEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
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
    private ObjectReferenceEditor _editor;


    /**
     * Construct a new <code>AbstractParticipationEditor</code>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param descriptor    the parent descriptor
     * @param context       the layout context. May be <code>null</code>
     */
    public AbstractParticipationEditor(Participation participation, Act parent,
                                       NodeDescriptor descriptor,
                                       LayoutContext context) {
        super(participation, parent, descriptor, context);
        NodeDescriptor entityNode = getDescriptor("entity");
        Property property = new ModifiableProperty(participation, entityNode);
        _editor = createObjectReferenceEditor(property, entityNode);
        getModifiableSet().add(participation, property);
    }

    /**
     * Returns the participation entity property.
     *
     * @return the participation entity property
     */
    public Property getEntity() {
        return getProperty("entity");
    }

    /**
     * Save the object.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    protected boolean saveObject() {
        Participation participation = (Participation) getObject();
        Act act = (Act) getParent();
        if (participation.getAct() == null) {
            participation.setAct(new IMObjectReference(act));
            act.addParticipation(participation);
            return SaveHelper.save(act);
        }
        return true;
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property   the reference property
     * @param descriptor the reference descriptor
     * @return a new object reference editor
     */
    protected ObjectReferenceEditor createObjectReferenceEditor(
            Property property, NodeDescriptor descriptor) {
        return new ObjectReferenceEditor(property, descriptor,
                                         getLayoutContext());
    }

    /**
     * Returns the object reference editor.
     *
     * @return the object reference editor
     */
    protected ObjectReferenceEditor getObjectReferenceEditor() {
        return _editor;
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
                                   LayoutContext context) {
                return _editor.getComponent();
            }
        };
    }

}
