/*
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2005 NextApp, Inc.
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */

//_________________
// Object KeyTable


var globalActiveKeyTable = null;

/**
 * Static object/namespace for Table support.
 * This object/namespace should not be used externally.
 * <p>
 * Constructor to create new <code>KeyTable</code> instance.
 *
 * @param element the supported <code>TABLE</code> DOM element
 */
KeyTable = function(elementId) {
    this.elementId = elementId;
    this.table = document.getElementById(elementId + "_table");
    this.tableFocus = document.getElementById(elementId + "_focus");

    this.enabled = true;
    this.multipleSelect = false;
    this.rolloverEnabled = false;
    this.rolloverStyle = null;
    this.selectionEnabled = false;
    this.selectionFocusStyle = null;
    this.selectionBlurStyle = null;
    this.selectionStyle = null;
    this.rowCount = 0;
    this.selectionState = null;
    this.headerVisible = false;
    this.lastSelectedIndex = -1;
    this.lastRolloverIndex = -1;
};

/**
 * Deselects all selected rows in a Table.
 */
KeyTable.prototype.clearSelected = function() {
    for (var i = 0; i < this.rowCount; ++i) {
        if (this.isSelected(i)) {
            this.setSelected(i, false);
        }
    }
};

/**
 * Disposes of an <code>KeyTable</code> instance, de-registering
 * listeners and cleaning up resources.
 */
KeyTable.prototype.dispose = function() {
    if (this.rolloverEnabled || this.selectionEnabled) {
        var mouseEnterLeaveSupport = EchoClientProperties.get("proprietaryEventMouseEnterLeaveSupported");
        for (var rowIndex = 0; rowIndex < this.rowCount; ++rowIndex) {
            var trElement = this.table.rows[rowIndex + (this.headerVisible ? 1 : 0)];
            if (this.rolloverEnabled) {
                if (mouseEnterLeaveSupport) {
                    EchoEventProcessor.removeHandler(trElement, "mouseenter");
                    EchoEventProcessor.removeHandler(trElement, "mouseleave");
                } else {
                    EchoEventProcessor.removeHandler(trElement, "mouseout");
                    EchoEventProcessor.removeHandler(trElement, "mouseover");
                }
            }
            if (this.selectionEnabled) {
                EchoEventProcessor.removeHandler(trElement, "click");
                EchoEventProcessor.removeHandler(trElement, "mousedown");
            }
        }
        if (this.selectionEnabled) {
            EchoEventProcessor.removeHandler(this.tableFocus, "focus");
            EchoEventProcessor.removeHandler(this.tableFocus, "blur");

            document.body.KeyTable_count--;
            if (document.body.KeyTable_count == 0) {
                EchoDomUtil.removeEventListener(document, "keydown", KeyTable.processKeyDown, false);
            }
        }
    }

    if (this == globalActiveKeyTable) {
        globalActiveKeyTable = null;
    }

    EchoDomPropertyStore.dispose(this.getElement());
};

/**
 * Redraws a row in the appropriate style (i.e., selected or deselected).
 *
 * @param rowIndex the index of the row to redraw
 */
KeyTable.prototype.drawRowStyle = function(rowIndex) {
    var selected = this.isSelected(rowIndex);
    var trElement = this.getRowElement(rowIndex);

    for (var i = 0; i < trElement.cells.length; ++i) {
        if (selected) {
            EchoCssUtil.restoreOriginalStyle(trElement.cells[i]);
            EchoCssUtil.applyTemporaryStyle(trElement.cells[i], this.selectionStyle);
        } else {
            EchoCssUtil.restoreOriginalStyle(trElement.cells[i]);
        }
    }
};

KeyTable.prototype.getElement = function() {
    return document.getElementById(this.elementId);
};

/**
 * Returns the <code>TR</code> element associated with a specific
 * row index.
 *
 * @param rowIndex the row index
 * @return the relevant <code>TR</code> element
 */
