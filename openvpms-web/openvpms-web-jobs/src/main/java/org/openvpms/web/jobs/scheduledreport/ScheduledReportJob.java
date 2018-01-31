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

package org.openvpms.web.jobs.scheduledreport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.bean.Policies;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ParameterType;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.app.PracticeMailContext;
import org.openvpms.web.component.im.doc.FileNameFormatter;
import org.openvpms.web.component.mail.DefaultMailerFactory;
import org.openvpms.web.component.mail.EmailTemplateEvaluator;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.component.service.PracticeMailService;
import org.openvpms.web.jobs.JobCompletionNotifier;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.admin.job.scheduledreport.ScheduledReportJobConfigurationEditor;
import org.openvpms.web.workspace.reporting.report.SQLReportPrinter;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Job for executing reports and sending the output to a file, email, or printer.
 *
 * @author Tim Anderson
 */
public class ScheduledReportJob implements InterruptableJob {

    /**
     * The job configuration.
     */
    private final IMObjectBean config;

    /**
     * The report factory.
     */
    private final ReportFactory reportFactory;

    /**
     * The mailer factory.
     */
    private final MailerFactory mailerFactory;

    /**
     * The email template evaluator.
     */
    private final EmailTemplateEvaluator emailTemplateEvaluator;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The file name formatter.
     */
    private final FileNameFormatter formatter;

    /**
     * The data source.
     */
    private final DataSource dataSource;

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * Used to send messages to users on completion or failure.
     */
    private final JobCompletionNotifier notifier;

