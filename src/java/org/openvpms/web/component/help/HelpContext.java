/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.help;

import echopointng.KeyStrokeListener;


/**
 * Help context.
 *
 * @author Tim Anderson
 */
public class HelpContext {

    /**
     * The parent help context. May be {@code null}
     */
    private final HelpContext parent;

    /**
     * The help topic
     */
    private final String topic;

    /**
     * The help shortcut key code.
     */
    private final int keyCode;

    /**
     * The listener to launch help for the current topic.
     */
    private final HelpListener listener;


    /**
     * Constructs a {@code HelpContext}.
     *
     * @param topic    the help topic
     * @param listener the listener to launch help for the current topic
     */
    public HelpContext(String topic, HelpListener listener) {
        this(null, topic, listener);
    }

    /**
     * Constructs a {@code HelpContext}.
     * <p/>
     * This inherits the listener from the parent
     *
     * @param parent the parent topic
     * @param topic  the help topic
     */
    public HelpContext(HelpContext parent, String topic) {
        this(parent, topic, parent.listener);
    }

    /**
     * Constructs a {@code HelpContext}.
     *
     * @param parent   the parent topic. May be {@code null}
     * @param topic    the help topic
     * @param listener the listener to launch help for the current topic
     */
    public HelpContext(HelpContext parent, String topic, HelpListener listener) {
        this.parent = parent;
        this.topic = topic;
        this.listener = listener;
        this.keyCode = KeyStrokeListener.VK_F1;
    }

    /**
     * Returns the help topic.
     *
     * @return the help topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Returns the parent context.
     *
     * @return the parent context. May be {@code null}
     */
    public HelpContext getParent() {
        return parent;
    }

    /**
     * Creates a new topic.
     *
     * @param topic the topic name
     */
    public HelpContext createTopic(String topic) {
        return new HelpContext(this, topic, listener);
    }

    /**
     * Creates a sub-topic.
     * <p/>
     * This topic is appended to the parent topic name.
     *
     * @param topic the topic name
     * @return a new sub-topic context
     */
    public HelpContext createSubtopic(String topic) {
        return new HelpContext(this, this.topic + "/" + topic, listener);
    }

    /**
     * Displays help for the current context.
     */
    public void show() {
        listener.show(this);
    }

    /**
     * Returns the help shortcut key code.
     *
     * @return the help shortcut key code
     */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * Returns a string representation of this.
     *
     * @return a string representation of this
     */
    public String toString() {
        String result = topic;
        if (parent != null) {
            result += "\n" + parent.toString();
        }
        return result;
    }

}
