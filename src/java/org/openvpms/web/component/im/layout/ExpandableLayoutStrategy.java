package org.openvpms.web.component.im.layout;

import java.util.List;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.layout.RowLayoutData;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Layout strategy that adds a button to expand/collapse the layout.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ExpandableLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Determines if only required nodes should be shown.
     */
    private final boolean _showRequiredOnly;

    /**
     * Determines if the button should be included.
     */
    private final boolean _showButton;

    /**
     * Button indicating to expand/collapse the layout.
     */
    private Button _button;


    /**
     * Construct a new <code>ExpandableLayoutStrategy</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     */
    public ExpandableLayoutStrategy(boolean showOptional) {
        this(showOptional, true);
    }

    /**
     * Construct a new <code>ExpandableLayoutStrategy</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     */
    public ExpandableLayoutStrategy(boolean showOptional, boolean showButton) {
        _showRequiredOnly = !showOptional;
        _showButton = showButton;
    }

    /**
     * Returns the button to expand/collapse the layout.
     *
     * @return the layout button, or <code>null</code> if the layout cannot be
     *         expanded/collapsed
     */
    public Button getButton() {
        return _button;
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object    the object to lay out
     * @param container the container to use
     * @param context
     */
    @Override
    protected void doLayout(IMObject object, Component container,
                            LayoutContext context) {
        super.doLayout(object, container, context);
        if (_button == null && _showButton) {
            Row row = getButtonRow();
            ColumnLayoutData right = new ColumnLayoutData();
            right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.TOP));
            row.setLayoutData(right);
            container.add(row);
        }
    }

    /**
     * Lays out child components in a 2x2 grid.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param context
     */
    @Override
    protected void doSimpleLayout(IMObject object,
                                  List<NodeDescriptor> descriptors,
                                  Component container,
                                  LayoutContext context) {
        if (_button != null || !_showButton) {
            super.doSimpleLayout(object, descriptors, container, context);
        } else if (!descriptors.isEmpty()) {
            Row group = RowFactory.create();
            super.doSimpleLayout(object, descriptors, group, context);
            group.add(getButtonRow());
            container.add(group);
        }
    }

    /**
     * Lays out each child component in a group box.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param context
     */
    @Override
    protected void doComplexLayout(IMObject object,
                                   List<NodeDescriptor> descriptors,
                                   Component container,
                                   LayoutContext context) {
        if (_button == null && _showButton && !descriptors.isEmpty()) {
            Row row = getButtonRow();
            ColumnLayoutData right = new ColumnLayoutData();
            right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.TOP));
            row.setLayoutData(right);
            container.add(row);
        }
        super.doComplexLayout(object, descriptors, container, context);
    }

    /**
     * Returns a node filter.
     *
     * @param context the context
     * @return a node filter to filter nodes
     */
    protected NodeFilter getNodeFilter(LayoutContext context) {
        return new BasicNodeFilter(!_showRequiredOnly, false);
    }

    /**
     * Determines if the layout button should be shown.
     *
     * @return <code>true</code> if the layout button should be included;
     *         otherwise <code>false</code>
     */
    protected boolean showButton() {
        return _showButton;
    }

    /**
     * Creates a row with the layout button in the top right.
     *
     * @return a new row
     */
    protected Row getButtonRow() {
        String key = (_showRequiredOnly) ? "plus" : "minus";
        _button = ButtonFactory.create(key);
        RowLayoutData right = new RowLayoutData();
        right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.TOP));
        right.setWidth(new Extent(100, Extent.PERCENT));
        _button.setLayoutData(right);
        Row wrapper = new Row();
        wrapper.add(_button);
        wrapper.setLayoutData(right);
        return wrapper;
    }


}
