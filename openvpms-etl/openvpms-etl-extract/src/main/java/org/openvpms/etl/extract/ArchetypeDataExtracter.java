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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.extract;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.LongStringParser;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.tools.data.loader.StaxArchetypeDataLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extracts data from an OpenVPMS database, in the format expected by {@link StaxArchetypeDataLoader}.
 *
 * @author Tim Anderson
 */
public class ArchetypeDataExtracter {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * If {@code true}, only extract active instances of the specified archetypes. Referenced objects may be inactive.
     */
    private final boolean activeOnly;

    /**
     * If true, output related objects.
     */
    private final boolean related;

    /**
     * The writer.
     */
    private final XMLStreamWriter writer;

    /**
     * Used to format dates in a format acceptable to {@link StaxArchetypeDataLoader}. TODO
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * The references of objects yet to be output.
     */
    private Set<IMObjectReference> pending = new LinkedHashSet<>();

    /**
     * The set of references of output objects.
     */
    private final Set<IMObjectReference> processed = new HashSet<>();

    /**
     * The archetype short names that are required but were not output.
     */
    private final Set<String> dependencies = new HashSet<>();

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";

    /**
     * Constructs an {@link ArchetypeDataExtracter}.
     *
     * @param service    the archetype service
     * @param activeOnly if {@code true}, only extract active instances of the specified archetypes. Referenced objects
     * @param related    if {@code true}, extract related objects
     * @param out        the output stream
     * @throws XMLStreamException for an XML error
     */
    public ArchetypeDataExtracter(IArchetypeService service, boolean activeOnly, boolean related, OutputStream out)
            throws XMLStreamException {
        this.service = service;
        this.activeOnly = activeOnly;
        this.related = related;
        writer = new Writer(XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8"));
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("archetype");
    }

    /**
     * Extracts objects matching the supplied archetypes.
     *
     * @param archetypes the archetypes
     * @throws XMLStreamException for any XML error
     */
    public void extract(String[] archetypes) throws XMLStreamException {
        String[] shortNames = DescriptorHelper.getShortNames(archetypes);
        for (String shortName : shortNames) {
            extract(shortName, -1, shortNames);
        }
        dependencies.removeAll(Arrays.asList(shortNames));
    }

    /**
     * Extracts objects matching the supplied archetype(s).
     *
     * @param archetype the archetype(s). May contain wildcards
     * @param id        the object identifier, or {@code -1} if retrieving multiple objects
     * @throws XMLStreamException for any XML error
     */
    public void extract(String archetype, long id) throws XMLStreamException {
        String[] shortNames = DescriptorHelper.getShortNames(archetype);
        for (String shortName : shortNames) {
            extract(shortName, id, shortNames);
        }
        dependencies.removeAll(Arrays.asList(shortNames));
    }

    /**
     * Closes the extracter.
     *
     * @throws XMLStreamException for any XML error
     */
    public void close() throws XMLStreamException {
        if (!dependencies.isEmpty()) {
            writer.writeCharacters("\n");
            List<String> list = new ArrayList<>(dependencies);
            Collections.sort(list);
            writer.writeComment("\nThe following archetypes were referenced but not output:\n"
                                + StringUtils.join(list, "\n") + "\n");
        }
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
    }

    /**
     * Main line.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            JSAP parser = createParser();
            JSAPResult config = parser.parse(args);
            if (!config.success()) {
                displayUsage(parser);
            } else {
                String contextPath = config.getString("context");
                String archetypes[] = config.getStringArray("archetype");
                long id = config.getLong("id");
                String file = config.getString("file");
                boolean inactive = config.getBoolean("inactive");
                boolean related = config.getBoolean("related");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }

                OutputStream out = (file != null) ? new FileOutputStream(file) : System.out;

                if (archetypes != null && archetypes.length > 0) {
                    IArchetypeService service = (IArchetypeService) context.getBean("archetypeService");
                    ArchetypeDataExtracter extract = new ArchetypeDataExtracter(service, !inactive, related, out);
                    if (archetypes.length == 1) {
                        extract.extract(archetypes[0], id);
                    } else {
                        extract.extract(archetypes);
                    }
                    extract.close();
                } else {
                    displayUsage(parser);
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("context").setShortFlag('c')
                                         .setLongFlag("context")
                                         .setDefault(APPLICATION_CONTEXT)
                                         .setHelp("Application context path"));
        parser.registerParameter(new FlaggedOption("archetype").setShortFlag('a').setLongFlag("archetype")
                                         .setAllowMultipleDeclarations(true).setHelp("Archetype short name."));
        parser.registerParameter(new FlaggedOption("id").setShortFlag('i')
                                         .setLongFlag("id")
                                         .setDefault("-1")
                                         .setStringParser(LongStringParser.getParser())
                                         .setHelp("Object identifier."));
        parser.registerParameter(new FlaggedOption("file").setShortFlag('f')
                                         .setLongFlag("file").setHelp("Output file name."));
        parser.registerParameter(new Switch("related").setShortFlag('r')
                                         .setLongFlag("related")
                                         .setHelp("Extract related objects."));
        parser.registerParameter(new Switch("inactive")
                                         .setLongFlag("inactive")
                                         .setHelp("Include inactive objects of the specified archetypes. "
                                                  + "Referenced objects may be inactive"));
        return parser;
    }

    /**
     * Prints usage information.
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err.println("Usage: java " + ArchetypeDataExtracter.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

    /**
     * Extracts objects matching the supplied archetype(s).
     *
     * @param archetype the archetype. Should not contain wildcards
     * @param id        the object identifier, or {@code -1} if retrieving multiple objects
     * @param include   archetypes to include in the extraction. If related objects aren't being automatically pulled
     *                  in, these determine if references to inactive objects should be included
     * @throws XMLStreamException for any XML error
     */
    private void extract(String archetype, long id, String[] include) throws XMLStreamException {
        ArchetypeQuery query = new ArchetypeQuery(archetype, false, activeOnly);
        if (id > 0) {
            query.add(Constraints.eq("id", id));
        }
        query.add(Constraints.sort("id"));
        IMObjectQueryIterator<IMObject> iterator = new IMObjectQueryIterator<>(service, query);
        while (iterator.hasNext()) {
            extract(iterator.next(), include);
        }
        while (!pending.isEmpty()) {
            IMObjectReference next = pending.iterator().next();
            pending.remove(next);
            if (!processed.contains(next)) {
                IMObject child = service.get(next);
                if (child != null) {
                    extract(child, include);
                }
            }
        }
    }

    /**
     * Extracts an object.
     *
     * @param object  the object
     * @param include archetypes to include in the extraction. If related objects aren't being automatically pulled
     *                in, these determine if references to inactive objects should be included
     * @throws XMLStreamException for any XML error
     */
    private void extract(IMObject object, String[] include) throws XMLStreamException {
        IMObjectReference reference = object.getObjectReference();
        if (processed.add(reference)) {
            write(object, null, include);
            writer.writeCharacters("\n");
        }
    }

    /**
     * Returns the objects from a collection.
     * <p/>
     * This filters out any relationships where the object is the target, as it expected these will be handled
     * from the source object.
     *
     * @param bean the object bean
     * @param node the collection node name
     * @return the collection objects
     */
    private List<IMObject> getCollection(IMObjectBean bean, String node) {
        IMObjectReference reference = bean.getReference();
        List<IMObject> result = new ArrayList<>();
        List<IMObject> values = bean.getValues(node, IMObject.class);
        for (IMObject value : values) {
            if (value instanceof IMObjectRelationship) {
                if (!ObjectUtils.equals(reference, ((IMObjectRelationship) value).getTarget())) {
                    result.add(value);
                }
            } else {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Writes a reference to an object in a collection.
     *
     * @param object     the object
     * @param collection the collection node name
     * @param include    archetypes to include in the extraction. If related objects aren't being automatically pulled
     *                   in, these determine if references to inactive objects should be included
     * @throws XMLStreamException for any XML error
     */
    private void writeCollectionReference(IMObject object, String collection, String[] include)
            throws XMLStreamException {
        writer.writeEmptyElement("data");
        writer.writeAttribute("collection", collection);
        writer.writeAttribute("archetype", object.getArchetypeId().getShortName());
        writer.writeAttribute("childId", getReference(object.getArchetypeId(), object.getId()));
        queue(object.getObjectReference(), include);
    }

    /**
     * Queues an object for output, if related objects are being output and it hasn't already been processed.
     *
     * @param reference the object reference
     * @param include   archetypes to include in the extraction. If related objects aren't being automatically pulled
     *                  in, these determine if references to inactive objects should be included
     */
    private void queue(IMObjectReference reference, String[] include) {
        if (!related && !TypeHelper.isA(reference, include)) {
            dependencies.add(reference.getArchetypeId().getShortName());
        } else if (!processed.contains(reference)) {
            pending.add(reference);
        }
    }

    /**
     * Writes an object to the stream.
     *
     * @param object     the object to write
     * @param collection the collection node name, or {@code null} if it is not part of a collection
     * @param include    archetypes to include in the extraction. If related objects aren't being automatically pulled
     *                   in, these determine if references to inactive objects should be included
     * @throws XMLStreamException for any XML error
     */
    private void write(IMObject object, String collection, String[] include) throws XMLStreamException {
        IMObjectBean bean = new IMObjectBean(object, service);
        List<NodeDescriptor> nodes = new ArrayList<>();
        Map<NodeDescriptor, List<IMObject>> collectionNodes = new LinkedHashMap<>();
        ArchetypeDescriptor archetype = bean.getArchetype();
        for (NodeDescriptor node : archetype.getAllNodeDescriptors()) {
            if (!node.isDerived()) {
                if (node.isCollection()) {
                    List<IMObject> objects = getCollection(bean, node.getName());
                    if (!objects.isEmpty()) {
                        collectionNodes.put(node, objects);
                    }
                } else {
                    nodes.add(node);
                }
            }
        }

        if (!collectionNodes.isEmpty()) {
            writer.writeStartElement("data");
        } else {
            writer.writeEmptyElement("data");
        }
        if (collection != null) {
            writer.writeAttribute("collection", collection);
        }
        writer.writeAttribute("archetype", object.getArchetypeId().getShortName());

        for (NodeDescriptor node : nodes) {
            String name = node.getName();
            String value = null;
            if (node.isObjectReference()) {
                IMObjectReference child = bean.getReference(name);
                if (child != null) {
                    queue(child, include);
                    value = getReference(child.getArchetypeId(), child.getId());
                }
            } else if ("id".equals(name)) {
                if (collection == null) {
                    value = getId(object.getArchetypeId(), object.getId());
                }
            } else if (node.isDate()) {
                Date date = bean.getDate(name);
                if (date != null) {
                    value = dateFormat.format(date);
                }
            } else {
                value = bean.getString(name);
            }
            if (value != null) {
                writer.writeAttribute(name, value);
            } else if (!StringUtils.isEmpty(node.getDefaultValue())) {
                writer.writeAttribute(name, "");
            }
        }
        for (Map.Entry<NodeDescriptor, List<IMObject>> entry : collectionNodes.entrySet()) {
            NodeDescriptor node = entry.getKey();
            List<IMObject> objects = entry.getValue();
            String name = node.getName();
            for (IMObject child : objects) {
                if (node.isParentChild()) {
                    write(child, name, include);
                } else {
                    writeCollectionReference(child, name, include);
                }
            }
        }
        if (!collectionNodes.isEmpty()) {
            writer.writeEndElement();
        }
    }

    /**
     * Generates a reference to an object in the format required by {@link StaxArchetypeDataLoader}.
     *
     * @param archetypeId the object archetype identifier
     * @param id          the object identifier
     * @return the object reference
     */
    private String getReference(ArchetypeId archetypeId, long id) {
        return "id:" + getId(archetypeId, id);
    }

    /**
     * Generates an identifier for an object.
     *
     * @param archetypeId the object archetype identifier
     * @param id          the object identifier
     * @return the identifier
     */
    private String getId(ArchetypeId archetypeId, long id) {
        return archetypeId.getShortName() + "-" + id;
    }

    /**
     * An XMLStreamWriter that pretty-prints output.
     */
    private static class Writer implements XMLStreamWriter {

        private final XMLStreamWriter writer;

        private int depth = 0;

        public Writer(XMLStreamWriter writer) {
            this.writer = writer;
        }

        @Override
        public void close() throws XMLStreamException {
            writer.close();
        }

        @Override
        public void flush() throws XMLStreamException {
            writer.flush();
        }

        @Override
        public NamespaceContext getNamespaceContext() {
            return writer.getNamespaceContext();
        }

        @Override
        public String getPrefix(String uri) throws XMLStreamException {
            return writer.getPrefix(uri);
        }

        @Override
        public Object getProperty(String name) throws IllegalArgumentException {
            return writer.getProperty(name);
        }

        @Override
        public void setDefaultNamespace(String uri) throws XMLStreamException {
            writer.setDefaultNamespace(uri);
        }

        @Override
        public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
            writer.setNamespaceContext(context);
        }

        @Override
        public void setPrefix(String prefix, String uri) throws XMLStreamException {
            writer.setPrefix(prefix, uri);
        }

        @Override
        public void writeAttribute(String localName, String value) throws XMLStreamException {
            writer.writeAttribute(localName, value);
        }

        @Override
        public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
            writer.writeAttribute(namespaceURI, localName, value);
        }

        @Override
        public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
                throws XMLStreamException {
            writer.writeAttribute(prefix, namespaceURI, localName, value);
        }

        @Override
        public void writeCData(String data) throws XMLStreamException {
            writer.writeCData(data);
        }

        @Override
        public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
            writer.writeCharacters(text, start, len);
        }

        @Override
        public void writeCharacters(String text) throws XMLStreamException {
            writer.writeCharacters(text);
        }

        @Override
        public void writeComment(String data) throws XMLStreamException {
            writer.writeComment(data);
        }

        @Override
        public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
            writer.writeDefaultNamespace(namespaceURI);
        }

        @Override
        public void writeDTD(String dtd) throws XMLStreamException {
            writer.writeDTD(dtd);
        }

        @Override
        public void writeEmptyElement(String localName) throws XMLStreamException {
            newElement();
            writer.writeEmptyElement(localName);
        }

        @Override
        public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
            newElement();
            writer.writeEmptyElement(namespaceURI, localName);
        }

