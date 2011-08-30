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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.admin.style;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.style.StylePropertyEvaluator;
import org.openvpms.web.component.style.UserStyleSheets;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * An editor for properties used in a stylesheet.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StyleEditor extends PopupDialog {

    /**
     * The properties being edited.
     */
    private Map<String, String> properties;

    /**
     * The fields corresponding to the properties.
     */
    private List<Property> fields = new ArrayList<Property>();

    /**
     * The style sheets.
     */
    private final UserStyleSheets styles;

    /**
     * The resolution width that the properties apply to.
     */
    private SimpleProperty width;

    /**
     * The resolution height that the properties apply to.
     */
    private SimpleProperty height;

    /**
     * The screen resolution.
     */
    private Dimension size;

    /**
     * Determines if the screen resolution can be edited.
     */
    private final boolean editSize;


    /**
     * Constructs a <tt>StyleEditor</tt>
     *
     * @param size       the screen resolution
     * @param properties the properties to edit
     * @param styles     the style sheets
     * @param editSize   if <tt>true</tt> add editors for the size
     */
    public StyleEditor(Dimension size, Map<String, String> properties, UserStyleSheets styles,
                       boolean editSize) {
        super(Messages.get("stylesheet.edit.title"), "EditDialog", OK_CANCEL);
        setModal(true);
        this.size = size;
        this.properties = new LinkedHashMap<String, String>(properties);
        this.editSize = editSize;
        this.styles = styles;
        if (editSize) {
            width = new SimpleProperty("width", size.width, Integer.class);
            height = new SimpleProperty("height", size.height, Integer.class);
            width.setValue(size.width);
            height.setValue(size.height);
        }
    }

    /**
     * Returns the properties.
     *
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Returns the screen resolution that the properties apply to.
     *
     * @return the screen resolution, or <tt>null</tt> if it is invalid
     */
    public Dimension getSize() {
        Dimension result = null;
        if (editSize) {
            if (width.isValid() && height.isValid()) {
                result = new Dimension((Integer) width.getValue(), (Integer) height.getValue());
            }
        } else {
            result = size;
        }
        return result;
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Grid grid = GridFactory.create(2);
        if (editSize) {
            StyleHelper.addProperty(grid, width);
            StyleHelper.addProperty(grid, height);
        } else {
            grid.add(LabelFactory.create("stylesheet.resolution"));
            if (StyleHelper.ANY_RESOLUTION.equals(size)) {
                grid.add(LabelFactory.create("stylesheet.anyresolution"));
            } else {
                Label label = LabelFactory.create();
                label.setText(Messages.get("stylesheet.size", size.width, size.height));
                grid.add(label);
            }
        }
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            SimpleProperty property = new SimpleProperty(entry.getKey(), entry.getValue(), String.class);
            property.setDisplayName(property.getName()); // avoid case changes
            StyleHelper.addProperty(grid, property);
            fields.add(property);
        }
        getLayout().add(grid);
    }

    /**
     * Invoked when the 'OK' button is pressed.
     */
    @Override
    protected void onOK() {
        Dimension size = getSize();
        if (size != null) {
            Map<String, String> edited = new HashMap<String, String>();
            for (Property field : fields) {
                String value = (field.getValue() != null) ? field.getValue().toString() : null;
                edited.put(field.getName(), value);
            }
            StylePropertyEvaluator template = new StylePropertyEvaluator(styles.getDefaultProperties());
            try {
                Dimension test = size.equals(StyleHelper.ANY_RESOLUTION) ? StyleHelper.DEFAULT_RESOLUTION : size;
                template.getProperties(test.width, test.height, edited);
                properties = edited;
                super.onOK();
            } catch (Throwable exception) {
                ErrorHelper.show(exception);
            }
        } else {
            ErrorDialog.show(Messages.get("stylesheet.invalidresolution", width.getValue(), height.getValue()));
        }
    }
}