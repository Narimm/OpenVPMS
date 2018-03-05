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

package org.openvpms.component.model.act;

import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.object.Relationship;

/**
 * Describes a participation relationship between an {@link Act} and a {@link Entity}.
 * i.e. the entity is the participant in the activity.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface Participation extends Relationship {

    /**
     * Returns the act reference.
     * <p>
     * This is synonymous with {@link Relationship#getSource()}.
     *
     * @return the act reference
     */
    Reference getAct();

    /**
     * Sets the act reference.
     * <p>
     * This is synonymous with {@link Relationship#setSource(Reference)}.
     *
     * @param act the act reference
     */
    void setAct(Reference act);

    /**
     * Returns the entity reference.
     * <p>
     * This is synonymous with {@link Relationship#getTarget()}.
     *
     * @return the entity reference
     */
    Reference getEntity();

    /**
     * Sets the entity reference.
     * <p>
     * This is synonymous with {@link Relationship#setTarget(Reference)}.
     *
     * @param entity the entity reference
     */
    void setEntity(Reference entity);

}
