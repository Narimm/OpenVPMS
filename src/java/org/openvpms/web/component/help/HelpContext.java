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
 * Enter description.
 *
 * @author Tim Anderson
 */
public class HelpContext {

    private final HelpContext parent;

    private final String topic;

    private final int keyCode;

    private final HelpListener listener;

    public HelpContext(String topic, HelpListener listener) {
        this(null, topic, listener);
    }

    public HelpContext(HelpContext parent, String topic) {
        this(parent, topic, parent.listener);
    }

    public HelpContext(HelpContext parent, String topic, HelpListener listener) {
        this.parent = parent;
        this.topic = topic;
        this.listener = listener;
        this.keyCode = KeyStrokeListener.VK_F1;
    }

    public String getTopic() {
        return topic;
    }

    public HelpContext getParent() {
        return parent;
    }

    public HelpContext createSubtopic(String topic) {
        return new HelpContext(this, this.topic + "/" + topic, listener);
    }

    public void show() {
        listener.show(this);
    }

    public int getKeyCode() {
        return keyCode;
    }

    public String toString() {
        String result = topic;
        if (parent != null) {
            result += "\n" + parent.toString();
        }
        return result;
    }

}
