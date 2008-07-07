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

import org.openvpms.component.business.dao.hibernate.im.act.ActDO;
import org.openvpms.component.business.dao.hibernate.im.act.ParticipationDO;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.domain.im.common.Participation;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ParticipationAssembler
        extends IMObjectAssembler<Participation, ParticipationDO> {

    public ParticipationAssembler() {
        super(Participation.class, ParticipationDO.class);
    }

    @Override
    protected void assembleDO(ParticipationDO result, Participation source,
                              Context context) {
        super.assembleDO(result, source, context);
        result.setEntity(get(source.getEntity(), EntityDO.class, context));
        result.setAct(get(source.getAct(), ActDO.class, context));
    }

    @Override
    protected void assembleObject(Participation result, ParticipationDO source,
                                  Context context) {
        super.assembleObject(result, source, context);
        result.setEntity(source.getEntity().getObjectReference());
        result.setAct(source.getAct().getObjectReference());
    }

    protected Participation create(ParticipationDO object) {
        return new Participation();
    }

    protected ParticipationDO create(Participation object) {
        return new ParticipationDO();
    }
}
