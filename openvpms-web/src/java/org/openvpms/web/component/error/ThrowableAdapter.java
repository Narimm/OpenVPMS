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

import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.PrintWriter;


/**
 * Adapts exception heirarchies to a simple structure to support XML serialization.
 * <p/>
 * This is to avoid serializing complex object graphs that may be referenced by the exceptions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ThrowableAdapter {

    /**
     * The exception class type.
     */
    private Class type;

    /**
     * The exception message.
     */
    private String message;

    /**
     * The stack trace.
     */
    private StackTraceElement[] stackTrace;

    /**
     * The cause. May be <tt>null</tt>.
     */
    private ThrowableAdapter cause;

    /**
     * Constructor provided to support serialization.
     */
    public ThrowableAdapter() {

    }

    /**
     * Constructs a <tt>ThrowableAdapter</tt>.
     *
     * @param exception the exception
     */
    public ThrowableAdapter(Throwable exception) {
        type = exception.getClass();
        message = exception.getLocalizedMessage();
        stackTrace = exception.getStackTrace();
        Throwable root = ExceptionUtils.getCause(exception);
        if (root != null) {
            cause = new ThrowableAdapter(root);
        }
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the stack trace.
     *
     * @return the stack trace
     */
    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    /**
     * Returns the cause.
     *
     * @return the cause. May be <tt>null</tt>
     */
    public ThrowableAdapter getCause() {
        return cause;
    }

    /**
     * Returns a short description of the exception.
     * The result is the concatenation of:
     * <ul>
     * <li> the exception class name
     * <li> ": " (a colon and a space)
     * <li> the exception message returned by <tt>getMessage()</tt>
     * </ul>
     * If <tt>getMessage()</tt> returns <tt>null</tt>, then just the class name is returned.
     */
    public String toString() {
        String result = type.getName();
        return (message != null) ? (result + ": " + message) : result;
    }

    /**
     * Prints the exception and its backtrace to the specified writer.
     *
     * @param writer the writer
     */
    public void printStackTrace(PrintWriter writer) {
        writer.println(this);
        for (StackTraceElement trace : stackTrace) {
            writer.println("\tat " + trace);
        }
        if (cause != null) {
            cause.printStackTraceAsCause(writer, stackTrace);
        }
    }

    /**
     * Print the stack trace as a cause for the specified stack trace.
     *
     * @param writer      the writer
     * @param causedTrace the parent stack trace
     */
    private void printStackTraceAsCause(PrintWriter writer, StackTraceElement[] causedTrace) {
        StackTraceElement[] trace = stackTrace;
        int m = trace.length - 1, n = causedTrace.length - 1;
        while (m >= 0 && n >= 0 && trace[m].equals(causedTrace[n])) {
            m--;
            n--;
        }
        int framesInCommon = trace.length - 1 - m;

        writer.println("Caused by: " + this);
        for (int i = 0; i <= m; i++) {
            writer.println("\tat " + trace[i]);
        }
        if (framesInCommon != 0) {
            writer.println("\t... " + framesInCommon + " more");
        }

        // Recurse if we have a cause
        if (cause != null) {
            cause.printStackTraceAsCause(writer, trace);
        }
    }

}
