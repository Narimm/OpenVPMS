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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupAssembler extends IMObjectAssembler<Lookup, LookupDO> {

    private static final SetAssembler<LookupRelationship, LookupRelationshipDO>
            RELATIONSHIPS = SetAssembler.create(LookupRelationship.class,
                                                LookupRelationshipDOImpl.class);

    public LookupAssembler() {
        super(Lookup.class, LookupDO.class, LookupDOImpl.class);
    }

    @Override
    protected void assembleDO(LookupDO target, Lookup source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);

        if (!ObjectUtils.equals(target.getCode(), source.getCode())) {
            target.setCode(source.getCode());
        }
        if (target.isDefaultLookup() != source.isDefaultLookup()) {
            target.setDefaultLookup(source.isDefaultLookup());
        }

        RELATIONSHIPS.assembleDO(target.getSourceLookupRelationships(),
                                 source.getSourceLookupRelationships(),
                                 state, context);

        RELATIONSHIPS.assembleDO(target.getTargetLookupRelationships(),
                                 source.getTargetLookupRelationships(),
                                 state, context);
    }

    @Override
    protected void assembleObject(Lookup target, LookupDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setCode(source.getCode());
        target.setDefaultLookup(source.isDefaultLookup());

        RELATIONSHIPS.assembleObject(target.getSourceLookupRelationships(),
                                     source.getSourceLookupRelationships(),
                                     context);

        RELATIONSHIPS.assembleObject(target.getTargetLookupRelationships(),
                                     source.getTargetLookupRelationships(),
                                     context);
    }

    protected Lookup create(LookupDO object) {
        return new Lookup();
    }

    protected LookupDO create(Lookup object) {
        return new LookupDOImpl();
    }
}
