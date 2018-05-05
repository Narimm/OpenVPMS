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

package org.openvpms.web.component.processor;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.component.processor.BatchProcessor;


/**
 * A {@link BatchProcessor} that renders to a component.
 *
 * @author Tim Anderson
 */
public interface BatchProcessorComponent extends BatchProcessor {

    /**
     * Returns the processor title.
     *
     * @return the processor title. May be {@code null}
     */
    String getTitle();

    /**
     * Returns the component.
     *
     * @return the component
     */
    Component getComponent();

}
