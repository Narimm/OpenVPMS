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
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
                   (((IMObject)object).getId() == uid)) {
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
    
    /**
     * Sum all the nodes, which will be BigDecimal nodes and and return
     * a BigDecimal
     * 
     * @param list the list to sum
     * @return BigDecimal            
     */
    public static BigDecimal sum(List<BigDecimal> list) {
        BigDecimal total = new BigDecimal(0);
        for (BigDecimal item : list) {
            total = total.add(item);
        }
        
        return total;
    }
    
    /**
     * Converts a list of double values to big decimals.
     * 
     * @param doubles the doubles
     * @return the converted values
     */
    public static List<BigDecimalValues> toBigDecimalValues(List<DoubleValues> doubles) {
        List<BigDecimalValues> dec = new ArrayList<BigDecimalValues>(doubles.size());
        
        for (DoubleValues obj : doubles) {
            dec.add(new BigDecimalValues(new BigDecimal(obj.getHigh()),
                    new BigDecimal(obj.getLow())));
        }
        
        return dec;
    }
    
    /**
     * Return the contacts as a string
     * 
     * @param party
     *            the party object
     * @return String            
     */
    public static String getContactsss(Party party) {
        StringBuffer buf = new StringBuffer();
        if (party != null) {
            for (Contact contact : party.getContacts()) {
                buf.append(contact.getDescription());
                buf.append(" ");
            }
        }
        
        return buf.toString();
    }
    
    /**
     * Return the contacts as a string.
     * @return String
     */
    public static String getContacts() {
        return "Jimmy";
    }

    /**
     * Returns the element in a list.
     *
     * @param list the list
     * @param index the index
     */
    public Object getValue(List list, int index) {
        return list.get(index);
    }

    /**
     * Returns the string 'foo'.
     *
     * @return a string
     */
    public String getValue() {
        return "foo";
    }
}
