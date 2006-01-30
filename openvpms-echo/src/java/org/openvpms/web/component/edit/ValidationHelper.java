package org.openvpms.web.component.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.DescriptorHelper;


/**
 * Validation helper.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ValidationHelper {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(ValidationHelper.class);


    /**
     * Validate an object.
     *
     * @param object the object to validate
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public static boolean isValid(IMObject object) {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        // check that we can retrieve a valid archetype for this object
        ArchetypeDescriptor descriptor
                = DescriptorHelper.getArchetypeDescriptor(object);

        // if there are nodes attached to the archetype then validate the
        // associated assertions
        if (!descriptor.getNodeDescriptors().isEmpty()) {
            JXPathContext context = JXPathContext.newContext(object);
            context.setLenient(true);
            validateObject(context, descriptor.getNodeDescriptors(), errors);
        }
        boolean valid = errors.isEmpty();
        if (!valid) {
            ValidationError error = errors.get(0);
            ErrorDialog.show("Error: " + error.getNodeName(),
                    errors.get(0).getErrorMessage());
        }
        return valid;
    }

    /**
     * Iterate through all the nodes and ensure that the object meets all the
     * specified assertions. The assertions are defined in the node and can be
     * hierarchical, which means that this method is re-entrant.
     *
     * @param context holds the object to be validated
     * @param nodes   assertions are managed by the nodes object
     * @param errors  the errors are collected in this object
     */
    private static void validateObject(JXPathContext context,
                                       Map<String, NodeDescriptor> nodes,
                                       List<ValidationError> errors) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        for (NodeDescriptor node : nodes.values()) {
            Object value = null;
            try {
                value = context.getValue(node.getPath());
            } catch (Exception ignore) {
                // ignore since context.setLenient doesn't
                // seem to be working.
                // TODO Need to sort out a better way since this
                // can also cause problems
            }

            // first check whether the value for this node is derived and if it
            // is then derive the value
            if (node.isDerived()) {
                try {
                    value = context.getValue(node.getDerivedValue());
                    context.getPointer(node.getPath()).setValue(value);
                } catch (Exception exception) {
                    value = null;
                    errors.add(new ValidationError(node.getName(),
                            "Cannot derive value"));
                }
            }

            // check the cardinality
            int minCardinality = node.getMinCardinality();
            int maxCardinality = node.getMaxCardinality();
            if ((minCardinality == 1) && (value == null)) {
                errors.add(new ValidationError(node.getName(),
                        "value is required"));
            }

            // if the node is a collection and there are cardinality
            // constraints then check them
            if (node.isCollection()) {
                if ((minCardinality > 0) && (getCollectionSize(value) < minCardinality))
                {
                    errors.add(new ValidationError(node.getName(),
                            " must supply at least " + minCardinality + " "
                                    + node.getBaseName()));

                }

                if ((maxCardinality > 0)
                        && (maxCardinality != NodeDescriptor.UNBOUNDED)
                        && (getCollectionSize(value) > maxCardinality)) {
                    errors.add(new ValidationError(node.getName(),
                            " cannot supply more than " + maxCardinality + " "
                                    + node.getBaseName()));
                }
            }

            if ((value != null)
                    && (node.getAssertionDescriptorsAsArray().length > 0)) {
                // only check the assertions for non-null values
                for (AssertionDescriptor assertion : node
                        .getAssertionDescriptorsAsArray()) {
                    AssertionTypeDescriptor assertionType =
                            service.getAssertionTypeDescriptor(assertion.getName());

                    // TODO
                    // no validation required where the type is not specified.
                    // This is currently a work around since we need to deal
                    // with assertions and some other type of declaration...
                    // which I don't have a name for.
                    if (assertionType.getActionType("assert") == null) {
                        continue;
                    }

                    try {
                        if (!assertionType.assertTrue(value, node, assertion)) {
                            errors.add(new ValidationError(node.getName(),
                                    assertion.getErrorMessage()));
                        }
                    } catch (Exception exception) {
                        _log.error(exception, exception);
                        // log the error
                        errors.add(new ValidationError(node.getName(),
                                assertion.getErrorMessage()));
                    }
                }
            }

            // if this node has other nodes then re-enter this method
            if (node.getNodeDescriptors().size() > 0) {
                validateObject(context, node.getNodeDescriptors(), errors);
            }
        }
    }

    /**
     * Determine the number of entries in the collection. If the collection is
     * null then return 0. The node descriptor defines the type of the
     * collection.
     *
     * @param collection the collection item
     * @return the collection size
     */
    private static int getCollectionSize(Object collection) {
        int size;
        if (collection instanceof Map) {
            size = ((Map) collection).size();
        } else if (collection instanceof Collection) {
            size = ((Collection) collection).size();
        } else {
            size = 0;
        }
        return size;
    }

}
