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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionTargetPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.Map;


/**
 * A {@link CollectionPropertyEditor} for collections of {@link ActRelationship}s.
 *
 * @author Tim Anderson
 */
public class ActRelationshipCollectionPropertyEditor extends RelationshipCollectionTargetPropertyEditor {

    /**
     * Constructs an {@link ActRelationshipCollectionPropertyEditor}.
     *
     * @param property the property to edit
     * @param act      the parent act
     */
    public ActRelationshipCollectionPropertyEditor(CollectionProperty property, Act act) {
        super(property, act);
    }

    /**
     * Returns the child acts.
     *
     * @return the child acts
     */
    @SuppressWarnings("unchecked")
    protected Map<Act, ActRelationship> getActs() {
        Map relationships = super.getTargets();
        return (Map<Act, ActRelationship>) relationships;
    }

    /**
     * Creates a relationship between two objects.
     *
     * @param source    the source object
     * @param target    the target object
     * @param shortName the relationship archetype short name
     * @return the new relationship, or <tt>null</tt> if it couldn't be created
     * @throws ArchetypeServiceException for any error
     */
    protected IMObjectRelationship addRelationship(IMObject source,
                                                   IMObject target,
                                                   String shortName) {
        ActBean bean = new ActBean((Act) source);
        return bean.addRelationship(getRelationshipShortName(), (Act) target);
    }

    /**
     * Removes a relationship.
     *
     * @param source       the source of the relationship.
     * @param target       the target of the relationship
     * @param relationship the relationship to remove
     * @return {@code true} if the relationship was removed
     */
    protected boolean removeRelationship(IMObject source, IMObject target, IMObjectRelationship relationship) {
        Act targetAct = ((Act) target);
        ActRelationship actRel = (ActRelationship) relationship;
        targetAct.removeActRelationship(actRel);

        // Remove the relationship from the source act. This will generate events, so invoke last
        return getProperty().remove(relationship);
    }
}


