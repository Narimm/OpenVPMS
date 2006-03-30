package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.LabelFactory;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components for
 * display in a table.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class TableComponentFactory extends AbstractReadOnlyComponentFactory {

    /**
     * Construct a new <code>TableComponentFactory</code>.
     *
     * @param context the layout context.
     */
    public TableComponentFactory(LayoutContext context) {
        super(context);
    }

    /**
     * Returns a component to display a lookup.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the lookup
     */
    protected Component getLookup(IMObject context, NodeDescriptor descriptor) {
        return getLabel(context, descriptor);
    }

    /**
     * Returns a component to display a number.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    protected Component getNumber(IMObject context, NodeDescriptor descriptor) {
        String value = getNumericValue(context, descriptor);
        Label label = LabelFactory.create();
        label.setText(value);
        TableLayoutData layout = new TableLayoutData();
        Alignment right = new Alignment(Alignment.RIGHT,
                                        Alignment.DEFAULT);
        layout.setAlignment(right);
        label.setLayoutData(layout);
        return label;
    }

    /**
     * Returns a component to display a date.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    protected Component getDate(IMObject context, NodeDescriptor descriptor) {
        String value = getDateValue(context, descriptor);
        Label label = LabelFactory.create();
        label.setText(value);
        return label;
    }

}
