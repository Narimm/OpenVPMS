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

package org.openvpms.component.business.service.archetype.functor;

import org.apache.commons.collections.Predicate;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * A <tt>Predicate</tt> that evaluates <tt>true</tt> if an object is an
 * instance of one of a set of archetypes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IsA implements Predicate {

    /**
     * The archetype short names.
     */
    private final String[] shortNames;

    /**
     * Creates a new <tt>IsA</tt>.
     *
     * @param shortNames the archetype short names
     */
    public IsA(String... shortNames) {
        this.shortNames = shortNames;
    }

    /**
     * Use the specified parameter to perform a test that returns true or false.
     *
     * @param object the object to evaluate. Must be an
     *               {@link IMObject IMObject}.
     * @return <tt>true</tt> if the object is one of the specified archetypes
     * @throws ClassCastException if the input is the wrong class
     */
    public boolean evaluate(Object object) {
        return TypeHelper.isA((IMObject) object, shortNames);
    }
}
