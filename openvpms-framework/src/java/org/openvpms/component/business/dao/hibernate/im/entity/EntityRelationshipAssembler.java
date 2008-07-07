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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.PeriodRelationshipAssembler;
import org.openvpms.component.business.domain.im.common.EntityRelationship;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class EntityRelationshipAssembler
        extends PeriodRelationshipAssembler<EntityRelationship,
        EntityRelationshipDO> {

    public EntityRelationshipAssembler() {
        super(EntityRelationship.class, EntityRelationshipDO.class,
              EntityDO.class);
    }

    @Override
    protected void assembleDO(EntityRelationshipDO result,
                              EntityRelationship source,
                              Context context) {
        super.assembleDO(result, source, context);
        EntityIdentityDO identity = get(source.getIdentity(),
                                        EntityIdentityDO.class, context
        );
        result.setIdentity(identity);
    }

    @Override
    protected void assembleObject(EntityRelationship result,
                                  EntityRelationshipDO source,
                                  Context context) {
        super.assembleObject(result, source, context);
    }
}
