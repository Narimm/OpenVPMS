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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;


/**
 * Assertion type descriptors.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AssertionTypeDescriptors implements Serializable {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The map of descriptors, keyed on name.
     */
    private HashMap<String, AssertionTypeDescriptor> assertionTypeDescriptors =
            new HashMap<String, AssertionTypeDescriptor>();

    /**
     * The mapping resource path.
     */
    private static final String MAPPING
            = "org/openvpms/component/business/domain/im/archetype/descriptor/"
            + "assertion-type-mapping-file.xml";


    /**
     * Default constructor.
     */
    public AssertionTypeDescriptors() {
    }

    /**
     * Returns the descriptors.
     *
     * @return the descriptors
     */
    public HashMap<String, AssertionTypeDescriptor>
            getAssertionTypeDescriptors() {
        return assertionTypeDescriptors;
    }

    /**
     * Sets the descriptors.
     *
     * @param descriptors the descriptors to set
     */
    public void setAssertionTypeDescriptors(
            HashMap<String, AssertionTypeDescriptor> descriptors) {
        this.assertionTypeDescriptors = descriptors;
    }

    /**
     * Reads descriptors from a stream.
     *
     * @param stream the stream to read from
     * @return the read descriptors
     * @throws DescriptorException if the descriptors cannot be read
     */
    public static AssertionTypeDescriptors read(InputStream stream) {
        return (AssertionTypeDescriptors) DescriptorIOHelper.read(stream,
                                                                  MAPPING);
    }

    /**
     * Write descriptors to a stream.
     *
     * @param descriptors the descriptors to write
     * @param stream      the stream to write to
     * @throws DescriptorException if the descriptors cannot be written
     */
    public static void write(AssertionTypeDescriptors descriptors,
                             OutputStream stream) {
        DescriptorIOHelper.write(descriptors, stream, MAPPING);
    }

}
