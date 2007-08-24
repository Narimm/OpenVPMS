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

package org.openvpms.component.business.domain.im.archetype.descriptor;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import static org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException.ErrorCode.*;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


/**
 * Helper to read/write descriptors.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class DescriptorIOHelper {

    /**
     * Unmarsals an object from a stream using the mapping from the specified
     * resource path.
     *
     * @param stream      the stream to read from
     * @param mappingPath the mapping resource path
     * @return the object
     * @throws DescriptorException if the read fails
     */
    public static Object read(InputStream stream, String mappingPath) {
        Object result;
        Mapping mapping = getMapping(mappingPath);
        try {
            Unmarshaller unmarshaller = new Unmarshaller(mapping);
            result = unmarshaller.unmarshal(new InputStreamReader(stream));
        } catch (Exception exception) {
            throw new DescriptorException(ReadError, exception, mappingPath,
                                          exception.getMessage());
        }
        return result;
    }

    /**
     * Marshals an object to a stream using the mapping from the specified
     * resource path.
     *
     * @param object      the object to write
     * @param mappingPath the mapping resource path
     * @throws DescriptorException if the write fails
     */
    public static void write(Object object, OutputStream stream,
                             String mappingPath) {
        Mapping mapping = getMapping(mappingPath);
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        try {
            Marshaller marshaller = new Marshaller(writer);
            marshaller.setMapping(mapping);
            marshaller.setMarshalAsDocument(true);
            marshaller.marshal(object);
            writer.flush();
        } catch (Exception exception) {
            throw new DescriptorException(WriteError, exception, mappingPath,
                                          exception.getMessage());
        }
    }

    /**
     * Loads the castor mapping at the specified resource path.
     *
     * @param path the resource path
     * @return the castor mapping
     * @throws DescriptorException if the mapping can't be loaded
     */
    public static Mapping getMapping(String path) {
        Mapping mapping = new Mapping();
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = loader.getResourceAsStream(path);
            mapping.loadMapping(new InputSource(new InputStreamReader(stream)));
        } catch (Exception exception) {
            throw new DescriptorException(MappingError, exception, path);
        }
        return mapping;
    }

}
