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

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Uses the memory based user details service to exercise the test cases
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
public class MemorySecurityServiceTestCase
        extends SecurityServiceTests {

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{"org/openvpms/component/business/service/security/memory-security-service-appcontext.xml"};
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        this.archetype = (IArchetypeService) applicationContext
                .getBean("archetypeService");
    }

    /**
     * Create a secure context so that we can do some authorization testing
     *
     * @param user      the user name
     * @param password  the password
     * @param authority the authority of the person
     */
    protected void createSecurityContext(String user, String password,
                                         String authority) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user, password, new GrantedAuthority[]{
                new ArchetypeAwareGrantedAuthority(authority)});
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
