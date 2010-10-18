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
package org.openvpms.esci.adapter;

import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.exception.ESCIException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;


/**
 * Base class for services mapping between UBL to OpenVPMS types.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractUBLServiceAdapter {

    /**
     * The user rules.
     */
    private UserRules rules;


    /**
     * Registers the user rules.
     *
     * @param rules the user rules
     */
    @Resource
    public void setUserRules(UserRules rules) {
        this.rules = rules;
    }

    /**
     * Returns the current ESCI user.
     *
     * @return the user
     * @throws ESCIException if no user is found
     */
    protected User getUser() {
        User result = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            User user = rules.getUser(auth.getName());
            if (TypeHelper.isA(user, UserArchetypes.ESCI_USER)) {
                result = user;
            }
        }
        if (result == null) {
            Message message = ESCIAdapterMessages.noESCIUser();
            throw new ESCIException(message.toString());
        }
        return result;
    }
}
