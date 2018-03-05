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
// Object SMSTextArea

/**
 * Object/namespace for Text Component support.
 * This object/namespace should not be used externally.
 */
SMSTextArea = Core.extend(EchoTextComponent, {
    GSM_TO_UNICODE: [
        [0x00, 0x0040], // COMMERCIAL AT
        [0x00, 0x0000], // NULL
        [0x01, 0x00A3], // POUND SIGN
        [0x02, 0x0024], // DOLLAR SIGN
        [0x03, 0x00A5], // YEN SIGN
        [0x04, 0x00E8], // LATIN SMALL LETTER E WITH GRAVE
        [0x05, 0x00E9], // LATIN SMALL LETTER E WITH ACUTE
        [0x06, 0x00F9], // LATIN SMALL LETTER U WITH GRAVE
        [0x07, 0x00EC], // LATIN SMALL LETTER I WITH GRAVE
        [0x08, 0x00F2], // LATIN SMALL LETTER O WITH GRAVE
        [0x09, 0x00E7], // LATIN SMALL LETTER C WITH CEDILLA
        [0x09, 0x00C7], // LATIN CAPITAL LETTER C WITH CEDILLA
        [0x0A, 0x000A], // LINE FEED
        [0x0B, 0x00D8], // LATIN CAPITAL LETTER O WITH STROKE
        [0x0C, 0x00F8], // LATIN SMALL LETTER O WITH STROKE
        [0x0D, 0x000D], // CARRIAGE RETURN
        [0x0E, 0x00C5], // LATIN CAPITAL LETTER A WITH RING ABOVE
        [0x0F, 0x00E5], // LATIN SMALL LETTER A WITH RING ABOVE
        [0x10, 0x0394], // GREEK CAPITAL LETTER DELTA
        [0x11, 0x005F], // LOW LINE
        [0x12, 0x03A6], // GREEK CAPITAL LETTER PHI
        [0x13, 0x0393], // GREEK CAPITAL LETTER GAMMA
        [0x14, 0x039B], // GREEK CAPITAL LETTER LAMDA
        [0x15, 0x03A9], // GREEK CAPITAL LETTER OMEGA
        [0x16, 0x03A0], // GREEK CAPITAL LETTER PI
        [0x17, 0x03A8], // GREEK CAPITAL LETTER PSI
        [0x18, 0x03A3], // GREEK CAPITAL LETTER SIGMA
        [0x19, 0x0398], // GREEK CAPITAL LETTER THETA
        [0x1A, 0x039E], // GREEK CAPITAL LETTER XI
        [0x1B, 0x00A0], // ESCAPE TO EXTENSION TABLE
        [0x1B0A, 0x000C], // FORM FEED
        [0x1B14, 0x005E], // CIRCUMFLEX ACCENT
        [0x1B28, 0x007B], // LEFT CURLY BRACKET
        [0x1B29, 0x007D], // RIGHT CURLY BRACKET
        [0x1B2F, 0x005C], // REVERSE SOLIDUS
        [0x1B3C, 0x005B], // LEFT SQUARE BRACKET
        [0x1B3D, 0x007E], // TILDE
        [0x1B3E, 0x005D], // RIGHT SQUARE BRACKET
        [0x1B40, 0x007C], // VERTICAL LINE
        [0x1B65, 0x20AC], // EURO SIGN
        [0x1C, 0x00C6], // LATIN CAPITAL LETTER AE
        [0x1D, 0x00E6], // LATIN SMALL LETTER AE
        [0x1E, 0x00DF], // LATIN SMALL LETTER SHARP S (German)
        [0x1F, 0x00C9], // LATIN CAPITAL LETTER E WITH ACUTE
        [0x20, 0x0020], // SPACE
        [0x21, 0x0021], // EXCLAMATION MARK
        [0x22, 0x0022], // QUOTATION MARK
        [0x23, 0x0023], // NUMBER SIGN
        [0x24, 0x00A4], // CURRENCY SIGN
        [0x25, 0x0025], // PERCENT SIGN
        [0x26, 0x0026], // AMPERSAND
        [0x27, 0x0027], // APOSTROPHE
        [0x28, 0x0028], // LEFT PARENTHESIS
        [0x29, 0x0029], // RIGHT PARENTHESIS
        [0x2A, 0x002A], // ASTERISK
        [0x2B, 0x002B], // PLUS SIGN
        [0x2C, 0x002C], // COMMA
        [0x2D, 0x002D], // HYPHEN-MINUS
        [0x2E, 0x002E], // FULL STOP
        [0x2F, 0x002F], // SOLIDUS
        [0x30, 0x0030], // DIGIT ZERO
        [0x31, 0x0031], // DIGIT ONE
        [0x32, 0x0032], // DIGIT TWO
        [0x33, 0x0033], // DIGIT THREE
        [0x34, 0x0034], // DIGIT FOUR
        [0x35, 0x0035], // DIGIT FIVE
        [0x36, 0x0036], // DIGIT SIX
        [0x37, 0x0037], // DIGIT SEVEN
        [0x38, 0x0038], // DIGIT EIGHT
        [0x39, 0x0039], // DIGIT NINE
        [0x3A, 0x003A], // COLON
        [0x3B, 0x003B], // SEMICOLON
        [0x3C, 0x003C], // LESS-THAN SIGN
        [0x3D, 0x003D], // EQUALS SIGN
        [0x3E, 0x003E], // GREATER-THAN SIGN
        [0x3F, 0x003F], // QUESTION MARK
        [0x40, 0x00A1], // INVERTED EXCLAMATION MARK
        [0x41, 0x0041], // LATIN CAPITAL LETTER A
        [0x41, 0x0391], // GREEK CAPITAL LETTER ALPHA
        [0x42, 0x0042], // LATIN CAPITAL LETTER B
        [0x42, 0x0392], // GREEK CAPITAL LETTER BETA
        [0x43, 0x0043], // LATIN CAPITAL LETTER C
        [0x44, 0x0044], // LATIN CAPITAL LETTER D
        [0x45, 0x0045], // LATIN CAPITAL LETTER E
        [0x45, 0x0395], // GREEK CAPITAL LETTER EPSILON
        [0x46, 0x0046], // LATIN CAPITAL LETTER F
        [0x47, 0x0047], // LATIN CAPITAL LETTER G
        [0x48, 0x0048], // LATIN CAPITAL LETTER H
        [0x48, 0x0397], // GREEK CAPITAL LETTER ETA
        [0x49, 0x0049], // LATIN CAPITAL LETTER I
        [0x49, 0x0399], // GREEK CAPITAL LETTER IOTA
        [0x4A, 0x004A], // LATIN CAPITAL LETTER J
        [0x4B, 0x004B], // LATIN CAPITAL LETTER K
        [0x4B, 0x039A], // GREEK CAPITAL LETTER KAPPA
        [0x4C, 0x004C], // LATIN CAPITAL LETTER L
        [0x4D, 0x004D], // LATIN CAPITAL LETTER M
        [0x4D, 0x039C], // GREEK CAPITAL LETTER MU
        [0x4E, 0x004E], // LATIN CAPITAL LETTER N
        [0x4E, 0x039D], // GREEK CAPITAL LETTER NU
        [0x4F, 0x004F], // LATIN CAPITAL LETTER O
        [0x4F, 0x039F], // GREEK CAPITAL LETTER OMICRON
        [0x50, 0x0050], // LATIN CAPITAL LETTER P
        [0x50, 0x03A1], // GREEK CAPITAL LETTER RHO
        [0x51, 0x0051], // LATIN CAPITAL LETTER Q
        [0x52, 0x0052], // LATIN CAPITAL LETTER R
        [0x53, 0x0053], // LATIN CAPITAL LETTER S
        [0x54, 0x0054], // LATIN CAPITAL LETTER T
        [0x54, 0x03A4], // GREEK CAPITAL LETTER TAU
        [0x55, 0x0055], // LATIN CAPITAL LETTER U
        [0x56, 0x0056], // LATIN CAPITAL LETTER V
        [0x57, 0x0057], // LATIN CAPITAL LETTER W
        [0x58, 0x0058], // LATIN CAPITAL LETTER X
        [0x58, 0x03A7], // GREEK CAPITAL LETTER CHI
        [0x59, 0x0059], // LATIN CAPITAL LETTER Y
        [0x59, 0x03A5], // GREEK CAPITAL LETTER UPSILON
        [0x5A, 0x005A], // LATIN CAPITAL LETTER Z
        [0x5A, 0x0396], // GREEK CAPITAL LETTER ZETA
        [0x5B, 0x00C4], // LATIN CAPITAL LETTER A WITH DIAERESIS
        [0x5C, 0x00D6], // LATIN CAPITAL LETTER O WITH DIAERESIS
        [0x5D, 0x00D1], // LATIN CAPITAL LETTER N WITH TILDE
        [0x5E, 0x00DC], // LATIN CAPITAL LETTER U WITH DIAERESIS
        [0x5F, 0x00A7], // SECTION SIGN
        [0x60, 0x00BF], // INVERTED QUESTION MARK
        [0x61, 0x0061], // LATIN SMALL LETTER A
        [0x62, 0x0062], // LATIN SMALL LETTER B
        [0x63, 0x0063], // LATIN SMALL LETTER C
        [0x64, 0x0064], // LATIN SMALL LETTER D
        [0x65, 0x0065], // LATIN SMALL LETTER E
        [0x66, 0x0066], // LATIN SMALL LETTER F
        [0x67, 0x0067], // LATIN SMALL LETTER G
        [0x68, 0x0068], // LATIN SMALL LETTER H
        [0x69, 0x0069], // LATIN SMALL LETTER I
        [0x6A, 0x006A], // LATIN SMALL LETTER J
        [0x6B, 0x006B], // LATIN SMALL LETTER K
        [0x6C, 0x006C], // LATIN SMALL LETTER L
        [0x6D, 0x006D], // LATIN SMALL LETTER M
        [0x6E, 0x006E], // LATIN SMALL LETTER N
        [0x6F, 0x006F], // LATIN SMALL LETTER O
        [0x70, 0x0070], // LATIN SMALL LETTER P
        [0x71, 0x0071], // LATIN SMALL LETTER Q
        [0x72, 0x0072], // LATIN SMALL LETTER R
        [0x73, 0x0073], // LATIN SMALL LETTER S
        [0x74, 0x0074], // LATIN SMALL LETTER T
        [0x75, 0x0075], // LATIN SMALL LETTER U
        [0x76, 0x0076], // LATIN SMALL LETTER V
        [0x77, 0x0077], // LATIN SMALL LETTER W
        [0x78, 0x0078], // LATIN SMALL LETTER X
        [0x79, 0x0079], // LATIN SMALL LETTER Y
        [0x7A, 0x007A], // LATIN SMALL LETTER Z
        [0x7B, 0x00E4], // LATIN SMALL LETTER A WITH DIAERESIS
        [0x7C, 0x00F6], // LATIN SMALL LETTER O WITH DIAERESIS
        [0x7D, 0x00F1], // LATIN SMALL LETTER N WITH TILDE
        [0x7E, 0x00FC], // LATIN SMALL LETTER U WITH DIAERESIS
        [0x7F, 0x00E0]  // LATIN SMALL LETTER A WITH GRAVE
    ],

    SINGLE_PART_GSM_LENGTH: 160,
    MULTI_PART_GSM_LENGTH: 153,
    SINGLE_PART_UNICODE_LENGTH: 70,
    MULTI_PART_UNICODE_LENGTH: 67,

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
        // set an upper maximum length based on the GSM length. This will be incorrect if a unicode message is
        // entered, but also means the message won't truncate unexpectedly if a unicode character is entered
        // (e.g. if the 160th char is chinese, and one part is allowed the message won't truncate to 70 characters)
        this.maximumLength = (this.maxParts == 1) ? this.SINGLE_PART_GSM_LENGTH
                : this.maxParts * this.MULTI_PART_GSM_LENGTH;

        this.updateCount();
    },

    updateClientMessage: function () {
        EchoTextComponent.prototype.updateClientMessage.call(this);
        this.updateCount();
    },

    updateCount: function () {
        var label = document.getElementById(this.elementId + "_label");
        EP.DOM.removeChildren(label);
        var value = this.getElement().value;
        var length = this.getGSMLength(value);
        var parts;
        var singlePartLength;
        var multiPartLength;
        var remaining;
        if (length == -1) {
            // unicode message
            length = value.length;
            singlePartLength = this.SINGLE_PART_UNICODE_LENGTH;
            multiPartLength = this.MULTI_PART_UNICODE_LENGTH;
        } else {
            singlePartLength = this.SINGLE_PART_GSM_LENGTH;
            multiPartLength = this.MULTI_PART_GSM_LENGTH;
        }
        if (length > singlePartLength) {
            parts = Math.floor(length / multiPartLength);
            if (length % multiPartLength > 0) {
                parts++;
            }
            remaining = (parts * multiPartLength) - length;
        } else {
            parts = 1;
            remaining = singlePartLength - length;
        }
        label.appendChild(document.createTextNode(remaining + "/" + parts));
    },

    /**
     * Returns the number of characters a message uses in a GSM SMS.
     *
     * @param message the message text
     * @return the number of characters, or {@code -1} if the message contains unsupported characters
     */
    getGSMLength: function (message) {
        var length = 0;
        for (var i = 0, len = message.length; i < len; i++) {
            var gsm = this.getGSM(message.charCodeAt([i]));
            if (gsm >= 0) {
                length++;
                if (gsm > 0x7F) {
                    // escaped character
                    length++;
                }
            } else {
                // character not supported
                length = -1;
                break;
            }
        }
        return length;
    },

    /**
     * Returns the GSM character(s) for a given unicode character.
     *
     * @param ch the unicode character
     * @return the GSM character(s), or {@code -1} if the character is not supported. Any value &gt; 0x7F is a
     * multi-byte character
     */
    getGSM: function (ch) {
        for (i = 0; i < this.GSM_TO_UNICODE.length; ++i) {
            if (this.GSM_TO_UNICODE[i][1] == ch) {
                return this.GSM_TO_UNICODE[i][0];
            }
        }
        return -1;
    },

});

/**
 * Static object/namespace for SMSTextArea Component MessageProcessor
 * implementation.
 */
SMSTextArea.MessageProcessor = {

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
                        SMSTextArea.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                        break;
                    case "dispose":
                        SMSTextArea.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                        break;
                    case "set-text":
                        SMSTextArea.MessageProcessor.processSetText(messagePartElement.childNodes[i]);
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

            var textComponent = new SMSTextArea(elementId);
            textComponent.enabled = item.getAttribute("enabled") != "false";
            textComponent.text = item.getAttribute("text") ? item.getAttribute("text") : null;
            textComponent.serverNotify = item.getAttribute("server-notify") == "true";
            var maxParts = item.getAttribute("max-parts") ? parseInt(item.getAttribute("max-parts")) : 1;
            if (maxParts <= 0) {
                maxParts = 1;
            }
            textComponent.maxParts = maxParts;
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
