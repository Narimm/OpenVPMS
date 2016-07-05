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

package org.openvpms.web.echo.text;

import echopointng.ComponentEx;
import echopointng.KeyStrokeListener;
import echopointng.MutableStyleEx;
import echopointng.able.Attributeable;
import echopointng.able.Borderable;
import echopointng.able.Insetable;
import echopointng.able.MouseCursorable;
import echopointng.able.Sizeable;
import echopointng.richtext.RichTextSpellChecker;
import echopointng.util.ColorKit;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Font;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.text.Document;
import nextapp.echo2.app.text.StringDocument;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Port of the EPNG RichTextArea to support macro expansion and keyboard shortcuts.
 */
public class RichTextArea extends TextComponent implements Sizeable, Insetable, MouseCursorable, Borderable, Attributeable {

    public static final Style DEFAULT_STYLE;


    public static final Color DEFAULT_TOOLBAR_BACKGROUND = new Color(0xefefef);

    public static final String PROPERTY_EDITABLE = "editable";

    public static final String PROPERTY_EDITOR_BACKGROUND = "editorBackground";

    public static final String PROPERTY_EDITOR_BORDER = "editorBorder";

    public static final String PROPERTY_EDITOR_FONT = "editorFont";

    public static final String PROPERTY_EDITOR_FOREGROUND = "editorForeground";

    public static final String PROPERTY_RENDERER = "renderer";

    public static final String PROPERTY_SPELL_CHECK_IN_PROGRESS = "spellCheckInProgress";

    public static final String PROPERTY_SPELL_CHECKER = "spellChecker";

    public static final String PROPERTY_TOOLBAR_ALIGNMENT = "toolBarAlignment";

    public static final String PROPERTY_TOOLBAR_BACKGROUND = "toolBarBackground";

    public static final String PROPERTY_MACRO_EXPANSION = "macro";

    static {
        MutableStyleEx style = new MutableStyleEx();

        style.setProperty(Sizeable.PROPERTY_WIDTH, new Extent(620));
        style.setProperty(Sizeable.PROPERTY_HEIGHT, new Extent(200));

        style.setProperty(PROPERTY_BACKGROUND, ColorKit.makeColor(0xEFEFDE));
        style.setProperty(PROPERTY_EDITOR_BACKGROUND, Color.WHITE);
        style.setProperty(PROPERTY_TOOLBAR_BACKGROUND, ColorKit.makeColor(0xEFEFDE));

        DEFAULT_STYLE = style;
    }

    private Map<String, Object> attributeMap;

    /**
     * Expands macros. May be {@code null}.
     */
    private MacroExpander macros;

    /**
     * The listener to handle keyboard shortcuts.
     */
    private KeyStrokeListener listener = new KeyStrokeListener();


    /**
     * Constructs a RichTextArea with the default size.
     */
    public RichTextArea() {
        this(null, null, null);
    }

    /**
     * Constructs a new RichTextArea with the given text and default size.
     *
     * @param document The model for this text field.
     */
    public RichTextArea(Document document) {
        this(document, null, null, null);
    }

    /**
     * Creates a new RichTextArea with the given text and size.
     *
     * @param document The model for this text field.
     * @param text     The initial text in the text field.
     * @param width    The width to display.
     * @param height   The height to display.
     */
    public RichTextArea(Document document, String text, Extent width, Extent height) {
        super(document);
        setFocusTraversalParticipant(true);
        setRenderer(new DefaultRichTextRenderer());
        setDocument(document);
        if (text != null) {
            setText(text);
        }
        setWidth(width);
        setHeight(height);
        setSpellCheckInProgress(false);
        setSupportsCursorPosition(false);
    }

    /**
     * Creates a new RichTextArea of the given size.
     *
     * @param width  The width to display.
     * @param height The height to display.
     */
    public RichTextArea(Extent width, Extent height) {
        this(null, width, height);
    }

    /**
     * Creates a new RichTextArea with the given text.
     *
     * @param text The initial text in the text field.
     */
    public RichTextArea(String text) {
        this(text, null, null);
    }

    /**
     * Creates a new RichTextArea with the given text and size.
     *
     * @param text   The initial text in the text field.
     * @param width  The width to display.
     * @param height The height to display.
     */
    public RichTextArea(String text, Extent width, Extent height) {
        this(new StringDocument(), text, width, height);
    }

    /**
     * @see echopointng.able.Attributeable#getAttribute(java.lang.String)
     */
    public Object getAttribute(String attributeName) {
        if (attributeMap != null) {
            return attributeMap.get(attributeName);
        }
        return null;
    }

    /**
     * @see echopointng.able.Attributeable#getAttributeNames()
     */
    public String[] getAttributeNames() {
        if (attributeMap == null) {
            return new String[0];
        }
        int count = 0;
        String[] attributeNames = new String[attributeMap.keySet().size()];
        for (Iterator iter = attributeMap.keySet().iterator(); iter.hasNext(); ) {
            attributeNames[count++] = (String) iter.next();
        }
        return attributeNames;
    }

