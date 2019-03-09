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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.scheduler;

import org.quartz.DisallowConcurrentExecution;

/**
 * A marker interface for jobs to indicate that only a single instance of the job class may be run at any given time.
 * <p/>
 * This means that only a single configuration containing the job class can exist.
 * <p/>
 * Attempting to schedule multiple instances will fail.
 *
 * @author Tim Anderson
 */
@DisallowConcurrentExecution
public interface SingletonJob {
}
