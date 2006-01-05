package org.openvpms.web.app.browser;


import java.util.List;
import java.util.StringTokenizer;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.app.OpenVPMSApp;
import org.openvpms.web.app.editor.IMObjectEditor;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.EditWindowPane;
import org.openvpms.web.component.IMObjectTable;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.SelectFieldFactory;
import org.openvpms.web.component.TextComponentFactory;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class Browser extends Column {

    /**
     * The search name.
     */
    private String _searchName;

    /**
     * The range to search.
     */
    private String _archetypeRange;

    /**
     * The archetype name.
     */
    private String _archetypeName;

    /**
     * Indicates to search all archetypes.
     */
    private static final String ALL = "All";

    private IMObjectTable _table;

    public Browser(String range) {
        setStyleName("Editor");
        setArchetypeRange(range);
        doLayout();
    }

    public void setArchetypeRange(String range) {
        _archetypeRange = range;
    }

    public String getArchetypeRange() {
        return _archetypeRange;
    }

    public void setSearchName(String name) {
        _searchName = name;
    }

    public String getSearchName() {
        return _searchName;
    }

    public void setArchetypeName(String name) {
        _archetypeName = name;
    }

    public String getArchetypeName() {
        return _archetypeName;
    }

    protected void doLayout() {
        // set up the type select field
        List<String> shortNames = getShortNames();
        final SelectField type = SelectFieldFactory.create(shortNames.toArray());
        type.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setArchetypeName((String) type.getSelectedItem());
            }
        });

        int index = shortNames.indexOf(getArchetypeName());
        if (index != -1) {
            type.setSelectedIndex(index);
        }

        // name text field
        TextField name = TextComponentFactory.create();

        // create button
        Button create = ButtonFactory.create("new", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onNew();
            }
        });

        // query button
        Button query = ButtonFactory.create("query", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                query();
            }
        });

        Label typeLabel = LabelFactory.create("type");
        Label nameLabel = LabelFactory.create("name");
        Row row = RowFactory.create(typeLabel, type, nameLabel, name, query, create);
        row.setStyleName("Editor.ControlRow");

        _table = new IMObjectTable();
        _table.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onEdit();
            }
        });
        add(row);
        add(_table);

        query();
    }

    protected void onNew() {
        if (_archetypeName != null && !_archetypeName.equalsIgnoreCase(ALL)) {
            IMObject object = (IMObject) getArchetypeService().create(
                    _archetypeName);
            edit(object);
        }
    }

    protected void onEdit() {
        IMObject selected = _table.getSelected();
        if (selected != null) {
            edit(selected);
        }
    }

    protected void edit(IMObject object) {
        IMObjectEditor editor = new IMObjectEditor(object);
        new EditWindowPane(editor);
    }

    protected List<String> getShortNames() {
        ArchetypeRange range = ArchetypeRange.parse(getArchetypeRange());
        IArchetypeService service = getArchetypeService();
        List<String> names = service.getArchetypeShortNames(
                range.getRefModelName(), range.getEntityName(),
                range.getConceptName(), true);
        names.add(0, ALL);
        return names;
    }

    protected void query() {
        ArchetypeRange range = ArchetypeRange.parse(getArchetypeRange());
        String name = getSearchName();
        if (!StringUtils.isEmpty(name)) {
            name += "*";
        }
        String type = getArchetypeName();

        if (type == null) {
            type = ALL;
        }
        if (!type.equalsIgnoreCase(ALL)) {
            range = ArchetypeRange.parse(range.getRefModelName(), type);
        }

        IArchetypeService service = getArchetypeService();
        List<IMObject> result = service.get(range.getRefModelName(),
                range.getEntityName(),
                range.getConceptName(),
                name, true, true);

        _table.setObjects(result);
    }


    protected IArchetypeService getArchetypeService() {
        return (IArchetypeService) OpenVPMSApp.getInstance().getApplicationContext().getBean(
                "archetypeService");
    }

    private static class ArchetypeRange {

        private final String _refModelName;

        private final String _entityName;

        private final String _conceptName;

        public ArchetypeRange(String refModelName, String entityName,
                              String conceptName) {
            _refModelName = refModelName;
            _entityName = entityName;
            _conceptName = conceptName;
        }

        public static ArchetypeRange parse(String range) {
            if (range == null) {
                range = "";
            }
            StringTokenizer t = new StringTokenizer(range, ".");
            String refModelName = t.hasMoreTokens() ? t.nextToken() : null;
            String entityName = t.hasMoreTokens() ? t.nextToken() : null;
            String conceptName = t.hasMoreTokens() ? t.nextToken() : null;
            return new ArchetypeRange(refModelName, entityName, conceptName);
        }

        public static ArchetypeRange parse(String refModelName, String type) {
            if (type == null) {
                type = "";
            }
            StringTokenizer t = new StringTokenizer(type, ".");
            String entityName = t.hasMoreTokens() ? t.nextToken() : null;
            String conceptName = t.hasMoreTokens() ? t.nextToken() : null;
            return new ArchetypeRange(refModelName, entityName, conceptName);
        }

        public String getRefModelName() {
            return _refModelName;
        }

        public String getEntityName() {
            return _entityName;
        }

        public String getConceptName() {
            return _conceptName;
        }

    }

}
