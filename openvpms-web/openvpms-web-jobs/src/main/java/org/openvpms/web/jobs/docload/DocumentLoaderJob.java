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
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.etl.tools.doc.DefaultLoaderListener;
import org.openvpms.etl.tools.doc.IdLoader;
import org.openvpms.etl.tools.doc.LoaderListener;
import org.openvpms.etl.tools.doc.LoggingLoaderListener;
import org.openvpms.web.jobs.JobCompletionNotifier;
import org.openvpms.web.resource.i18n.Messages;
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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DocumentLoaderJob.class);

    /**
     * Determines if loading should stop.
     */
    private volatile boolean stop;

    /**
     * Used to send messages to users on completion or failure.
     */
    private final JobCompletionNotifier notifier;

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
        notifier = new JobCompletionNotifier(service);
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
            IMObjectBean bean = new IMObjectBean(configuration, service);
            File source = getDir(bean.getString("sourceDir"));
            if (source == null || !source.exists()) {
                throw new IllegalStateException("Invalid source directory: " + source);
            }
            File target = getDir(bean.getString("targetDir"));
            if (target == null || !target.exists()) {
                throw new IllegalStateException("Invalid destination directory: " + target);
            }
            File error = getDir(bean.getString("errorDir"));
            if (error != null && !error.exists()) {
                throw new IllegalStateException("Invalid error directory: " + error);
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
            LoaderListener delegate = logLoad ? new LoggingLoaderListener(log, target, error, true)
                                              : new DefaultLoaderListener(target, error, true);
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
     * <p>
     * Called by the {@link Scheduler} when a user interrupts the {@code Job}.
     *
     * @throws UnableToInterruptJobException if there is an exception while interrupting the job.
     */
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        stop = true;
    }

    /**
     * Invoked on completion of a job. Sends a message notifying the registered users of completion or failure of the
     * job.
     *
     * @param listener  the loader listener. May be {@code null}
     * @param exception the exception, if the job failed, otherwise {@code null}
     */
    private void complete(Listener listener, Throwable exception) {
        if ((listener != null && (listener.getErrors() != 0 || listener.getLoaded() != 0)) || exception != null) {
            Set<User> users = notifier.getUsers(configuration);
            if (!users.isEmpty()) {
                notifyUsers(listener, exception, users);
            }
        }
    }

    /**
     * Notifies users of completion or failure of the job.
     *
     * @param listener  the loader listener. May be {@code null}, if {@code exception} non-null
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
            append(text, listener.errors, "docload.error", "docload.error.item");
            append(text, listener.missingAct, "docload.missingAct", "docload.missingAct.item");
            append(text, listener.alreadyLoaded, "docload.alreadyLoaded", "docload.alreadyLoaded.item");
            append(text, listener.loaded, "docload.loaded", "docload.loaded.item");
        }
        notifier.send(users, subject, reason, text.toString());
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
     * Appends items to the text.
     *
     * @param text       the message text
     * @param entries    the entries to append
     * @param headingKey the heading resource bundle key
     * @param itemKey    the item resource bundle key
     */
    private <T> void append(StringBuilder text, Map<File, T> entries, String headingKey, String itemKey) {
        if (!entries.isEmpty()) {
            if (text.length() != 0) {
                text.append("\n\n");
            }
            text.append(Messages.get(headingKey));
            text.append("\n");
            for (Map.Entry<File, T> entry : entries.entrySet()) {
                text.append(Messages.format(itemKey, entry.getKey(), entry.getValue()));
                text.append("\n");
            }
        }
    }

    private class Listener extends DelegatingLoaderListener {

        private LinkedHashMap<File, Long> loaded = new LinkedHashMap<>();

        private LinkedHashMap<File, Long> alreadyLoaded = new LinkedHashMap<>();

        private LinkedHashMap<File, Long> missingAct = new LinkedHashMap<>();

        private LinkedHashMap<File, String> errors = new LinkedHashMap<>();


        /**
         * Constructs a {@link Listener}.
         *
         * @param listener the listener to delegate to
         */
        public Listener(org.openvpms.etl.tools.doc.LoaderListener listener) {
            super(listener);
        }

        /**
         * Notifies when a file is loaded.
         *
         * @param file the file
         * @param id   the corresponding act identifier
         * @return the new location of the file. May be {@code null}
         */
        @Override
        public File loaded(File file, long id) {
            File target = super.loaded(file, id);
            if (target != null) {
                file = target;
            }
            loaded.put(file, id);
            return target;
        }

        /**
         * Notifies that a file couldn't be loaded as it or another file had already been processed.
         *
         * @param file the file
         * @param id   the corresponding act identifier
         */
        @Override
        public File alreadyLoaded(File file, long id) {
            File target = super.alreadyLoaded(file, id);
            if (target != null) {
                file = target;
            }
            alreadyLoaded.put(file, id);
            return target;
        }

        /**
         * Notifies that a file couldn't be loaded as there was no corresponding act.
         *
         * @param file the file
         * @param id   the corresponding act identifier
         */
        @Override
        public File missingAct(File file, long id) {
            File target = super.missingAct(file, id);
            if (target != null) {
                file = target;
            }
            missingAct.put(file, id);
            return target;
        }

        /**
         * Notifies that a file couldn't be loaded due to error.
         *
         * @param file      the file
         * @param exception the error
         */
        @Override
        public File error(File file, Throwable exception) {
            File target = super.error(file, exception);
            if (target != null) {
                file = target;
            }
            errors.put(file, exception.getMessage());
            return target;
        }

    }

}
