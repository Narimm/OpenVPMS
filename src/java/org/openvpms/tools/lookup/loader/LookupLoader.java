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

package org.openvpms.tools.lookup.loader;

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

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This tool will process the specified XML document and process all of the 
 * elements. It creates a spring application context to load the appropriate
 * services.
 * <p>
 * It will process the lookupItem element first and then the lookupRel element
 * in 2 passes of the document. If it encounters any problem while processing
 * an element it will display and error and process the next element in the
 * document
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupLoader {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(LookupLoader.class);

    /**
     * A reference to the application context
     */
    private ApplicationContext context;

    /**
     * a reference to the archetype service
     */
    private IArchetypeService archetypeService;
    
    /**
     * A reference to the lookup service
     */
    private ILookupService lookupService;
    
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
    public LookupLoader(String fileName) throws Exception {
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
        LookupLoader loader = new LookupLoader(args[0]);
        loader.processLookups();
        loader.processRelationships();
        logger.info("End Loading Archetype Instance Data");
    }

    /**
     * Process the lookupItem elements. The id attribute determines the 
     * lookup archetype we need to create from the element. Iterate through
     * all the element attributes and attempt to set the specified value
     * using the archetype's node descriptors. The attribute name must 
     * correspond to a valid node descriptor
     * <p>
     * If the lookup already exists in the database then do not replace it. If 
     * it cannot create the archetype or validate the object then log an 
     * error and continue processing the next element.
     * 
     * @throws Exception
     *            propagate all exceptions to client
     */
    protected void processLookups() throws Exception {
        reset();
        Element root = getData().getRootElement();
        for (Object obj1 : root.getChildren())  {
            Element elem = (Element)obj1;
            if (elem.getName().equals("lookupItem")) {
                String id = elem.getAttributeValue("id");
                if (StringUtils.isEmpty(id)) {
                    logger.error("Failed to process <lookupItem> element because "
                            + "it has no -id- attribute");
                    continue;
                }

                ArchetypeDescriptor adesc = archetypeService
                    .getArchetypeDescriptor("lookup." + id);
                if (adesc == null) {
                    logger.error("Failed to process <lookupItem> element because "
                            + "there is no archetype define for lookup." + id);
                    continue;
                }

                Lookup lookup = (Lookup)archetypeService.create(adesc.getType());
                boolean valid = true;
                for (Object obj2 : elem.getAttributes()) {
                    Attribute attr = (Attribute)obj2;
                    if (StringUtils.equals(attr.getName(), "id")) {
                        // no need to process the id element
                        continue;
                    }
                    
                    NodeDescriptor ndesc = adesc.getNodeDescriptor(attr.getName());
                    if (ndesc == null) {
                        logger.error("Failed to process <lookupItem> with id " 
                                + id + " because the attribute " + attr.getName() 
                                + " is invalid");
                        valid = false;
                        break;
                    }
  
                    try {
                        ndesc.setValue(lookup, attr.getValue());
                    } catch (Exception exception) {
                        logger.error("Failed to process <lookupItem> with id "
                                + id + ". Error trying to set the attribute " 
                                + attr.getName() + " with value " + attr.getValue()
                                + ". [Exception] " + exception.toString());
                        valid = false;
                    }
                }
            
                // check that we should continue processing this element.
                if (!valid) {
                    continue;
                }
                
                // now if there are no errors the proceed to save the element
                if (findLookup(id, lookup.getValue()) == null) {
                    try {
                        archetypeService.save(lookup);
                    } catch (Exception exception) {
                        logger.error("Failed to process " + elem.toString()
                                + ". Error trying to savre the lookup" 
                                + ". [Exception] " + exception.toString());
                    }
                }
            }
        }
    }

    /**
     * Process the lookRel elements in the document. It first attempts to
     * locate the lookup. If it doesn't exist then log an error and continue
     * processing otherwise retrieve the relationships for the source lookup.
     * If the relationship already exists then continue processing otherwise
     * create and save the relationship.
     * 
     * @throws Exception
     *            propagate all exceptions to the client
     */
    protected void processRelationships() throws Exception {
        reset();
        Element root = getData().getRootElement();
        for (Object obj1 : root.getChildren())  {
            Element elem = (Element)obj1;
            if (elem.getName().equals("lookupRel")) {
                Element sourceElem = elem.getChild("source");
                if (sourceElem == null) {
                    logger.error("<lookupRel> element does not have a <source> elememt." 
                            + elem.toString());
                    continue;
                }
                
                // check that the source lookup exists
                String sconcept = sourceElem.getAttributeValue("id");
                if (StringUtils.isEmpty(sconcept)) {
                    logger.error("<source> element does not contain and -id- attribute");
                    continue;
                }
                String svalue = sourceElem.getAttributeValue("value");
                if (StringUtils.isEmpty(svalue)) {
                    logger.error("<source id=\"> " + sconcept + "\" element does not contain and -value- attribute");
                    continue;
                }
                Lookup source = findLookup(sconcept, svalue);
                if (source == null) {
                    logger.error("Failed to process <lookupRel> element since "
                            + "it cannot find a lookup with <source> id=" 
                            + sconcept + " value=" + svalue);
                    continue;
                }
                
                Element targetElem = elem.getChild("target");
                if (targetElem == null) {
                    logger.error("<lookupRel> element does not have a <target> elememt." 
                            + elem.toString());
                    continue;
                }
                
                // check that the target lookup exists.
                String tconcept = targetElem.getAttributeValue("id");
                if (StringUtils.isEmpty(tconcept)) {
                    logger.error("<target> element does not contain and -id- attribute");
                    continue;
                }
                String tvalue = targetElem.getAttributeValue("value");
                if (StringUtils.isEmpty(tvalue)) {
                    logger.error("<target id=\"> " + tconcept + "\" element does not contain and -value- attribute");
                    continue;
                }
                Lookup target = findLookup(tconcept, tvalue);
                if (target == null) {
                    logger.error("Failed to process <lookupRel> element since "
                            + "it cannot find a lookup with <target> id=" 
                            + tconcept + " value=" + tvalue);
                    continue;
                }
                
                // check if such a relationship exists
                if (relationshipExists(source, target)) {
                    continue;
                }
            
                // if it doesn't exist then create the relationship
                LookupRelationship relationship = new LookupRelationship(
                        source, target);
                
                try {
                    lookupService.add(relationship);
                } catch (Exception exception) {
                    logger.error("Failed to create a lookup relationship between " 
                            + source.toString() + " and " + target.toString() 
                            + ". [Exception] " + exception.toString());
                }
            }
        }

    }

    /**
     * Initialise and start the spring container
     */
    private void init() throws Exception {
        context =  new ClassPathXmlApplicationContext(
                "org/openvpms/tools/lookup/loader/archetype-data-loader-appcontext.xml");
        archetypeService = (IArchetypeService)context.getBean("archetypeService");
        lookupService = (ILookupService)context.getBean("lookupService");
    }

    /**
     * Return the lookup with the specified concept and value.
     * 
     * @param concept
     *            the conept to look for
     * @param value
     *            the value to search for
     * @return Lookup
     *            true if it exists and galse otherwise
     * @throws Exception
     *             propagate exception to caller
     */
    private Lookup findLookup(String concept, String value) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("concept", concept);
        params.put("value", value);
        
        List<IMObject> result = archetypeService.getByNamedQuery(
                "lookup.getLookupForConceptAndValue", params, 0,
                ArchetypeQuery.ALL_ROWS).getRows();
        
        return (Lookup)(result.size() > 0 ? result.get(0) : null);
    }
    
    /**
     * Determine if there is a relationship between the specified source and 
     * target lookups. 
     * 
     * @param source
     *            the source lookup
     * @param target 
     *            the target lookup
     * @return boolean
     *            true if a relatioship already exists
     * @throws Exception
     *            propagate all exceptions to caller                                                
     */
    private boolean relationshipExists(Lookup source, Lookup target)
    throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("uid", source.getUid());
        params.put("type", new StringBuffer(
                source.getArchetypeId().getConcept())
                .append(".")
                .append(target.getArchetypeId().getConcept())
                .toString());
        
        List<IMObject> result = archetypeService.getByNamedQuery(
                "lookupRelationship.getTargetLookups", params, 0,
                ArchetypeQuery.ALL_ROWS).getRows();
        for (IMObject imobj : result) {
            if (target.equals((Lookup)imobj)) {
                return true;
            }
        }
        
        return false;
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
