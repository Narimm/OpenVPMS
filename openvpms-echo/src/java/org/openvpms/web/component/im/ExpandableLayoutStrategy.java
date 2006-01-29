package org.openvpms.web.component.im;

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
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.RowFactory;


/**
 * Layout strategy that adds a button to expand/collapse the layout.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ExpandableLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Button indicating to expand/collapse the layout.
     */
    private Button _button;


    /**
     * Construct a new <code>ExpandableLayoutStrategy</code>.
     *
     * @param showRequiredOnly
     */
    public ExpandableLayoutStrategy(boolean showRequiredOnly) {
        super(showRequiredOnly, false);
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
     * @param factory   the component factory
     */
    @Override
    protected void doLayout(IMObject object, Component container, IMObjectComponentFactory factory) {
        super.doLayout(object, container, factory);
        if (_button == null) {
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
     * @param factory     the component factory
     */
    @Override
    protected void doSimpleLayout(IMObject object, List<NodeDescriptor> descriptors,
                                  Component container, IMObjectComponentFactory factory) {
        Row group = RowFactory.create();

        super.doSimpleLayout(object, descriptors, group, factory);
        group.add(getButtonRow());
        container.add(group);
    }

    /**
     * Lays out each child component in a group box.
     *
     * @param object      the parent object
     * @param descriptors the child descriptors
     * @param container   the container to use
     * @param factory     the component factory
     */
    @Override
    protected void doComplexLayout(IMObject object, List<NodeDescriptor> descriptors,
                                   Component container, IMObjectComponentFactory factory) {
        if (_button == null) {
            Row row = getButtonRow();
            ColumnLayoutData right = new ColumnLayoutData();
            right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.TOP));
            row.setLayoutData(right);
            container.add(row);
            super.doComplexLayout(object, descriptors, container, factory);
        } else {
            super.doComplexLayout(object, descriptors, container, factory);
        }
    }

    /**
     * Creates a row with the layout button in the top right.
     *
     * @return a new row
     */
    protected Row getButtonRow() {
        String key = (showOptional()) ? "minus" : "plus";
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
