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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.tools;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Report template loader.
 *
 * @author Tim Anderson
 */
public class TemplateLoader {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(TemplateLoader.class);


    /**
     * Constructs a {@link TemplateLoader}.
     *
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public TemplateLoader(IArchetypeService service, DocumentHandlers handlers) {
        this.service = service;
        this.handlers = handlers;
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
                    IArchetypeService service = (IArchetypeService) context.getBean("archetypeService");
                    DocumentHandlers handlers = (DocumentHandlers) context.getBean("documentHandlers");
                    TemplateLoader loader = new TemplateLoader(service, handlers);
                    loader.load(file);
                } else {
                    displayUsage(parser);
                }
            }
        } catch (Throwable exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    /**
     * Loads all templates from a file.
     *
     * @param path the file path
     * @throws IOException               for any I/O error
     * @throws ArchetypeServiceException for any archetype service error
     * @throws JAXBException             for any JAXB error
     */
    public void load(String path) throws IOException, JAXBException {
        File file = new File(path);
        JAXBContext context = JAXBContext.newInstance(Templates.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Templates templates = (Templates) unmarshaller.unmarshal(file);
        File dir = file.getParentFile();
        Map<String, Entity> emailTemplates = new HashMap<>();

        // first pass loads all email templates
        for (BaseTemplate template : templates.getTemplateOrEmailTemplate()) {
            if (template instanceof EmailTemplate) {

                Entity entity = loadEmailTemplate((EmailTemplate) template, dir);
                emailTemplates.put(entity.getName(), entity);
            }
        }

        // second pass loads all document templates
        for (BaseTemplate template : templates.getTemplateOrEmailTemplate()) {
            if (template instanceof Template) {
                loadTemplate((Template) template, dir, emailTemplates);
            }
        }
    }

    /**
     * Load a document template.
     * <br/>
     * NOTE: email templates must have already been loaded prior to this being invoked.
     *
     * @param template       the report template to load
     * @param dir            the parent directory for resolving relative paths
     * @param emailTemplates the email templates
     * @throws DocumentException         if the document cannot be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void loadTemplate(Template template, File dir, Map<String, Entity> emailTemplates) {
        DocLoader loader = new DocLoader(emailTemplates);
        loader.load(template, dir);
    }

    /**
     * Loads an email template.
     *
     * @param template the template
     * @param dir      the parent directory for resolving relative paths
     * @return the template entity
     */
    private Entity loadEmailTemplate(EmailTemplate template, File dir) {
        EmailLoader loader = new EmailLoader(template.getSystem());
        return loader.load(template, dir);
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

    /**
     * Loads a template.
     */
    private abstract class Loader<T extends BaseTemplate> {

        private final String archetype;

        private Document document;

        private Entity entity;

        private DocumentAct act;

        /**
         * Constructs a {@link Loader}.
         *
         * @param archetype the template archetype
         */
        public Loader(String archetype) {
            this.archetype = archetype;
        }

        /**
         * Loads a the template.
         *
         * @param template the template descriptor
         * @param dir      the directory to locate relative paths
         * @return the template entity
         */
        public Entity load(T template, File dir) {
            Entity entity = prepare(template, dir);
            save();
            log.info("Loaded '" + entity.getName() + "'");
            return entity;
        }

        /**
         * Prepares the template.
         *
         * @param template the template descriptor
         * @param dir      the directory to locate relative paths
         * @return the template entity
         */
        protected Entity prepare(T template, File dir) {
            document = getDocument(dir, template.getPath(), template.getDocType(), template.getMimeType());
            ArchetypeQuery query = new ArchetypeQuery(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT, false, true);
            query.add(Constraints.eq("name", document.getName()));
            query.add(Constraints.join("template").add(Constraints.isA("entity", archetype)));
            query.setFirstResult(0);
            query.setMaxResults(1);
            List<IMObject> rows = service.get(query).getResults();
            if (!rows.isEmpty()) {
                act = (DocumentAct) rows.get(0);
                ActBean bean = new ActBean(act, service);
                entity = bean.getNodeParticipant("template");
            } else {
                act = (DocumentAct) service.create(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
            }
            act.setFileName(document.getName());
            act.setMimeType(document.getMimeType());
            act.setDescription(DescriptorHelper.getDisplayName(document, service));

            if (entity == null) {
                entity = (Entity) service.create(archetype);
                if (entity == null) {
                    throw new IllegalStateException("Failed to create " + archetype + ": archetype not found");
                }
                ActBean bean = new ActBean(act, service);
                bean.setNodeParticipant("template", entity);
            }

            act.setDocument(document.getObjectReference());
            entity.setName(template.getName());
            entity.setDescription(template.getDescription());
            return entity;
        }

        /**
         * Saves the document, template, and document act
         */
        public void save() {
            service.save(Arrays.asList(document, entity, act));
        }

        /**
         * Creates a new {@link Document} from a template.
         *
         * @param dir      the directory to locate relative paths
         * @param path     the template path
         * @param docType  the document archetype. May be {@code null}
         * @param mimeType the mime type
         * @return a new document containing the serialized template
         * @throws DocumentException for any error
         */
        protected Document getDocument(File dir, String path, String docType, String mimeType) {
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(dir, path);
            }
            if (docType == null) {
                docType = DocumentArchetypes.DEFAULT_DOCUMENT;
            }
            return DocumentHelper.create(file, docType, mimeType, handlers);
        }
    }

    /**
     * Loads document templates.
     */
    private class DocLoader extends Loader<Template> {

        /**
         * The email templates, keyed on name.
         */
        private final Map<String, Entity> emailTemplates;

        /**
         * Constructs a {@link DocLoader}.
         *
         * @param emailTemplates the email templates, keyed on name
         */
        public DocLoader(Map<String, Entity> emailTemplates) {
            super(DocumentArchetypes.DOCUMENT_TEMPLATE);
            this.emailTemplates = emailTemplates;
        }

        /**
         * Prepares the template.
         *
         * @param template the template descriptor
         * @param dir      the directory to locate relative paths
         * @return the template entity
         */
        @Override
        protected Entity prepare(Template template, File dir) {
            Entity entity = super.prepare(template, dir);
            IMObjectBean bean = new IMObjectBean(entity, service);
            bean.setValue("archetype", template.getArchetype());
            bean.setValue("reportType", template.getReportType());
            if (template.getOrientation() != null) {
                bean.setValue("orientation", template.getOrientation().value());
            }
            Template.EmailTemplate email = template.getEmailTemplate();
            if (email != null) {
                Entity emailTemplate = emailTemplates.get(email.getName());
                if (emailTemplate == null) {
                    throw new IllegalStateException("Template '" + template.getName()
                                                    + "' references non-existent email template: " + email.getName());
                }
                EntityLink link = (EntityLink) bean.getValue("email", PredicateUtils.truePredicate());
                // hack to return the first value of collection
                if (link == null) {
                    bean.addNodeTarget("email", emailTemplate);
                } else {
                    link.setTarget(emailTemplate.getObjectReference());
                }
            }
            return entity;
        }
    }

    /**
     * Loads email templates.
     */
    private class EmailLoader extends Loader<EmailTemplate> {

        /**
         * Constructs a {@link EmailLoader}.
         *
         * @param useSystem if {@code true} use <em>entity.documentTemplateEmailSystem</em>,
         *                  otherwise use <em>entity.documentTemplateEmailUser</em>
         */
        public EmailLoader(boolean useSystem) {
            super(useSystem ? DocumentArchetypes.SYSTEM_EMAIL_TEMPLATE : DocumentArchetypes.USER_EMAIL_TEMPLATE);
        }

        /**
         * Prepares the template.
         *
         * @param template the template descriptor
         * @param dir      the directory to locate relative paths
         * @return the template entity
         */
        @Override
        protected Entity prepare(EmailTemplate template, File dir) {
            Entity entity = super.prepare(template, dir);
            IMObjectBean bean = new IMObjectBean(entity, service);
            bean.setValue("subject", template.getSubject());
            bean.setValue("subjectType", template.getSubjectType());
            bean.setValue("contentType", "DOCUMENT");
            return entity;
        }
    }
}
