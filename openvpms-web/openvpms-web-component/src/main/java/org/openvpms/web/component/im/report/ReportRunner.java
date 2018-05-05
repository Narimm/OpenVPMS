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

package org.openvpms.web.component.im.report;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.report.Report;

import java.util.function.Supplier;

/**
 * Runs a report, logging the elapsed time.
 *
 * @author Tim Anderson
 */
public class ReportRunner {

    /**
     * The report.
     */
    private final Report report;

    /**
     * The object being reported on, or {@code null} if a collection is being reported on
     */
    private final Object object;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReportRunner.class);

    /**
     * Constructs a {@link ReportRunner}.
     *
     * @param report the report
     */
    public ReportRunner(Report report) {
        this(report, null);
    }

    /**
     * Constructs a {@link ReportRunner}.
     *
     * @param report the report
     * @param object tthe object being reported on, or {@code null} if a collection is being reported on
     */
    public ReportRunner(Report report, Object object) {
        this.report = report;
        this.object = object;
    }

    /**
     * Runs the report to generate a document, performing performance logging if logging is enabled.
     *
     * @param generator the report generator
     * @return the generated document
     */
    public Document run(Supplier<Document> generator) {
        Document result;
        StopWatch stopWatch = init();
        try {
            result = generator.get();
        } finally {
            if (stopWatch != null) {
                complete(stopWatch);
            }
        }
        return result;
    }

    /**
     * Runs the report, performing performance logging if logging is enabled.
     *
     * @param generator the report generator
     */
    public void run(Runnable generator) {
        StopWatch stopWatch = init();
        try {
            generator.run();
        } finally {
            if (stopWatch != null) {
                complete(stopWatch);
            }
        }
    }

    /**
     * Logs the start of report generation, if logging is enabled.
     *
     * @return the stop watch or {@code null} if logging is disabled
     */
    private StopWatch init() {
        StopWatch stopWatch = null;
        if (log.isDebugEnabled()) {
            stopWatch = new StopWatch();
            stopWatch.start();
            String value;
            if (object instanceof IMObject) {
                value = ((IMObject) object).getObjectReference().toString();
            } else if (object != null) {
                value = object.toString();
            } else {
                value = "collection";
            }
            log.debug("Running report='" + report.getName() + "', for " + value);
        }
        return stopWatch;
    }

    /**
     * Logs the end of report generation.
     *
     * @param stopWatch the stop watch
     */
    private void complete(StopWatch stopWatch) {
        log.debug("Finished report='" + report.getName() + "', elapsed=" + stopWatch);
    }

}
