/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.Set;

/**
 * Helper to notify users on the completion of a job.
 *
 * @author Tim Anderson
 */
public class JobCompletionNotifier {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The maximum subject length.
     */
    private final int subjectLength;

    /**
     * The maximum message length.
     */
    private final int messageLength;

    /**
     * The user rules.
     */
    private final UserRules rules;


    /**
     * Constructs a {@link JobCompletionNotifier}.
     *
     * @param service the archetype service
     */
    public JobCompletionNotifier(IArchetypeService service) {
        this.service = service;
        rules = new UserRules(service);
        ArchetypeDescriptor descriptor = DescriptorHelper.getArchetypeDescriptor(MessageArchetypes.SYSTEM, service);
        subjectLength = getMaxLength(descriptor, "description");
        messageLength = getMaxLength(descriptor, "message");
    }

    /**
     * Returns the users to notify at the completion of a job.
     *
     * @param configuration the job configuration
     * @return the users
     */
    public Set<User> getUsers(Entity configuration) {
        EntityBean bean = new EntityBean(configuration, service);
        return rules.getUsers(bean.getNodeTargetEntities("notify"));
    }

    /**
     * Sends a message to a set of users.
     * <p/>
     * Long messages will be automatically truncated.
     *
     * @param users   the users
     * @param subject the subject
     * @param reason  the reason
     * @param message the message text
     */
    public void send(Set<User> users, String subject, String reason, String message) {
        subject = truncate(subject, subjectLength);
        message = truncate(message, messageLength);
        for (User user : users) {
            send(user, subject, reason, message);
        }
    }

    /**
     * Sends a message to a user.
     *
     * @param user    the user to send to
     * @param subject the subject
     * @param reason  the reason
     * @param text    the message text
     */
    protected void send(User user, String subject, String reason, String text) {
        Act act = (Act) service.create(MessageArchetypes.SYSTEM);
        ActBean message = new ActBean(act);
        message.addNodeParticipation("to", user);
        message.setValue("reason", reason);
        message.setValue("description", subject);
        message.setValue("message", text);
        message.save();
    }

    /**
     * Helper to truncate a string if it exceeds a maximum length.
     *
     * @param value     the value to truncate
     * @param maxLength the maximum length
     * @return the new value
     */
    private String truncate(String value, int maxLength) {
        return StringUtils.abbreviate(value, maxLength);
    }

    /**
     * Helper to determine the maximum length of a node.
     *
     * @param archetype the archetype descriptor
     * @param node      the node name
     * @return the maximum length of the node
     */
    private int getMaxLength(ArchetypeDescriptor archetype, String node) {
        int result = NodeDescriptor.DEFAULT_MAX_LENGTH;
        if (archetype != null) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor != null) {
                result = descriptor.getMaxLength();
            }
        }
        return result;
    }

}
