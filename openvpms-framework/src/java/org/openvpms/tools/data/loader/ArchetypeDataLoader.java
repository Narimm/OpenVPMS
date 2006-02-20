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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// log4j
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

// hibernate
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

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
public class ArchetypeDataLoader {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ArchetypeDataLoader.class);

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
    private Map<String, IMObjectReference> idCache =
        new HashMap<String, IMObjectReference>();
    
    /**
     * The name of the file holding the data
     */
    private String fileName; 
    
    /**
     * A reference to the data that will be processed
     */
    private Document data;

    /**
     * Process the records in the specified XML document
     * 
     * @param fileName
     *            the file that holds the data
     */
    public ArchetypeDataLoader(String fileName) throws Exception {
        // init
        init();
        this.fileName = fileName;
    }

    /**
     * The main line
     * 
     * @param args
     *            the file where the data is stored is passed in as the first
     *            argument
     */
    public static void main(String[] args) throws Exception {
        logger.info("Start Loading Archetype Instance Data");
        ArchetypeDataLoader loader = new ArchetypeDataLoader(args[0]);
        loader.processData();
        logger.info("End Loading Archetype Instance Data");
    }

    /**
     * Process the data elements. 
     * 
     * @throws Exception
     *            propagate all exceptions to client
     */
    private void processData() throws Exception {
        for (int iteration = 0; iteration < 2; iteration++) { 
            boolean firstParse = (iteration == 0 ? true : false);
            
            if (firstParse) {
                logger.error("Executing first parse...");
            } else {
                logger.error("Executing second parse...");
            }
            
            reset();
            Element root = getData().getRootElement();
            for (Object obj1 : root.getChildren())  {
                processElement(null, (Element)obj1, firstParse);
            }
        }
    }

    /**
     * Process the specified element and all nested elements. If the parent
     * object is specified then then the specified element is in a parent
     * child relationship.
     * <p>
     * The archetype attribute determines the archetype we need to us to 
     * create from the element. Iterate through all the element attributes 
     * and attempt to set the specified value using the archetype's node 
     * descriptors. The attribute name must correspond to a valid node 
     * descriptor
     * <p>     
     * @param parent
     *            the parent of this element if it exists
     * @param elem
     *            the element to process
     * @param firstParse
     *            indicates whether it is a first parse  
     * @return IMObject
     *            the associated IMObject                                  
     */
    private IMObject processElement(IMObject parent, Element elem, boolean firstParse) 
    throws Exception {
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
                logger.error("Failed to process " 
                        + elem.toString()
                        + " because it does not define an -arhetype- attribute");
                return object;
            }

            ArchetypeDescriptor adesc = archetypeService
                .getArchetypeDescriptor(shortName);
            if (adesc == null) {
                logger.error("Failed to process element because "
                        + "there is no archetype defined for " + shortName);
                return object;
            }

            // create the default object and then iterate through the 
            // attributed and set the appropriate node value
            object = archetypeService.create(adesc.getType());
            boolean valid = true;
            for (Object obj2 : elem.getAttributes()) {
                Attribute attr = (Attribute)obj2;
                if ((StringUtils.equals(attr.getName(), "archetype")) ||
                    (StringUtils.equals(attr.getName(), "id")) ||
                    (StringUtils.equals(attr.getName(), "collection"))) {
                    // no need to process the archetype, id or collection element
                    continue;
                }
                
                NodeDescriptor ndesc = adesc.getNodeDescriptor(attr.getName());
                if (ndesc == null) {
                    logger.error("Failed to process " + elem.toString()
                            + " because the attribute " + attr.getName() 
                            + " is invalid");
                        valid = false;
                        break;
                    }
  
                    try {
                        if (isAttributeIdRef(attr)) {
                            String ref = attr.getValue().substring("id:".length());
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
                        ndesc.setValue(object, attr.getValue());
                    }
                } catch (Exception exception) {
                    logger.error("Failed to process " + elem.toString() 
                            + ". Error trying to set the attribute " 
                            + attr.getName() + " with value " + attr.getValue(),
                            exception);
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
                for (Object obj1 : elem.getChildren())  {
                    Element childElem = (Element)obj1;
                    String collectionNode = childElem.getAttributeValue("collection");
                    if (StringUtils.isEmpty(collectionNode)) {
                        logger.error("Failed to process " 
                                + childElem.toString()
                                + " a child element must have a collection attribute");
                        continue;
                    }
                    IMObject child = processElement(object, childElem, firstParse);
                    NodeDescriptor ndesc = adesc.getNodeDescriptor(collectionNode);
                    if ((ndesc.isCollection()) && 
                        (ndesc.isParentChild())){
                        ndesc.addChildToCollection(object, child);
                    } else {
                        logger.error("Failed to process " 
                                + childElem.toString()
                                + ". The collection node name must be a "
                                + "parent-child collection");
                        continue;
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
                    
                    logger.error("Creating data with archetype: " 
                            + object.getArchetypeId().getShortName()
                            + " and name " 
                            + object.getName());
                }
            } catch(ValidationException validation) {
                logger.error("Failed to process " + elem.toString()
                        + ". Got the following validation errors:"); 
                for (ValidationError error : validation.getErrors()) {
                    logger.error(error.toString());
                }
            } catch (Exception exception) {
                logger.error("Failed to process " + elem.toString()
                        + ". Error trying to save the object" 
                        + ". [Exception] " + exception.toString());
            }
        }
        
        return object;
    }
    
    /**
     * @param attributes
     * @return
     */
    private boolean attributesContainReferences(List attributes) {
        for (Object attr : attributes) {
            if (isAttributeIdRef((Attribute)attr)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Determine if the attribute value is for a reference. All reference
     * values start with id:
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
        context =  new ClassPathXmlApplicationContext(
                "org/openvpms/tools/data/loader/archetype-data-loader-appcontext.xml");
        archetypeService = (IArchetypeService)context.getBean("archetypeService");
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
     * @return Document
     */
    private Document getData() 
    throws Exception {
        if (data == null) {
            SAXBuilder builder = new SAXBuilder();
            data = builder.build(new File(fileName));
        }
        
        return data;
    }
}
