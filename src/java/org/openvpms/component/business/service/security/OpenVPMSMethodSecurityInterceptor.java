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
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.Collection;


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
            Object arg = mi.getArguments()[0];
            String shortName = null;
            if (arg instanceof IMObject) {
                shortName = ((IMObject) arg).getArchetypeId().getShortName();
            } else if (arg instanceof Collection) {
                Object[] values = ((Collection<Object>) arg).toArray(
                        new Object[0]);
                if (values.length != 0 && values[0] instanceof IMObject) {
                    ArchetypeId id = ((IMObject) values[0]).getArchetypeId();
                    shortName = id.getShortName();
                }
            }
            throw new OpenVPMSAccessDeniedException(
                    OpenVPMSAccessDeniedException.ErrorCode.AccessDenied,
                    exception);
        }
    }

}
