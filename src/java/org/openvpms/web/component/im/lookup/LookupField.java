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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.lookup;

import nextapp.echo2.app.SelectField;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;

import java.util.List;


/**
 * Field to display {@link Lookup}s in a dropdown list.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupField extends SelectField {

    /**
     * Creates a new <tt>LookupField</tt> that displays a list of lookups.
     *
     * @param lookups the lookups to display
     * @param all     if <tt>true</tt>, add a localised "All"
     */
    public LookupField(List<Lookup> lookups, boolean all) {
        this(new LookupListModel(new ListLookupQuery(lookups), all));
    }

    /**
     * Creates a new <tt>LookupField</tt>.
     *
     * @param source the lookup source
     */
    public LookupField(LookupQuery source) {
        this(source, false);
    }

    /**
     * Creates a new <tt>LookupField</tt>.
     *
     * @param source the lookup source
     * @param all    if <tt>true</tt>, add a localised "All"
     */
    public LookupField(LookupQuery source, boolean all) {
        this(source, all, false);
    }

    /**
     * Creates a new <tt>LookupField</tt>.
     *
     * @param source the lookup source
     * @param all    if <tt>true</tt>, add a localised "All"
     * @param none   if <tt>true</tt>, add a localised "None"
     */
    public LookupField(LookupQuery source, boolean all, boolean none) {
        this(new LookupListModel(source, all, none));
    }

    /**
     * Creates a new <tt>LookupField</tt> that displays a list of lookups.
     *
     * @param model the list model
     */
    public LookupField(LookupListModel model) {
        super(model);
        setCellRenderer(LookupListCellRenderer.INSTANCE);
    }

    /**
     * Returns the selected lookup's code.
     *
     * @return the selected lookup's code, or <tt>null</tt> if no lookup is
     *         selected, or the selected entry is 'All' or 'None'
     */
    public String getSelectedCode() {
        Lookup selected = getSelected();
        return (selected != null) ? selected.getCode() : null;
    }

    /**
     * Returns the selected lookup.
     *
     * @return the selected lookup, or <tt>null</tt> if no lookup is selected.
     */
    public Lookup getSelected() {
        int index = getSelectedIndex();
        if (index != -1) {
            LookupListModel model = getModel();
            return model.getLookup(index);
        }
        return null;
    }

    /**
     * Sets the selected lookup.
     *
     * @param lookup the lookup. May be <tt>null</tt>
     */
    public void setSelected(Lookup lookup) {
        String code = (lookup != null) ? lookup.getCode() : null;
        setSelected(code);
    }

    /**
     * Sets the selected lookup.
     *
     * @param code the lookup code. May be <tt>null</tt>
     */
    public void setSelected(String code) {
        LookupListModel model = getModel();
        setSelectedIndex(model.indexOf(code));
    }

    /**
     * Returns the model.
     *
     * @return the model
     */
    @Override
    public LookupListModel getModel() {
        return (LookupListModel) super.getModel();
    }

    /**
     * Refreshes the model if required.
     * <p/>
     * If the model refreshes and selected lookup is different in the new model, the selection will be cleared,
     * and if possible, set to 'none'.
     *
     * @return <tt>true</tt> if the model refreshed
     */
    public boolean refresh() {
        LookupListModel model = getModel();
        Lookup selected = getSelected();
        boolean refreshed = model.refresh();
        if (refreshed) {
            if (getSelectedIndex() >= model.size() || !ObjectUtils.equals(selected, getSelected())) {
                getSelectionModel().clearSelection();
                int none = model.getNoneIndex();
                if (none != -1) {
                    setSelectedIndex(none);
                }
            }
        }
        return refreshed;
    }
}
