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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Implementation of {@link IMObjectSessionHandler} for
 * {@link ArchetypeDescriptor}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ArchetypeDescriptorSessionHandler extends AbstractIMObjectSessionHandler {

    /**
     * Node descriptor merge handler.
     */
    private final IMObjectSessionHandler nodeHandler;


    /**
     * Creates a new <tt>ArchetypeDescriptorSesionHandler<tt>.
     *
     * @param dao the DAO
     */
    public ArchetypeDescriptorSessionHandler(IMObjectDAO dao) {
        super(dao);
        nodeHandler = new NodeDescriptorSessionHandler(dao);
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
        ArchetypeDescriptor descriptor = (ArchetypeDescriptor) object;
        saveNew(descriptor.getAllNodeDescriptors(), session);
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
        ArchetypeDescriptor targetDesc = (ArchetypeDescriptor) target;
        ArchetypeDescriptor sourceDesc = (ArchetypeDescriptor) source;
        update(targetDesc.getAllNodeDescriptors(),
               sourceDesc.getAllNodeDescriptors(), nodeHandler);
        super.updateIds(target, source);
    }

}
