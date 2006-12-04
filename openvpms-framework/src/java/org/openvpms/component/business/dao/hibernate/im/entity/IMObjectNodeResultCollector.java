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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class IMObjectNodeResultCollector
        extends AbstractNodeResultCollector<IMObject> {

    private List<IMObject> result = new ArrayList<IMObject>();

    public IMObjectNodeResultCollector(IArchetypeService service,
                                       Collection<String> nodes) {
        super(service, nodes);
    }

    public void collect(Object object) {
        if (object instanceof IMObject) {
            IMObject obj = (IMObject) object;
            for (NodeDescriptor descriptor : getDescriptors(obj)) {
                loadValue(descriptor, obj);
            }
            result.add(obj);
        }
    }

    protected List<IMObject> getResults() {
        return result;
    }
}
