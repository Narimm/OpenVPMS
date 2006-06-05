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

import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.create.IMObjectCreator;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;


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
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     */
    protected ActEditor(Act act, IMObject parent, LayoutContext context) {
        this(act, parent, true, context);
    }

    /**
     * Construct a new <code>ActEditor</code>.
     *
     * @param act       the act to edit
     * @param parent    the parent object. May be <code>null</code>
     * @param editItems if <code>true</code> create an editor for any items node
     * @param context   the layout context. May be <code>null</code>
     */
    protected ActEditor(Act act, IMObject parent, boolean editItems,
                        LayoutContext context) {
        super(act, parent, context);
        if (editItems) {
            CollectionProperty items = (CollectionProperty) getProperty(
                    "items");
            if (items != null && !items.getDescriptor().isHidden()) {
                _editor = new ActRelationshipCollectionEditor(items, act,
                                                              getLayoutContext());
                _editor.addModifiableListener(new ModifiableListener() {
                    public void modified(Modifiable modifiable) {
                        updateTotals();
                    }
                });
                getEditors().add(_editor);
            }
        }
    }

    /**
     * Returns the act collection editor.
     *
     * @return the act collection editor. May be <code>null</code>
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
        if (_editor != null) {
            return new ActLayoutStrategy(_editor);
        }
        return new ActLayoutStrategy(false);
    }

    /**
     * Update totals when an act item changes.
     * <p/>
     * todo - workaround for OVPMS-211
     */
    protected abstract void updateTotals();

    /**
     * Helper to initialises a participation, if it exists and is empty.
     *
     * @param name   the participation name
     * @param entity the participation entity
     */
    protected void initParticipation(String name, IMObject entity) {
        Property property = getProperty(name);
        if (property != null) {
            Participation participant = getParticipation(property);
            if (participant != null) {
                if (participant.getAct() == null) {
                    participant.setAct(getObject().getObjectReference());
                }
                if (entity != null && participant.getEntity() == null) {
                    participant.setEntity(entity.getObjectReference());
                }
            }
        }
    }

    /**
     * Helper to return a participation.
     *
     * @param property the participation property
     * @return the participation
     */
    protected Participation getParticipation(Property property) {
        Object value = null;
        if (property instanceof CollectionProperty) {
            CollectionProperty c = (CollectionProperty) property;
            Object[] values = c.getValues().toArray();
            if (values.length > 0) {
                value = values[0];
            } else {
                String[] shortNames = DescriptorHelper.getShortNames(
                        property.getDescriptor());
                if (shortNames.length == 1) {
                    value = IMObjectCreator.create(shortNames[0]);
                    if (value != null) {
                        c.add(value);
                    }
                }
            }
        } else {
            value = property.getValue();
        }
        return (value instanceof Participation) ? (Participation) value : null;
    }

}
