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


package org.openvpms.component.business.domain.im.archetype.descriptor;

// openvpms-framework

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * All the descriptor classes inherit from this base class, which provides
 * support for identity, hibernate and serialization
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class Descriptor extends IMObject {

    /**
     * An enumeration of different descriptor types
     */
    public enum DescriptorType {
        ArchetypeDescriptor,
        NodeDescriptor,
        AssertionDescriptor,
        PropertyDescriptor,
        AssertionTypeDescriptor
    }

    /**
     * An enumeration of the different validation errors
     */
    public enum ValidationError {
        IsRequired,
        DuplicateNodeDescriptor
    }

    /**
     * Default constructor
     */
    public Descriptor() {
        super();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Returns the class for the specified class name.
     *
     * @param className may be null/empty
     * @return the class, or <tt>null</tt> if <tt>className</tt> null/empty
     * @throws DescriptorException if the class can't be loaded
     */
    protected Class getClass(String className) {
        Class result;
        if (StringUtils.isEmpty(className)) {
            result = null;
        } else {
            try {
                result = Thread.currentThread().getContextClassLoader()
                        .loadClass(className);
            } catch (Exception exception) {
                throw new DescriptorException(
                        DescriptorException.ErrorCode.InvalidType,
                        exception, className);
            }
        }
        return result;
    }
}
