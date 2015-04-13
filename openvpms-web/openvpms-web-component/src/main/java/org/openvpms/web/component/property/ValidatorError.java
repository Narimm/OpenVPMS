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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Validator error.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ValidatorError {

    /**
     * Resource bundle key for validation errors associated with an archetype
     * node.
     */
    public static final String NODE_KEY = ValidatorError.class.getName() + ".node.formatted";

    /**
     * Resource bundle key for validation errors associated with a simple
     * property.
     */
    public static final String PROP_KEY = ValidatorError.class.getName() + ".prop.formatted";

    /**
     * Resource bundle key for validation errors not associated with a node
     * or property.
     */
    public static final String MSG_KEY = ValidatorError.class.getName() + ".msg.formatted";

    /**
     * The archetype short name of the object that failed to validate.
     * May be <tt>null</tt>.
     */
    private String archetype;

    /**
     * The property (or node) name. May be <tt>null</tt>
     */
    private String property;

    /**
     * The property display name. May be <tt>null</tt>
     */
    private String displayName;

    /**
     * The error message. May be <tt>null</tt>
     */
    private String message;

    /**
     * Constructs a new <tt>ValidatorError</tt> from a validation error.
     *
     * @param error the validation error
     */
    public ValidatorError(ValidationError error) {
        this(error.getArchetype(), error.getNode(), error.getMessage());
    }

    /**
     * Constructs a new <tt>ValidatorError</tt>.
     *
     * @param archetype the archetype short name
     * @param node      the archetype node
     * @param message   the error message
     */
    public ValidatorError(String archetype, String node, String message) {
        this.archetype = archetype;
        this.property = node;
        this.message = message;

    }

    /**
     * Constructs a new <tt>ValidatorError</tt> for a property.
     *
     * @param property the property
     * @param message  the error message
     */
    public ValidatorError(Property property, String message) {
        this.property = property.getName();
        this.displayName = property.getDisplayName();
        this.message = message;
    }

    /**
     * Constructs a new <tt>ValidatorError</tt> containing just the message.
     *
     * @param message the error message
     */
    public ValidatorError(String message) {
        this.message = message;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns a formatted message for this error.
     *
     * @return a formatted message
     */
    public String toString() {
        if (archetype != null) {
            return format(archetype, property, message);
        } else if (property != null) {
            String name = (displayName != null) ? displayName : property;
            return format(name, message);
        }
        return formatMessage();
    }

    /**
     * Formats a message for an archetype node.
     *
     * @param shortName the archetype short name
     * @param node      the archetype node
     * @param message   the error message
     * @return the formatted message
     */
    public static String format(String shortName, String node, String message) {
        String archetypeName = null;
        String nodeName = null;
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(shortName);
        if (archetype != null) {
            archetypeName = archetype.getDisplayName();
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor != null) {
                nodeName = descriptor.getDisplayName();
            }
        }
        return Messages.format(NODE_KEY, archetypeName, nodeName, message);
    }

    /**
     * Formats a message for a property.
     *
     * @param property the property
     * @param message  the error message
     * @return the formatted message
     */
    public static String format(String property, String message) {
        return Messages.format(PROP_KEY, property, message);
    }

    /**
     * Formats a message.
     *
     * @return the formatted message
     */
    private String formatMessage() {
        return Messages.format(MSG_KEY, message);
    }
}
