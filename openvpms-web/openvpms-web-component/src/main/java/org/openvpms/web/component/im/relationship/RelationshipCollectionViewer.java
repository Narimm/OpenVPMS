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

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.IMTableCollectionViewer;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.echo.i18n.Messages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Viewer for collections of {@link IMObjectRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipCollectionViewer
    extends IMTableCollectionViewer<RelationshipState> {

    /**
     * Determines if the parent is the source or target of the relationships.
     */
    private final boolean parentIsSource;

    /**
     * The relationship states, keyed on their corresponding
     * relationships.
     */
    private Map<IMObjectRelationship, RelationshipState> states
        = new LinkedHashMap<IMObjectRelationship, RelationshipState>();

    /**
     * Determines if inactive relationships should be displayed.
     */
    private CheckBox hideInactive;


    /**
     * Constructs a new <tt>RelationshipCollectionViewer</tt>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param context  the layout context. May be <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public RelationshipCollectionViewer(CollectionProperty property,
                                        IMObject parent,
                                        LayoutContext context) {
        super(property, parent, context);
        RelationshipStateQuery query = createQuery(parent);
        parentIsSource = query.parentIsSource();
        states = query.query();
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected IMTableModel<RelationshipState> createTableModel(
        LayoutContext context) {
        return new RelationshipStateTableModel(context, parentIsSource);
    }

    /**
     * Selects an object in the table.
     *
     * @param object the object to select
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    protected void setSelected(IMObject object) {
        RelationshipState state = states.get(object);
        getTable().getTable().setSelected(state);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be <tt>null</tt>
     */
    protected IMObject getSelected() {
        RelationshipState state = getTable().getTable().getSelected();
        return (state != null) ? state.getRelationship() : null;
    }

    /**
     * Creates a new result set.
     *
     * @return a new result set
     */
    protected ResultSet<RelationshipState> createResultSet() {
        List<RelationshipState> result = getRelationshipStates();
        return new RelationshipStateResultSet(result, parentIsSource, ROWS);
    }

    /**
     * Returns the relationship states, filtering inactive relationships if
     * {@link #hideInactive()} is <tt>true</tt>.
     *
     * @return the relationships
     */
    protected List<RelationshipState> getRelationshipStates() {
        List<RelationshipState> result;
        if (hideInactive()) {
            result = new ArrayList<RelationshipState>();
            for (RelationshipState relationship : states.values()) {
                if (relationship.isActive()) {
                    result.add(relationship);
                }
            }
        } else {
            result = new ArrayList<RelationshipState>(states.values());
        }
        return result;
    }

    /**
     * Creates a new relationship state query.
     *
     * @param parent the parent object
     * @return a new query
     */
    protected RelationshipStateQuery createQuery(IMObject parent) {
        return new RelationshipStateQuery(
            parent, getObjects(), getProperty().getArchetypeRange());
    }

    /**
     * Determines if the parent is the source or target of the relationship.
     *
     * @return <tt>true</tt> if the parent is the source of the relationship,
     *         <tt>false</tt> if it is the target
     */
    protected boolean parentIsSource() {
        return parentIsSource;
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        String name = getProperty().getDisplayName();
        String label = Messages.get("relationship.hide.inactive", name);
        hideInactive = CheckBoxFactory.create(null, true);
        hideInactive.setText(label);
        hideInactive.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onHideInactiveChanged();
            }
        });
        Component component = super.doLayout();
        component.add(hideInactive, 0);
        return component;
    }

    /**
     * Determines if inactive objects should be hidden.
     *
     * @return <code>true</code> if inactive objects should be hidden
     */
    protected boolean hideInactive() {
        return hideInactive.isSelected();
    }

    /**
     * Invoked when the 'hide inactive' checkbox changes.
     */
    private void onHideInactiveChanged() {
        populateTable();
    }

}
