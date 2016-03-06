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

import nextapp.echo2.app.Border;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.ImageReference;

import java.io.Serializable;

/**
 * Port of the EPNG RichTextRenderer to support macro expansion.
 *
 * @author Tim Anderson
 */
public interface RichTextRenderer extends Serializable {

    /**
     * <code>CommandAppearance</code> controls what a command
     * image looks like when it is rolled over or currently 
     * in a selected state.
     */
    public static interface CommandAppearance {

        public Color getBackground();

        public Border getBorder();

        public Color getRolloverBackground();

        public Border getRolloverBorder();

        public Color getSelectedBackground();

        public Border getSelectedBorder();
    }

    /**
     * Available RichTextArea Commands
     */
    public static final String CMD_BOLD 		= "bold";
    public static final String CMD_COPY 		= "copy";
    public static final String CMD_CREATELINK	= "createlink";
    public static final String CMD_CUT 			= "cut";
    public static final String CMD_FORECOLOR	= "forecolor";
    public static final String CMD_BACKCOLOR	= "hilitecolor";
    public static final String CMD_INDENT 		= "indent";
    public static final String CMD_INSERTHR 	= "inserthorizontalrule";
    public static final String CMD_INSERTIMAGE 	= "insertimage";
    public static final String CMD_NUMBERS 		= "insertorderedlist";
    public static final String CMD_INSERTTABLE 	= "inserttable";
    public static final String CMD_BULLETS 		= "insertunorderedlist";
    public static final String CMD_ITALIC 		= "italic";
    public static final String CMD_ALIGN_CENTER = "justifycenter";
    public static final String CMD_JUSTIFY 		= "justifyfull";
    public static final String CMD_ALIGN_LEFT 	= "justifyleft";
    public static final String CMD_ALIGN_RIGHT 	= "justifyright";
    public static final String CMD_OUTDENT 		= "outdent";
    public static final String CMD_PASTE 		= "paste";
    public static final String CMD_REDO 		= "redo";
    public static final String CMD_REMOVEFORMAT = "removeformat";
    public static final String CMD_SELECTALL	= "selectall";
    public static final String CMD_SPELLCHECK	= "spellcheck";
    public static final String CMD_SUBSCRIPT 	= "subscript";
    public static final String CMD_SUPERSCRIPT 	= "superscript";
    public static final String CMD_UNDERLINE 	= "underline";
    public static final String CMD_UNDO 		= "undo";

    /**
     * A rendering hint that puts the command on a new line
     * within the RichTextArea toolbar.
     */
    public static final String CMD_HINT_NEWLINE = "hintNewLine";
    /**
     * A rendering hint that puts the spacer within the 
     * RichTextArea toolbar.
     */
    public static final String CMD_HINT_SPACER	= "hintSpacer";

    /**
     * This returns a String[n][2] array of font name values and 
     * descriptive names.  If null is returned, then the
     * Font Names select field will not be rendered by the
     * RickTextArea.
     *
     * For example {"courier new,courier,monospace","Courier New"},   
     * @param rta    - the RichTextArea component in question
     * @param userAgent    - the user agent string of the client
     * @return        - a [n][2] sized array mapping values to descriptive names.
     */
    public String[][] getFontNames(RichTextArea rta, String userAgent);

    /**
     * This returns a String[n][2] array of font size values and 
     * descriptive names.  If null is returned, then the
     * Font Sizes select field will not be rendered by the
     * RickTextArea.
     *
     * For example {"5","Size 5"},   
     * @param rta    - the RichTextArea component in question
     * @param userAgent    - the user agent string of the client
     * @return        - a [n][2] sized array mapping values to descriptive names.
     */
    public String[][] getFontSizes(RichTextArea rta, String userAgent);

    /**
     * This returns a String[n][2] array of paragraph style values and 
     * descriptive names.  If null is returned, then the
     * Paragraph Styles select field will not be rendered by the
     * RickTextArea.
     *
     * For example {"h1","Heading 1"},   
     * @param rta    - the RichTextArea component in question
     * @param userAgent    - the user agent string of the client
     * @return        - a [n][2] sized array mapping values to descriptive names.
     */
    public String[][] getParagraphStyles(RichTextArea rta, String userAgent);


    /**
     * This returns a String[n][2] array of supported commands and descriptive names.
     * If null is returned, then no commands buttons will be rendered by the
     * RickTextArea.
     * <p>
     * The order of the array is the order in which the commands will be rendered.  The 
     * special markers
     * @param rta    - the RichTextArea component in question
     * @param userAgent    - the user agent string of the client
     * @return        - a [n][2] sized array mapping command values to descriptive names.
     */
    public String[][] getSupportedCommands(RichTextArea rta, String userAgent);

    /**
     * This returns an image for a given RTA command.
     * <p> 
     * This function will only be called for commands that have been allowed via
     * a previous call to the getSupportedCommands method.
     *
     * @param rta    - the RichTextArea component in question
     * @param userAgent    - the user agent string of the client
     * @param command - the command in question
     * @return - an ImageReference for the command
     */
    public ImageReference getCommandImage(RichTextArea rta, String userAgent, String command);


    /**
     * This returns the appearance of the command button images.  This denotes
     * the standard borders and backgrounds and well as rollover and stateful
     * appearance.  
     *
     * @param rta    - the RichTextArea component in question
     * @param userAgent    - the user agent string of the client
     * @return - an CommandAppearance instance
     */
    public CommandAppearance getCommandAppearance(RichTextArea rta, String userAgent);

}
