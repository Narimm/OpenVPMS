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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.tools.data.loader.StaxArchetypeDataLoader;
import org.openvpms.maven.archetype.AbstractHibernateMojo;
import org.springframework.context.ApplicationContext;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;


/**
 * Validates data using the {@link StaxArchetypeDataLoader}.
 *
 * @goal validate
 * @requiresDependencyResolution test
 */
public class DataValidateMojo extends AbstractDataMojo {

    /**
     * Validates data in the specified directory.
     *
     * @param loader the archetype data loader to use
     * @throws XMLStreamException    if a file cannot be read
     * @throws FileNotFoundException if a file cannot be found
     */
    protected void doExecute(StaxArchetypeDataLoader loader) throws XMLStreamException, FileNotFoundException {
        loader.setVerbose(isVerbose());
        loader.setValidateOnly(true);
        loader.load(getDir().getPath());
    }
}