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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Launcher settings dialog.
 *
 * @author Tim Anderson
 */
public class SettingsDialog extends JDialog {

    /**
     * The root panel.
     */
    private JPanel contentPane;

    /**
     * The editor path field.
     */
    private JTextField path;

    /**
     * The browse button.
     */
    private JButton browse;

    /**
     * The OK button.
     */
    private JButton ok;

    /**
     * The cancel button.
     */
    private JButton cancel;

    /**
     * Determines if the OS is windows.
     */
    private final boolean windows;

    /**
     * Constructs a {@link SettingsDialog}.
     */
    public SettingsDialog() {
        String os = System.getProperty("os.name");
        windows = (os != null) && os.toLowerCase().startsWith("windows");

        layoutComponents();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(ok);

        path.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!checkPath()) {
                    onBrowse();
                }
            }
        });
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        ok.setEnabled(false);
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onBrowse();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * Sets the editor path.
     *
     * @param path the editor path. May be {@code null}
     */
    public void setPath(String path) {
        this.path.setText(path);
        checkPath();
    }

    /**
     * Returns the editor path.
     *
     * @return the editor path
     */
    public String getPath() {
        return path.getText();
    }

    /**
     * Checks the path. If valid, enables the OK button.
     *
     * @return {@code true} if the path is valid
     */
    private boolean checkPath() {
        String path = getPath();
        boolean enabled = false;
        if (path != null) {
            File file = new File(path);
            enabled = isExecutable(file);
        }
        ok.setEnabled(enabled);
        return enabled;
    }

    /**
     * Invoked when the "browse" button is pressed.
     */
    private void onBrowse() {
        JFileChooser chooser = new JFileChooser();
        String path = getPath();
        if (path != null && path.length() != 0) {
            try {
                Path p = Paths.get(path);
                while (p != null && !Files.exists(p)) {
                    p = p.getParent();
                }
                if (p != null) {
                    path = p.toString();
                }
            } catch (InvalidPathException ignore) {
                // do nothing
            }
            if (path != null) {
                chooser.setSelectedFile(new File(path));
            }
        }

        FileFilter filter = getFilter();
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setPath(chooser.getSelectedFile().getPath());
        }
    }

    /**
     * Invoked when the OK button is pressed. This closes the dialog.
     */
    private void onOK() {
        dispose();
    }

    /**
     * Invoked when the cancel button is pressed. This exits the app.
     */
    private void onCancel() {
        System.exit(1);
    }

    /**
     * Returns the file chooser filter.
     * <p/>
     * This returns a filter that selects executables and directories.
     *
     * @return file chooser filter
     */
    private FileFilter getFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || isExecutable(f);
            }

            @Override
            public String getDescription() {
                return "Executable files";
            }
        };
    }

    /**
     * Determines if a file is executable
     *
     * @param f the file
     * @return {@code true} if the file is executable
     */
    private boolean isExecutable(File f) {
        return !f.isDirectory() && windows && f.getName().toLowerCase().endsWith(".exe")
               || (!windows && Files.isExecutable(f.toPath()));
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setTitle("OpenVPMS Editor Launcher");
        JLabel logo = new JLabel();
        logo.setIcon(new ImageIcon(getClass().getResource("/openvpms.png")));
        JLabel prompt = new JLabel("Please enter the path to OpenOffice/LibreOffice");
        JLabel label = new JLabel("Path");
        path = new JTextField();
        path.setColumns(50);
        browse = new JButton("Browse");
        ok = new JButton("OK");
        cancel = new JButton("Cancel");
        ok.setPreferredSize(cancel.getPreferredSize());

        contentPane = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        contentPane.setLayout(layout);
        add(contentPane, logo, 0, 0, 3, GridBagConstraints.EAST);
        add(contentPane, prompt, 0, 1, 3, GridBagConstraints.WEST);
        add(contentPane, label, 0, 2, 1, GridBagConstraints.WEST);
        add(contentPane, path, 1, 2, 1, GridBagConstraints.CENTER);
        add(contentPane, browse, 2, 2, 1, GridBagConstraints.EAST);
        add(contentPane, ok, 0, 3, 1, GridBagConstraints.WEST);
        add(contentPane, cancel, 1, 3, 1, GridBagConstraints.WEST);
    }

    /**
     * Helper to add a component to a panel using {@code GridBagConstraints}.
     *
     * @param panel     the panel
     * @param component the component to add
     * @param x         the x position
     * @param y         the y position
     * @param colspan   the no. of columns to span
     * @param anchor    the anchor constraint
     */
    private void add(JPanel panel, JComponent component, int x, int y, int colspan, int anchor) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.anchor = anchor;
        if (anchor == GridBagConstraints.CENTER) {
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
        }
        c.gridwidth = colspan;
        c.insets = new Insets(10, 10, 10, 10);
        panel.add(component, c);
    }

}
