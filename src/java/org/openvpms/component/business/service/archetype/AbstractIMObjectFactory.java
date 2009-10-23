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
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.util.Collection;
import java.util.Map;


/**
 * Abstract implementation of the {@link IMObjectFactory} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMObjectFactory implements IMObjectFactory {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractIMObjectFactory.class);


    /**
     * Creates a domain object given an archetype short name.
     *
     * @param shortName the archetype short name
     * @return a new object, or <tt>null</tt> if there is no corresponding archetype descriptor for <tt>shortName</tt>
     * @throws ArchetypeServiceException if the object can't be created
     */
    public IMObject create(String shortName) {
        ArchetypeDescriptor desc = getArchetypeDescriptor(shortName);
        return (desc != null) ? create(desc) : null;
    }

    /**
     * Creates a domain object given an archetype id.
     *
     * @param id the archetype identifier
     * @return a new object, or <tt>null</tt> if there is no corresponding archetype descriptor for <tt>id</tt>
     * @throws ArchetypeServiceException if the object can't be created
     */
    public IMObject create(ArchetypeId id) {
        ArchetypeDescriptor desc = getArchetypeDescriptor(id);
        return (desc != null) ? create(desc) : null;
    }

    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified short name.
     * <p/>
     * If there are multiple archetype descriptors with the same name then it will retrieve the first descriptor marked
     * with latest=true.
     *
     * @param shortName the archetype short name
     * @return the archetype descriptor or null if there is no corresponding archetype descriptor for <tt>shortName</tt>
     */
    protected abstract ArchetypeDescriptor getArchetypeDescriptor(String shortName);

    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified {@link ArchetypeId}.
     * <p/>
     * This implementation delegates to {@link #getArchetypeDescriptor(String)}. Subclasses should take into account
     * any version supplied with the identifier.
     *
     * @param id the archetype id
     * @return the archetype descriptor or null if there is no corresponding archetype descriptor for <tt>id</tt>
     */
    protected ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id) {
        return getArchetypeDescriptor(id.getShortName());
    }

    /**
     * This method will create a default object using the specified archetype
     * descriptor. Fundamentally, it will set the default value when specified
     * and it will also create an object through a default constructur if a
     * cardinality constraint is specified.
     *
     * @param descriptor the archetype descriptor
     * @return IMObject
     * @throws ArchetypeServiceException if it failed to create the object
     */
    protected IMObject create(ArchetypeDescriptor descriptor) {
        IMObject result;
        try {
            Class domainClass = Thread.currentThread().getContextClassLoader().loadClass(descriptor.getClassName());
            if (!IMObject.class.isAssignableFrom(domainClass)) {
                throw new ArchetypeServiceException(ArchetypeServiceException.ErrorCode.InvalidDomainClass,
                                                    descriptor.getClassName());
            }

            result = (IMObject) domainClass.newInstance();
            result.setArchetypeId(descriptor.getType());

            // first create a JXPath context and use it to process the nodes
            // in the archetype
            JXPathContext context = JXPathHelper.newContext(result);
            context.setFactory(new JXPathGenericObjectCreationFactory());
            create(context, descriptor.getNodeDescriptors());
        } catch (Exception exception) {
            // rethrow as a runtime exception
            throw new ArchetypeServiceException(ArchetypeServiceException.ErrorCode.FailedToCreateObject,
                                                exception, descriptor.getType().getShortName());
        }

        return result;
    }

    /**
     * Iterate through all the nodes in the archetype definition and create the default object.
     *
     * @param context the JXPath
     * @param nodes   the node descriptors for the archetype
     * @throws ArchetypeServiceException if the create fails
     */
    private void create(JXPathContext context, Map<String, NodeDescriptor> nodes) {
        for (NodeDescriptor node : nodes.values()) {
            // only create a node if it is a collection, or it has child nodes, or it has a default value
            if (node.isCollection() || node.getNodeDescriptorCount() > 0
                || !StringUtils.isEmpty(node.getDefaultValue())) {
                create(context, node);
            }

            for (AssertionDescriptor assertion : node.getAssertionDescriptorsAsArray()) {
                try {
                    assertion.create(context.getContextBean(), node);
                } catch (Exception exception) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.FailedToExecuteCreateFunction,
                            exception, assertion.getName());
                }
            }

            // if this node has children then process them recursively
            if (node.getNodeDescriptors().size() > 0) {
                create(context, node.getNodeDescriptors());
            }
        }
    }

    /**
     * Creates a node in the context, populating any default value.
     *
     * @param context the jxpath context
     * @param node    the node to create
     * @throws ArchetypeServiceException if the create fails
     */
    private void create(JXPathContext context, NodeDescriptor node) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to create path " + node.getPath() + " for node " + node.getName());
        }

        context.getVariables().declareVariable("node", node);
        context.createPath(node.getPath());

        String expression = node.getDefaultValue();
        if (!StringUtils.isEmpty(expression)) {
            if (log.isDebugEnabled()) {
                log.debug("evaluating default value expression for node " + node.getName() + " path " + node.getPath()
                          + " and expression " + expression);
            }
            Object value = context.getValue(expression);
            IMObject object = (IMObject) context.getContextBean();
            if (node.isCollection()) {
                if (value != null) {
                    if (Collection.class.isAssignableFrom(value.getClass())) {
                        for (Object v : (Collection) value) {
                            node.addChildToCollection(object, v);
                        }
                    } else {
                        node.addChildToCollection(object, value);
                    }
                }
            } else {
                node.setValue(object, value);
            }
        }
    }

}
