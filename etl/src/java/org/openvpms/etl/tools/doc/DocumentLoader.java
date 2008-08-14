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

package org.openvpms.etl.tools.doc;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.NodeSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Document loader.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentLoader {

    /**
     * The document creator.
     */
    private final DocumentFactory factory;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Determines if the generator should fail on error.
     */
    private boolean failOnError = true;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DocumentLoader.class);

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";


    /**
     * Constructs a new <tt>DocumentLoader</tt>.
     */
    public DocumentLoader(IArchetypeService service) {
        this(service, new FileDocumentFactory());
    }

    /**
     * Constructs a new <tt>DocumentLoader</tt>.
     *
     * @param service the archetype service
     * @param factory the document creator
     */
    public DocumentLoader(IArchetypeService service, DocumentFactory factory) {
        this.service = service;
        this.factory = factory;
    }

    /**
     * Determines if generation should fail when an error occurs.
     * Defaults to <tt>true</tt>.
     *
     * @param failOnError if <tt>true</tt> fail when an error occurs
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Loads documents for all document acts that have null docReference nodes
     * and a non-null file name.
     */
    public void load() {
        load(null);
    }

    /**
     * Loads documents for all document acts matching the specified short name
     * that have null docReference nodes and a non-null file name.
     *
     * @param shortName the archetype short name. May be <tt>null</tt> or
     *                  contain wildcards
     */
    public void load(String shortName) {
        ShortNameConstraint shortNames;
        if (shortName == null) {
            shortNames = new ShortNameConstraint(getShortNames(), false, true);
        } else {
            shortNames = new ShortNameConstraint(shortName, false, true);
        }
        ArchetypeQuery query = new ArchetypeQuery(shortNames);
        if (log.isInfoEnabled()) {
            StringBuffer buff = new StringBuffer();
            for (String s : shortNames.getShortNames()) {
                if (buff.length() != 0) {
                    buff.append(", ");
                }
                buff.append(s);
            }
            log.info("Querying archetypes: " + buff);
        }
        List<IMObjectReference> refs = new ArrayList<IMObjectReference>();
        query.add(new NodeConstraint("docReference", RelationalOp.IsNULL));
        query.setMaxResults(1000);
        List<String> nodes = Arrays.asList("fileName");
        Iterator<NodeSet> iter = new NodeSetQueryIterator(query, nodes);

        // need to build up a list of matching references first, as updates
        // to the document reference will affect paging
        while (iter.hasNext()) {
            NodeSet set = iter.next();
            String fileName = (String) set.get("fileName");
            if (!StringUtils.isEmpty(fileName)) {
                refs.add(set.getObjectReference());
            }
        }
        log.info("Found " + refs.size() + " documents");
        if (!refs.isEmpty()) {
            int count = 0;
            int errors = 0;
            for (IMObjectReference ref : refs) {
                try {
                    DocumentAct act = getDocumentAct(ref);
                    if (act != null) {
                        Document doc = factory.create(act);
                        act.setDocument(doc.getObjectReference());
                        act.setMimeType(doc.getMimeType());
                        service.save(Arrays.asList(act, doc));
                    }
                } catch (OpenVPMSException exception) {
                    if (failOnError) {
                        throw exception;
                    } else {
                        ++errors;
                        log.error(exception.getMessage());
                    }
                }
            }
            log.info("Loaded " + count + " documents");
            if (errors != 0) {
                log.warn("There were " + errors + " errors");
            } else {
                log.info("There were no errors");
            }
        }
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
                String type = config.getString("type");
                String dir = config.getString("dir");

                if (!new File(contextPath).exists()) {
                    new ClassPathXmlApplicationContext(contextPath);
                } else {
                    new FileSystemXmlApplicationContext(contextPath);
                }
                DocumentLoader loader = new DocumentLoader(
                        ArchetypeServiceHelper.getArchetypeService(),
                        new FileDocumentFactory(dir));
                loader.setFailOnError(config.getBoolean("failOnError"));
                loader.load(type);
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
            System.exit(1);
        }
    }

    /**
     * Helper to return a document act given its reference.
     *
     * @param reference the document act reference
     * @return the corresponding act, or <tt>null</tt>
     */
    private DocumentAct getDocumentAct(IMObjectReference reference) {
        return (DocumentAct) service.get(reference);
    }

    /**
     * Returns all document act short names with a docReference node.
     *
     * @return a list of short names
     */
    private String[] getShortNames() {
        List<String> result = new ArrayList<String>();
        for (ArchetypeDescriptor archetype
                : service.getArchetypeDescriptors()) {
            if (DocumentAct.class.getName().equals(archetype.getClassName())
                    && archetype.getNodeDescriptor("docReference") != null) {
                result.add(archetype.getType().getShortName());
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("dir").setShortFlag('d')
                .setLongFlag("dir")
                .setHelp("The directory to load files from. "
                + "Defaults to the current directory"));
        parser.registerParameter(new FlaggedOption("type").setShortFlag('t')
                .setLongFlag("type")
                .setHelp("The archetype short name. May contain wildcards. "
                + "If not specified, defaults to all document acts"));
        parser.registerParameter(new FlaggedOption("failOnError")
                .setShortFlag('e')
                .setLongFlag("failOnError")
                .setDefault("true")
                .setStringParser(BooleanStringParser.getParser())
                .setHelp("Fail on error"));
        parser.registerParameter(new FlaggedOption("context").setShortFlag('c')
                .setLongFlag("context")
                .setDefault(APPLICATION_CONTEXT)
                .setHelp("Application context path"));
        return parser;
    }

    /**
     * Prints usage information.
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err.println("Usage: java "
                + DocumentLoader.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }
}
