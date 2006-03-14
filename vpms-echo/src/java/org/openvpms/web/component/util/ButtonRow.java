package org.openvpms.web.component.util;

import java.util.EventListener;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.EventListenerList;


/**
 * A row of buttons.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ButtonRow extends Row {

    /**
     * The button event listener.
     */
    private final ActionListener _listener;

    /**
     * The tab indexer. May be <code>null</code>
     */
    private final TabIndexer _indexer;

    /**
     * The row style.
     */
    private static final String STYLE = "ButtonRow";

    /**
     * The button style.
     */
    private static final String BUTTON_STYLE = "ButtonRow.Button";


    /**
     * Construct a new <code>ButtonRow</code>.
     */
    public ButtonRow() {
        this(null);
    }

    /**
     * Construct a new <code>ButtonRow</code>.
     *
     * @param indexer the tab indexer. May be <code>null</code>
     */
    public ButtonRow(TabIndexer indexer) {
        setStyleName(STYLE);

        _indexer = indexer;
        _listener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                doAction(event);
            }
        };
    }

    /**
     * Add a button. The identifier is used to get localised text for the
     * button, and is returned by {@link ActionEvent#getActionCommand} when
     * triggered.
     *
     * @param id the button identifier
     */
    public void addButton(String id) {
        add(id);
    }

    /**
     * Add a button, and register an event listener.
     *
     * @param id       the button identifier
     * @param listener the listener to add
     * @return the button
     */
    public Button addButton(String id, ActionListener listener) {
        Button button = add(id);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Adds a listener to receive notification when the user presses a button.
     * The listener receives events from all buttons.
     *
     * @param listener the listener to add
     */
    public void addActionListener(ActionListener listener) {
        getEventListenerList().addListener(ActionListener.class, listener);
    }

    /**
     * Adds a listener to receive notification when the user presses a specific
     * button.
     *
     * @param id       the button identifier
     * @param listener the listener to add
     */
    public void addActionListener(String id, ActionListener listener) {
        Button button = (Button) getComponent(id);
        button.addActionListener(listener);
    }

    /**
     * Removes an <code>ActionListener</code> from receiving notification when
     * the user presses a button.
     *
     * @param listener the listener to remove
     */
    public void removeActionListener(ActionListener listener) {
        getEventListenerList().removeListener(ActionListener.class, listener);
    }

    /**
     * Removes an <code>ActionListener</code> from receiving notification when
     * the user presses a specific button.
     *
     * @param id       the button identifier
     * @param listener the listener to remove
     */
    public void removeActionListener(String id, ActionListener listener) {
        Button button = (Button) getComponent(id);
        button.removeActionListener(listener);
    }

    /**
     * Invoked when a button is pressed. Forwards the event to any registered
     * listener.
     *
     * @param event the button event
     */
    protected void doAction(ActionEvent event) {
        EventListenerList list = getEventListenerList();
        EventListener[] listeners = list.getListeners(ActionListener.class);
        ActionEvent forward = new ActionEvent(this, event.getActionCommand());
        for (EventListener listener : listeners) {
            ((ActionListener) listener).actionPerformed(forward);
        }
    }

    /**
     * Add a button.
     *
     * @param id the button identifier
     * @return the button.
     */
    protected Button add(String id) {
        Button button = ButtonFactory.create(id, BUTTON_STYLE, _listener);
        button.setId(id);
        button.setActionCommand(id);
        if (_indexer != null) {
            _indexer.setTabIndex(button);
        }
        add(button);
        return button;
    }

}