        @Override
        public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            newElement();
            writer.writeEmptyElement(prefix, namespaceURI, localName);
        }

        @Override
        public void writeEndDocument() throws XMLStreamException {
            writer.writeEndDocument();
        }

        @Override
        public void writeStartElement(String localName) throws XMLStreamException {
            startElement();
            writer.writeStartElement(localName);
        }

        @Override
        public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
            startElement();
            writer.writeStartElement(namespaceURI, localName);
        }

        @Override
        public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            startElement();
            writer.writeStartElement(prefix, namespaceURI, localName);
        }

        @Override
        public void writeEndElement() throws XMLStreamException {
            endElement();
            writer.writeEndElement();
        }

        @Override
        public void writeEntityRef(String name) throws XMLStreamException {
            writer.writeEntityRef(name);
        }

        @Override
        public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
            writer.writeNamespace(prefix, namespaceURI);
        }

        @Override
        public void writeProcessingInstruction(String target) throws XMLStreamException {
            writer.writeProcessingInstruction(target);
        }

        @Override
        public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
            writer.writeProcessingInstruction(target, data);
        }

        @Override
        public void writeStartDocument() throws XMLStreamException {
            writer.writeStartDocument();
            writer.writeCharacters("\n");
        }

        @Override
        public void writeStartDocument(String version) throws XMLStreamException {
            writer.writeStartDocument(version);
            writer.writeCharacters("\n");
        }

        @Override
        public void writeStartDocument(String encoding, String version) throws XMLStreamException {
            writer.writeStartDocument(encoding, version);
            writer.writeCharacters("\n");
        }

        private void startElement() throws XMLStreamException {
            newElement();
            depth++;
        }

        private void endElement() throws XMLStreamException {
            writer.writeCharacters("\n");
            depth--;
            indent();
        }

        private void newElement() throws XMLStreamException {
            if (depth > 0) {
                writer.writeCharacters("\n");
            }
            indent();
        }

        private void indent() throws XMLStreamException {
            for (int i = 0; i < depth; ++i) {
                writer.writeCharacters("  ");
            }
        }
    }

}
