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
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launches OpenOffice to edit a document.
 *
 * @author Tim Anderson
 */
public class EditorLauncher {


    /**
     * The URL of the file to edit.
     */
    private final String url;

    /**
     * The configuration, or {@code null} if the BasicService or PersistenceService aren't supported.
     */
    private final Configuration configuration;

    /**
     * The default editor paths resource.
     */
    private static final String DEFAULT_EDITOR_PATHS = "/defaultEditorPaths.txt";

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(EditorLauncher.class.getName());

    /**
     * Constructs an {@link EditorLauncher}.
     *
     * @param url                the URL of the file to edit
     * @param basicService       the basic service
     * @param persistenceService the persistence service
     */
    public EditorLauncher(String url, BasicService basicService, PersistenceService persistenceService) {
        this.url = url;
        configuration = (basicService != null && persistenceService != null)
                        ? new Configuration(basicService, persistenceService) : null;
    }

    /**
     * Launches the editor.
     */
    public void launch() {
        String path = configuration != null ? configuration.getODTEditorPath() : null;
        if (!exists(path)) {
            path = getDefaultPath();
            path = configure(path, null);
        }
        launch(path);
    }

    /**
     * The main line.
     *
     * @param args the command line arguments. This accepts a single argument, the URL of the document to edit
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            logger.info("Launching soffice, os.name=" + System.getProperty("os.name") + ", os.arch="
                        + System.getProperty("os.arch"));
            String url = args[0];
            BasicService basicService = getService(BasicService.class);
            PersistenceService persistenceService = getService(PersistenceService.class);
            EditorLauncher launcher = new EditorLauncher(url, basicService, persistenceService);
            launcher.launch();
        } else {
            System.err.println("usage: " + EditorLauncher.class.getName() + " <url>");
        }
    }

    /**
     * Configures the editor.
     *
     * @param path    the configured path, or {@code null} if no path has been configured
     * @param message an error message, if the path couldn't be used to launch an editor
     * @return the new path
     */
    protected String configure(String path, final String message) {
        final SettingsDialog dialog = new SettingsDialog();
        dialog.setPath(path);
        dialog.pack();
        if (message != null) {
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    JOptionPane.showMessageDialog(dialog, message, "Launch Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        dialog.setVisible(true);
        path = dialog.getPath();
        if (configuration != null) {
            configuration.setODTEditorPath(path);
        }
        return path;
    }

    /**
     * Launches the editor at the specified path.
     *
     * @param path the path
     */
    private void launch(String path) {
        ProcessBuilder builder = new ProcessBuilder(path, url);
        try {
            Process process = builder.inheritIO().start();
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                logger.log(Level.SEVERE, "Editor at path: " + path + " terminated with error code: " + exitValue);
                relaunch(path, "The program exited with error code: " + exitValue);
            }
        } catch (Throwable exception) {
            logger.log(Level.SEVERE, "Failed to launch editor at path: " + path, exception);
            relaunch(path, "The editor failed to start: " + exception.getMessage());
        }
    }

    /**
     * Invoked when launching fails to display the configuration dialog and re-launch.
     *
     * @param path  the editor path
     * @param error the error message
     */
    private void relaunch(String path, String error) {
        path = configure(path, error);
        launch(path);
    }

    /**
     * Determines if a path exists.
     *
     * @param path the path. May be {@code null}
     * @return {@code true} if the path exists
     */
    private boolean exists(String path) {
        boolean result = false;
        if (path != null) {
            try {
                result = Files.exists(Paths.get(path));
            } catch (InvalidPathException ignore) {
                // do nothing
            }
        }
        return result;
    }

    /**
     * Returns the default editor path.
     *
     * @return the default editor path, or {@code null} if none is found
     */
    private String getDefaultPath() {
        String path = null;
        InputStream stream = getClass().getResourceAsStream(DEFAULT_EDITOR_PATHS);
        if (stream != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        line = line.trim();
                        if (!line.isEmpty() && exists(line)) {
                            path = line;
                            break;
                        }
                    }
                }
            } catch (IOException exception) {
                logger.log(Level.SEVERE, "Failed to read " + DEFAULT_EDITOR_PATHS, exception);
            }
        }
        return path;
    }

    /**
     * Returns a JNLP service.
     *
     * @param type the service type
     * @return {@code null} if the service is not supported
     */
    @SuppressWarnings("unchecked")
    private static <T> T getService(Class<T> type) {
        try {
            return (T) ServiceManager.lookup(type.getName());
        } catch (UnavailableServiceException e) {
            logger.log(Level.SEVERE, "Service " + type.getName() + " is not supported on this platform", e);
        }
        return null;
    }
}

