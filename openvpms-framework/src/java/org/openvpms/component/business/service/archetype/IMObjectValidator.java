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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service.archetype;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Validates {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class IMObjectValidator {

    /**
     * The archetype descriptor cache.
     */
    private final IArchetypeDescriptorCache cache;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectValidator.class);

    /**
     * Control characters, excluding <em>'\n', '\r', '\t'</em>.
     */
    private static final Pattern CNTRL_CHARS
            = Pattern.compile(".*[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F].*");


    /**
     * Creates a new <tt>IMObjectValidator</tt>.
     *
     * @param cache the archetype descriptor cache
     */
    public IMObjectValidator(IArchetypeDescriptorCache cache) {
        this.cache = cache;
    }

    /**
     * Validates an object.
     *
     * @param object the object to validate
     * @return a list of validation errors encountered. Empty if no errors were found
     */
    public List<ValidationError> validate(IMObject object) {
        List<ValidationError> errors = new ArrayList<ValidationError>();
        validate(object, errors);
        return errors;
    }

    /**
     * Validates an object.
     *
     * @param object the object to validate
     * @param errors the list to add validation errors to
     */
    protected void validate(IMObject object, List<ValidationError> errors) {
        ArchetypeId id = object.getArchetypeId();
        if (log.isDebugEnabled()) {
            log.debug("Validating object of type " + id.getShortName() + " with id " + object.getId()
                      + " and version " + object.getVersion());
        }

        ArchetypeDescriptor archetype = cache.getArchetypeDescriptor(id);
        if (archetype == null) {
            addError(errors, object, null, "No archetype definition for " + id);
            log.error("No archetype definition for " + id);
        } else {
            // if there are nodes attached to the archetype then validate the
            // associated assertions
            if (archetype.getNodeDescriptors().size() > 0) {
                JXPathContext context = JXPathHelper.newContext(object);
                validateNodes(object, context, archetype, archetype.getNodeDescriptors(), errors);
            }
        }
    }

    /**
     * Validates each node of the parent object.
     *
     * @param parent    the parent object
     * @param context   holds the object to be validated
     * @param archetype the archetype descriptor
     * @param nodes     the nodes to validate
     * @param errors    the list to add validation errors to
     */
    protected void validateNodes(IMObject parent, JXPathContext context, ArchetypeDescriptor archetype,
                                 Map<String, NodeDescriptor> nodes, List<ValidationError> errors) {
        for (NodeDescriptor node : nodes.values()) {
            validateNode(parent, context, archetype, node, errors);
        }
    }

    /**
     * Validates a node.
     *
     * @param parent    the parent object
     * @param context   holds the object to be validated
     * @param archetype the archetype descriptor
     * @param node      the node to validate
     * @param errors    the list to add validation errors to
     */
    protected void validateNode(IMObject parent, JXPathContext context, ArchetypeDescriptor archetype,
                                NodeDescriptor node, List<ValidationError> errors) {
        Object value;
        try {
            value = node.getValue(context);
        } catch (Exception exception) {
            addError(errors, parent, node, "Failed to get value");
            log.error("Failed to get value for " + node.getName(), exception);
            return;
        }

        // first check whether the value for this node is derived and if it
        // is then set the derived value
        if (node.isDerived()) {
            try {
                context.getPointer(node.getPath()).setValue(value);
            } catch (Exception exception) {
                addError(errors, parent, node, "Cannot derive value");
                log.error("Failed to derive value for " + node.getName(), exception);
                return;
            }
        }

        if (node.isCollection()) {
            checkCollection(parent, node, value, errors);
        } else {
            checkSimpleValue(parent, node, value, errors);
        }

        if (value != null) {
            // only check the assertions for non-null values. Null values are handled by minCardinality checks.
            checkAssertions(parent, node, value, errors);
        }

        // validate any child nodes
        if (!node.getNodeDescriptors().isEmpty()) {
            validateNodes(parent, context, archetype, node.getNodeDescriptors(), errors);
        }
    }

    /**
     * Checks that a simple value meets minCardinality requirements, and for strings, doesn't contain control
     * characters.
     *
     * @param parent the parent object
     * @param node   the node to validate
     * @param value  the node value to check
     * @param errors the list to add validation errors to
     */
    protected void checkSimpleValue(IMObject parent, NodeDescriptor node, Object value, List<ValidationError> errors) {
        int min = node.getMinCardinality();
        if (min == 1 && (value == null || value instanceof String && StringUtils.isEmpty((String) value))) {
            addError(errors, parent, node, "value is required");
        }

        if (value instanceof String) {
            checkControlChars(parent, node, value, errors);
        }
    }

    /**
     * Checks a collection.
     *
     * @param parent the parent object
     * @param node   the node to validate
     * @param value  the node value to check
     * @param errors the list to add validation errors to
     */
    protected void checkCollection(IMObject parent, NodeDescriptor node, Object value, List<ValidationError> errors) {
        int min = node.getMinCardinality();
        int max = node.getMaxCardinality();
        Collection collection = node.toCollection(value);

        if (min > 0 && (collection == null || collection.size() < min)) {
            addError(errors, parent, node, "must supply at least " + min + " " + node.getBaseName());
        }

        // check the max cardinality if specified
        if (collection != null) {
            if (max > 0 && max != NodeDescriptor.UNBOUNDED && collection.size() > max) {
                addError(errors, parent, node, "cannot supply more than " + max + " " + node.getBaseName());
            }

            // if it's a parent-child relationship then validate the children
            if (node.isParentChild()) {
                for (Object object : collection) {
                    if (object instanceof IMObject) {
                        validate((IMObject) object, errors);
                    }
                }
            }
        }
    }

    /**
     * Checks a nodes validation assertions.
     *
     * @param parent the parent object
     * @param node   the node to validate
     * @param value  the node value to check
     * @param errors the list to add validation errors to
     */
    protected void checkAssertions(IMObject parent, NodeDescriptor node, Object value, List<ValidationError> errors) {
        for (AssertionDescriptor assertion : node.getAssertionDescriptorsAsArray()) {
            checkAssertion(parent, node, value, assertion, errors);
        }
    }

    /**
     * Checks an assertion for a node.
     *
     * @param parent    the parent object
     * @param node      the node to validate
     * @param value     the node value to check
     * @param assertion the assertion
     * @param errors    the list to add validation errors to
     */
    protected void checkAssertion(IMObject parent, NodeDescriptor node, Object value, AssertionDescriptor assertion,
                                  List<ValidationError> errors) {
        try {
            if (!assertion.validate(value, parent, node)) {
                String message = assertion.getErrorMessage();
                if (message == null) {
                    message = "Validation failed for assertion " + assertion.getName();
                }
                addError(errors, parent, node, message);
            }
        } catch (Exception exception) {
            log.error("Assertion " + assertion.getName() + " failed for node " + node, exception);
            addError(errors, parent, node, exception.getMessage());
        }
    }

    /**
     * Checks a string for invalid control characters.
     *
     * @param parent the parent object.
     * @param node   the node descriptor
     * @param value  the string to check
     * @param errors the errors to add to if the string is invalid
     */
    protected void checkControlChars(IMObject parent, NodeDescriptor node, Object value, List<ValidationError> errors) {
        if (CNTRL_CHARS.matcher((String) value).matches()) {
            addError(errors, parent, node, " contains invalid characters");
        }
    }

    /**
     * Adds a validation error.
     *
     * @param errors  the errors to add to if the string is invalid
     * @param object  the object that the error relates to
     * @param node    the node that validation failed for
     * @param message the error message
     */
    private void addError(List<ValidationError> errors, IMObject object, NodeDescriptor node, String message) {
        String shortName = object.getArchetypeId().getShortName();
        errors.add(new ValidationError(shortName, node.getName(), message));
        if (log.isDebugEnabled()) {
            log.debug("Validation failed: archetype=" + shortName + ", node=" + node.getName() + ", message=" +
                      message);
        }
    }

}
