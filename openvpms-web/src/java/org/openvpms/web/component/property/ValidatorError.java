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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.property;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.resource.util.Messages;


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
     * Returns a formatted message for this error.
     *
     * @return a formatted message
     */
    public String toString() {
        if (archetype != null) {
            return formatNode();
        } else if (property != null) {
            return formatProperty();
        }
        return formatMessage();
    }

    /**
     * Formats a message for an archetype node.
     *
     * @return the formatted message
     */
    private String formatNode() {
        String archetypeName = null;
        String nodeName = null;
        ArchetypeDescriptor descriptor
                = DescriptorHelper.getArchetypeDescriptor(archetype);
        if (descriptor != null) {
            archetypeName = descriptor.getDisplayName();
            NodeDescriptor node = descriptor.getNodeDescriptor(property);
            if (node != null) {
                nodeName = node.getDisplayName();
            }
        }
        return Messages.get(NODE_KEY, archetypeName, nodeName, message);
    }

    /**
     * Formats a message for a property.
     *
     * @return the formatted message
     */
    private String formatProperty() {
        String name = (displayName != null) ? displayName : property;
        return Messages.get(PROP_KEY, name, message);
    }

    /**
     * Formats a message.
     *
     * @return the formatted message
     */
    private String formatMessage() {
        return Messages.get(MSG_KEY, message);
    }
}
