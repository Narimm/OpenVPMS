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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// log4j
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

// hibernate
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

// spring framework
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeQueryHelper;
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
@Deprecated
public class ArchetypeDataLoader {
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
     * A reference to the data that will be processed
     */
    private Document data;

    /**
     * Used to process the command line
     */
    private JSAP jsap = new JSAP();

    /**
     * The results once the commnad line has been processed
     */
    private JSAPResult config;

    /**
     * A reference to an XMLOutputter, which is used to format XML data
     */
    private XMLOutputter formatter = new XMLOutputter();

    /**
     * Process the records in the specified XML document
     * 
     * @param args
     *            the command line
     */
    public ArchetypeDataLoader(String[] args) throws Exception {
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
    private static void main(String[] args) throws Exception {
        ArchetypeDataLoader loader = new ArchetypeDataLoader(args);
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

            reset();
            Element root = getData(file).getRootElement();
            for (Object obj1 : root.getChildren()) {
                processElement(null, (Element) obj1, firstParse);
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
     * @param elem
     *            the element to process
     * @param firstParse
     *            indicates whether it is a first parse
     * @return IMObject the associated IMObject
     */
    private IMObject processElement(IMObject parent, Element elem,
            boolean firstParse) throws Exception {
        IMObject object = null;
        if (elem.getName().equals("data")) {
            // if its the first parse then don't process data that
            // contains attribute value references.
            if ((firstParse) && 
                (attributesContainReferences(elem.getAttributes()))) {
                return object;
            }

            if (!(firstParse) && 
                !(attributesContainReferences(elem.getAttributes()))) {
                return object;
            }

            // extract the optional id element
            String id = elem.getAttributeValue("id");

            // ensure that the archetype attribute is defined. If not
            // display an error.
            String shortName = elem.getAttributeValue("archetype");
            if (StringUtils.isEmpty(shortName)) {
                logger.error("\n[ERR: No archetype attribute defined" + "]\n"
                        + formatter.outputString(elem));
                return object;
            }

            ArchetypeDescriptor adesc = archetypeService
                    .getArchetypeDescriptor(shortName);
            if (adesc == null) {
                logger.error("\n[ERR: No archetype defined for  " + shortName
                        + "]\n" + formatter.outputString(elem));
                return object;
            }

            // create the default object and then iterate through the
            // attributed and set the appropriate node value
            object = archetypeService.create(adesc.getType());
            boolean valid = true;
            for (Object obj2 : elem.getAttributes()) {
                Attribute attr = (Attribute) obj2;
                if ((StringUtils.equals(attr.getName(), "archetype"))
                        || (StringUtils.equals(attr.getName(), "id"))
                        || (StringUtils.equals(attr.getName(), "collection"))) {
                    // no need to process the archetype, id or collection
                    // element
                    continue;
                }

                // if the attribute value is empty then just continue;
                if (StringUtils.isEmpty(attr.getValue())) {
                    continue;
                }

                NodeDescriptor ndesc = adesc.getNodeDescriptor(attr.getName());
                if (ndesc == null) {
                    logger.error("\n[ERR: Invalid Attribute " + attr.getName()
                            + "]\n" + formatter.outputString(elem));
                    valid = false;
                    break;
                }

                try {
                    if (isAttributeIdRef(attr)) {
                        processIdReference(object, attr, ndesc);
                    } else {
                        ndesc.setValue(object, attr.getValue());
                    }
                } catch (Exception exception) {
                    logger.error("\n[ERR: Trying to set attr " + attr.getName()
                            + " with value " + attr.getValue() + "]\n"
                            + exception + formatter.outputString(elem));
                    valid = false;
                    break;
                }
            }

            // check that we should continue processing this element.
            if (!valid) {
                return object;
            }

            // check if it has nested elements and process them before saving
            // the object
            if (elem.getChildren().size() > 0) {
                for (Object obj1 : elem.getChildren()) {
                    Element childElem = (Element) obj1;
                    String collectionNode = childElem
                            .getAttributeValue("collection");
                    if (StringUtils.isEmpty(collectionNode)) {
                        logger.error("\n[ERR: Failed to process child element "
                                + "]\n" + formatter.outputString(childElem));
                        continue;
                    }
                    
                    NodeDescriptor ndesc = adesc.getNodeDescriptor(collectionNode);
                    if (!ndesc.isCollection()) {
                        logger.error("\n[ERR: Failed to process child element. "
                                + "The collection node must be a collection"
                                + "]\n" + formatter.outputString(childElem));
                        continue;
                    }
                    
                    // now check whether the attribute childID is defined. If
                    // it is then we assume that it is a reference to a non
                    // parent-child collection element.
                    if (childElem.getAttribute("childId") != null) {
                        processIdReference(object, childElem.getAttribute("childId"), ndesc);
                        continue;
                    } else {
                        IMObject child = processElement(object, childElem, firstParse);
                        ndesc.addChildToCollection(object, child);
                    }
                }
            }

            // now if there are no errors the proceed to save the element. Note
            // that we only save the object if the parent is null.
            try {
                if (parent == null) {
                    archetypeService.save(object);
                    if (!StringUtils.isEmpty(id)) {
                        idCache.put(id, new IMObjectReference(object));
                    }

                    if (verbose) {
                        logger.info("\n[PROCESSED]\n"
                                + formatter.outputString(elem));
                    }
                }
            } catch (ValidationException validation) {
                logger.error("\n[ERR: Failed to process element. "
                        + "Validation errors follow].");
                for (ValidationError error : validation.getErrors()) {
                    logger.error(error.toString());
                }
                logger.error(formatter.outputString(elem));
            } catch (Exception exception) {
                logger.error("\n[ERR: Frror trying to save object" + "]\n"
                        + exception + formatter.outputString(elem));
            }
        }

        return object;
    }

    /**
     * Process the id reference attribute value and set it accordingly.
     * 
     * @param object
     *            the context object
     * @param attr
     *            the attribute which has an id reference
     * @param ndesc
     *            the corresponding node descriptor
     * @throws Exception
     *            propagate exception to caller            
     */
    private void processIdReference(IMObject object, Attribute attr, NodeDescriptor ndesc) {
        String ref = attr.getValue().substring("id:".length());
        IMObjectReference imref = idCache.get(ref);
        Object imobj = null;
        if (ndesc.isObjectReference()) {
            imobj = imref;
        } else {
            imobj = ArchetypeQueryHelper.getByObjectReference(archetypeService, 
                    imref);
        }

        if (ndesc.isCollection()) {
            ndesc.addChildToCollection(object, imobj);
        } else {
            ndesc.setValue(object, imobj);
        }
    }

    /**
     * @param attributes
     * @return
     */
    private boolean attributesContainReferences(List attributes) {
        for (Object attr : attributes) {
            if (isAttributeIdRef((Attribute) attr)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determine if the attribute value is for a reference. All reference values
     * start with id:
     * 
     * @param attribute
     *            the attribute to evaluate
     * @return boolean
     */
    private boolean isAttributeIdRef(Attribute attribute) {
        if (attribute.getValue().startsWith("id:")) {
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
     * Reset to the begining of the document
     */
    private void reset() {
        data = null;
    }

    /**
     * Get a reference to the data. This is a stateful attribute
     * 
     * @param file
     *            the file
     * @return Document
     */
    private Document getData(String file) throws Exception {
        if (data == null) {
            SAXBuilder builder = new SAXBuilder();
            data = builder.build(new File(file));
        }

        return data;
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
