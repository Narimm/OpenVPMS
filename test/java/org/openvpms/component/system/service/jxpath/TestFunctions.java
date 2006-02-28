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


package org.openvpms.component.system.service.jxpath;

// java core
import java.util.List;

// openvpms-framework
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Some test finctions for JXPath test cases
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class TestFunctions {
    /**
     * Find {@link IMObject} with the specified uid
     * 
     * @param list
     *            the list to use                        
     * @param uid
     *            the uid to search for
     * @return IMObject
     *            the matching object or null            
     */
    public static IMObject findObjectWithUid(List list, long uid) {
       if (list != null) {
           for (Object object : list) {
               if ((object instanceof IMObject) &&
                   (((IMObject)object).getUid() == uid)) {
                   return (IMObject)object;
               }
           }
       }
       
       return null;
    }
    
    /**
     * Test that the single {@link IMObject} argument has the name attribute
     * set.
     * 
     * @param imobj
     *            the object to test
     * @return boolean
     *            true if name attribute is set            
     */
    public static boolean testName(IMObject imobj) {
        return !StringUtils.isEmpty(imobj.getName());
    }
}