    /**
     * @return the background of the <code>RichTextArea</code> portion
     */
    public Color getEditorBackground() {
        return (Color) getProperty(PROPERTY_EDITOR_BACKGROUND);
    }

    /**
     * @return the border of the <code>RichTextArea</code> portion
     */
    public Border getEditorBorder() {
        return (Border) getProperty(PROPERTY_EDITOR_BORDER);
    }

    /**
     * @return the font of the <code>RichTextArea</code> portion
     */
    public Font getEditorFont() {
        return (Font) getProperty(PROPERTY_EDITOR_FONT);
    }

    /**
     * @return the foreground of the <code>RichTextArea</code> portion
     */
    public Color getEditorForeground() {
        return (Color) getProperty(PROPERTY_EDITOR_FOREGROUND);
    }

    /**
     * @see echopointng.able.MouseCursorable#getMouseCursor()
     */
    public int getMouseCursor() {
        return ComponentEx.getProperty(this, PROPERTY_MOUSE_CURSOR, CURSOR_AUTO);
    }

    /**
     * @see echopointng.able.MouseCursorable#getMouseCursorUri()
     */
    public String getMouseCursorUri() {
        return (String) getProperty(PROPERTY_MOUSE_CURSOR_URI);
    }

    /**
     * @see echopointng.able.Insetable#getOutsets()
     */
    public Insets getOutsets() {
        return (Insets) getProperty(PROPERTY_OUTSETS);
    }

    /**
     * Returns the RichTextRenderer in use
     *
     * @return the RichTextRenderer in use
     */
    public RichTextRenderer getRenderer() {
        return (RichTextRenderer) getProperty(PROPERTY_RENDERER);
    }


    /**
     * @return the spell checker associated with the <code>RichTextArea</code>
     */
    public RichTextSpellChecker getSpellChecker() {
        return (RichTextSpellChecker) getProperty(PROPERTY_SPELL_CHECKER);
    }

    /**
     * Returns the toolbar alignment of the contents of this RichTextArea.
     *
     * @return The toolbar alignment of the contents of this RichTextArea, one
     * of the following values:
     * <ul>
     * <li>Alignment.TOP (the default)</li>
     * <li>Alignment.BOTTOM</li>
     * </ul>
     */
    public int getToolBarAlignment() {
        return ComponentEx.getProperty(this, PROPERTY_TOOLBAR_ALIGNMENT, Alignment.TOP);
    }

    /**
     * Returns the toolbar background color
     *
     * @return the toolbar background color
     */
    public Color getToolBarBackground() {
        return (Color) getProperty(PROPERTY_TOOLBAR_BACKGROUND);
    }

    /**
     * @return whether the text in the <code>RichTextArea</code> can be edited
     * or not.
     */
    public boolean isEditable() {
        return ComponentEx.getProperty(this, PROPERTY_EDITABLE, true);
    }


    /**
     * @return whether a spell check is currently in progress
     */
    public boolean isSpellCheckInProgress() {
        return ComponentEx.getProperty(this, PROPERTY_SPELL_CHECK_IN_PROGRESS, false);
    }

    /**
     * @see nextapp.echo2.app.text.TextComponent#processInput(java.lang.String, java.lang.Object)
     */
    public void processInput(String inputName, Object inputValue) {
        if (KeyStrokeListener.KEYSTROKE_CHANGED_PROPERTY.equals(inputName)) {
            listener.processInput(inputName, inputValue);
        } else {
            if (TEXT_CHANGED_PROPERTY.equals(inputName)) {
                inputValue = makeValidXHTML(new StringBuffer((String) inputValue));
            }
            super.processInput(inputName, inputValue);
            if ("spellcheck".equals(inputName)) {
                // toggle the spell check
                setSpellCheckInProgress(!isSpellCheckInProgress());
            } else if ("macro".equals(inputName) && macros != null) {
                insertMacro((String) inputValue);
            }
        }
    }

    /**
     * Expands a macro at the current cursor position.
     *
     * @param macro the macro code
     */
    public void insertMacro(String macro) {
        firePropertyChange(PROPERTY_MACRO_EXPANSION, null, expand(macro));
    }

    /**
     * Registers the macro expander.
     *
     * @param macros the macro expander
     */
    public void setMacroExpander(MacroExpander macros) {
        this.macros = macros;
    }

    /**
     * Returns the listener for keyboard short cuts.
     *
     * @return the listener
     */
    public KeyStrokeListener getListener() {
        return listener;
    }

