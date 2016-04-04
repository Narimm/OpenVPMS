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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.webdav.launch;

import javax.jnlp.BasicService;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The launcher the configuration.
 *
 * @author Tim Anderson
 */
class Configuration {

    /**
     * The basic service.
     */
    private final BasicService bs;

    /**
     * The persistence service.
     */
    private final PersistenceService ps;

    /**
     * The .odt editor path property name.
     */
    private static final String ODT_EDITOR_PATH = "ODTEditorPath";

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Configuration.class.getName());

    /**
     * Constructs a {@link Configuration}.
     *
     * @param basicService       the basic service
     * @param persistenceService the persistence service
     */
    public Configuration(BasicService basicService, PersistenceService persistenceService) {
        this.bs = basicService;
        this.ps = persistenceService;
    }

    /**
     * Returns the path to the .odt editor.
     *
     * @return the path to the .odt editor. May be {@code nul}
     */
    public String getODTEditorPath() {
        return getProperty(ODT_EDITOR_PATH);
    }

    /**
     * Returns the path to the .odt editor.
     *
     * @param path the path to the .odt editor. May be {@code null}
     */
    public void setODTEditorPath(String path) {
        setProperty(ODT_EDITOR_PATH, path);
    }

    /**
     * Sets a property.
     *
     * @param name  the property name
     * @param value the property value. May be {@code null}
     */
    private void setProperty(String name, String value) {
        try {
            URL url = new URL(bs.getCodeBase().toString() + name);
            try {
                ps.delete(url);
            } catch (Throwable ignore) {
                // do nothing
            }
            if (value != null) {
                byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                ps.create(url, bytes.length);
                FileContents fileContents = ps.get(url);
                OutputStream outputStream = fileContents.getOutputStream(true);
                outputStream.write(bytes);
                outputStream.close();
            }
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Failed to save property=" + name + ", value=" + value, exception);
        }
    }

    private String getProperty(String name) {
        String result = null;
        try {
            URL pathURL = new URL(bs.getCodeBase().toString() + name);
            try {
                FileContents fileContents = ps.get(pathURL);
                InputStream inputStream = fileContents.getInputStream();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                }
                inputStream.close();
                output.close();
                result = new String(output.toByteArray(), StandardCharsets.UTF_8);
            } catch (FileNotFoundException ignore) {
                // do nothing
            }
        } catch (Throwable exception) {
            logger.log(Level.WARNING, "Failed to retrieve property=" + name, exception);
        }
        return result;
    }

}