KeyTable.prototype.getRowElement = function(rowIndex) {
    if (this.headerVisible) {
        if (rowIndex == -1) {
            return this.table.rows[0];
        } else if (rowIndex >= 0 && rowIndex < this.rowCount) {
            return this.table.rows[rowIndex + 1];
        }
    } else {
        if (rowIndex >= 0 && rowIndex < this.rowCount) {
            return this.table.rows[rowIndex];
        }
    }
    return null;
};

/**
 * Determines the index of a table row based on a
 * <code>TR</code> element.  This method is used for
 * processing events.
 *
 * @param trElement the <code>TR</code> element to evaluate
 * @return the row index
 */
KeyTable.prototype.getRowIndex = function(trElement) {
    var stringIndex = trElement.id.lastIndexOf("_tr_") + 4;
    var rowIndex = trElement.id.substring(stringIndex);
    if (rowIndex == "header") {
        return -1;
    } else {
        return parseInt(rowIndex);
    }
};

/**
 * Initializes the state of an <code>KeyTable</code> instance,
 * registering event handlers and binding it to it target
 * <code>TABLE</code> DOM element.
 */
KeyTable.prototype.init = function() {
    this.selectionState = new Array();
    this.rowCount = this.table.rows.length - (this.headerVisible ? 1 : 0);

    if (this.rolloverEnabled || this.selectionEnabled) {
        if (this.selectionEnabled) {
            this.selectionFocusStyle = this.selectionStyle;
            if (!this.selectionBlurStyle) {
                this.selectionBlurStyle = this.selectionStyle;
            } else {
                // default selection style is the blur style, until the
                // table receives focus
                this.selectionStyle = this.selectionBlurStyle;
            }

            var element = this.getElement();
            EchoEventProcessor.addHandler(this.tableFocus, "focus", "KeyTable.processFocus");
            EchoEventProcessor.addHandler(this.tableFocus, "blur", "KeyTable.processBlur");

            // register a key handler for this document if none is already registered
            if (!document.body.KeyTable_count || document.body.KeyTable_count <= 0) {
                EchoDomUtil.addEventListener(document, "keydown", KeyTable.processKeyDown, false);
                document.body.KeyTable_count = 1;
            } else {
                document.body.KeyTable_count++;
            }
        }

        var mouseEnterLeaveSupport = EchoClientProperties.get("proprietaryEventMouseEnterLeaveSupported");
        for (var rowIndex = 0; rowIndex < this.rowCount; ++rowIndex) {
            var trElement = this.table.rows[rowIndex + (this.headerVisible ? 1 : 0)];
            if (this.rolloverEnabled) {
                if (mouseEnterLeaveSupport) {
                    EchoEventProcessor.addHandler(trElement, "mouseenter", "KeyTable.processRolloverEnter");
                    EchoEventProcessor.addHandler(trElement, "mouseleave", "KeyTable.processRolloverExit");
                } else {
                    EchoEventProcessor.addHandler(trElement, "mouseout", "KeyTable.processRolloverExit");
                    EchoEventProcessor.addHandler(trElement, "mouseover", "KeyTable.processRolloverEnter");
                }
            }
            if (this.selectionEnabled) {
                EchoEventProcessor.addHandler(trElement, "click", "KeyTable.processClick");
                EchoEventProcessor.addHandler(trElement, "mousedown", "KeyTable.processMouseDown");
            }
        }
    }

    EchoDomPropertyStore.setPropertyValue(this.getElement(), "component", this);
};

/**
 * Determines if a row is selected.
 *
 * @param index the index of the row to evaluate
 * @return true if the row is selected
 */
KeyTable.prototype.isSelected = function(index) {
    if (this.selectionState.length <= index) {
        return false;
    } else {
        return this.selectionState[index];
    }
};

KeyTable.prototype.setActive = function(active) {
    if (active) {
        if (globalActiveKeyTable) {
            globalActiveKeyTable.setActive(false);
        }
        globalActiveKeyTable = this;
        this.active = true;
    } else if (this.active) {
        globalActiveKeyTable = null;
        this.active = false;
    }
}

