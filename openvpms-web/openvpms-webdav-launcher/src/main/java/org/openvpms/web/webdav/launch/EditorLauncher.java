package org.openvpms.web.webdav.launch;

import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import javax.swing.JOptionPane;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Launches OpenOffice to edit a document.
 * <p>
 * Note that this uses the SWT Program class on Windows, Macs and some flavours of Linux to launch OpenOffice.
 * <p>
 * On other platforms it falls back to launching it from the PATH.
 * <p>
 * SWT is used as it is more likely to support Linux than java.awt.Desktop. The PATH is used as a fallback as users
 * may not have OpenOffice in the PATH.
 *
 * @author Tim Anderson
 */
public class EditorLauncher {

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(EditorLauncher.class.getName());

    /**
     * The main line.
     *
     * @param args the command line arguments. This accepts single argument, the URL of the document to edit
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            logger.info("Launching soffice, os.name=" + System.getProperty("os.name") + ", os.arch="
                        + System.getProperty("os.arch"));
            String url = args[0];
            if (!launchViaSWT(url) && !launchViaProcess(url)) {
                JOptionPane.showMessageDialog(null, "OpenOffice could not be located to edit the document.",
                                              "OpenVPMS Editor Launcher", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.err.println("usage: " + EditorLauncher.class.getName() + " <url>");
        }
    }


    /**
     * Launches OpenOffice using the SWT Program class.
     *
     * @param url the document URL
     * @return {@code true} if the launch was successful
     */
    private static boolean launchViaSWT(String url) {
        boolean result = false;
        Display display = null;
        try {
            display = new Display();
            Program program = Program.findProgram(".odt");
            if (program != null) {
                program.execute(url);
                result = true;
            }
        } catch (Throwable exception) {
            logger.log(Level.WARNING, "Failed to launch OpenOffice via SWT", exception);
        } finally {
            if (display != null) {
                display.dispose();
            }
        }
        return result;
    }

    /**
     * Launches OpenOffice using ProcessBuilder.
     *
     * @param url the document URL
     * @return {@code true} if the launch was successful
     */
    private static boolean launchViaProcess(String url) {
        boolean result = false;
        Path path = getPath();
        if (path != null) {
            ProcessBuilder builder = new ProcessBuilder(path.toString(), url);
            try {
                Process process = builder.inheritIO().start();
                result = process.isAlive();
            } catch (Throwable exception) {
                logger.log(Level.SEVERE, "Failed to launch OpenOffice ath path: " + path, exception);
            }
        }
        return result;
    }

    /**
     * Returns the path to soffice, it it is located in the PATH environment variable.
     *
     * @return the path, or {@code null} if it doesn't exist
     */
    private static Path getPath() {
        Path result = null;
        String osName = System.getProperty("os.name");
        String name = osName != null && osName.startsWith("Windows") ? "soffice.exe" : "soffice";
        String paths = System.getenv("PATH");
        if (paths != null) {
            for (String path : paths.split(Pattern.quote(File.pathSeparator))) {
                try {
                    Path dir = Paths.get(path);
                    Path file = dir.resolve(name);
                    if (Files.exists(file)) {
                        result = file;
                        break;
                    }
                } catch (InvalidPathException ignore) {
                    // do nothing
                }
            }
        }
        if (result == null) {
            logger.warning(name + " not found in PATH: " + paths);
        }
        return result;
    }
}

