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
 */

package org.openvpms.component.business.domain.im.archetype.descriptor;

import org.castor.xml.XMLProperties;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException.ErrorCode.MappingError;
import static org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException.ErrorCode.ReadError;
import static org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException.ErrorCode.WriteError;


/**
 * Helper to read/write descriptors.
 *
 * @author Tim Anderson
 */
class DescriptorIOHelper {

    /**
     * Reads an object from a stream using the mapping from the specified resource path.
     *
     * @param stream      the stream to read from
     * @param mappingPath the mapping resource path
     * @return the object
     * @throws DescriptorException if the read fails
     */
    public static Object read(InputStream stream, String mappingPath) {
        Object result;
        Mapping mapping = getMapping(mappingPath);
        result = read(stream, mapping, mappingPath);
        return result;
    }

    /**
     * Reads an object from a stream using the supplied mapping.
     *
     * @param stream  the stream to read from
     * @param mapping the mapping
     * @return the object
     * @throws DescriptorException if the read fails
     */
    public static Object read(InputStream stream, Mapping mapping) {
        Object result;
        result = read(stream, mapping, "<unknown mapping path>");
        return result;
    }

    /**
     * Writes an object to a stream using the mapping from the specified resource path.
     *
     * @param object      the object to write
     * @param mappingPath the mapping resource path
     * @throws DescriptorException if the write fails
     */
    public static void write(Object object, OutputStream stream, String mappingPath) {
        Mapping mapping = getMapping(mappingPath);
        write(object, stream, mapping, mappingPath, false, false);
    }

    /**
     * Writes an object to a stream using the mapping from the specified resource path.
     *
     * @param object  the object to write
     * @param mapping the mapping
     * @throws DescriptorException if the write fails
     */
    public static void write(Object object, OutputStream stream, Mapping mapping) {
        write(object, stream, mapping, false, false);
    }

    /**
     * Writes an object to a stream using the mapping from the specified resource path.
     *
     * @param object      the object to write
     * @param mapping     the mapping
     * @param fragment    if {@code true} omit xml declarations
     * @param prettyPrint if {@code true}, indent the XML and remove namespace and type information
     * @throws DescriptorException if the write fails
     */
    public static void write(Object object, OutputStream stream, Mapping mapping, boolean fragment,
                             boolean prettyPrint) {
        write(object, stream, mapping, "<unknown mapping path>", fragment, prettyPrint);
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
            InputSource source = new InputSource(new InputStreamReader(stream));
            source.setSystemId(path);
            mapping.loadMapping(source);
        } catch (Exception exception) {
            throw new DescriptorException(MappingError, exception, path);
        }
        return mapping;
    }

    /**
     * Reads an object from a stream using the supplied mapping.
     *
     * @param stream      the stream to read from
     * @param mapping     the mapping
     * @param mappingPath the mapping path, for error reporting purposes
     * @return the object
     * @throws DescriptorException if the read fails
     */
    private static Object read(InputStream stream, Mapping mapping, String mappingPath) {
        Object result;
        try {
            Unmarshaller unmarshaller = new Unmarshaller(mapping);
            result = unmarshaller.unmarshal(new InputStreamReader(stream));
        } catch (Exception exception) {
            throw new DescriptorException(ReadError, exception, mappingPath, exception.getMessage());
        }
        return result;
    }

    /**
     * Writes an object to a stream, using the supplied mapping.
     *
     * @param object      the object to write
     * @param stream      the stream to write to
     * @param mapping     the mapping
     * @param mappingPath the mapping path, for error reporting purposes
     * @param fragment    if {@code true} omit xml declarations
     * @param prettyPrint if {@code true}, indent the XML and remove namespace and type information
     * @throws DescriptorException if the write fails
     */
    private static void write(Object object, OutputStream stream, Mapping mapping, String mappingPath,
                              boolean fragment, boolean prettyPrint) {
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        try {
            Marshaller marshaller = new Marshaller(writer);
            marshaller.setMapping(mapping);
            if (prettyPrint) {
                marshaller.setSuppressNamespaces(true);
                marshaller.setSuppressXSIType(true);
                marshaller.setProperty(XMLProperties.USE_INDENTATION, "true");
            }
            marshaller.setMarshalAsDocument(!fragment);
            marshaller.marshal(object);
            writer.flush();
        } catch (Exception exception) {
            throw new DescriptorException(WriteError, exception, mappingPath, exception.getMessage());
        }
    }

}
