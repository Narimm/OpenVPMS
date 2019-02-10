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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import echopointng.KeyStrokes;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.DefaultIMObjectActions;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.workspace.AbstractCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.appointment.AbstractCalendarEventEditDialog;

import java.util.Date;

import static org.openvpms.archetype.rules.workflow.ScheduleArchetypes.ROSTER_EVENT;

/**
 * Roster CRUD window.
 * <p/>
 * This is responsible for editing <em>act.rosterEvent</em> instances.
 *
 * @author Tim Anderson
 */
abstract class RosterCRUDWindow extends AbstractCRUDWindow<Act> {

    /**
     * The roster browser.
     */
    private final RosterBrowser browser;

    /**
     * Constructs a {@link RosterCRUDWindow}.
     *
     * @param browser the browser
     * @param context the context
     * @param help    the help context
     */
    public RosterCRUDWindow(RosterBrowser browser, Context context, HelpContext help) {
        super(Archetypes.create(ROSTER_EVENT, Act.class), DefaultIMObjectActions.getInstance(), context, help);
        this.browser = browser;
        context.setSchedule(null);
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    protected RosterBrowser getBrowser() {
        return browser;
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(createEditButton());
        buttons.add(createDeleteButton());
        buttons.addKeyListener(KeyStrokes.CONTROL_MASK | 'C', new ActionListener() {
            public void onAction(ActionEvent event) {
                onCopy();
            }
        });
        buttons.addKeyListener(KeyStrokes.CONTROL_MASK | 'X', new ActionListener() {
            public void onAction(ActionEvent event) {
                onCut();
            }
        });
        buttons.addKeyListener(KeyStrokes.CONTROL_MASK | 'V', new ActionListener() {
            public void onAction(ActionEvent event) {
                onPaste();
            }
        });
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.setEnabled(EDIT_ID, enable);
        buttons.setEnabled(DELETE_ID, enable);
    }

    /**
     * Copies an act and pastes it to the specified entity and start time.
     *
     * @param act    the act
     * @param entity the new entity
     * @param date   the new start time
     */
    protected void copy(Act act, Entity entity, Date date) {
        Act copy = (Act) IMObjectCreator.create(act.getArchetype());
        IMObjectBean source = new IMObjectBean(act);
        IMObjectBean target = new IMObjectBean(copy);
        target.setTarget("author", getContext().getUser());
        target.setTarget("schedule", source.getTargetRef("schedule"));
        target.setTarget("user", source.getTargetRef("user"));
        paste(copy, entity, date);
    }

    /**
     * Populates an entity, copying or moving an event.
     *
     * @param editor the editor
     * @param entity the entity
     */
    protected abstract void setEntity(RosterEventEditor editor, Entity entity);

    /**
     * Invoked to copy an event.
     */
    private void onCopy() {
        browser.clearMarked();
        PropertySet selected = browser.getSelected();
        Act event = browser.getAct(selected);
        if (event != null) {
            browser.setMarked(selected, false);
        }
    }

    /**
     * Invoked to cut an event.
     */
    private void onCut() {
        browser.clearMarked();
        PropertySet selected = browser.getSelected();
        Act event = browser.getAct(selected);
        if (event != null) {
            browser.setMarked(selected, true);
        }
    }

    /**
     * Invoked to paste an act.
     * <p/>
     * For the paste to be successful:
     * <ul>
     * <li>the act must still exist
     * <li>a schedule must be selected
     * <li>a time slot must be selected
     * </ul>
     */
    private void onPaste() {
        if (browser.getMarked() != null) {
            Act act = browser.getAct(browser.getMarked());
            Entity entity = browser.getSelectedEntity();
            Date date = browser.getSelectedDate();
            if (act == null) {
                InformationDialog.show(Messages.get("workflow.scheduling.appointment.paste.title"),
                                       Messages.get("workflow.scheduling.appointment.paste.noexist"));
                onRefresh((Act) null);    // force redraw
                browser.clearMarked();
            } else if (entity == null || date == null) {
                InformationDialog.show(Messages.get("workflow.scheduling.appointment.paste.title"),
                                       Messages.get("workflow.scheduling.appointment.paste.noslot"));
            } else {
                if (browser.isCut()) {
                    cut(act, entity, date);
                } else {
                    copy(act, entity, date);
                }
            }
        }
    }

    /**
     * Cuts an act and pastes it to the specified entity and date.
     *
     * @param act    the act
     * @param entity the new entity
     * @param date   the new date
     */
    private void cut(Act act, Entity entity, Date date) {
        paste(act, entity, date);
        browser.clearMarked();
    }

    /**
     * Pastes an act to the specified user and date.
     *
     * @param act    the act
     * @param entity the new entity
     * @param date   the new date
     */
    private void paste(Act act, Entity entity, Date date) {
        LayoutContext context = createLayoutContext(createEditTopic(act));
        RosterEventEditor editor = (RosterEventEditor) createEditor(act, context);
        AbstractCalendarEventEditDialog dialog = (AbstractCalendarEventEditDialog) edit(editor, null);
        setEntity(editor, entity);
        Date time = editor.getStartTime();
        editor.setStartTime(DateRules.addDateTime(date, time));
        dialog.setAlwaysCheckOverlap(true); // checks for overlapping events
        dialog.save(true);
        browser.setSelected(null);
    }

}
