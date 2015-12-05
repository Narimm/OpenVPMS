package org.openvpms.web.webdav.launch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import javax.swing.JOptionPane;

/**
 * Launches OpenOffice to edit a document.
 *
 * @author Tim Anderson
 */
public class EditorLauncher {

    /**
     * The main line.
     *
     * @param args the command line arguments. This accepts single argument, the URL of the document to edit
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            String file = args[0];
            launch(file);
        } else {
            System.err.println("usage: " + EditorLauncher.class.getName() + " <file>");
        }
    }

    private static void launch(String file) {
        int index = file.lastIndexOf(".");
        if (index != -1) {
            Display display = null;
            try {
                display = new Display();
                String suffix = file.substring(index);
                Program program = Program.findProgram(suffix);
                if (program != null) {
                    program.execute(file);
                } else {
                    JOptionPane.showMessageDialog(null, "thank you for using java");
                    MessageBox error = new MessageBox(display.getActiveShell(), SWT.ICON_ERROR | SWT.OK);
                    error.setText("OpenVPMS Editor Launcher");
                    error.setMessage("OpenOffice could not be located to edit the document.");
                    error.open();
                }
            } catch (Throwable exception) {
                if (display != null) {
                    MessageBox error = new MessageBox(display.getActiveShell(), SWT.ICON_ERROR | SWT.OK);
                    error.setText("OpenVPMS Editor Launcher");
                    error.setMessage("Failed to launch the editor: " + exception.getMessage());
                    exception.printStackTrace();
                } else {
                    exception.printStackTrace();
                }
            } finally {
                if (display != null) {
                    display.dispose();
                }
            }
        }
    }
}

