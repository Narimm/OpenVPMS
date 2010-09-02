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

import java.util.List;
import java.util.Collections;


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
    private List<String> excludes;

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
     * <p/>
     * If there are no error codes, then all instances of the exception are excluded.
     *
     * @param excludes the error codes to exclude from reporting. May be <tt>null</tt>
     */
    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    /**
     * Returns the error codes to exclude from reporting.
     * <p/>
     * If there are no error codes, then all instances of the exception are excluded.
     *
     * @return the error codes to exclude from reporting
     */
    public List<String> getExcludes() {
        return excludes != null ? excludes : Collections.<String>emptyList();
    }

    /**
     * Determines if an exception is excluded from reporting.
     * <p/>
     * By default an exception is excluded, unless the configuration specifies error codes as specific exclusions
     * via {@link #getExcludes()}.
     *
     * @param exception the exception to check
     * @return <tt>true</tt> if the exception is excluded from reporting
     */
    public boolean isExcluded(Throwable exception) {
        boolean result = true;
        if (excludes != null && !excludes.isEmpty()) {
            try {
                // TODO - bit of a hack. All OpenVPMSException subclasses currently define a getErrorCode() method that
                // returns an enum.
                String s = BeanUtils.getProperty(exception, "errorCode");
                result = excludes.contains(s);
            } catch (Throwable ignore) {
                // do nothing
            }
        }
        return result;
    }

}
