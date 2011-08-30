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

package org.openvpms.web.app;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.workflow.MessageArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.app.workflow.messaging.MessageMonitor;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Collections;
import java.util.List;


/**
 * Title pane.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TitlePane extends ContentPane {

    /**
     * The location selector.
     */
    private SelectField locationSelector;

    /**
     * The message monitor
     */
    private final MessageMonitor monitor;

    /**
     * The message listener.
     */
    private final MessageMonitor.MessageListener listener;

    /**
     * The user the listener was registered for.
     */
    private User user;

    /**
     * The project logo.
     */
    private final String LOGO = "/org/openvpms/web/resource/image/openvpms.gif";

    /**
     * The style name.
     */
    private static final String STYLE = "TitlePane";

    /**
     * Mail button.
     */
    private Button mail;

    /**
     * Reference to the mail icon.
     */
    private static final ImageReference MAIL
            = new ResourceImageReference("/org/openvpms/web/resource/image/buttons/mail.png");

    /**
     * Reference to the new mail icon.
     */
    private static final ImageReference UNREAD_MAIL
            = new ResourceImageReference("/org/openvpms/web/resource/image/buttons/mail-unread.png");

    /**
     * Constructs a <tt>TitlePane</tt>.
     *
     * @param monitor the message monitor
     */
    public TitlePane(MessageMonitor monitor) {
        this.monitor = monitor;
        listener = new MessageMonitor.MessageListener() {
            public void onMessage(Act message) {
                notifyMessage();
            }
        };
        user = GlobalContext.getInstance().getUser();
        doLayout();
    }

    /**
     * Life-cycle method invoked when the <code>Component</code> is added
     * to a registered hierarchy.
     * <p/>
     * This implementation registers a listener for message notification.
     */
    @Override
    public void init() {
        super.init();
        if (user != null) {
            monitor.addListener(user, listener);
        }
    }

    /**
     * Life-cycle method invoked when the <code>Component</code> is removed
     * from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (user != null) {
            monitor.removeListener(user, listener);
        }
    }

    /**
     * Lay out the component.
     */
    protected void doLayout() {
        setStyleName(STYLE);

        Label logo = LabelFactory.create(new ResourceImageReference(LOGO));
        RowLayoutData centre = new RowLayoutData();
        centre.setAlignment(new Alignment(Alignment.DEFAULT, Alignment.CENTER));
        logo.setLayoutData(centre);

        Label userLabel = LabelFactory.create(null, "small");
        String userName = (user != null) ? user.getUsername() : null;
        userLabel.setText(Messages.get("label.user", userName));

        mail = ButtonFactory.create();
        mail.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                ContextApplicationInstance.getInstance().switchTo(MessageArchetypes.USER);
            }
        });
        updateMessageStatus();

        Row locationUserRow = RowFactory.create("CellSpacing", userLabel, mail);

        List<Party> locations = getLocations();
        if (!locations.isEmpty()) {
            Party defLocation = getDefaultLocation();
            Label location = LabelFactory.create("app.location", "small");
            IMObjectListModel model = new IMObjectListModel(locations,
                                                            false, false);
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
        }

        Row inset = RowFactory.create("InsetX", locationUserRow);
        RowLayoutData right = new RowLayoutData();
        right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.BOTTOM));
        right.setWidth(new Extent(100, Extent.PERCENT));
        inset.setLayoutData(right);
        Row row = RowFactory.create(logo, inset);
        add(row);
    }

    private void updateMessageStatus() {
        boolean update = false;
        if (user != null) {
            update = monitor.hasNewMessages(user);
        }
        updateMessageStatus(update);
    }

    private void updateMessageStatus(boolean newMessages) {
        if (newMessages) {
            mail.setIcon(UNREAD_MAIL);
            mail.setToolTipText(Messages.get("messages.unread.tooltip"));
        } else {
            mail.setIcon(MAIL);
            mail.setToolTipText(Messages.get("messages.read.tooltip"));
        }
    }

    /**
     * Changes the location.
     */
    private void changeLocation() {
        Party selected = (Party) locationSelector.getSelectedItem();
        GlobalContext.getInstance().setLocation(selected);
    }

    /**
     * Returns the locations for the current user.
     *
     * @return the locations
     */
    private List<Party> getLocations() {
        List<Party> locations = Collections.emptyList();
        GlobalContext context = GlobalContext.getInstance();
        User user = context.getUser();
        if (user != null) {
            UserRules rules = new UserRules();
            locations = rules.getLocations(user);
            if (locations.isEmpty()) {
                Party practice = context.getPractice();
                if (practice != null) {
                    PracticeRules practiceRules = new PracticeRules();
                    locations = practiceRules.getLocations(practice);
                }
            }
        }
        return locations;
    }

    /**
     * Returns the default location for the current user.
     *
     * @return the default location, or <tt>null</tt> if none is found
     */
    private Party getDefaultLocation() {
        Party location = null;
        User user = GlobalContext.getInstance().getUser();
        if (user != null) {
            UserRules rules = new UserRules();
            location = rules.getDefaultLocation(user);
            if (location == null) {
                Party practice = GlobalContext.getInstance().getPractice();
                if (practice != null) {
                    PracticeRules practiceRules = new PracticeRules();
                    location = practiceRules.getDefaultLocation(practice);
                }
            }
        }
        return location;
    }

    private void notifyMessage() {
    }
}
