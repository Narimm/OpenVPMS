package org.openvpms.web.component.im.edit;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.edit.Validator;
import org.openvpms.web.spring.ServiceHelper;


/**
 * A validator that validates using an {@link NodeDescriptor}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class NodeValidator implements Validator {

    /**
     * The node descriptor.
     */
    private final NodeDescriptor _node;


    /**
     * Construct a new <code>NodeValidator</code>.
     *
     * @param descriptor the node descriptor.
     */
    public NodeValidator(NodeDescriptor descriptor) {
        _node = descriptor;
    }

    /**
     * Perform validation, returning a list of errors if the object is invalid.
     *
     * @param value the value to validate
     * @return a list of error messages if the object is invalid; or an empty
     *         list if valid
     */
    public List<String> validate(Object value) {
        List<String> errors = new ArrayList<String>();

        if (value != null) {
            // only check the assertions for non-null values
            IArchetypeService service = ServiceHelper.getArchetypeService();

            for (AssertionDescriptor assertion :
                    _node.getAssertionDescriptorsAsArray()) {
                AssertionTypeDescriptor assertionType =
                        service.getAssertionTypeDescriptor(assertion.getName());

                // @todo
                // no validation required where the type is not specified.
                // This is currently a work around since we need to deal
                // with assertions and some other type of declaration.
                if (assertionType.getActionType("assert") != null) {
                    checkAssertion(assertionType, value, assertion, errors);
                }
            }
        }
        return errors;
    }

    /**
     * Determines if the object is valid.
     *
     * @param value the value to validate
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid(Object value) {
        return validate(value).isEmpty();
    }

    /**
     * Check an assertion.
     *
     * @param type      the assertion type
     * @param value     the value to check
     * @param assertion the assertion
     * @param errors    the list of errors to populate
     */
    private void checkAssertion(AssertionTypeDescriptor type,
                                Object value, AssertionDescriptor assertion,
                                List<String> errors) {
        try {
            if (!type.validate(value, _node, assertion)) {
                errors.add(assertion.getErrorMessage());
            }
        } catch (Exception exception) {
            errors.add(assertion.getErrorMessage());
        }
    }

}
