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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.error;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.Collections;
import java.util.List;


/**
 * Exception configuration, used by {@link ErrorReporterConfig}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ExceptionConfig {

    /**
     * The exception class name.
     */
    private String className;

    /**
     * The set of error codes to exclude from reporting.
     */
    private List<String> codes;

    /**
     * The set of error messages to exclude from reporting.
     */
    private List<String> messages;

    private List<ExceptionConfig> causes;

    /**
     * Default constructor.
     */
    public ExceptionConfig() {
    }

    /**
     * Constructs an <tt>ExceptionConfig</tt>.
     *
     * @param className the exception class name
     */
    public ExceptionConfig(String className) {
        setClassName(className);
    }

    /**
     * Sets the exception class name.
     *
     * @param className the class name
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Returns the exception class name.
     *
     * @return the exception class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the error codes to exclude from reporting.
     *
     * @param codes the error codes to exclude from reporting. May be <tt>null</tt>
     */
    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    /**
     * Returns the error codes to exclude from reporting.
     *
     * @return the error codes to exclude from reporting
     */
    public List<String> getCodes() {
        return codes != null ? codes : Collections.<String>emptyList();
    }

    /**
     * Sets the exception messages to exclude from reporting.
     * Note: These should only be used were error messages are not localised.
     *
     * @param messages the messages. May be <tt>null</tt>
     */
    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    /**
     * Returns the exception messages to exclude from reporting.
     *
     * @return the exception messages to exclude from reporting
     */
    public List<String> getMessages() {
        return messages != null ? messages : Collections.<String>emptyList();
    }

    /**
     * Sets the root causes to exclude from reporting.
     *
     * @param causes the causes. May be <tt>null</tt>
     */
    public void setCauses(List<ExceptionConfig> causes) {
        this.causes = causes;
    }

    /**
     * Returns the root causes to exclude from reporting.
     *
     * @return the causes to exclude from reporting
     */
    public List<ExceptionConfig> getCauses() {
        return causes;
    }

    /**
     * Determines if an exception is excluded from reporting.
     * <p/>
     * By default, all exceptions are excluded, unless the configuration specifies error codes, messages, or causes
     * as specific exclusions.
     * <p/>
     * Where a cause is configured, it is evaluated against the root cause of the exception.
     *
     * @param exception the exception to check
     * @return <tt>true</tt> if the exception is excluded from reporting
     */
    public boolean isExcluded(Throwable exception) {
        boolean eval = false;
        if (codes != null && !codes.isEmpty()) {
            eval = true;
            try {
                // TODO - bit of a hack. All OpenVPMSException subclasses currently define a getErrorCode() method that
                // returns an enum.
                String s = BeanUtils.getProperty(exception, "errorCode");
                if (codes.contains(s)) {
                    return true;
                }
            } catch (Throwable ignore) {
                // do nothing
            }
        }
        if (messages != null && !messages.isEmpty()) {
            eval = true;
            String message = exception.getMessage();
            if (message != null) {
                if (messages.contains(message)) {
                    return true;
                }
            }
        }
        if (causes != null && !causes.isEmpty()) {
            eval = true;
            Throwable root = ExceptionUtils.getRootCause(exception);
            if (root != null) {
                for (ExceptionConfig cause : causes) {
                    if (cause.isExcluded(root)) {
                        return true;
                    }
                }
            }
        }
        return !eval;
    }

}