/**
 * Processes a row selection (click) event.
 *
 * @param echoEvent the event, preprocessed by the
 *        <code>EchoEventProcessor</code>
 */
KeyTable.prototype.processClick = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement())) {
        return;
    }

    if (!this.selectionEnabled) {
        return;
    }

    try {
        // need to move the focus off the current focused field.
        EchoEventProcessor.removeHandler(this.tableFocus, "focus");
        this.tableFocus.focus();
        EchoEventProcessor.addHandler(this.tableFocus, "focus", "KeyTable.processFocus");
    } catch (ex) {
    }

    this.setActive(true);
    this.selectionStyle = this.selectionFocusStyle;

    var trElement = echoEvent.registeredTarget;
    var rowIndex = this.getRowIndex(trElement);
    if (rowIndex == -1) {
        return;
    }

    EchoDomUtil.preventEventDefault(echoEvent);

    if (!this.multipleSelect || !(echoEvent.shiftKey || echoEvent.ctrlKey || echoEvent.metaKey || echoEvent.altKey)) {
        this.clearSelected();
    }

    if (echoEvent.shiftKey && this.lastSelectedIndex != -1) {
        if (this.lastSelectedIndex < rowIndex) {
            startIndex = this.lastSelectedIndex;
            endIndex = rowIndex;
        } else {
            startIndex = rowIndex;
            endIndex = this.lastSelectedIndex;
        }
        for (var i = startIndex; i <= endIndex; ++i) {
            this.setSelected(i, true);
        }
    } else {
        this.lastSelectedIndex = rowIndex;
        this.setSelected(rowIndex, !this.isSelected(rowIndex));
    }

    // Update ClientMessage.
    this.updateClientMessage();

    // Notify server if required.
    if (this.serverNotify) {
        EchoClientMessage.setActionValue(this.elementId, "action");
        EchoServerTransaction.connect();
    }
};

KeyTable.prototype.processEnter = function(echoEvent) {
    if (this.lastSelectedIndex != -1) {
        EchoDomUtil.preventEventDefault(echoEvent);

        // Notify server if required.
        if (this.serverNotify) {
            EchoClientMessage.setActionValue(this.elementId, "action");
            EchoServerTransaction.connect();
        }
    }
}

KeyTable.prototype.processTab = function(echoEvent) {
    if (this.lastSelectedIndex == -1 && this.rowCount != 0) {
        this.lastSelectedIndex = 0;
        this.setSelected(this.lastSelectedIndex, true)
        // Update ClientMessage.
        this.updateClientMessage();
    }
}

KeyTable.prototype.processSelection = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement())) {
        return;
    }

    if (!this.selectionEnabled) {
        return;
    }

    var index = this.lastSelectedIndex;
    var changePage;

    if (index == -1) {
        if (this.rowCount > 0) {
            index = 0;
        }
    } else if (echoEvent.keyCode == 38) {
        if (index > 0) {
            index--;
        } else {
            changePage = "previous-bottom";
        }
    } else if (echoEvent.keyCode == 40) {
        if (index + 1 < this.rowCount) {
            index++;
        } else  {
            changePage = "next-top";
        }
    }
    if (index != this.lastSelectedIndex) {
        this.clearSelected();
        this.setSelected(index, true);
        this.lastSelectedIndex = index;
    }
    if (this.lastRolloverIndex != -1) {
        this.drawRowStyle(this.lastRolloverIndex);
        this.lastRolloverIndex = -1;
    }

    if (index != -1) {
        var trElement = this.getRowElement(index);

        var distance = trElement.offsetTop + trElement.offsetHeight + 20;
        if (distance > (this.table.offsetHeight + this.table.scrollTop)) {
            var scrollTop = distance - this.table.offsetHeight;
        } else if (trElement.offsetTop < this.table.scrollTop) {
            var scrollTop = trElement.offsetTop - 5;
        }
        if (scrollTop) {
            this.table.scrollTop = scrollTop;
        }
    }

    EchoDomUtil.preventEventDefault(echoEvent);

    if (changePage && this.serverPageNotify) {
        // notify server to move to the next/previous page
        EchoClientMessage.setActionValue(this.elementId, "page", changePage);
        EchoServerTransaction.connect();
    } else {
        // Update ClientMessage.
        this.updateClientMessage();
    }
};

