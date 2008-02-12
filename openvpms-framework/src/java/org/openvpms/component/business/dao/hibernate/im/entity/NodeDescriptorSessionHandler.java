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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.hibernate.Session;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of {@link IMObjectSessionHandler} for {@link NodeDescriptor}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class NodeDescriptorSessionHandler extends AbstractIMObjectSessionHandler {

    /**
     * Creates a new <tt>NodeDescriptorSessionHandler<tt>.
     *
     * @param dao the DAO
     */
    public NodeDescriptorSessionHandler(IMObjectDAO dao) {
        super(dao);
    }

    /**
     * Saves an object.
     *
     * @param object  the object to merge
     * @param session the session to use
     * @return the result of <tt>Session.merge(object)</tt>
     */
    @Override
    public IMObject save(IMObject object, Session session) {
        NodeDescriptor descriptor = (NodeDescriptor) object;
        saveNew(descriptor.getAssertionDescriptors().values(), session);
        return super.save(object, session);
    }

    /**
     * Updates the target object with the identifier and version of the source.
     *
     * @param target the object to update
     * @param source the object to update from
     */
    @Override
    public void updateIds(IMObject target, IMObject source) {
        NodeDescriptor targetDesc = (NodeDescriptor) target;
        NodeDescriptor sourceDesc = (NodeDescriptor) source;
        updateTypeDescriptors(targetDesc.getAssertionDescriptors().values(),
                              sourceDesc.getAssertionDescriptors().values());
        super.updateIds(target, source);
    }

    /**
     * Updates the target objects with the identifier and version of the their
     * corresponding sources.
     *
     * @param targets the targets to update
     * @param sources the sources to update from
     */
    protected void updateTypeDescriptors(
            Collection<AssertionDescriptor> targets,
            Collection<AssertionDescriptor> sources) {
        if (!targets.isEmpty()) {
            Map<String, AssertionDescriptor> map
                    = new HashMap<String, AssertionDescriptor>();
            for (AssertionDescriptor source : sources) {
                map.put(source.getName(), source);
            }
            for (AssertionDescriptor target : targets) {
                AssertionDescriptor source = map.get(target.getName());
                if (source != null) {
                    super.updateIds(target, source);
                }
            }
        }
    }

}
