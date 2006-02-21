package org.openvpms.web.component.dialog;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.component.util.ButtonRow;


/**
 * Generic popup window.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class PopupWindow extends WindowPane {

    /**
     * The layout pane.
     */
    private final SplitPane _layout;

    /**
     * The button row.
     */
    private final ButtonRow _row;


    /**
     * Construct a new <code>PopupWindow</code>.
     *
     * @param title the window title
     */
    public PopupWindow(String title) {
        this(title, null);
    }

    /**
     * Construct a new <code>PopupWindow</code>
     *
     * @param title the window title
     * @param style the window style
     */
    public PopupWindow(String title, String style) {
        super(title, null, null);
        setStyleName(style);

        _row = new ButtonRow();

        _layout = new SplitPane(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                new Extent(32));              // @todo - stylehseet
        _layout.add(_row);
        add(_layout);
    }

    /**
     * Show the window.
     */
    public void show() {
        if (getParent() == null) {
            Window root = ApplicationInstance.getActive().getDefaultWindow();
            root.getContent().add(this);
        }
    }

    /**
     * Close the window.
     */
    public void close() {
        userClose();
    }

    /**
     * Adds a listener to receive notification when the user presses a button.
     * The listener receives events from all buttons.
     *
     * @param listener the listener to add
     */
    public void addActionListener(ActionListener listener) {
        _row.addActionListener(listener);
    }

    /**
     * Adds a listener to receive notification when the user presses a specific
     * button.
     *
     * @param id       the button identifier
     * @param listener the listener to add
     */
    public void addActionListener(String id, ActionListener listener) {
        _row.addActionListener(id, listener);
    }

    /**
     * Removes an <code>ActionListener</code> from receiving notification when
     * the user presses a button.
     *
     * @param listener the listener to remove
     */
    public void removeActionListener(ActionListener listener) {
        _row.removeActionListener(listener);
    }

    /**
     * Removes an <code>ActionListener</code> from receiving notification when
     * the user presses a specific button.
     *
     * @param id       the button identifier
     * @param listener the listener to remove
     */
    public void removeActionListener(String id, ActionListener listener) {
        _row.removeActionListener(id, listener);
    }

    /**
     * Returns the layout pane.
     */
    protected SplitPane getLayout() {
        return _layout;
    }

    /**
     * Add a button.
     *
     * @param id the button identifier
     */
    protected void addButton(String id, ActionListener listener) {
        _row.addButton(id, listener);
    }

}
