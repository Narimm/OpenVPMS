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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.error;

import com.thoughtworks.xstream.XStream;
import org.openvpms.web.system.Version;

import java.util.Properties;


/**
 * Error report containing information to help diagnose errors.
 * <p/>
 * This includes the:
 * <ul>
 * <li>OpenVPMS version and version control revision</li>
 * <li>formatted error message</li>
 * <li>exception</li>
 * <li>JVM memory information</li>
 * <li>system properties</li>
 * <ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ErrorReport {

    /**
     * The OpenVPMS version.
     */
    private String version;

    /**
     * The OpenVPMS version control revision.
     */
    private String revision;

    /**
     * The error message.
     */
    private String message;

    /**
     * The exception.
     */
    private ThrowableAdapter exception;

    /**
     * Free memory.
     */
    private long freeMemory;

    /**
     * Total memory.
     */
    private long totalMemory;

    /**
     * Max memory.
     */
    private long maxMemory;

    /**
     * System properties.
     */
    private Properties properties;

    public StackTraceElement[] stackTrace;


    /**
     * Constructs an <tt>ErrorReport</tt>.
     *
     * @param message   the error message
     * @param exception the cause of the error
     */
    public ErrorReport(String message, Throwable exception) {
        this.message = message;
        version = Version.VERSION;
        revision = Version.REVISION;
        this.exception = (exception != null) ? new ThrowableAdapter(exception) : null;
        freeMemory = Runtime.getRuntime().freeMemory();
        totalMemory = Runtime.getRuntime().totalMemory();
        maxMemory = Runtime.getRuntime().maxMemory();
        properties = new Properties();
        properties.putAll(System.getProperties());
    }

    /**
     * Returns the OpenVPMS version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the OpenVPMS version.
     *
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the version control revision.
     *
     * @return the version control revision
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Sets the version control revision.
     *
     * @param revision the version control revision
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message.
     *
     * @param message the error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the exception.
     *
     * @return the exception. May be <tt>null</tt>
     */
    public ThrowableAdapter getException() {
        return exception;
    }

    /**
     * Sets the exception.
     *
     * @param exception the exception
     */
    public void setException(ThrowableAdapter exception) {
        this.exception = exception;
    }

    /**
     * Returns free memory.
     * <p/>
     * This is an approximation to the total amount of memory available for allocation, at the time the error occurred.
     *
     * @return the free memory, in bytes
     */
    public long getFreeMemory() {
        return freeMemory;
    }

    /**
     * Sets free memory.
     *
     * @param freeMemory the free memory, in bytes
     */
    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }

    /**
     * Returns total memory.
     * </p>
     * This is an approximation of the total amount of memory in the Java virtual machine, at the time the error
     * occurred.
     *
     * @return the total memory, in bytes
     */
    public long getTotalMemory() {
        return totalMemory;
    }

    /**
     * Sets total memory.
     *
     * @param totalMemory the total memory, in bytes
     */
    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    /**
     * Returns the maximum amount of memory that the virtual machine will attempt to use.
     *
     * @return the maximum amount of memory, in bytes.
     */
    public long getMaxMemory() {
        return maxMemory;
    }

    /**
     * Sets the maximum amount of memory that the virtual machine will attempt to use.
     *
     * @param maxMemory the maximum amount of memory, in bytes.
     */
    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    /**
     * Returns the system properties.
     *
     * @return the system properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the system properties.
     *
     * @param properties the system properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Serialize this to a pretty-printed XML String.
     *
     * @return a XML string representing this
     * @throws com.thoughtworks.xstream.core.BaseException
     *          if the object cannot be serialized
     */
    public String toXML() {
        XStream stream = new XStream();
        stream.alias("error-report", ErrorReport.class);
        stream.alias("exception", ThrowableAdapter.class);
        return stream.toXML(this);
    }

}
