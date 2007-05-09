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

import org.hibernate.HibernateException;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;


/**
 * Default implementation of the {@link ObjectLoader} interface.
 * This implementation only loads instances of {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultObjectLoader extends AbstractObjectLoader {

    /**
     * No-op loader.
     */
    private static final ObjectLoader NO_OP = new NoOpObjectLoader();


    /**
     * Constructs a new <code>DefaultObjectLoader</code>.
     */
    public DefaultObjectLoader() {
        setLoader(ActRelationship.class, NO_OP);
        setLoader(EntityRelationship.class, NO_OP);
        setLoader(LookupRelationship.class, NO_OP);
    }

    /**
     * Loads an object. This implementation only loads instances of
     * {@link IMObject}.
     *
     * @param object the object to load
     * @throws HibernateException for any hibernate error
     */
    public void load(Object object) {
        if (object instanceof IMObject) {
            getLoader(object).load(object);
        }
    }
}
