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

package org.openvpms.web.component.im.lookup;

import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.List;


/**
 * Helper to enable lookups to be retrieved from different sources.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface LookupQuery {

    /**
     * Returns the lookups.
     *
     * @return the lookups
     */
    List<Lookup> getLookups();

    /**
     * Returns the default lookup.
     *
     * @return the default lookup, or <tt>null</tt> if none is defined
     */
    Lookup getDefault();
}
