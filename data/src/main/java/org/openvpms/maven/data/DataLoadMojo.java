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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.maven.data;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.openvpms.tools.data.loader.StaxArchetypeDataLoader;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;


/**
 * Loads data using the {@link StaxArchetypeDataLoader}.
 *
 * @goal load
 */
public class DataLoadMojo extends AbstractDataMojo {


    /**
     * The batch size, used to reduce database access.
     *
     * @parameter expression="1000"
     * @optional
     */
    private int batchSize;

    /**
     * Sets the batch size, used to batch database writes.
     *
     * @param batchSize the batch size
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Returns the batch size, used to batch databases writes.
     *
     * @return the batch size
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Loads data from the specified directory.
     *
     * @param loader the archetype data loader to use
     * @throws XMLStreamException    if a file cannot be read
     * @throws FileNotFoundException if a file cannot be found
     */
    protected void doExecute(StaxArchetypeDataLoader loader) throws XMLStreamException, FileNotFoundException {
        loader.setVerbose(isVerbose());
        loader.setBatchSize(batchSize);
        loader.load(getDir().getPath());
    }
}