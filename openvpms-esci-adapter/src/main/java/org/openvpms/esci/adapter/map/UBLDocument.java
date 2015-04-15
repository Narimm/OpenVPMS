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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter.map;

import org.openvpms.esci.adapter.util.ESCIAdapterException;


/**
 * Wrapper around UBL documents.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface UBLDocument {

    /**
     * Returns the type name.
     *
     * @return the type name
     */
    String getType();

    /**
     * Returns the document identifier.
     *
     * @return the document identifier
     * @throws ESCIAdapterException if the identifier isn't set
     */
    String getID();

    /**
     * Returns the UBL version identifier.
     *
     * @return the UBL version
     * @throws ESCIAdapterException if the identifier isn't set
     */
    String getUBLVersionID();

}
