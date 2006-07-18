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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
     * The default name of the application context file
     */
    private final static String DEFAULT_APP_CONTEXT_FNAME = "application-context.xml";
    
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
     * Indicates whether it should only validate the data
     */
    private boolean validateOnly;
    
    /**
     * A sequence id generator
     */
    private int sequenceId;

    /**
     * A reference to the idCache
     */
    private Cache idCache;
    
    /**
     * The batch size that we should use for saving objects
     */
    private int batchSaveSize = 0;
    
    /**
     * The cache for batching saved objects
     */
    private ArrayList batchSaveCache = new ArrayList();

    /**
     * A linkId to id cache
     */
    private Cache linkIdCache;
    
    /**
     * A cache of unprocessed elements
     */
    private Cache unprocessedElementCache;
    
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
     * Maintains a list of archetypes and count to indicate the number of 
     * each saved or validated
     */
    private Map<String, Long> statistics = new HashMap<String, Long>();
    
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
        // set up the command line options and the logger 
        createOptions();
        createLogger();
        
        // process the configuration 
        config = jsap.parse(args);
        if (!config.success()) {
            displayUsage();
        }
        // init
        init();

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
        // set some of the configuration options
        verbose = config.getBoolean("verbose");
        validateOnly = config.getBoolean("validateOnly");
        batchSaveSize = config.getInt("batchSaveSize");
        
        if (config.getString("file") != null) {
            // first parse
            processFile(config.getString("file"));
            
            // second parse
            processUnprocessedElementCache();
            
            // force a flush on the cache
            flushBatchSaveCache();
            
            // dump the statistics
            dumpStatistics();
        } else if (config.getString("dir") != null) {
            // process the files
            processDir(config.getString("dir"));
            
            // dump the statistics;
            dumpStatistics();
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
        processUnprocessedElementCache();
        
        // force a flush on the cache
        flushBatchSaveCache();
    }

    /**
     * Process the data elements.
     * 
     * @param file
     *            the file to process
     * @param boolean
     *            if this is the first parse for this file            
     * @throws Exception
     *             propagate all exceptions to client
     */
    private void processFile(String file) throws Exception {
        logger.info("\n[PROCESSING FILE : " + file + "]\n");

        XMLStreamReader reader = getReader(file);
        StringBuffer elemData = new StringBuffer("<archetype>");
        Stack<IMObject> stack = new Stack<IMObject>();
        boolean hasReference = false;
        for (int event = reader.next(); event != XMLStreamConstants.END_DOCUMENT; event = reader
                .next()) {
            IMObject current = null;
            switch (event) {
            case XMLStreamConstants.START_DOCUMENT:
                break;

            case XMLStreamConstants.START_ELEMENT:
                buildElementData(event, reader, elemData);
                if (verbose) {
                    logger.info("\n[START PROCESSING element=" 
                            + reader.getLocalName() 
                            + " parent=" + (stack.size() == 0 
                            ? "none" : (stack.peek() == null) ? "unknown" :
                                stack.peek().getArchetypeIdAsString()) 
                            + "]\n" + formatElement(reader));
                }
                
                if (reader.getLocalName().equals("data")) {
                    try {
                        hasReference |= attributesContainReferences(reader);
                        if (!hasReference) {
                            // process the element
                            if (stack.size() > 0) {
                                if (stack.peek() != null) {
                                    current = processElement(stack.peek(), reader);
                                }
                            } else {
                                current = processElement(null, reader);
                            }
                        }
                        
                        stack.push(current);
                    } catch (Exception exception) {
                        logger.error("Error in start element\n" +
                                formatElement(reader) + "\n", exception);
                    }
                }
                break;

            case XMLStreamConstants.END_DOCUMENT:
                break;

            case XMLStreamConstants.END_ELEMENT:
                buildElementData(event, reader, elemData);
                if (stack.size() > 0) {
                    current = stack.pop();
                    try {
                        if (stack.size() == 0) {
                            elemData.append("</archetype>");
                            if (hasReference) {
                                // if this element has a reference then we 
                                // need to stick it in the unprocessed element
                                // cache and process it in a second parse.
                                unprocessedElementCache.put(
                                        new Element(new Integer(sequenceId++),
                                                elemData.toString()));
                                if (verbose) {
                                    logger.info("\n[CACHED FOR SECOND PARSE SEQ:" +
                                        sequenceId + "]\n" + elemData.toString());
                                }
                            } else {
                                validateOrSave(current);
                            }
                            elemData = new StringBuffer("<archetype>");
                            hasReference = false;
                        }
                    } catch (ValidationException validation) {
                        logger.error("Failed to validate object\n" +
                                current.toString(), validation);
                    } catch (Exception exception) {
                        logger.error("Failed to save object\n" +
                                current.toString(), exception);
                    }
                } 
                
                if (verbose) {
                    logger.info("\n[END PROCESSING element=" 
                            + reader.getLocalName() + "]\n");
                }
                
                
                break;

            default:
                break;
            }
        }
    }

    /**
     * This will process all elements in unprocessed element cache
     * @throws Exception
     *             propagate all exceptions to client
     */
    private void processUnprocessedElementCache() throws Exception {
        logger.info("\n[PROCESSING " + sequenceId + " ELEMENTS WITH REFERENCES]\n");

        XMLInputFactory factory = XMLInputFactory.newInstance();
        Stack<IMObject> stack = new Stack<IMObject>();
        for (int index = 0; index < sequenceId; index++) {
            // remove the element from the cache
            Integer key = new Integer(index);
            Element elem = unprocessedElementCache.get(key);
            unprocessedElementCache.remove(key);
            if (elem == null) {
                continue;
            }
            
            // create an xml stream reader form the string
            XMLStreamReader reader = factory.createXMLStreamReader(
                    new ByteArrayInputStream(((String)elem.getValue()).getBytes())); 

            // process the stream
            for (int event = reader.next(); event != XMLStreamConstants.END_DOCUMENT; 
                event = reader.next()) {
                IMObject current = null;
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (verbose) {
                            logger.info("\n[START PROCESSING element=" 
                                    + reader.getLocalName() 
                                    + " parent=" + (stack.size() == 0 
                                    ? "none" : stack.peek().getArchetypeIdAsString()) 
                                    + "]\n" + formatElement(reader));
                        }   
                
                        if (reader.getLocalName().equals("data")) {
                            try {
                                if (stack.size() > 0) {
                                    current = processElement(stack.peek(), reader);
                                } else {
                                    current = processElement(null, reader);
                                }
                                stack.push(current);
                            } catch (Exception exception) {
                                logger.error("Error in start element\n" +
                                        formatElement(reader) + "\n", exception);
                            }
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if (stack.size() > 0) {
                            current = stack.pop();
                            try {
                                if (stack.size() == 0) {
                                    validateOrSave(current);
                                }
                            } catch (ValidationException validation) {
                                logger.error("Failed to validate object\n" +
                                        current.toString(), validation);
                            } catch (Exception exception) {
                                logger.error("Failed to save object\n" +
                                        current.toString(), exception);
                            }
                        } 
                        
                        if (verbose) {
                            logger.info("\n[END PROCESSING element=" 
                                    + reader.getLocalName() + "]\n");
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
     * @return IMObject the associated IMObject
     */
    private IMObject processElement(IMObject parent, XMLStreamReader reader) 
        throws Exception {
        IMObject object = null;
        if (reader.getLocalName().equals("data")) {
            // extract the optional id element
            String id = reader.getAttributeValue(null, "id");

            // ensure that the archetype attribute is defined. If not
            // display an error.
            String shortName = reader.getAttributeValue(null, "archetype");
            if (StringUtils.isEmpty(shortName)) {
                throw new ArchetypeDataLoaderException(
                        ArchetypeDataLoaderException.ErrorCode.NoArchetypeDefined,
                        new Object[] {shortName, formatElement(reader)});
            }

            ArchetypeDescriptor adesc = archetypeService
                    .getArchetypeDescriptor(shortName);
            if (adesc == null) {
                throw new ArchetypeDataLoaderException(
                        ArchetypeDataLoaderException.ErrorCode.NoArchetypeDefined,
                        new Object[] {shortName, formatElement(reader)});
            }

            // if a childId node is defined then we can skip the create object
            // process since the object already exists
            boolean valid = true;
            if (StringUtils.isEmpty(reader.getAttributeValue(null, "childId"))) {
                object = archetypeService.create(adesc.getType());
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
                        throw new ArchetypeDataLoaderException(
                                ArchetypeDataLoaderException.ErrorCode.InvalidAttribute,
                                new Object[] {attName, formatElement(reader)});
                    }
    
                    try {
                        if (isAttributeIdRef(attValue)) {
                            processIdReference(object, attValue, ndesc, validateOnly);
                        } else {
                            ndesc.setValue(object, attValue);
                        }
                    } catch (Exception exception) {
                        throw new ArchetypeDataLoaderException(
                                ArchetypeDataLoaderException.ErrorCode.FailedToSetAtribute,
                                new Object[] {attName, attValue, formatElement(reader)},
                                exception);
                    }
                }
            } else {
                // childId is defined then we need to retrieve that object. We 
                // need to force a flush first though
                flushBatchSaveCache();
                object = getObjectForId(reader.getAttributeValue(null, "childId"), validateOnly);
            }

            // check whether there is a collection attribute specified. If it is
            // then we may need to set up a parent child relationship.
            String collectionNode = reader.getAttributeValue(null, "collection");
            if ((parent != null) && (StringUtils.isEmpty(collectionNode))) {
                throw new ArchetypeDataLoaderException(
                        ArchetypeDataLoaderException.ErrorCode.NoCollectionAttribute,
                        new Object[] {formatElement(reader)});
            }
            
            if ((valid) && (!StringUtils.isEmpty(collectionNode))) {
                if (parent == null) {
                    throw new ArchetypeDataLoaderException(
                            ArchetypeDataLoaderException.ErrorCode.NoParentForChild,
                            new Object[] {formatElement(reader)});
                } else {
                    ArchetypeDescriptor padesc= archetypeService
                        .getArchetypeDescriptor(parent.getArchetypeId());
                    if (padesc == null) {
                        throw new ArchetypeDataLoaderException(
                                ArchetypeDataLoaderException.ErrorCode.NoArchetypeId,
                                new Object[] {parent.toString()});
                    } else {
                        NodeDescriptor ndesc = padesc.getNodeDescriptor(collectionNode);
                        if (ndesc.isCollection()) {
                            ndesc.addChildToCollection(parent, object);
                        } else {
                            throw new ArchetypeDataLoaderException(
                                    ArchetypeDataLoaderException.ErrorCode.ParentNotACollection,
                                    new Object[] {formatElement(reader)});
                        }
                    }
                }
            }
            
            if (valid) {
                if (!StringUtils.isEmpty(id)) {
                    linkIdCache.put(new Element(object.getLinkId(), id));
                }
            } else {
                object = null;
            }
        }
            
        return object;
    }

    /**
     * Validate or save the specified object
     * 
     * @param current
     *            the object to validate or save
     * @throws Exception
     *            propagate the exception to caller            
     * 
     */
    private void validateOrSave(IMObject current)
    throws Exception {
        if (validateOnly) {
            archetypeService.validateObject(current);
        } else {
            if (batchSaveSize > 0) {
                batchSaveEntity(current, false);
            } else {
                archetypeService.save(current);
            }
        }
        
        // update the stats
        String shortName = current.getArchetypeId().getShortName();
        Long count = statistics.get(shortName);
        if (count == null) {
            statistics.put(shortName, new Long(1));
        } else {
            statistics.put(shortName, new Long(count.longValue() + 1));
        }
        
        // process the linkid  and ids        
        Element element = linkIdCache.get(current.getLinkId());
        String id = null;
        if (element != null) {
            id = (String)element.getValue();
            linkIdCache.remove(current.getLinkId());
        }
        
        if (id != null) {
            idCache.put(new Element(id, 
                    new IMObjectReference(current)));
        }

        if (verbose) {
            logger.info("\n[CREATED]\n"
                    + current.toString());
        }
        
    }
    /**
     * This method will cache the entity and save it only when the size of 
     * the cache reaches the configured batch size.
     * 
     * @param current
     *            the entity to save
     * @param forceFlush
     *            if true always flush the cache            
     */
    @SuppressWarnings("unchecked")
    private void batchSaveEntity(IMObject current, boolean forceFlush) {
        if (current != null) {
            // Lets validate before adding to batch so whole batch doesn't fail later
            // TODO:  Temporay fix for problem with participations and act relationships that have a mincardinality
            // of 1 and fail validation during data load. see OBF-116
            if (TypeHelper.isA(current,"actRelationship.*"))
                archetypeService.validateObject(current);
            else if (TypeHelper.isA(current,"act.*"))
                archetypeService.deriveValues(current);
            else
                archetypeService.validateObject(current);
            batchSaveCache.add(current);
        }
        
        // determine whether we need to flush
        if (((forceFlush) ||(batchSaveCache.size() >= batchSaveSize)) && (batchSaveCache.size() > 0)) {
            try {
                archetypeService.save(batchSaveCache, false);
            } catch (OpenVPMSException exception) {
                // Ok one of the batch failed so nothing saved.  Try again one by one logging each error.
                for (Object object : batchSaveCache) {
                    try {
                       archetypeService.save((IMObject)object);
                    } catch (OpenVPMSException e) {
                        logger.error("Failed to save object\n" +
                                object.toString(), e);
                    }
                }                
            }
            batchSaveCache.clear();
        }
    }
    
    /**
     * For a flush on the batchSaveCache
     */
    private void flushBatchSaveCache() {
        if (batchSaveCache.size() > 0) {
            archetypeService.save(batchSaveCache, false);
            batchSaveCache.clear();
        }
    }

    /**
     * Process the id reference attribute value and set it accordingly.
     * 
     * @param object
     *            the context object
     * @param attValue
     *            the attribute vlaue that has an id reference
     * @param ndesc
     *            the corresponding node descriptor
     * @param validateOnly
     *            are we only validating            
     * @throws Exception
     *            propagate exception to caller            
     */
    private void processIdReference(IMObject object, String attValue,
            NodeDescriptor ndesc, boolean validateOnly) 
        throws Exception {
        String ref = attValue.substring("id:".length());
        if (StringUtils.isEmpty(ref)) {
            throw new ArchetypeDataLoaderException(
                    ArchetypeDataLoaderException.ErrorCode.NullReference);
        }
        Element element = idCache.get(ref);
        if (element == null) {
            throw new ArchetypeDataLoaderException(
                    ArchetypeDataLoaderException.ErrorCode.ReferenceNotFound,
                    new Object[] {ref});
        }
        
        IMObjectReference imref = (IMObjectReference)element.getValue();
        Object imobj = null;
        if (ndesc.isObjectReference()) {
            imobj = imref;
        } else {
            if (validateOnly) {
                imobj = archetypeService.create(imref.getArchetypeId());
            } else {
                imobj = ArchetypeQueryHelper.getByObjectReference(
                        archetypeService, imref);
            }
        }

        if (ndesc.isCollection()) {
            ndesc.addChildToCollection(object, imobj);
        } else {
            ndesc.setValue(object, imobj);
        }
    }
    
    /**
     * Retrieve thr object given the specified reference
     * 
     * @param id
     *            the id in the link cache
     * @param validateOnly
     *            whether we are doing a validate only            
     * @return IMObject
     *            the returned object
     * @throws Exception                        
     */
    private IMObject getObjectForId(String id, boolean validateOnly) 
    throws Exception {
        String ref = id.substring("id:".length());
        if (StringUtils.isEmpty(ref)) {
            throw new ArchetypeDataLoaderException(
                    ArchetypeDataLoaderException.ErrorCode.NullReference);
        }
        
        Element element = idCache.get(ref);
        if (element == null) {
            throw new ArchetypeDataLoaderException(
                    ArchetypeDataLoaderException.ErrorCode.ReferenceNotFound,
                    new Object[] {ref});
        }
        
        IMObjectReference imref = (IMObjectReference)element.getValue();
        IMObject imobj = null;
        
        if (validateOnly) {
            if (imref != null) {
                imobj = archetypeService.create(imref.getArchetypeId());
            }
        } else {
            imobj = ArchetypeQueryHelper.getByObjectReference(
                    archetypeService, imref);
        }

        return imobj;
    }
    
    /**
     * This will build the element data
     * 
     * @param event
     *            the current event  
     * @param reader
     *            the stream
     * @param elemData
     *            append to this buffer
     */
    private void buildElementData(int event, XMLStreamReader reader, StringBuffer elemData) {
        switch (event) {
        case XMLStreamConstants.START_DOCUMENT:
            break;
            
        case XMLStreamConstants.START_ELEMENT:
            elemData.append("<");
            elemData.append(reader.getLocalName());
            elemData.append(" ");
            for (int index = 0; index < reader.getAttributeCount(); index++) {
                elemData.append(reader.getAttributeLocalName(index));
                elemData.append("=\"");
                elemData.append(reader.getAttributeValue(index));
                elemData.append("\" ");
            }
            elemData.append(">");
            break;

        case XMLStreamConstants.END_ELEMENT:
            elemData.append("</");
            elemData.append(reader.getLocalName());
            elemData.append(">");
            break;
            
        case XMLStreamConstants.END_DOCUMENT:
            break;    
        }
    }            

    /**
     * Dump the statistics using the logger
     */
    private void dumpStatistics() {
        logger.info("\n\n\n[STATISTICS]\n");
        for (String shortName : statistics.keySet()) {
            Long count = statistics.get(shortName);
            logger.info(String.format("%42s %6d", new Object[]{shortName, count}));
        }
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
            if (isAttributeIdNotCached(reader.getAttributeValue(index))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determine if the attribute value is for a reference and not in cache.
     * 
     * @param value
     *            the attribute value to evaluate
     * @return boolean
     */
    private boolean isAttributeIdNotCached(String value) {
        if (value.startsWith("id:")) {
            try {
                String ref = value.substring("id:".length());
                if (StringUtils.isEmpty(ref))
                    return true;
                Element element = idCache.get(ref);
                if (element == null) {
                    return true;
                }
            } catch (Exception exception) {
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
        String contextFile = StringUtils.isEmpty(config.getString("context")) ?
                DEFAULT_APP_CONTEXT_FNAME : config.getString("context"); 

        logger.info("Using  application context [" + contextFile + "]");
        context = new ClassPathXmlApplicationContext(contextFile);
        archetypeService = (IArchetypeService) context
                .getBean("archetypeService");

        linkIdCache = (Cache)context.getBean("linkIdCache");
        idCache = (Cache)context.getBean("idCache");
        unprocessedElementCache = (Cache)context.getBean("unprocessedElementCache");
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
        jsap.registerParameter(new FlaggedOption("context").setShortFlag('c')
                .setLongFlag("context").setHelp(
                        "Application context for the data loader. Defaults to archetype-data-loader-appcontext.xml in classpath"));
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
        jsap.registerParameter(new Switch("validateOnly")
                .setLongFlag("validateOnly").setDefault("false").setHelp(
                        "Only validate the data file. Do not process."));
        jsap.registerParameter(new FlaggedOption("batchSaveSize")
                .setStringParser(JSAP.INTEGER_PARSER).setDefault("0")
                .setShortFlag('b').setLongFlag("batchSaveSize")
                .setHelp("The batch size for saving entities."));
    }

    /**
     * Creater the logger
     * 
     * @throws Exception
     */
    private void createLogger() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("log4j.properties");
        System.out.println("Using log4j property file: " + url.toString());
        if (url != null) {
            PropertyConfigurator.configure(url);
            logger = Logger.getLogger(StaxArchetypeDataLoader.class);
        } else {
            // set the root logger level to error
            Logger.getRootLogger().setLevel(Level.ERROR);
            Logger.getRootLogger().removeAllAppenders();
            
            logger = Logger.getLogger(StaxArchetypeDataLoader.class);
            logger.setLevel(Level.INFO);
            logger.addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
        }
    }

    /**
     * Print usage information
     */
    private void displayUsage() {
        System.err.println();
        System.err
                .println("Usage: java " + StaxArchetypeDataLoader.class.getName());
        System.err.println("                " + jsap.getUsage());
        System.err.println();
        System.err.println(jsap.getHelp());
        System.exit(1);

    }

}
