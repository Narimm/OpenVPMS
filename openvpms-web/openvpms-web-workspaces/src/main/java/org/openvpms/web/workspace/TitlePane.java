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

package org.openvpms.web.workspace;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.ImageCache;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.prefs.PreferencesDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.pane.ContentPane;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.Collections;
import java.util.List;


/**
 * Title pane.
 *
 * @author Tim Anderson
 */
public class TitlePane extends ContentPane {

    /**
     * The practice rules.
     */
    private final PracticeRules practiceRules;

    /**
     * The user rules.
     */
    private final UserRules userRules;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The logo.
     */
    private Label logo;

    /**
     * The location selector.
     */
    private SelectField locationSelector;

    /**
     * The style name.
     */
    private static final String STYLE = "TitlePane";


    /**
     * Constructs a {@link TitlePane}.
     *
     * @param practiceRules the practice rules
     * @param userRules     the user rules
     * @param context       the context
     */
    public TitlePane(PracticeRules practiceRules, UserRules userRules, Context context) {
        this.practiceRules = practiceRules;
        this.userRules = userRules;
        this.context = context;
        doLayout();
    }

    /**
     * Lay out the component.
     */
    protected void doLayout() {
        setStyleName(STYLE);

        logo = LabelFactory.create(null, "logo");
        RowLayoutData centre = new RowLayoutData();
        centre.setAlignment(new Alignment(Alignment.DEFAULT, Alignment.CENTER));
        logo.setLayoutData(centre);

        Label user = LabelFactory.create(null, "small");
        user.setText(Messages.format("label.user", getUserName()));

        Row locationUserRow = RowFactory.create(Styles.CELL_SPACING, user);
        if (canEditPreferences()) {
            Button preferences = ButtonFactory.create(null, "button.preferences", new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    editPreferences();
                }
            });
            locationUserRow.add(preferences);
        }

        List<Party> locations = getLocations();
        if (!locations.isEmpty()) {
            Party defLocation = getDefaultLocation();
            Label location = LabelFactory.create("app.location", "small");
            IMObjectListModel model = new IMObjectListModel(locations, false, false);
            locationSelector = SelectFieldFactory.create(model);
            if (defLocation != null) {
                locationSelector.setSelectedItem(defLocation);
            }
            locationSelector.setCellRenderer(IMObjectListCellRenderer.NAME);
            locationUserRow.add(location, 0);
            locationUserRow.add(locationSelector, 1);
            locationSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    changeLocation();
                }
            });
            setLogo(defLocation);
        }

        Row inset = RowFactory.create("InsetX", locationUserRow);
        RowLayoutData right = new RowLayoutData();
        right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.BOTTOM));
        right.setWidth(new Extent(100, Extent.PERCENT));
        inset.setLayoutData(right);
        Row row = RowFactory.create(logo, inset);
        add(row);
    }

    /**
     * Returns the user name for the current user.
     *
     * @return the user name
     */
    protected String getUserName() {
        User user = context.getUser();
        return (user != null) ? user.getName() : null;
    }

    /**
     * Changes the location.
     */
    private void changeLocation() {
        Party selected = (Party) locationSelector.getSelectedItem();
        context.setLocation(selected);
        setLogo(selected);
    }

    /**
     * Sets the logo for the practice location.
     *
     * @param location the location. May be {@code null}
     */
    private void setLogo(Party location) {
        ImageReference ref = null;
        if (location != null) {
            Participation participation = QueryHelper.getParticipation(location, DocumentArchetypes.LOGO_PARTICIPATION);
            if (participation != null) {
                DocumentAct act = (DocumentAct) IMObjectHelper.getObject(participation.getAct(), context);
                if (act != null) {
                    ImageCache cache = ServiceHelper.getBean(ImageCache.class);
                    ref = cache.getImage(act);
                }
            }
        }
        logo.setIcon(ref);
    }

    /**
     * Returns the locations for the current user.
     *
     * @return the locations
     */
    private List<Party> getLocations() {
        List<Party> locations = Collections.emptyList();
        User user = context.getUser();
        Party practice = context.getPractice();
        if (user != null && practice != null) {
            locations = userRules.getLocations(user, practice);
            IMObjectSorter.sort(locations, "name");
        }
        return locations;
    }

    /**
     * Returns the default location for the current user.
     *
     * @return the default location, or {@code null} if none is found
     */
    private Party getDefaultLocation() {
        Party location = null;
        User user = context.getUser();
        if (user != null) {
            location = userRules.getDefaultLocation(user);
            if (location == null) {
                Party practice = context.getPractice();
                if (practice != null) {
                    location = practiceRules.getDefaultLocation(practice);
                }
            }
        }
        return location;
    }

    /**
     * Determines if the user can edit preferences.
     *
     * @return {@code true} if the user can edit preferences
     */
    private boolean canEditPreferences() {
        boolean result = false;
        User user = context.getUser();
        if (user != null) {
            IMObjectBean bean = new IMObjectBean(user);
            result = bean.getBoolean("editPreferences");
        }
        return result;
    }

    /**
     * Edits user preferences.
     */
    private void editPreferences() {
        User user = IMObjectHelper.reload(context.getUser());
        if (user != null) {
            PreferencesDialog dialog = new PreferencesDialog(user, context.getPractice(), new LocalContext(context));
            dialog.show();
        }
    }

}
