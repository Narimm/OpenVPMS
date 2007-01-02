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

package org.openvpms.web.app.workflow.messaging;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.DocumentEvent;
import nextapp.echo2.app.event.DocumentListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ShortcutHelper;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.FastLookupHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Query for <em>act.userMessage</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MessageQuery extends DefaultActQuery<Act> {

    /**
     * The user name.
     */
    private TextField name;

    /**
     * The clinician name listener.
     */
    private DocumentListener nameListener;


    /**
     * Constructs a new <code>MessageQuery</code>.
     */
    public MessageQuery(Entity user) {
        super(user, "to", "participation.user", new String[]{"act.userMessage"},
              getLookups(), null);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        Label label = LabelFactory.create("messaging.user");
        name = TextComponentFactory.create();
        nameListener = new DocumentListener() {
            public void documentUpdate(DocumentEvent event) {
                onNameChanged();
            }
        };
        name.getDocument().addDocumentListener(nameListener);

        // add an action listener so document updates get propagated in a
        // timely fashion
        name.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            }
        });
        Button select = ButtonFactory.create(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelect();
            }
        });
        select.setText(ShortcutHelper.getLocalisedText("button.select"));
        container.add(label);
        container.add(name);
        container.add(select);
        getFocusGroup().add(name);
    }

    /**
     * Invoked when the 'select' button is pressed. This pops up an {@link
     * Browser} to select a clinician.
     */
    protected void onSelect() {
        try {
            String shortName = "security.user";
            Query<IMObject> query = QueryFactory.create(
                    shortName, GlobalContext.getInstance());
            final Browser<IMObject> browser
                    = new IMObjectTableBrowser<IMObject>(query);

            String title = Messages.get(
                    "imobject.select.title",
                    DescriptorHelper.getDisplayName(shortName));
            final BrowserDialog<IMObject> popup = new BrowserDialog<IMObject>(
                    title, browser);

            popup.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    Entity object = (Entity) popup.getSelected();
                    if (object != null) {
                        onUserSelected(object);
                    }
                }
            });

            popup.show();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the user name is updated.
     */
    private void onNameChanged() {
        String name = this.name.getText();
        if (StringUtils.isEmpty(name)) {
            setEntity(null);
        } else {
            try {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                IPage<IMObject> page = ArchetypeQueryHelper.get(
                        service, "system", "security", "user", name, true, 0,
                        2);
                List<IMObject> rows = page.getResults();
                if (rows.size() != 1) {
                    // no matches or multiple matches
                    setEntity(null);
                } else {
                    Entity user = (Entity) rows.get(0);
                    this.name.getDocument().removeDocumentListener(
                            nameListener);
                    this.name.setText(user.getName());
                    this.name.getDocument().addDocumentListener(nameListener);
                    setEntity(user);
                }
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
        onQuery();
    }

    /**
     * Invoked when a user is selected.
     *
     * @param user the user
     */
    private void onUserSelected(Entity user) {
        String name;
        if (user != null) {
            setEntity(user);
            name = user.getName();
        } else {
            setEntity(null);
            name = null;
        }
        this.name.getDocument().removeDocumentListener(nameListener);
        this.name.setText(name);
        this.name.getDocument().addDocumentListener(nameListener);
        onQuery();
    }

    private static List<Lookup> getLookups() {
        String shortName = "act.userMessage";
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(shortName);
        NodeDescriptor statuses = archetype.getNodeDescriptor("status");
        return FastLookupHelper.getLookups(statuses);
    }

}
