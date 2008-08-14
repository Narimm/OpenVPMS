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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.tools;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.report.jasper.tools.Template;
import org.openvpms.report.jasper.tools.Templates;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;


/**
 * Report template loader.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TemplateLoader {

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;


    /**
     * Construct a new <code>JasperTemplateLoader</code>.
     *
     * @param service the archetype service
     * @param handlers the document handlers
     */
    public TemplateLoader(IArchetypeService service,
                          DocumentHandlers handlers) {
        this.service = service;
        this.handlers = handlers;
    }

    /**
     * Loads all templates from a file.
     *
     * @param path the file path
     * @throws IOException               for any I/O error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void load(String path) throws IOException, ValidationException,
                                         MarshalException {
        File file = new File(path);
        FileReader reader = new FileReader(file);
        Templates templates = (Templates) Templates.unmarshal(reader);
        File dir = file.getParentFile();
        for (Template template : templates.getTemplate()) {
            load(template, dir);
        }
    }

    /**
     * Load a report template.
     *
     * @param template the report template to load
     * @param dir      the parent directory for resolving relative paths
     * @throws DocumentException         if the document cannot be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void load(Template template, File dir) {
        Document document = getDocument(template, dir);
        Entity entity;
        DocumentAct act;
        Participation participation;
        ArchetypeQuery query
                = new ArchetypeQuery("act.documentTemplate", false, true);
        query.add(new NodeConstraint("name", document.getName()));
        query.setFirstResult(0);
        query.setMaxResults(1);
        List<IMObject> rows = service.get(query).getResults();
        if (!rows.isEmpty()) {
            act = (DocumentAct) rows.get(0);
            ActBean bean = new ActBean(act);
            entity = bean.getParticipant("participation.document");
            if (entity == null) {
                entity = (Entity) service.create("entity.documentTemplate");
                bean.setParticipant("participation.document", entity);
            }
        } else {
            entity = (Entity) service.create("entity.documentTemplate");
            act = (DocumentAct) service.create("act.documentTemplate");
            act.setFileName(document.getName());
            act.setMimeType(document.getMimeType());
            act.setDescription(DescriptorHelper.getDisplayName(document));
            participation = (Participation) service.create(
                    "participation.document");
            participation.setAct(act.getObjectReference());
            act.addParticipation(participation);
            participation.setEntity(entity.getObjectReference());
        }

        act.setDocument(document.getObjectReference());
        service.save(Arrays.asList(document, act));

        String name = template.getName();
        if (name == null) {
            name = document.getName();
        }
        entity.setName(name);
        EntityBean bean = new EntityBean(entity);
        bean.setValue("archetype", template.getArchetype());
        service.save(entity);
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
                String file = config.getString("file");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }

                if (file != null) {
                    IArchetypeService service
                            = (IArchetypeService) context.getBean(
                            "archetypeService");
                    DocumentHandlers handlers
                            = (DocumentHandlers) context.getBean(
                            "documentHandlers");
                    TemplateLoader loader = new TemplateLoader(service,
                                                               handlers);
                    loader.load(file);
                } else {
                    displayUsage(parser);
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Creates a new {@link Document} from a template.
     *
     * @param template the template descriptor
     * @param dir      the directory to locate relative paths
     * @return a new document containing the serialized template
     * @throws DocumentException for any error
     */
    private Document getDocument(Template template, File dir) {
        File file = new File(template.getPath());
        if (!file.isAbsolute()) {
            file = new File(dir, template.getPath());
        }
        return DocumentHelper.create(file, template.getDocType(),
                                     template.getMimeType(), handlers);
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
        parser.registerParameter(new FlaggedOption("file").setShortFlag('f')
                .setLongFlag("file").setHelp(
                "The template configuration file to load."));
        return parser;
    }

    /**
     * Prints usage information.
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err
                .println("Usage: java " + TemplateLoader.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
