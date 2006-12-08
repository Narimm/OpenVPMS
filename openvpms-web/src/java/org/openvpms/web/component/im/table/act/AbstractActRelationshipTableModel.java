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

package org.openvpms.web.component.im.table.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.web.component.im.table.DelegatingIMObjectTableModel;

import java.util.ArrayList;
import java.util.List;


/**
 * A table model for {@link ActRelationship}s that models the target acts
 * referred to by the relationships. The model for the target acts is determined
 * by subclasses.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractActRelationshipTableModel<T extends Act>
        extends DelegatingIMObjectTableModel<ActRelationship, T> {

    /**
     * The act relationships.
     */
    private List<ActRelationship> _relationships;


    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<ActRelationship> objects) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        _relationships = objects;
        List<T> acts = new ArrayList<T>();
        for (IMObject object : objects) {
            ActRelationship relationship = (ActRelationship) object;
            if (relationship.getTarget() != null) {
                T act = (T) ArchetypeQueryHelper.getByObjectReference(
                        service, relationship.getTarget());
                if (act != null) {
                    acts.add(act);
                }
            }
        }
        getModel().setObjects(acts);
    }

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    public List<ActRelationship> getObjects() {
        return _relationships;
    }
}