    /**
     * Determines if the job should stop.
     */
    private volatile boolean stop;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ScheduledReportJob.class);

    /**
     * PDF output.
     */
    private static final String PDF = "PDF";

    /**
     * CSV output.
     */
    private static final String CSV = "CSV";

    /**
     * Constructs a {@link ScheduledReportJob}.
     *
     * @param configuration          the job configuration
     * @param reportFactory          the report factory
     * @param mailService            the mail service
     * @param emailTemplateEvaluator the email template evaluator
     * @param practiceService        the practice service
     * @param handlers               the document handlers
     * @param formatter              the file name formatter
     * @param dataSource             the data source
     * @param service                the archetype service
     */
    public ScheduledReportJob(Entity configuration, ReportFactory reportFactory, PracticeMailService mailService,
                              EmailTemplateEvaluator emailTemplateEvaluator, DocumentHandlers handlers,
                              PracticeService practiceService, FileNameFormatter formatter,
                              @Qualifier("reportingDataSource") DataSource dataSource, IArchetypeRuleService service) {
        config = service.getBean(configuration);
        this.reportFactory = reportFactory;
        this.mailerFactory = new DefaultMailerFactory(mailService, handlers);
        this.emailTemplateEvaluator = emailTemplateEvaluator;
        this.handlers = handlers;
        this.practiceService = practiceService;
        this.formatter = formatter;
        this.dataSource = dataSource;
        this.service = service;
        notifier = new JobCompletionNotifier(service);
    }

    /**
     * Called by the {@code {@link Scheduler }} when a user interrupts the {@code Job}.
     */
    @Override
    public void interrupt() {
        stop = true;
    }

    /**
     * Called by the {@code {@link Scheduler }} when a {@code {@link Trigger }} fires that is associated with the
     * {@code Job}.
     *
     * @param context the job execution context
     */
    @Override
    public void execute(JobExecutionContext context) {
        State state = new State();
        try {
            report(state);
            complete(null, state);
        } catch (Throwable exception) {
            log.error(exception, exception);
            complete(exception, state);
        }
    }

    /**
     * Generates the report(s).
     *
     * @param state the reporting state
     * @throws IOException for any I/O error
     */
    protected void report(State state) throws IOException {
        DocumentTemplate template = getTemplate();
        Context context = new LocalContext();
        context.setPractice(practiceService.getPractice());
        context.setLocation(config.getTarget("location", Party.class));
        context.setUser(config.getTarget("runAs", User.class));
        state.setTemplate(template);
        SQLReportPrinter printer = new SQLReportPrinter(template, context, reportFactory, formatter, dataSource,
                                                        service);
        Set<ParameterType> parameterTypes = printer.getParameterTypes();
        Map<String, Object> parameters = new HashMap<>();
        int i = 0;
        while (true) {
            String node = "paramName" + i;
            if (config.hasNode(node)) {
                String name = config.getString(node);
                if (!StringUtils.isEmpty(name)) {
                    Object value = config.getValue("paramValue" + i);
                    checkParameterType(parameterTypes, name);
                    parameters.put(name, value);
                }
            } else {
                break;
            }
            ++i;
        }
        printer.setParameters(parameters);
        if (!stop && config.getBoolean(ScheduledReportJobConfigurationEditor.FILE)) {
            file(printer, state);
        }
        if (!stop && config.getBoolean(ScheduledReportJobConfigurationEditor.EMAIL)) {
            email(printer, context, state);
        }
        if (!stop && config.getBoolean(ScheduledReportJobConfigurationEditor.PRINT)) {
            print(printer, state);
        }
    }

    /**
     * Returns the report template.
     *
     * @return the report template
     * @throws IllegalStateException if the template does not exist
     */
    protected DocumentTemplate getTemplate() {
        Entity entity = config.getTarget("report", Entity.class, Policies.active());
        if (entity == null) {
            throw new IllegalStateException(config.getDisplayName() + " has no active report: "
                                            + config.getObject().getName());
        }
        return new DocumentTemplate(entity, service);
    }

    /**
     * Verifies that a parameter is supported by the report.
     *
     * @param parameterTypes the report parameter types
     * @param name           the parameter name
     * @throws IllegalStateException if the parameter is not supported
     */
    private void checkParameterType(Set<ParameterType> parameterTypes, String name) {
        for (ParameterType type : parameterTypes) {
            if (!type.isSystem() && StringUtils.equals(type.getName(), name)) {
                return;
            }
        }
        throw new IllegalStateException("Invalid parameter " + name);
    }

    /**
     * Writes the report to a file.
     *
     * @param printer the report printer
     * @param state   the reporting state
     * @throws IOException for any I/O error
     */
    private void file(SQLReportPrinter printer, State state) throws IOException {
        String mimeType;
        String fileType = config.getString("fileType");
        if (PDF.equals(fileType)) {
            mimeType = DocFormats.PDF_TYPE;
        } else if (CSV.equals(fileType)) {
            mimeType = DocFormats.CSV_TYPE;
        } else {
            throw new IllegalStateException("Unrecognised file type: " + fileType);
        }
        Document document = printer.getDocument(mimeType, false);
        File file = new File(config.getString(ScheduledReportJobConfigurationEditor.DIRECTORY), document.getName());
        DocumentHandler handler = handlers.get(document);
        FileUtils.copyInputStreamToFile(handler.getContent(document), file);
        state.setFile(file);
    }

    /**
     * Emails the report.
     *
     * @param printer the report printer
     * @param context the context
     * @param state   the reporting state
     */
    private void email(SQLReportPrinter printer, Context context, State state) {
        String mimeType;
        String type = config.getString("attachmentType");
        if (PDF.equals(type)) {
            mimeType = DocFormats.PDF_TYPE;
        } else if (CSV.equals(type)) {
            mimeType = DocFormats.CSV_TYPE;
        } else {
            throw new IllegalStateException("Unrecognised attachment type: " + type);
        }
        Document document = printer.getDocument(mimeType, false);

        Mailer mailer = mailerFactory.create(new PracticeMailContext(context));
        String from = config.getString(ScheduledReportJobConfigurationEditor.EMAIL_FROM);

        List<String> to = new ArrayList<>();
        for (int i = 0; ; ++i) {
            String name = ScheduledReportJobConfigurationEditor.EMAIL_TO + i;
            if (config.hasNode(name)) {
                String address = config.getString(name);
                if (!StringUtils.isEmpty(address)) {
                    to.add(address);
                }
            } else {
                break;
            }
        }
        String subject;
        String body;
        Entity emailTemplate = state.getTemplate().getEmailTemplate();
        if (emailTemplate != null) {
            subject = emailTemplateEvaluator.getSubject(emailTemplate, null, context);
            body = emailTemplateEvaluator.getMessage(emailTemplate, null, context);
        } else {
            String name = config.getObject().getName();
            subject = Messages.format("scheduledreport.email.subject", name);
            body = Messages.format("scheduledreport.email.message", name);
        }
        mailer.setFrom(from);
        mailer.setTo(to.toArray(new String[to.size()]));
        mailer.setSubject(subject);
        mailer.setBody(body);
        mailer.addAttachment(document);
        mailer.send();
        state.setEmail(StringUtils.join(to, "; "));
    }

    /**
     * Prints the report.
     *
     * @param printer the report printer
     * @param state   the reporting state
     */
    private void print(SQLReportPrinter printer, State state) {
        String printerName = config.getString(ScheduledReportJobConfigurationEditor.PRINTER);
        printer.print(printerName);
        state.setPrinter(printerName);
    }

    /**
     * Invoked on completion of a job. Sends a message notifying the registered users of completion or failure of the
     * job if required.
     *
     * @param exception the exception, if the job failed, otherwise {@code null}
     * @param state     the job state
     */
    private void complete(Throwable exception, State state) {
        if (exception != null || config.getBoolean("notifyOnSuccess")) {
            Set<User> users = notifier.getUsers((Entity) config.getObject());
            if (!users.isEmpty()) {
                notifyUsers(users, state, exception);
            }
        }
    }

    /**
     * Notifies users of completion or failure of the job.
     *
     * @param users     the users to notify
     * @param state     the job state
     * @param exception the exception, if the job failed, otherwise {@code null}
     */
    private void notifyUsers(Set<User> users, State state, Throwable exception) {
        String subject;
        String reason;
        StringBuilder text = new StringBuilder();
        if (exception != null) {
            reason = SystemMessageReason.ERROR;
            subject = Messages.format("scheduledreport.exception.subject", config.getObject().getName());
            text.append(Messages.format("scheduledreport.exception.message", exception.getMessage()));
        } else {
            reason = SystemMessageReason.COMPLETED;
            double seconds = state.getElapsed() / 1000;
            subject = Messages.format("scheduledreport.subject", config.getObject().getName(), seconds);
            if (state.getFile() != null) {
                text.append(Messages.format("scheduledreport.filed", state.getFile()));
            }
            if (state.getEmail() != null) {
                text.append(Messages.format("scheduledreport.emailed", state.getEmail()));
            }
            if (state.getPrinter() != null) {
                text.append(Messages.format("scheduledreport.printed", state.getPrinter()));
            }
        }
        notifier.send(users, subject, reason, text.toString());
    }

    private static class State {

        private final long begin;

        private File file;

        private String to;

        private String printer;

        private DocumentTemplate template;

        public State() {
            begin = System.currentTimeMillis();
        }

        public void setFile(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public void setEmail(String to) {
            this.to = to;
        }

        public String getEmail() {
            return to;
        }

        public void setPrinter(String printer) {
            this.printer = printer;
        }

        public String getPrinter() {
            return printer;
        }

        public long getElapsed() {
            return System.currentTimeMillis() - begin;
        }

        public void setTemplate(DocumentTemplate template) {
            this.template = template;
        }

        public DocumentTemplate getTemplate() {
            return template;
        }
    }
}