    /**
     * The client rich text support do necessarily provide back valid
     * XHTML in the editied text.  This method provides a mechanism
     * to process the input HTML text and convert it to XHTML before it is
     * stored in the backing <code>Document</code>.
     * <p/>
     * This default implementation simply checks for missing end tags in
     * &lt;br/&gt; tags and &lt;img/&gt; tags and puts them in.
     * <p/>
     * You can derive your own implementation to get more complex behaviour
     *
     * @param buffer the buffer containing the HTML text
     * @return the String document to be stored in the Document
     */
    protected String makeValidXHTML(StringBuffer buffer) {
        String tags[] = new String[]{"<br", "<img", "<col", "<hr", "<input"};
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            int index = buffer.indexOf(tag);
            while (index != -1) {
                int endindex = buffer.indexOf(">", index);
                String currentTag = buffer.substring(index, endindex + 1);
                currentTag = currentTag.substring(0, currentTag.length() - 1);
                currentTag += "/>";

                buffer.replace(index, endindex + 1, currentTag);

                index = buffer.indexOf(tag, endindex + 1);
            }
        }
        return buffer.toString();
    }

    /**
     * @see echopointng.able.Attributeable#setAttribute(java.lang.String,
     * java.lang.Object)
     */
    public void setAttribute(String attributeName, Object attributeValue) {
        if (attributeMap == null) {
            attributeMap = new HashMap<>();
        }
        attributeMap.put(attributeName, attributeValue);
    }

    /**
     * Controls whether the text in the RichTextArea can be edited or not.
     *
     * @param newValue -
     *                 thew new edtiable flag
     */
    public void setEditable(boolean newValue) {
        ComponentEx.setProperty(this, PROPERTY_EDITABLE, newValue);
    }

    /**
     * Sets the background of the <code>RichTextArea</code> portion
     */
    public void setEditorBackground(Color editorBackground) {
        setProperty(PROPERTY_EDITOR_BACKGROUND, editorBackground);
    }

    /**
     * Sets the border of the <code>RichTextArea</code> portion
     */
    public void setEditorBorder(Border editorBorder) {
        setProperty(PROPERTY_EDITOR_BORDER, editorBorder);
    }

    /**
     * Sets the font of the <code>RichTextArea</code> portion
     */
    public void setEditorFont(Font editorFont) {
        setProperty(PROPERTY_EDITOR_FONT, editorFont);
    }

    /**
     * Sets the foreground of the <code>RichTextArea</code> portion
     */
    public void setEditorForeground(Color editorForeground) {
        setProperty(PROPERTY_EDITOR_FOREGROUND, editorForeground);
    }

    /**
     * @see echopointng.able.MouseCursorable#setMouseCursor(int)
     */
    public void setMouseCursor(int mouseCursor) {
        ComponentEx.setProperty(this, PROPERTY_MOUSE_CURSOR, mouseCursor);
    }

    /**
     * @see echopointng.able.MouseCursorable#setMouseCursorUri(java.lang.String)
     */
    public void setMouseCursorUri(String mouseCursorURI) {
        setProperty(PROPERTY_MOUSE_CURSOR_URI, mouseCursorURI);
    }

    /**
     * @see echopointng.able.Insetable#setOutsets(nextapp.echo2.app.Insets)
     */
    public void setOutsets(Insets newValue) {
        setProperty(PROPERTY_OUTSETS, newValue);
    }

    /**
     * Sets the RichTextRenderer to be used
     *
     * @param renderer -
     *                 the new RichTextRenderer to be used
     */
    public void setRenderer(RichTextRenderer renderer) {
        if (renderer == null) {
            throw new IllegalArgumentException("The RichTextRenderer must be non null");
        }

        setProperty(PROPERTY_RENDERER, renderer);
    }

    /**
     * Sets the spell checker associated with the <code>RichTextArea</code>
     */
    public void setSpellChecker(RichTextSpellChecker spellChecker) {
        setProperty(PROPERTY_SPELL_CHECKER, spellChecker);
    }

    /**
     * Sets whether a seplling check is in progress
     */
    public void setSpellCheckInProgress(boolean spellCheckInProgress) {
        ComponentEx.setProperty(this, PROPERTY_SPELL_CHECK_IN_PROGRESS, spellCheckInProgress);
    }

    /**
     * Sets the toolbar alignment of the contents of this RichTextArea.
     *
     * @param newValue The toolbar alignment of the contents of this RichTextArea,
     *                 one of the following values.
     *                 <ul>
     *                 <li>Alignment.TOP (the default)</li>
     *                 <li>Alignment.BOTTOM</li>
     *                 </ul>
     */
    public void setToolBarAlignment(int newValue) {
        if (newValue != Alignment.TOP && newValue != Alignment.BOTTOM) {
            throw new IllegalArgumentException("The toolbar alignment must be either Alignment.TOP or Alignment.BOTTOM");
        }
        ComponentEx.setProperty(this, PROPERTY_TOOLBAR_ALIGNMENT, newValue);
    }

    /**
     * Sets the toolbar background color
     *
     * @param newValue -
     *                 the new toolbar background color
     */
    public void setToolBarBackground(Color newValue) {
        setProperty(PROPERTY_TOOLBAR_BACKGROUND, newValue);
    }

    /**
     * Expands a macro, escaping any characters using HTML entities.
     *
     * @param macro the macro to expand
     * @return the result of the expansion. May be {@code null}
     */
    protected String expand(String macro) {
        String result = macros.expand(macro);
        if (!StringUtils.isEmpty(result)) {
            result = StringEscapeUtils.escapeHtml(result);
            result = result.replace("\n", "<br/>").replace(" ", "&nbsp;");
        }
        return result;
    }

}