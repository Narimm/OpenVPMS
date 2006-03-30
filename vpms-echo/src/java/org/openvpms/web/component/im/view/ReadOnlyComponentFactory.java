package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.text.TextComponent;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ReadOnlyComponentFactory extends AbstractReadOnlyComponentFactory {

    /**
     * Construct a new <code>ReadOnlyComponentFactory</code>.
     *
     * @param context the layout context.
     */
    public ReadOnlyComponentFactory(LayoutContext context) {
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
        return getTextComponent(context, descriptor);
    }

    /**
     * Returns a component to display a number.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    protected Component getNumber(IMObject context, NodeDescriptor descriptor) {
        final int columns = 10; // @todo. should determine from descriptor.
        TextComponent text = TextComponentFactory.create(columns);
        text.setAlignment(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
        text.setText(getNumericValue(context, descriptor));
        return text;
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
        int columns = (value != null) ? value.length() : 10;
        TextComponent text = TextComponentFactory.create(columns);
        text.setText(value);
        return text;
    }

}