KeyTable.prototype.processPage = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement())) {
        return;
    }

    EchoDomUtil.preventEventDefault(echoEvent);

    // Notify server if required.
    if (this.serverPageNotify) {
        if (echoEvent.keyCode == 33) {
            EchoClientMessage.setActionValue(this.elementId, "page", "previous");
        } else if (echoEvent.keyCode == 34) {
            EchoClientMessage.setActionValue(this.elementId, "page", "next");
        } else if (echoEvent.keyCode == 35) {
            EchoClientMessage.setActionValue(this.elementId, "page", "last");
        } else {
            EchoClientMessage.setActionValue(this.elementId, "page", "first");
        }
        EchoServerTransaction.connect();
    }
};


/**
 * Processes a row mouse over event.
 *
 * @param echoEvent the event, preprocessed by the
 *        <code>EchoEventProcessor</code>
 */
KeyTable.prototype.processRolloverEnter = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement())) {
        return;
    }

    var trElement = echoEvent.registeredTarget;
    var rowIndex = this.getRowIndex(trElement);

    if (rowIndex == -1) {
        return;
    }

    if (this.rolloverStyle) {
        for (var i = 0; i < trElement.cells.length; ++i) {
            EchoCssUtil.applyTemporaryStyle(trElement.cells[i], this.rolloverStyle);
        }
    }
    this.lastRolloverIndex = rowIndex;
};

/**
 * Processes a row mouse out event.
 *
 * @param echoEvent the event, preprocessed by the
 *        <code>EchoEventProcessor</code>
 */
KeyTable.prototype.processRolloverExit = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement())) {
        return;
    }

    var trElement = echoEvent.registeredTarget;
    var rowIndex = this.getRowIndex(trElement);

    if (rowIndex == -1) {
        return;
    }

    this.drawRowStyle(rowIndex);
    this.lastRolloverIndex = -1;
};

KeyTable.prototype.processFocus = function(echoEvent) {
    if (!this.enabled || !this.verifyInput()) {
        return;
    }

    this.setActive(true);
    this.selectionStyle = this.selectionFocusStyle;

    if (this.lastSelectedIndex == -1) {
        if (this.rowCount > 0) {
            this.lastSelectedIndex = 0;
            this.setSelected(this.lastSelectedIndex, true)
            // Update ClientMessage.
            this.updateClientMessage();
        }
    } else {
        for (var i = 0; i < this.rowCount; ++i) {
            if (this.isSelected(i)) {
                this.drawRowStyle(i);
            }
        }
    }

    EchoDomUtil.preventEventDefault(echoEvent);
}

//
// Workaround for the EchoClientEngine.verifyInput() method. The
// EchoClientEngine
// implementation returns false if the supplied element isn't a child of the
// current modal element. However this doesn't take into account a change to
// the modal element in the server message.
// This is probably a bug in EchoServerMessage.processPhase2(), which invokes
// EchoServerMessage.processMessageParts() prior to
// EchoServerMessage.processApplicationProperties(). The latter is responsible
// updating the modal element, and should probably occur prior to
// processMessageParts()
//
KeyTable.prototype.verifyInput = function() {
    var element = this.getElement();
    if (!EchoModalManager.isElementInModalContext(element)) {
        var modalElementId = EchoServerMessage.messageDocument.documentElement.getAttribute("modal-id");
        if (modalElementId) {
            var modalElement = document.getElementById(modalElementId);
            if (!EchoDomUtil.isAncestorOf(modalElement, element)) {
                return false;
            }
        } else {
            return false;
        }
    }
    if (EchoDomPropertyStore.getPropertyValue(element, "EchoClientEngine.inputDisabled")) {
        return false;
    }
    return true;
}

