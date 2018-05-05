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

package org.openvpms.web.component.prefs;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.clinician.ClinicianSelectField;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.SelectFieldIMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for scheduling and work list preferences.
 *
 * @author Tim Anderson
 */
public abstract class AbstractSchedulePreferenceGroupEditor extends AbstractIMObjectEditor {

    /**
     * The schedule/work list view editor.
     */
    private final SelectFieldIMObjectReferenceEditor<Entity> viewEditor;

    /**
     * The schedule/work list editor.
     */
    private final SelectFieldIMObjectReferenceEditor<Entity> scheduleEditor;

    /**
     * The clinician editor.
     */
    private final ClinicianSelectField clinicianEditor;

    /**
     * The available views.
     */
    private List<Entity> views;

    /**
     * Constructs an {@link AbstractSchedulePreferenceGroupEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public AbstractSchedulePreferenceGroupEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        Property view = getProperty("view");
        view.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onViewChanged();
            }
        });
        viewEditor = new SelectFieldIMObjectReferenceEditor<>(view, getViews(), false);
        List<Entity> schedules = getSchedules();
        scheduleEditor = new SelectFieldIMObjectReferenceEditor<>(getProperty("schedule"), schedules, true);
        getEditors().add(viewEditor);
        getEditors().add(scheduleEditor);

        clinicianEditor = new ClinicianSelectField(true);
        clinicianEditor.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onClinicianChanged();
            }
        });
        clinicianEditor.setSelected(getProperty("clinician").getReference());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        strategy.addComponent(new ComponentState(viewEditor));
        strategy.addComponent(new ComponentState(scheduleEditor));
        strategy.addComponent(new ComponentState(clinicianEditor, getProperty("clinician")));
        return strategy;
    }

    /**
     * Returns the views associated with the locations available to the user.
     *
     * @return the views
     */
    protected List<Entity> getViews() {
        if (views == null) {
            views = new ArrayList<>();
            UserRules rules = ServiceHelper.getBean(UserRules.class);
            Context context = getLayoutContext().getContext();
            User user = context.getUser();
            Party practice = context.getPractice();
            if (user != null && practice != null) {
                List<Party> locations = rules.getLocations(user, practice);
                for (Party location : locations) {
                    IMObjectBean bean = new IMObjectBean(location);
                    views.addAll(getViews(bean));
                }
                IMObjectSorter.sort(views, "name");
            }
        }
        return views;
    }

    /**
     * Returns the schedule/work list views associated with a practice location.
     *
     * @param location the location
     * @return the views
     */
    protected abstract List<Entity> getViews(IMObjectBean location);

    /**
     * Returns the schedules associated with a view.
     *
     * @param view the view
     * @return the schedules/work lists
     */
    protected abstract List<Entity> getSchedules(Entity view);

    /**
     * Returns the schedules associated with selected view.
     *
     * @return the schedules
     */
    protected List<Entity> getSchedules() {
        List<Entity> schedules = Collections.emptyList();
        Entity view = IMObjectHelper.getObject(getProperty("view").getReference(), getViews());
        if (view != null) {
            schedules = getSchedules(view);
            IMObjectSorter.sort(schedules, "name");
        }
        return schedules;
    }

    /**
     * Invoked when the view changes. Updates the available schedules.
     */
    private void onViewChanged() {
        scheduleEditor.setObjects(getSchedules());
        scheduleEditor.setObject(null); // all
    }

    /**
     * Invoked when the clinician changes. This updates the preference.
     */
    private void onClinicianChanged() {
        User selected = clinicianEditor.getSelected();
        getProperty("clinician").setValue(selected != null ? selected.getObjectReference() : null);
    }

}