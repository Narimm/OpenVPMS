package org.openvpms.web.component.im.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import echopointng.GroupBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.table.TableNavigator;


/**
 * Read-only viewer for a collection of {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class CollectionViewer extends Column {

    /**
     * The object to view.
     */
    private final IMObject _object;

    /**
     * Collection to browse.
     */
    private IMObjectTable _table;

    /**
     * The node descriptor.
     */
    private final NodeDescriptor _descriptor;

    /**
     * The layout context.
     */
    private final LayoutContext _context;

    /**
     * Box to display child objects in.
     */
    private GroupBox _box;


    /**
     * Construct a new <code>CollectionViewer</code>.
     *
     * @param object     the object being viewed
     * @param descriptor the node descriptor
     */
    public CollectionViewer(IMObject object, NodeDescriptor descriptor) {
        setStyleName("WideCellSpacing");

        _context = new DefaultLayoutContext();

        // filter out the uid (aka "id") field
        NodeFilter idFilter = new NamedNodeFilter("uid");
        NodeFilter filter = FilterHelper.chain(
                idFilter, _context.getDefaultNodeFilter());
        _context.setNodeFilter(filter);

        _object = object;
        _descriptor = descriptor;
        doLayout();
    }

    /**
     * Lays out the component.
     */
    protected void doLayout() {
        IMObjectTableModel model
                = IMObjectTableModelFactory.create(_descriptor);
        _table = new IMObjectTable(model);
        _table.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBrowse();
            }
        });
        add(_table);

        populate();
    }

    /**
     * Populates the table.
     */
    protected void populate() {
        Collection values = (Collection) _descriptor.getValue(_object);
        int size = (values != null) ? values.size() : 0;
        if (size != 0) {
            List<IMObject> objects = new ArrayList<IMObject>();
            for (Object value : values) {
                objects.add((IMObject) value);
            }
            _table.setObjects(objects);

            int rowsPerPage = _table.getRowsPerPage();
            if (size > rowsPerPage) {
                // display the navigator before the table
                TableNavigator navigator = new TableNavigator(_table);
                add(navigator);
            }
            add(_table);
        } else {
            _table.setObjects(new ArrayList<IMObject>());
        }
    }

    /**
     * Browses the selected object.
     */
    protected void onBrowse() {
        IMObject object = _table.getSelected();
        if (object != null) {
            browse(object);
        }
    }

    /**
     * Browse an object.
     *
     * @param object the object to browse.
     */
    protected void browse(IMObject object) {
        if (_box == null) {
            _box = new GroupBox();
        } else {
            _box.removeAll();
            // workaround for a bug in EPNG
            remove(_box);
        }
        add(_box);
        IMObjectViewer viewer = new IMObjectViewer(object, _context);
        _box.setTitle(viewer.getTitle());
        _box.add(viewer.getComponent());
    }

}
