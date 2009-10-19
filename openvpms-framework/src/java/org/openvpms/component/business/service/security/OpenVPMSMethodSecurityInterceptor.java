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


package org.openvpms.component.business.service.security;

import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.intercept.method.aopalliance.MethodSecurityInterceptor;
import org.aopalliance.intercept.MethodInvocation;


/**
 * This class overrides the {@link MethodSecurityInterceptor}
 * and overrides the {@link #invoke} method to wrap the exception thrown.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OpenVPMSMethodSecurityInterceptor
        extends MethodSecurityInterceptor {

    /**
     * Default constructor
     */
    public OpenVPMSMethodSecurityInterceptor() {
        super();
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.intercept.method.aopalliance.MethodSecurityInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        try {
            return super.invoke(mi);
        } catch (AccessDeniedException exception) {
            throw new OpenVPMSAccessDeniedException(OpenVPMSAccessDeniedException.ErrorCode.AccessDenied, exception);
        }
    }

}
