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

import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * An {@link ResultCollector} that collects partially populated
 * {@link IMObject}s.
 * This may be used to selectively load parts of object graphs to improve
 * performance.
 * All simple properties of the returned objects are populated - the
 * <code>nodes</code> argument is used to specify which collection nodes to
 * populate. If empty, no collections will be loaded, and the behaviour of
 * accessing them is undefined.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class IMObjectNodeResultCollector
        extends AbstractNodeResultCollector<IMObject> {

    /**
     * The collected objects.
     */
    private List<IMObject> result = new ArrayList<IMObject>();

    /**
     * Constructs a new <code>IMObjectNodeResultCollector</code>.
     *
     * @param service the archetype service
     * @param nodes   the nodes to collect
     */
    public IMObjectNodeResultCollector(IArchetypeService service,
                                       Collection<String> nodes) {
        super(service, nodes);
    }

    /**
     * Collects an object.
     *
     * @param object the object to collect
     */
    public void collect(Object object) {
        if (object instanceof IMObject) {
            IMObject obj = (IMObject) object;
            for (NodeDescriptor descriptor : getDescriptors(obj)) {
                loadValue(descriptor, obj);
            }
            result.add(obj);
        }
    }

    /**
     * Returns the results.
     *
     * @return the results
     */
    protected List<IMObject> getResults() {
        return result;
    }
}
