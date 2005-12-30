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


package org.openvpms.component.business.service.security.memory;

//java core
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

// java beans
import java.beans.PropertyEditorSupport;

// acegi security
import org.acegisecurity.GrantedAuthority;

// spring framework
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.domain.im.security.User;
import org.springframework.util.StringUtils;
import org.springframework.beans.propertyeditors.PropertiesEditor;

/**
 * This is used to support creating {@link User} objects from 
 * properties.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class UserMapEditor extends PropertyEditorSupport {

    /* (non-Javadoc)
     * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
     */
    @Override
    public void setAsText(String str) throws IllegalArgumentException {
        UserMap userMap = new UserMap();

        if (StringUtils.hasText(str)) {
            // Use properties editor to tokenize the string
            PropertiesEditor propertiesEditor = new PropertiesEditor();
            propertiesEditor.setAsText(str);

            Properties props = (Properties) propertiesEditor.getValue();
            addUsersFromProperties(userMap, props);
        }

        setValue(userMap);
    }

    public static UserMap addUsersFromProperties(UserMap userMap,
            Properties props) {
        for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
            String username = (String) iter.next();
            String value = props.getProperty(username);
            
            // now retrieve the rest of the user details including the 
            // details and the authorities
            String[] tokens = StringUtils.commaDelimitedListToStringArray(value);
            String password = tokens[0];
            
            // the rest need to be granted authorities
            ArrayList<GrantedAuthority> authorities = 
                new ArrayList<GrantedAuthority>();
            for (int index = 1; index < tokens.length; index++) {
                authorities.add(new ArchetypeAwareGrantedAuthority(tokens[index]));
            }
            
            userMap.addUser(new User(username, password, true));
        }

        return userMap;
    }
}
