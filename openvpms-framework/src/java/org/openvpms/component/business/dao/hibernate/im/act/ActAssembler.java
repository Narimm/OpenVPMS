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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.domain.im.act.Act;


/**
 * An {@link Assembler} responsible for assembling {@link ActDO} instances from {@link Act}s and vice-versa.
 *
 * @author Tim Anderson
 */
public class ActAssembler extends AbstractActAssembler<Act, ActDO> {

    /**
     * Constructs an {@link ActAssembler}.
     */
    public ActAssembler() {
        super(org.openvpms.component.model.act.Act.class, Act.class, ActDO.class, ActDOImpl.class);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected Act create(ActDO object) {
        return new Act();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected ActDO create(Act object) {
        return new ActDOImpl();
    }
}
