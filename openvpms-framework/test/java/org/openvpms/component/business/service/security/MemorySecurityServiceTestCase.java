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
 *  $Id: MemorySecurityServiceTestCase.java 328 2005-12-07 13:31:09Z jalateras $
 */

package org.openvpms.component.business.service.security;

import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;


/**
 * Uses the memory based user details service to exercise the test cases
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
@ContextConfiguration("memory-security-service-appcontext.xml")
public class MemorySecurityServiceTestCase extends SecurityServiceTests {

    /**
     * Create a secure context so that we can do some authorization testing.
     *
     * @param user        the user name
     * @param password    the password
     * @param authorities the authorities of the person
     */
    protected void createSecurityContext(String user, String password,
                                         String... authorities) {
        List<GrantedAuthority> granted = new ArrayList<GrantedAuthority>();
        for (String authority : authorities) {
            granted.add(new ArchetypeAwareGrantedAuthority(authority));
        }
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, password, granted);
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
