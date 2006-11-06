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

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.resource.util.Messages;


/**
 * Editor for <em>participation.documentTemplate</em> participation
 * relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentTemplateParticipationEditor
        extends AbstractIMObjectEditor {

    /**
     * The template editor.
     */
    private final IMObjectReferenceEditor _templateEditor;


    /**
     * Constructs a new <code>DocumentTemplateParticipationEditor</code>.
     *
     * @param participation the participation
     * @param parent        the parent act
     * @param context       the layout context. May be <code>null</code>
     */
    public DocumentTemplateParticipationEditor(Participation participation,
                                               DocumentAct parent,
                                               LayoutContext context) {
        super(participation, parent, context);
        Property act = getProperty("act");
        if (act.getValue() == null) {
            act.setValue(parent.getObjectReference());
        }
        Property entity = getProperty("entity");
        _templateEditor = new AbstractIMObjectReferenceEditor(entity, context) {

            /**
             * Pops up a dialog to select an object.
             */
            @Override
            protected void onSelect() {
                Query<IMObject> query = createQuery();
                String shortname = null;
                if (getParent() != null)
                    shortname = getParent().getArchetypeId().getShortName();

                final Browser<IMObject> browser
                        = new DocumentTemplateTableBrowser<IMObject>(
                        query, shortname);

                String title = Messages.get("imobject.select.title",
                                            getDescriptor().getDisplayName());
                final BrowserDialog<IMObject> popup = new BrowserDialog<IMObject>(
                        title, browser);

                popup.addWindowPaneListener(new WindowPaneListener() {
                    public void windowPaneClosing(WindowPaneEvent event) {
                        IMObject object = popup.getSelected();
                        if (object != null) {
                            setObject(object);
                        }
                    }
                });

                popup.show();
            }
        };

        getEditors().add(_templateEditor);
    }

    /**
     * Creates a query to select objects.
     *
     * @return a new query
     */
    protected Query getQuery(Query query) {
        return query;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new IMObjectLayoutStrategy() {
            public Component apply(IMObject object, PropertySet properties,
                                   IMObject parent, LayoutContext context) {
                return _templateEditor.getComponent();
            }
        };
    }

}
