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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.table.DelegatingIMObjectTableModel;

import java.util.List;


/**
 * A table model for {@link IMObjectRelationship}s that models the target objects referred to by the relationships.
 * The model for the target objects is determined by subclasses.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class DelegatingIMObjectRelationshipTargetTableModel<R extends IMObjectRelationship, T extends IMObject>
    extends DelegatingIMObjectTableModel<R, T> {

    /**
     * The relationships.
     */
    private List<R> relationships;


    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<R> objects) {
        relationships = objects;
        List<T> targets = RelationshipHelper.getTargets(objects);
        getModel().setObjects(targets);
    }

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    public List<R> getObjects() {
        return relationships;
    }

    /**
     * Helper to return the short names for the target of a set of relationships.
     *
     * @param relationshipTypes the relationship types
     * @return the target node archetype short names
     */
    protected String[] getTargetShortNames(String... relationshipTypes) {
        return RelationshipHelper.getTargetShortNames(relationshipTypes);
    }
}
