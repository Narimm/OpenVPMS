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

package org.openvpms.web.component.im.contact;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.StyleSheetHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * View layout strategy for <em>contact.website</em> contacts.
 * <p/>
 * This opens a new browser window when the URL is clicked.
 *
 * @author Tim Anderson
 */
public class WebsiteContactViewLayout extends AbstractContactViewLayout {

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        Button link = ButtonFactory.create(null, "hyperlink");
        Property property = properties.get("url");
        final String url = getURL(property);
        link.setText(url);
        link.setBackground(null); // want to inherit style of parent
        link.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                launch(url);
            }
        });
        addComponent(new ComponentState(link, property));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns the URL.
     *
     * @param property the URL property
     * @return the URL
     */
    protected String getURL(Property property) {
        String url = property.getString();
        try {
            URI uri = new URI(url);
            if (uri.getScheme() == null) {
                url = "http://" + url;
            }
        } catch (URISyntaxException e) {
            // do nothing.
        }
        return url;
    }

    /**
     * Launch the website URL.
     *
     * @param url the url
     */
    private void launch(String url) {
        String features = StyleSheetHelper.getProperty("HelpBrowser.features");
        ApplicationInstance.getActive().enqueueCommand(new BrowserOpenWindowCommand(url, null, features));
    }
}
