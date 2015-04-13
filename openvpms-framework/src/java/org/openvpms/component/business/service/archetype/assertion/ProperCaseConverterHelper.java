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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service.archetype.assertion;


/**
 * Helper for managing the singleton instance of the {@link ProperCaseConverter}.
 * <p/>
 * TODO - this is an antipattern.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProperCaseConverterHelper {

    /**
     * The singleton instance.
     */
    private static ProperCaseConverter singleton = new ProperCaseNameConverter();
    
    /**
     * Registers the singleton instance.
     *
     * @param converter the singleton instance
     */
    public ProperCaseConverterHelper(ProperCaseConverter converter) {
        singleton = converter;
    }

    /**
     * Returns the singleton instance.
     * <p/>
     * By default, this is an instance of {@link ProperCaseNameConverter}.
     *
     * @return the singleton instance, or <tt>null</tt> if none is registered
     */
    public static ProperCaseConverter getConverter() {
        return singleton;
    }
}
