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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs.docload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.etl.tools.doc.DefaultLoaderListener;
import org.openvpms.etl.tools.doc.IdLoader;
import org.openvpms.etl.tools.doc.LoaderListener;
import org.openvpms.etl.tools.doc.LoggingLoaderListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.messaging.MessageHelper;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A job for loading documents using the {@link IdLoader}.
 *
 * @author Tim Anderson
 */
public class DocumentLoaderJob implements InterruptableJob, StatefulJob {

    /**
     * The job configuration.
     */
    private final Entity configuration;

    /**
     * The archetype service
     */
    private final IArchetypeService service;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * The maximum subject length.
     */
    private final int subjectLength;

    /**
     * The maximum message length.
     */
    private final int messageLength;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DocumentLoaderJob.class);

    /**
     * Determines if loading should stop.
     */
    private volatile boolean stop;

    /**
     * Constructs a {@link DocumentLoaderJob}.
     *
     * @param configuration      the configuration
     * @param service            the archetype service
     * @param transactionManager the transaction manager
     */
    public DocumentLoaderJob(Entity configuration, IArchetypeRuleService service,
                             PlatformTransactionManager transactionManager) {

        this.configuration = configuration;
        this.service = service;
        this.transactionManager = transactionManager;
        ArchetypeDescriptor descriptor = DescriptorHelper.getArchetypeDescriptor(MessageArchetypes.SYSTEM, service);
        subjectLength = getMaxLength(descriptor, "description");
        messageLength = getMaxLength(descriptor, "message");
    }

    /**
     * Called by the {@link Scheduler} when a {@link Trigger} fires that is associated with the {@code Job}.
     *
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Listener listener = null;
        try {
            stop = false;
            IMObjectBean bean = new IMObjectBean(configuration, service);
            File source = getDir(bean.getString("sourceDir"));
            if (source == null || !source.exists()) {
                throw new IllegalStateException("Invalid source directory: " + source);
            }
            File target = getDir(bean.getString("targetDir"));
            if (target == null || !target.exists()) {
                throw new IllegalStateException("Invalid destination directory: " + target);
            }
            String idPattern = bean.getString("idPattern");
            boolean overwrite = bean.getBoolean("overwrite");
            boolean recurse = bean.getBoolean("recurse");
            String[] types = bean.getString("archetypes", "").split(",");
            types = StringUtils.trimArrayElements(types);
            boolean logLoad = bean.getBoolean("log");
            boolean stopOnError = bean.getBoolean("stopOnError");

            IdLoader loader = new IdLoader(source, types, service, transactionManager, recurse, overwrite,
                                           Pattern.compile(idPattern));
            LoaderListener delegate = logLoad ? new LoggingLoaderListener(log, target)
                                              : new DefaultLoaderListener(target);
            listener = new Listener(delegate);
            loader.setListener(listener);

            while (!stop && loader.hasNext()) {
                if (!loader.loadNext() && stopOnError) {
                    break;
                }
            }
            complete(listener, null);
        } catch (Throwable exception) {
            log.error(exception, exception);
            complete(listener, exception);
        }
    }

    /**
     * <p/>
     * Called by the {@link Scheduler} when a user interrupts the {@code Job}.
     *
     * @throws UnableToInterruptJobException if there is an exception while interrupting the job.
     */
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        stop = true;
    }

    /**
     * Invoked on completion of a jobSends a message notifying the registered users of completion or failure of the job.
     *
     * @param listener  the loader listener
     * @param exception the exception, if the job failed, otherwise {@code null}
     */
    private void complete(Listener listener, Throwable exception) {
        if ((listener != null && (listener.getErrors() != 0 || listener.getLoaded() != 0)) || exception != null) {
            EntityBean bean = new EntityBean(configuration, service);
            Set<User> users = MessageHelper.getUsers(bean.getNodeTargetEntities("notify"));
            if (!users.isEmpty()) {
                notifyUsers(listener, exception, users);
            }
        }
    }

    /**
     * Notifies users of completion or failure of the job.
     *
     * @param listener  the loader listener
     * @param exception the exception, if the job failed, otherwise {@code null}
     * @param users     the users to notify
     */
    private void notifyUsers(Listener listener, Throwable exception, Set<User> users) {
        String subject;
        String reason;
        StringBuilder text = new StringBuilder();
        if (exception != null) {
            reason = SystemMessageReason.ERROR;
            subject = Messages.format("docload.subject.exception", configuration.getName());
            text.append(Messages.format("docload.exception", exception.getMessage()));
        } else {
            int loaded = listener.getLoaded();
            int errors = listener.getErrors();
            if (errors != 0) {
                reason = SystemMessageReason.ERROR;
                subject = Messages.format("docload.subject.errors", configuration.getName(), errors);
            } else {
                reason = SystemMessageReason.COMPLETED;
                subject = Messages.format("docload.subject.success", configuration.getName(), loaded);
            }
        }
        if (listener != null) {
            if (!listener.errors.isEmpty()) {
                if (text.length() != 0) {
                    text.append("\n\n");
                }
                text.append(Messages.get("docload.error"));
                text.append("\n");
                for (Map.Entry<File, String> entry : listener.errors.entrySet()) {
                    text.append(Messages.format("docload.error.item", entry.getKey(), entry.getValue()));
                    text.append("\n");
                }
            }
            if (!listener.missingAct.isEmpty()) {
                if (text.length() != 0) {
                    text.append("\n\n");
                }
                text.append(Messages.get("docload.missingAct"));
                text.append("\n");
                for (Map.Entry<File, Long> entry : listener.missingAct.entrySet()) {
                    text.append(Messages.format("docload.missingAct.item", entry.getValue(), entry.getKey()));
                    text.append("\n");
                }
            }
            if (!listener.alreadyLoaded.isEmpty()) {
                if (text.length() != 0) {
                    text.append("\n\n");
                }
                text.append(Messages.get("docload.alreadyLoaded"));
                text.append("\n");
                for (Map.Entry<File, Long> entry : listener.alreadyLoaded.entrySet()) {
                    text.append(Messages.format("docload.alreadyLoaded.item", entry.getValue(), entry.getKey()));
                    text.append("\n");
                }
            }
        }
        subject = truncate(subject, subjectLength);
        String message = truncate(text.toString(), messageLength);
        for (User user : users) {
            send(user, subject, reason, message);
        }
    }

    /**
     * Sends a message to a user.
     *
     * @param user    the user to send to
     * @param subject the subject
     * @param reason  the reason
     * @param text    the message text
     */
    private void send(User user, String subject, String reason, String text) {
        Act act = (Act) service.create(MessageArchetypes.SYSTEM);
        ActBean message = new ActBean(act);
        message.addNodeParticipation("to", user);
        message.setValue("reason", reason);
        message.setValue("description", subject);
        message.setValue("message", text);
        message.save();
    }

    /**
     * Helper to determine the maximum length of a node.
     *
     * @param archetype the archetype descriptor
     * @param node      the node name
     * @return the maximum length of the node
     */
    private int getMaxLength(ArchetypeDescriptor archetype, String node) {
        int result = NodeDescriptor.DEFAULT_MAX_LENGTH;
        if (archetype != null) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor != null) {
                result = descriptor.getMaxLength();
            }
        }
        return result;
    }

    /**
     * Helper to return a file given a path.
     *
     * @param path the path. May be {@code null}
     * @return the file. May be {@code null}
     */
    private File getDir(String path) {
        return path != null ? new File(path) : null;
    }

    /**
     * Helper to truncate a string if it exceeds a maximum length.
     *
     * @param value     the value to truncate
     * @param maxLength the maximum length
     * @return the new value
     */
    private String truncate(String value, int maxLength) {
        return value != null && value.length() > maxLength ? value.substring(0, maxLength) : value;
    }


    private class Listener extends DelegatingLoaderListener {

        private LinkedHashMap<File, Long> alreadyLoaded = new LinkedHashMap<File, Long>();

        private LinkedHashMap<File, Long> missingAct = new LinkedHashMap<File, Long>();

        private LinkedHashMap<File, String> errors = new LinkedHashMap<File, String>();


        /**
         * Constructs a {@link Listener}.
         *
         * @param listener the listener to delegate to
         */
        public Listener(org.openvpms.etl.tools.doc.LoaderListener listener) {
            super(listener);
        }

        /**
         * Notifies that a file couldn't be loaded as it or another file had already been processed.
         *
         * @param file the file
         * @param id   the corresponding act identifier
         */
        @Override
        public void alreadyLoaded(File file, long id) {
            super.alreadyLoaded(file, id);
            alreadyLoaded.put(file, id);
        }

        /**
         * Notifies that a file couldn't be loaded as there was no corresponding act.
         *
         * @param file the file
         * @param id   the corresponding act identifier
         */
        @Override
        public void missingAct(File file, long id) {
            super.missingAct(file, id);
            missingAct.put(file, id);
        }

        /**
         * Notifies that a file couldn't be loaded due to error.
         *
         * @param file      the file
         * @param exception the error
         */
        @Override
        public void error(File file, Throwable exception) {
            super.error(file, exception);
            errors.put(file, exception.getMessage());
        }

    }

}
