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

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.focus.FocusSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.TableBrowser;
import org.openvpms.web.component.im.select.Selector;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;

import org.openvpms.archetype.util.TypeHelper;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;


/**
 * Editor for {@link IMObjectReference}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectReferenceEditor extends AbstractPropertyEditor {

    /**
     * The selector.
     */
    private Selector _selector;


    /**
     * Construct a new <code>IMObjectReferenceEditor</code>.
     *
     * @param property the reference property
     * @param context  the layout context
     */
    public IMObjectReferenceEditor(Property property, LayoutContext context) {
        this(property, false, context);
    }

    /**
     * Construct a new <code>IMObjectReferenceEditor</code>.
     *
     * @param property the reference property
     * @param readOnly if <code>true</code> the reference cannot be edited
     * @param context  the layout context
     */
    public IMObjectReferenceEditor(Property property, boolean readOnly,
                                   LayoutContext context) {
        super(property);
        if (readOnly) {
            _selector = new Selector(Selector.ButtonStyle.HIDE);
        } else {
            _selector = new Selector(Selector.ButtonStyle.RIGHT);
            FocusSet set = new FocusSet("IMObjectReferenceEditor");
            set.add(_selector.getSelect());
            context.getFocusTree().add(set);
            _selector.getSelect().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSelect();
                }
            });
        }
        _selector.setFormat(Selector.Format.NAME);
        IMObjectReference reference = (IMObjectReference) property.getValue();
        if (reference != null) {
            NodeDescriptor descriptor = property.getDescriptor();
            _selector.setObject(getObject(reference, descriptor));
        }
    }

    /**
     * Sets the value of the reference to the supplied object.
     *
     * @param object the object. May  be <code>null</code>
     */
    public void setObject(IMObject object) {
        IMObjectReference reference
                = (object != null) ? new IMObjectReference(object) : null;
        getProperty().setValue(reference);
        _selector.setObject(object);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return _selector.getComponent();
    }

    /**
     * Returns the object reference's descriptor.
     *
     * @return the object reference's descriptor
     */
    public NodeDescriptor getDescriptor() {
        return getProperty().getDescriptor();
    }

    /**
     * Returns an object given its reference and descriptor. If the reference is
     * null, determines if the descriptor matches that of the current object
     * being viewed/edited and returns that instead.
     *
     * @param reference  the object reference. May be <code>null</code>
     * @param descriptor the node descriptor
     * @return the object matching <code>reference</code>, or
     *         <code>descriptor</code>, or <code>null</code> if there is no
     *         matches
     */
    public static IMObject getObject(IMObjectReference reference,
                                     NodeDescriptor descriptor) {
        IMObject result = null;
        if (reference == null) {
            result = match(descriptor);
        } else {
            IMObject edit = Context.getInstance().getCurrent();
            if (edit != null && edit.getObjectReference().equals(reference)) {
                result = edit;
            }
            if (result == null) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                result = ArchetypeQueryHelper.getByObjectReference(service,
                                                                   reference);
            }
        }
        return result;
    }

    /**
     * Pops up a dialog to select an object.
     */
    protected void onSelect() {
        Query query = createQuery();
        final Browser browser = new TableBrowser(query);

        String title = Messages.get("imobject.select.title",
                                    getDescriptor().getDisplayName());
        final BrowserDialog popup = new BrowserDialog(title, browser);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                IMObject object = popup.getSelected();
                if (object != null) {
                    onSelected(object);
                }
            }
        });

        popup.show();
    }

    /**
     * Invoked when an object is selcted.
     *
     * @param object the selected object
     */
    protected void onSelected(IMObject object) {
        setObject(object);
    }

    /**
     * Creates a query to select objects.
     *
     * @return a new query
     */
    protected Query createQuery() {
        String[] shortNames = getDescriptor().getArchetypeRange();
        return QueryFactory.create(shortNames);
    }

    /**
     * Determines if the current object being edited matches archetype range of
     * the specified descriptor.
     *
     * @param descriptor the node descriptor
     * @return the current object being edited, or <code>null</code> if its type
     *         doesn't matches the specified descriptor's archetype range
     */
    private static IMObject match(NodeDescriptor descriptor) {
        IMObject result = null;
        IMObject object = Context.getInstance().getCurrent();
        if (object != null) {
            for (String shortName : descriptor.getArchetypeRange()) {
                if (TypeHelper.matches(object.getArchetypeId(), shortName)) {
                    result = object;
                    break;
                }
            }
        }
        return result;
    }

}
