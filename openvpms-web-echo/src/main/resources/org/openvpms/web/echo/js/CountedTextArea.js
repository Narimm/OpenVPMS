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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

//_________________________
// Object CountedTextArea

/**
 * Object/namespace for Text Component support.
 * This object/namespace should not be used externally.
 */
CountedTextArea = Core.extend(EchoTextComponent, {

    $construct: function (elementId) {
        EchoTextComponent.call(this, elementId);
    },

    init: function () {
        EchoTextComponent.prototype.init.call(this);

        var element = document.getElementById(this.elementId);
        var containerElement = document.getElementById(this.elementId + "_container");
        var labelId = this.elementId + "_label";
        var label = document.getElementById(labelId);
        if (!label) {
            label = document.createElement("div");
            label.id = this.elementId + "_label";
            label.style.textAlign = "right";
            label.style.fontFamily = element.style.fontFamily;
            label.style.fontSize = element.style.fontSize;
            containerElement.insertBefore(label, element);
        }
        this.updateCount();
    },

    updateClientMessage: function () {
        EchoTextComponent.prototype.updateClientMessage.call(this);
        this.updateCount();
    },

    updateCount: function () {
        var label = document.getElementById(this.elementId + "_label");
        EP.DOM.removeChildren(label);
        var length = this.maximumLength - this.getElement().value.length;
        label.appendChild(document.createTextNode(length));
        label.value = length;
    }

});

/**
 * Static object/namespace for CountedTextArea Component MessageProcessor
 * implementation.
 */
CountedTextArea.MessageProcessor = {

    $construct: function (elementId) {
        EchoTextComponent.call(this, elementId);
    },

    /**
     * MessageProcessor process() implementation
     * (invoked by ServerMessage processor).
     *
     * @param messagePartElement the <code>message-part</code> element to process.
     */
    process: function (messagePartElement) {
        for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
            if (messagePartElement.childNodes[i].nodeType == 1) {
                switch (messagePartElement.childNodes[i].tagName) {
                    case "init":
                        CountedTextArea.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                        break;
                    case "dispose":
                        CountedTextArea.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                        break;
                    case "set-text":
                        CountedTextArea.MessageProcessor.processSetText(messagePartElement.childNodes[i]);
                        break;
                }
            }
        }
    },

    /**
     * Processes a <code>dispose</code> message to finalize the state of a
     * Text Component that is being removed.
     *
     * @param disposeMessageElement the <code>dispose</code> element to process
     */
    processDispose: function (disposeMessageElement) {
        for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
            var elementId = item.getAttribute("eid");
            var textComponent = EchoTextComponent.getComponent(elementId);
            if (textComponent) {
                textComponent.dispose();
            }
        }
    },

    /**
     * Processes a <code>set-text</code> message to update the text displayed in a
     * Text Component.
     *
     * @param setTextMessageElement the <code>set-text</code> element to process
     */
    processSetText: function (setTextMessageElement) {
        for (var item = setTextMessageElement.firstChild; item; item = item.nextSibling) {
            var elementId = item.getAttribute("eid");
            var text = item.getAttribute("text");
            var textComponent = EchoTextComponent.getComponent(elementId);
            textComponent.setText(text);

            // Remove any updates to text component that occurred during client/server transaction.
            EchoClientMessage.removePropertyElement(textComponent.id, "text");
        }
    },

    /**
     * Processes an <code>init</code> message to initialize the state of a
     * Text Component that is being added.
     *
     * @param initMessageElement the <code>init</code> element to process
     */
    processInit: function (initMessageElement) {
        for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
            var elementId = item.getAttribute("eid");

            var textComponent = new CountedTextArea(elementId);
            textComponent.enabled = item.getAttribute("enabled") != "false";
            textComponent.text = item.getAttribute("text") ? item.getAttribute("text") : null;
            textComponent.serverNotify = item.getAttribute("server-notify") == "true";
            textComponent.maximumLength = item.getAttribute("maximum-length") ? item.getAttribute("maximum-length") : 255;
            textComponent.horizontalScroll = item.getAttribute("horizontal-scroll") ?
                                             parseInt(item.getAttribute("horizontal-scroll"), 10) : 0;
            textComponent.verticalScroll = item.getAttribute("vertical-scroll") ?
                                           parseInt(item.getAttribute("vertical-scroll"), 10) : 0;
            textComponent.cursorPosition = item.getAttribute("cursor-position") ?
                                           parseInt(item.getAttribute("cursor-position"), 10) : 0;

            textComponent.init();
        }
    }
};
