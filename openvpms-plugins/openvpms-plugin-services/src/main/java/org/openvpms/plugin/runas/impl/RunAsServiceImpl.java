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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.plugin.runas.impl;

import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.plugin.runas.RunAsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Implementation of the {@link RunAsService} interface.
 *
 * @author Tim Anderson
 */
public class RunAsServiceImpl implements RunAsService {

    private final IArchetypeService service;

    public RunAsServiceImpl(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the default user for running plugins.
     *
     * @return the default user, or {@code null} if there is no default user
     */
    @Override
    public String getDefaultUser() {
        return "admin";
    }

    /**
     * Runs an operation as the specified user.
     *
     * @param runnable the operation to run
     * @param user     the user to run the operation as
     */
    @Override
    public void runAs(Runnable runnable, String user) {
        SecurityContext existing = SecurityContextHolder.getContext();
        try {
            UserRules rules = new UserRules(service);
            String runAs = getDefaultUser();
            User u = rules.getUser(runAs);
            if (u == null) {
                throw new IllegalArgumentException("User '" + runAs + "' does not correspond to a valid user");
            }
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken authentication
                    = new UsernamePasswordAuthenticationToken(u, u.getPassword(), u.getAuthorities());
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            runnable.run();
        } finally {
            SecurityContextHolder.setContext(existing);
        }
    }
}