KeyTable.prototype.processBlur = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement(), true)) {
        return;
    }

    this.selectionStyle = this.selectionBlurStyle;
    for (var i = 0; i < this.rowCount; ++i) {
        if (this.isSelected(i)) {
            this.drawRowStyle(i);
        }
    }

    EchoDomUtil.preventEventDefault(echoEvent);
}

/**
 * Processes a keydown event.
 *
 * @param echoEvent the event
 */
KeyTable.prototype.processKeyDown = function(echoEvent) {
    if (!this.enabled || !EchoClientEngine.verifyInput(this.getElement())) {
        EchoDomUtil.preventEventDefault(echoEvent);
        return;
    }
    if (echoEvent.keyCode == 13) {  // enter
        this.processEnter(echoEvent);
    } else if (echoEvent.keyCode == 38 || echoEvent.keyCode == 40) { // up/down arrow
        this.processSelection(echoEvent);
    } else if (echoEvent.keyCode >= 33 && echoEvent.keyCode <= 36) { // page up/down, end, home
        this.processPage(echoEvent);
    }
}

/**
 * Sets the selection state of a table row.
 *
 * @param rowIndex the index of the row
 * @param newValue the new selection state (a boolean value)
 */
KeyTable.prototype.setSelected = function(rowIndex, newValue) {
    this.selectionState[rowIndex] = newValue;

    // Redraw.
    this.drawRowStyle(rowIndex);
};

/**
 * Updates the selection state in the outgoing <code>ClientMessage</code>.
 * If any server-side <code>ActionListener</code>s are registered, an action
 * will be set in the ClientMessage and a client-server connection initiated.
 */
KeyTable.prototype.updateClientMessage = function() {
    var propertyElement = EchoClientMessage.createPropertyElement(this.elementId, "selection");

    // remove previous values
    while (propertyElement.hasChildNodes()) {
        propertyElement.removeChild(propertyElement.firstChild);
    }

    for (var i = 0; i < this.rowCount; ++i) {
        if (this.isSelected(i)) {
            var rowElement = EchoClientMessage.messageDocument.createElement("row");
            rowElement.setAttribute("index", i);
            propertyElement.appendChild(rowElement);
        }
    }

    EchoDebugManager.updateClientMessage();
};

/**
 * Returns the Table data object instance based on the root element
 * of the Table.
 *
 * @param element the root element or element id of the Table
 * @return the relevant Tableinstance
 */
KeyTable.getComponent = function(element) {
    return EchoDomPropertyStore.getPropertyValue(element, "component");
};

/**
 * Static object/namespace for Table MessageProcessor
 * implementation.
 */
KeyTable.MessageProcessor = function() {
};

/**
 * MessageProcessor process() implementation
 * (invoked by ServerMessage processor).
 *
 * @param messagePartElement the <code>message-part</code> element to process.
 */
KeyTable.MessageProcessor.process = function(messagePartElement) {
    for (var i = 0; i < messagePartElement.childNodes.length; ++i) {
        if (messagePartElement.childNodes[i].nodeType == 1) {
            switch (messagePartElement.childNodes[i].tagName) {
                case "init":
                    KeyTable.MessageProcessor.processInit(messagePartElement.childNodes[i]);
                    break;
                case "dispose":
                    KeyTable.MessageProcessor.processDispose(messagePartElement.childNodes[i]);
                    break;
            }
        }
    }
};

/**
 * Processes a <code>dispose</code> message to finalize the state of a
 * Table component that is being removed.
 *
 * @param disposeMessageElement the <code>dispose</code> element to process
 */
KeyTable.MessageProcessor.processDispose = function(disposeMessageElement) {
    for (var item = disposeMessageElement.firstChild; item; item = item.nextSibling) {
        var tableElementId = item.getAttribute("eid");
        var table = KeyTable.getComponent(tableElementId);
        if (table) {
            table.dispose();
        }
    }
};

