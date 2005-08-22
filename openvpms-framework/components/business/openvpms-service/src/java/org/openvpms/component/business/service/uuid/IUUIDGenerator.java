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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.service.uuid;

/**
 * This class is responsible for generating unique ids.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public interface IUUIDGenerator {
    /**
     * Return a unique identity.
     * 
     * @return String
     * @throws UUIDServiceException
     *            if the request is unsuccessful                     
     */
    public String nextId();
    
    /**
     * Return a unique identity with the specified prefix
     * 
     * @param prefix
     *            prefix the id with this
     * @return String
     * @throws UUIDServiceException
     *            if the request is unsuccessful                     
     */
    public String nextId(String prefix);
}
