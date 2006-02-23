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

package org.openvpms.tools.data.loader;

// java core
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

// log4j
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

// spring framework
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.tools.archetype.loader.ArchetypeLoader;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;

/**
 * This tool will reads the specified XML document and process all of the
 * elements. It creates a spring application context to load the appropriate
 * services.
 * <p>
 * It will process the all data items using two passes. The first pass will
 * process and store data elements that do not reference other data elements.
 * The second pass will process and store data elements that make reference to
 * other elements.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class StaxArchetypeDataLoader {
    /**
     * Define a logger for this class
     */
    private Logger logger;

    /**
     * A reference to the application context
     */
    private ApplicationContext context;

    /**
     * a reference to the archetype service
     */
    private IArchetypeService archetypeService;

    /**
     * A reference to the idCache
     */
    private Map<String, IMObjectReference> idCache = new HashMap<String, IMObjectReference>();

    /**
     * A linkId to id cache
     */
    private Map<String, String> linkIdCache = new HashMap<String, String>();

    /**
     * Indicates whether the directory search is recursive
     */
    private boolean subdir;

    /**
     * Specifies the file extension to filter. Defaults to adl
     */
    private String extension = "xml";

    /**
     * Indicates whether to display verbose information while loading the
     * archetypes
     */
    private boolean verbose;

    /**
     * Used to process the command line
     */
    private JSAP jsap = new JSAP();

    /**
     * The results once the commnad line has been processed
     */
    private JSAPResult config;

    /**
     * Process the records in the specified XML document
     * 
     * @param args
     *            the command line
     */
    public StaxArchetypeDataLoader(String[] args) throws Exception {
        // init
        init();

        config = jsap.parse(args);
        if (!config.success()) {
            displayUsage();
        }
    }

    /**
     * The main line
     * 
     * @param args
     *            the file where the data is stored is passed in as the first
     *            argument
     */
    public static void main(String[] args) throws Exception {
        StaxArchetypeDataLoader loader = new StaxArchetypeDataLoader(args);
        loader.load();
    }

    /**
     * Load all the elements
     * 
     * @throws Exception
     */
    private void load() throws Exception {
        verbose = config.getBoolean("verbose");
        if (config.getString("file") != null) {
            processFile(config.getString("file"));
        } else if (config.getString("dir") != null) {
            processDir(config.getString("dir"));
        } else {
            displayUsage();
        }
    }

    /**
     * Process all the files in the directory
     * 
     * @param the
     *            directory
     */
    private void processDir(String dir) throws Exception {
        String[] extensions = { extension };
        Collection collection = FileUtils.listFiles(new File(dir), extensions,
                subdir);
        for (Object obj : collection) {
            File file = (File) obj;
            processFile(file.getAbsolutePath());
        }
    }

    /**
     * Process the data elements.
     * 
     * @param file
     *            the file to process
     * @throws Exception
     *             propagate all exceptions to client
     */
    private void processFile(String file) throws Exception {
        for (int iteration = 0; iteration < 2; iteration++) {
            boolean firstParse = (iteration == 0 ? true : false);

            if (firstParse) {
                logger.info("\n[Executing first parse for : " + file + "]\n");
            } else {
                logger.info("\n[Executing second parse for : " + file + "]\n");
            }

            XMLStreamReader reader = getReader(file);
            Stack<IMObject> stack = new Stack<IMObject>();
            for (int event = reader.next(); event != XMLStreamConstants.END_DOCUMENT; event = reader
                    .next()) {
                IMObject current = null;
                switch (event) {
                case XMLStreamConstants.START_DOCUMENT:
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    if (reader.getLocalName().equals("data")) {
                        try {
                            if (stack.size() > 0) {
                                current = processElement(stack.peek(), reader,
                                        firstParse);
                            } else {
                                current = processElement(null, reader,
                                        firstParse);
                            }
                            
                            if (current != null) {
                                stack.push(current);
                            }
                        } catch (Exception exception) {
                            logger.error(exception);
                        }
                    }
                    break;

                case XMLStreamConstants.END_DOCUMENT:
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (stack.size() > 0) {
                        current = stack.pop();
                        try {
                            if (stack.size() == 0) {
                                archetypeService.save(current);
                                String id = linkIdCache.remove(current.getLinkId());
                                if (id != null) {
                                    idCache.put(id, new IMObjectReference(current));
                                }
    
                                if (verbose) {
                                    logger.info("\n[CREATED]\n"
                                            + current.toString());
                                }
                            }
                        } catch (ValidationException validation) {
                            logger.error("\n[ERR: Failed to process element. "
                                    + "Validation errors follow].");
                            for (ValidationError error : validation.getErrors()) {
                                logger.error(error.toString());
                            }
                            logger.error(current.toString());
                        } catch (Exception exception) {
                            logger.error("\n[ERR: Frror trying to save object" + "]\n"
                                    + exception + "\n" + current.toString());
                        }
                    }                    
                    break;

                default:
                    break;
                }
            }
        }
    }

    /**
     * Process the specified element and all nested elements. If the parent
     * object is specified then then the specified element is in a parent child
     * relationship.
     * <p>
     * The archetype attribute determines the archetype we need to us to create
     * from the element. Iterate through all the element attributes and attempt
     * to set the specified value using the archetype's node descriptors. The
     * attribute name must correspond to a valid node descriptor
     * <p>
     * 
     * @param parent
     *            the parent of this element if it exists
     * @param reader
     *            the xml stream element
     * @param firstParse
     *            indicates whether it is a first parse
     * @return IMObject the associated IMObject
     */
    private IMObject processElement(IMObject parent, XMLStreamReader reader,
            boolean firstParse) throws Exception {
        IMObject object = null;
        if (reader.getLocalName().equals("data")) {
            // if its the first parse then don't process data that
            // contains attribute value references.
            if ((firstParse) && (attributesContainReferences(reader))) {
                return object;
            }

            if (!(firstParse) && !(attributesContainReferences(reader))) {
                return object;
            }

            // extract the optional id element
            String id = reader.getAttributeValue(null, "id");

            // ensure that the archetype attribute is defined. If not
            // display an error.
            String shortName = reader.getAttributeValue(null, "archetype");
            if (StringUtils.isEmpty(shortName)) {
                logger.error("\n[ERR: No archetype attribute defined" + "]\n"
                        + formatElement(reader));
                return object;
            }

            ArchetypeDescriptor adesc = archetypeService
                    .getArchetypeDescriptor(shortName);
            if (adesc == null) {
                logger.error("\n[ERR: No archetype defined for  " + shortName
                        + "]\n" + formatElement(reader));
                return object;
            }

            // create the default object and then iterate through the
            // attributed and set the appropriate node value
            object = archetypeService.create(adesc.getType());
            boolean valid = true;
            for (int index = 0; index < reader.getAttributeCount(); index++) {
                String attName = reader.getAttributeLocalName(index);
                String attValue = reader.getAttributeValue(index);

                if ((StringUtils.equals(attName, "archetype"))
                        || (StringUtils.equals(attName, "id"))
                        || (StringUtils.equals(attName, "collection"))) {
                    // no need to process the archetype, id or collection
                    // element
                    continue;
                }

                // if the attribute value is empty then just continue;
                if (StringUtils.isEmpty(attValue)) {
                    continue;
                }

                NodeDescriptor ndesc = adesc.getNodeDescriptor(attName);
                if (ndesc == null) {
                    logger.error("\n[ERR: Invalid Attribute " + attName + "]\n"
                            + formatElement(reader));
                    valid = false;
                    break;
                }

                try {
                    if (isAttributeIdRef(attValue)) {
                        String ref = attValue.substring("id:".length());
                        IMObjectReference imref = idCache.get(ref);
                        Object imobj = null;
                        if (ndesc.isObjectReference()) {
                            imobj = imref;
                        } else {
                            imobj = archetypeService.get(imref);
                        }

                        if (ndesc.isCollection()) {
                            ndesc.addChildToCollection(object, imobj);
                        } else {
                            ndesc.setValue(object, imobj);
                        }
                    } else {
                        ndesc.setValue(object, attValue);
                    }
                } catch (Exception exception) {
                    logger.error("\n[ERR: Trying to set attr " + attName
                            + " with value " + attValue + "]\n"
                            + exception + "\n" + formatElement(reader));
                    valid = false;
                    break;
                }
            }

            // check whether there is a collection attribute specified. If it is
            // then we may need to set up a parent child relationship.
            String collectionNode = reader.getAttributeValue(null, "collection");
            if ((valid) && (!StringUtils.isEmpty(collectionNode))) {
                if (parent == null) {
                    logger.error("\n[ERR: No parent for child element "
                        + "]\n" + formatElement(reader));
                } else {
                    ArchetypeDescriptor padesc= archetypeService
                        .getArchetypeDescriptor(parent.getArchetypeId());
                    if (padesc == null) {
                        logger.error("\n[ERR: No archetypeId for parent "
                                + "]\n" + parent.toString());
                    } else {
                        NodeDescriptor ndesc = padesc.getNodeDescriptor(collectionNode);
                        if ((ndesc.isCollection()) && (ndesc.isParentChild())) {
                            ndesc.addChildToCollection(parent, object);
                        } else {
                            logger.error("\n[ERR: Failed to process child element. "
                                + "The collection node must a parent-child coll"
                                + "]\n"
                                + formatElement(reader));
                        }
                    }
                }
            }
            
            if (valid) {
                if (!StringUtils.isEmpty(id)) {
                    linkIdCache.put(object.getLinkId(), id);
                }
            } else {
                object = null;
            }
        }
            
        return object;
    }

    /**
     * @param reader
     * @return
     */
    private String formatElement(XMLStreamReader reader) {
        StringBuffer buf = new StringBuffer("<");
        buf.append(reader.getLocalName());
        buf.append(" ");
        for (int index = 0; index < reader.getAttributeCount(); index++) {
            buf.append(reader.getAttributeLocalName(index));
            buf.append("=\"");
            buf.append(reader.getAttributeValue(index));
            buf.append("\" ");
        }
        buf.append(">");
        
        return buf.toString();
    }

    /**
     * Iterate through the the attributes and determine whether the an id
     * attribute is defined
     * 
     * @param reader
     * @return boolean
     */
    private boolean attributesContainReferences(XMLStreamReader reader) {
        for (int index = 0; index < reader.getAttributeCount(); index++) {
            if (isAttributeIdRef(reader.getAttributeValue(index))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determine if the attribute value is for a reference. All reference values
     * start with id:
     * 
     * @param value
     *            the attribute value to evaluate
     * @return boolean
     */
    private boolean isAttributeIdRef(String value) {
        if (value.startsWith("id:")) {
            return true;
        }

        return false;
    }

    /**
     * Initialise and start the spring container
     */
    private void init() throws Exception {
        context = new ClassPathXmlApplicationContext(
                "org/openvpms/tools/data/loader/archetype-data-loader-appcontext.xml");
        archetypeService = (IArchetypeService) context
                .getBean("archetypeService");

        // set up the options for the command line and creater the logger
        createOptions();
        createLogger();
    }

    /**
     * Return the XMLReader for the specified file
     * 
     * @param file
     *            the file
     * @return XMLStreamReader
     * @throws Exception
     *             propagate to the caller
     */
    private XMLStreamReader getReader(String file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        XMLInputFactory factory = (XMLInputFactory) XMLInputFactory
                .newInstance();

        return (XMLStreamReader) factory.createXMLStreamReader(fis);
    }

    /**
     * Configure the options for this applications
     * 
     * @throws Exception
     *             let the caller handle the error
     */
    private void createOptions() throws Exception {
        jsap.registerParameter(new FlaggedOption("dir").setShortFlag('d')
                .setLongFlag("dir").setHelp(
                        "Directory where data files reside."));
        jsap.registerParameter(new Switch("subdir").setShortFlag('s')
                .setLongFlag("subdir").setDefault("false").setHelp(
                        "Search the subdirectories as well."));
        jsap.registerParameter(new FlaggedOption("file").setShortFlag('f')
                .setLongFlag("file").setHelp("Name of file containing data"));
        jsap.registerParameter(new Switch("verbose").setShortFlag('v')
                .setLongFlag("verbose").setDefault("false").setHelp(
                        "Displays verbose info to the console."));
    }

    /**
     * Creater the logger
     * 
     * @throws Exception
     */
    private void createLogger() throws Exception {
        BasicConfigurator.configure();

        // set the root logger level to error
        Logger.getRootLogger().setLevel(Level.ERROR);
        Logger.getRootLogger().removeAllAppenders();

        logger = Logger.getLogger(ArchetypeLoader.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
    }

    /**
     * Print usage information
     */
    private void displayUsage() {
        System.err.println();
        System.err
                .println("Usage: java " + ArchetypeDataLoader.class.getName());
        System.err.println("                " + jsap.getUsage());
        System.err.println();
        System.err.println(jsap.getHelp());
        System.exit(1);

    }

}