/**
 * Processes an <code>init</code> message to initialize the state of a
 * Table component that is being added.
 *
 * @param initMessageElement the <code>init</code> element to process
 */
KeyTable.MessageProcessor.processInit = function(initMessageElement) {
    var rolloverStyle = initMessageElement.getAttribute("rollover-style");
    var selectionStyle = initMessageElement.getAttribute("selection-style");
    var selectionBlurStyle = initMessageElement.getAttribute("selection-blur-style");

    for (var item = initMessageElement.firstChild; item; item = item.nextSibling) {
        var containerId = item.getAttribute("eid");

        var table = new KeyTable(containerId);
        table.enabled = item.getAttribute("enabled") != "false";
        table.headerVisible = item.getAttribute("header-visible") == "true";
        table.rolloverEnabled = item.getAttribute("rollover-enabled") == "true";
        if (table.rolloverEnabled) {
            table.rolloverStyle = rolloverStyle;
        }
        table.selectionEnabled = item.getAttribute("selection-enabled") == "true";
        if (table.selectionEnabled) {
            table.selectionStyle = selectionStyle;
            table.selectionBlurStyle = selectionBlurStyle;
            table.multipleSelect = item.getAttribute("selection-mode") == "multiple";
            table.serverNotify = item.getAttribute("server-notify") == "true";
        }
        table.serverPageNotify = item.getAttribute("server-page-notify") == "true";

        table.init();

        var rowElements = item.getElementsByTagName("row");
        for (var rowIndex = 0; rowIndex < rowElements.length; ++rowIndex) {
            var tableRowIndex = parseInt(rowElements[rowIndex].getAttribute("index"));
            table.setSelected(tableRowIndex, true);
            table.lastSelectedIndex = tableRowIndex;
        }
    }
};

/**
 * Processes a row selection (click) event.
 * Finds the appropriate <code>KeyTable</code> instance and
 * delegates processing to it.
 *
 * @param echoEvent the event, preprocessed by the
 *        <code>EchoEventProcessor</code>
 */
KeyTable.processClick = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var table = KeyTable.getComponent(componentId);
    table.processClick(echoEvent);
};

KeyTable.processMouseDown = function(echoEvent) {
    EchoDomUtil.preventEventDefault(echoEvent);
};

KeyTable.processKeyDown = function(event) {
    if (!event.target && event.srcElement) {
        // The Internet Explorer event model stores the target element in the 'srcElement' property of an event.
        // Modify the event such the target is retrievable using the W3C DOM Level 2 specified property 'target'.
        event.target = event.srcElement;
    }
    if (globalActiveKeyTable != null) {
        if (event.target != globalActiveKeyTable.tableFocus
                && event.target.parentNode != document     // ff
                && !EP.isAncestorOf(event.target, globalActiveKeyTable.getElement())) { // IE
            return true;
        }
        globalActiveKeyTable.processKeyDown(event);
    }
}

/**
 * Processes a row mouse over event.
 * Finds the appropriate <code>KeyTable</code> instance and
 * delegates processing to it.
 *
 * @param echoEvent the event, preprocessed by the
 *        <code>EchoEventProcessor</code>
 */
KeyTable.processRolloverEnter = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var table = KeyTable.getComponent(componentId);
    table.processRolloverEnter(echoEvent);
};

/**
 * Processes a row mouse out event.
 * Finds the appropriate <code>KeyTable</code> instance and
 * delegates processing to it.
 *
 * @param echoEvent the event, preprocessed by the
 *        <code>EchoEventProcessor</code>
 */
KeyTable.processRolloverExit = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var table = KeyTable.getComponent(componentId);
    table.processRolloverExit(echoEvent);
};

KeyTable.processFocus = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var table = KeyTable.getComponent(componentId);
    table.processFocus(echoEvent);
};

KeyTable.processBlur = function(echoEvent) {
    var componentId = EchoDomUtil.getComponentId(echoEvent.registeredTarget.id);
    var table = KeyTable.getComponent(componentId);
    table.processBlur(echoEvent);
}

