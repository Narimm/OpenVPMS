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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit;

/**
 * A popup window that displays an {@link IMObjectEditor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EditDialog extends AbstractEditDialog {

    /**
     * Constructs an <tt>EditDialog</tt>.
     *
     * @param editor the editor
     */
    public EditDialog(IMObjectEditor editor) {
        this(editor, true);
    }

    /**
     * Constructs an <tt>EditDialog</tt>.
     *
     * @param editor the editor
     * @param save   if <tt>true</tt>, saves the editor when the 'OK' or
     *               'Apply' buttons are pressed.
     */
    public EditDialog(IMObjectEditor editor, boolean save) {
        this(editor, save, false);
    }

    /**
     * Constructs an <tt>EditDialog</tt>.
     *
     * @param editor the editor
     * @param save   if <tt>true</tt>, saves the editor when the 'OK' or
     *               'Apply' buttons are pressed.
     * @param skip   if <tt>true</tt> display a 'Skip' button that simply
     *               closes the dialog
     */
    public EditDialog(IMObjectEditor editor, boolean save, boolean skip) {
        this(editor, save, true, skip);
    }

    /**
     * Constructs an <tt>EditDialog</tt>.
     *
     * @param editor the editor
     * @param save   if <tt>true</tt>, saves the editor when the 'OK' or
     *               'Apply' buttons are pressed.
     * @param apply  if <tt>true</tt>, display an 'Apply' button
     * @param skip   if <tt>true</tt> display a 'Skip' button that simply
     *               closes the dialog
     */
    public EditDialog(IMObjectEditor editor, boolean save, boolean apply, boolean skip) {
        this(editor, save, apply, true, skip);
    }

    /**
     * Constructs an <tt>EditDialog</tt>.
     *
     * @param editor the editor
     * @param save   if <tt>true</tt>, saves the editor when the 'OK' or
     *               'Apply' buttons are pressed.
     * @param apply  if <tt>true</tt>, display an 'Apply' button
     * @param cancel if <tt>true</tt>, display a 'Cancel' button
     * @param skip   if <tt>true</tt> display a 'Skip' button that simply
     *               closes the dialog
     */
    public EditDialog(IMObjectEditor editor, boolean save, boolean apply, boolean cancel, boolean skip) {
        super(editor, getButtons(apply, cancel, skip), save);
    }


}
