package org.openvpms.web.app.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.apache.commons.jxpath.Pointer;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.IMObjectTable;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.SelectFieldFactory;
import org.openvpms.web.component.TableNavigator;
import org.openvpms.web.component.edit.EditWindowPane;
import org.openvpms.web.component.model.ArchetypeShortNameListModel;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Editor for a collection of {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 * @see IMObjectEditor
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
     * Table navigator.
     */
    private TableNavigator _navigator;

    /**
     * The node descriptor.
     */
    private final NodeDescriptor _descriptor;

    /**
     * The archetype short name used to create a new object.
     */
    private String _shortname;


    /**
     * Construct a new <code>CollectionEditor</code>.
     *
     * @param descriptor the node descriptor
     */
    public CollectionEditor(IMObject object, NodeDescriptor descriptor) {
        _object = object;
        _descriptor = descriptor;
        doLayout();
    }

    /**
     * Lays out the component.
     */
    protected void doLayout() {
        setStyleName("Editor");

        Button delete = ButtonFactory.create("delete", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });

        Button create = ButtonFactory.create("new", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onNew();
            }
        });

        Row row = RowFactory.create(delete, create);
        row.setStyleName("Editor.ControlRow");

        String[] range = _descriptor.getArchetypeRange();
        if (range.length == 1) {
            _shortname = range[0];
        } else if (range.length > 1) {
            final ArchetypeShortNameListModel model
                    = new ArchetypeShortNameListModel(range);
            final SelectField archetypeNames = SelectFieldFactory.create(range);
            _shortname = (String) archetypeNames.getSelectedItem();

            archetypeNames.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int index = archetypeNames.getSelectedIndex();
                    if (index != -1) {
                        _shortname = model.getShortName(index);
                    }
                }
            });
            row.add(archetypeNames);
        }

        add(row);
        populate();
    }

    /**
     * Populates the table.
     */
    protected void populate() {
        Collection values = getValues();
        int size = values.size();
        if (size != 0) {
            if (_table == null) {
                boolean deletable = false;
                if (_descriptor.isParentChild()) {
                    deletable = true;
                }
                _table = new IMObjectTable(deletable);
                _table.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        onEdit();
                    }
                });
                add(_table);
            }

            List<IMObject> objects = new ArrayList<IMObject>();
            for (Object value : values) {
                objects.add((IMObject) value);
            }
            _table.setObjects(objects);

            int rowsPerPage = _table.getRowsPerPage();
            if (_navigator == null && size > rowsPerPage) {
                // display the navigator before the table
                _navigator = new TableNavigator(_table);
                add(_navigator, indexOf(_table));
            } else if (_navigator != null && size <= rowsPerPage) {
                remove(_navigator);
            }
        } else if (_table != null) {
            _table.setObjects(new ArrayList<IMObject>());
            if (_navigator != null) {
                remove(_navigator);
            }
        }
    }

    /**
     * Invoked when the "New" button is pressed. Creates a new instance of the
     * selected archetype, and pops up an editor for it.
     */
    protected void onNew() {
        if (_shortname != null) {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            IMObject object = (IMObject) service.create(_shortname);
            IMObjectEditor editor = new IMObjectEditor((IMObject) object,
                    _object, _descriptor);
            edit(editor);
        }
    }

    /**
     * Deletes the selected object.
     */
    protected void onDelete() {
        List<IMObject> marked = _table.getMarked();
        if (!marked.isEmpty()) {
            Collection values = getValues();
            values.removeAll(marked);

/*
            for (IMObject child : marked) {
                _descriptor.removeChildFromCollection(_object, child);
            }
*/
            populate();
        }
    }

    /**
     * Edits the selected object.
     */
    protected void onEdit() {
        IMObject object = _table.getSelected();
        if (object != null) {
            IMObjectEditor editor
                    = new IMObjectEditor(object, _object, _descriptor);
            edit(editor);
        }
    }

    /**
     * Edit an object. This pops up a window containing the editor.
     *
     * @param editor the editor
     */
    protected void edit(final IMObjectEditor editor) {
        EditWindowPane pane = new EditWindowPane(editor);
        pane.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                onEditCompleted(editor);
            }
        });
    }

    /**
     * Invoked when an editor is closed. This populates the table if the edited
     * object changed.
     *
     * @param editor the editor
     */
    private void onEditCompleted(IMObjectEditor editor) {
        if (editor.isModified() || editor.isDeleted()) {
            populate();
        }
    }

    /**
     * Helper to return the underlying collection.
     *
     * @return the underlying collection
     */
    private Collection getValues() {
        Pointer pointer = _object.pathToCollection(_descriptor.getPath());
        return (Collection) pointer.getValue();
    }
}
