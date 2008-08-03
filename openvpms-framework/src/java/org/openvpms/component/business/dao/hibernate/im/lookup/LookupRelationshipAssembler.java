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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.lookup;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectRelationshipAssembler;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;


/**
 * Assembles {@link LookupRelationship}s from {@link LookupRelationshipDO}s and
 * vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupRelationshipAssembler
        extends IMObjectRelationshipAssembler<LookupRelationship,
        LookupRelationshipDO> {

    /**
     * Creates a new <tt>LookupRelationshipAssembler</tt>.
     */
    public LookupRelationshipAssembler() {
        super(LookupRelationship.class, LookupRelationshipDO.class,
              LookupRelationshipDOImpl.class, LookupDO.class,
              LookupDOImpl.class);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected LookupRelationship create(LookupRelationshipDO object) {
        return new LookupRelationship();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected LookupRelationshipDO create(LookupRelationship object) {
        return new LookupRelationshipDOImpl();
    }
}
