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
package org.openvpms.esci.adapter.dispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;

import javax.annotation.Resource;


/**
 * Base class for listeners that create an <em>act.systemMessage</em> for the events they receive.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractSystemMessageFactory {

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

    /**
     * The logger.
     */
    private Log log = LogFactory.getLog(AbstractSystemMessageFactory.class);


    /**
     * Registers the bean factory.
     *
     * @param factory the bean factory
     */
    @Resource
    public void setBeanFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

    /**
     * Creates a system message linked to the supplied act and addressed to the act's author.
     * <p/>
     * The act's author is determined using {@link #getAddressee}.
     * If there is no author, no system message will be created.
     *
     * @param act    the act
     * @param reason the reason
     */
    protected void createMessage(Act act, String reason) {
        User user = getAddressee(act, reason);
        if (user != null) {
            ActBean message = factory.createActBean(MessageArchetypes.SYSTEM);
            message.addNodeRelationship("item", act);
            message.addNodeParticipation("to", user);
            message.setValue("reason", reason);
            message.save();
        }
    }

    /**
     * Returns the user to address a message to.
     * <p/>
     * By default, this is the author of the supplied act. If no author is present, and the act
     * has an associated stock location, the stock location's default author will be used, if any.
     *
     * @param act    the act
     * @param reason the reason, for logging purposes
     * @return the author, or <tt>null</tt> if none is found
     */
    protected User getAddressee(Act act, String reason) {
        ActBean bean = factory.createActBean(act);
        User result = null;
        if (bean.hasNode("author")) {
            result = (User) bean.getNodeParticipant("author");
        }
        Party stockLocation = null;
        if (result == null && bean.hasNode("stockLocation")) {
            stockLocation = (Party) bean.getNodeParticipant("stockLocation");
            if (stockLocation != null) {
                EntityBean locBean = factory.createEntityBean(stockLocation);
                result = (User) locBean.getNodeTargetEntity("defaultAuthor");
            }
        }
        if (result == null && log.isInfoEnabled()) {
            StringBuilder message = new StringBuilder("Cannot create ");
            message.append(MessageArchetypes.SYSTEM);
            message.append(" for ");
            message.append(act.getArchetypeId().getShortName());
            message.append(":");
            message.append(act.getId());
            message.append(" with reason ");
            message.append(reason);
            message.append(". The act has no author");
            if (stockLocation != null) {
                message.append(" and stock location ");
                message.append(stockLocation.getName());
                message.append(" has no defaultAuthor configured");
            }
            log.info(message);
        }

        return result;
    }
}
