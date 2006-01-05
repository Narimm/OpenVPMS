package org.openvpms.web.app.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.jxpath.Pointer;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.EditWindowPane;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.SelectFieldFactory;
import org.openvpms.web.component.IMObjectTable;
import org.openvpms.web.app.OpenVPMSApp;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class CollectionEditor extends Column {

    /**
     * The object to edit.
     */
    private final IMObject _object;

    /**
     * Collection to edit.
     */
    private IMObjectTable _table;

    /**
     * The node descriptor.
     */
    private final NodeDescriptor _descriptor;

    /**
     * The archetype name combo.
     */
    private SelectField _archetypeNames;

    /**
     * Construct a new <code>CollectionEditor</code>.
     *
     * @param descriptor the node descriptor
     */
    public CollectionEditor(IMObject object, NodeDescriptor descriptor) {
        _object = object;
        _descriptor = descriptor;
        setStyleName("Editor");
    }

    public void init() {
        super.init();
        doLayout();
    }

    protected void doLayout() {
        Button create = ButtonFactory.create("new", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onNew();
            }
        });

        Button delete = ButtonFactory.create("delete", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });
        _archetypeNames = SelectFieldFactory.create(_descriptor.getArchetypeRange());

        Row row = RowFactory.create(create, delete, _archetypeNames);
        row.setStyleName("Editor.ControlRow");
        add(row);
        Object object = _descriptor.getValue(_object);
        Collection values = null;
        if (object instanceof Collection) {
            values = (Collection) object;
        } else if (object instanceof Map) {
            values = ((Map) object).values();
        } else if (object instanceof PropertyList) {
            values = ((PropertyList) object).values();
        }
        if (values != null) {
            _table = new IMObjectTable();
            _table.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onEdit();
                }
            });

            List<IMObject> objects = new ArrayList<IMObject>();
            for (Object value : values) {
                objects.add((IMObject) value);
            }
            _table.setObjects(objects);
            add(_table);
        }
    }

    protected void onNew() {
        String name = (String) _archetypeNames.getSelectedItem();
        if (name != null) {
            IArchetypeService service = getArchetypeService();
            Object object = service.create(name);
            Pointer pointer = _object.pathToObject(_descriptor.getPath());
            if (object instanceof IMObject) {
                IMObjectEditor editor = new IMObjectEditor((IMObject) object, _object, _descriptor);
                new EditWindowPane(editor);
            }
        }
    }

    protected void onDelete() {

    }

    protected void onEdit() {
        IMObject object = _table.getSelected();
        if (object != null) {
            IMObjectEditor editor = new IMObjectEditor(object);
            new EditWindowPane(editor);
        }
    }

    protected IArchetypeService getArchetypeService() {
        return (IArchetypeService) OpenVPMSApp.getInstance().getApplicationContext().getBean(
                "archetypeService");
    }

    protected ILookupService getLookupService() {
        return (ILookupService) OpenVPMSApp.getInstance().getApplicationContext().getBean(
                "lookupService");
    }

}
