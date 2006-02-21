package org.openvpms.web.component.im.select;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Component that provides a 'select' button and object summary.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class Selector {

    /**
     * Determines the layout of the 'select' button.
     */
    public enum ButtonStyle {LEFT, RIGHT, HIDE};

    /**
     * The 'select' button.
     */
    private Button _select;

    /**
     * Selected object's summary.
     */
    private Label _summary;

    /**
     * Deactivated label.
     */
    private Label _deactivated;

    /**
     * Determines the layout of the 'select' button.
     */
    private ButtonStyle _buttonStyle;

    /**
     * The component.
     */
    private Component _component;


    /**
     * Construct a new <code>Selector</code>.
     */
    public Selector() {
        this(ButtonStyle.LEFT);
    }

    /**
     * Construct a new <code>Selector</code>.
     *
     * @param style determines the layout of the 'select' button
     */
    public Selector(ButtonStyle style) {
        _buttonStyle = style;
    }


    /**
     * Returns the selector component.
     *
     * @return the selector component
     */
    public Component getComponent() {
        if (_component == null) {
            doLayout();
        }
        return _component;
    }

    /**
     * Returns the 'select' button.
     *
     * @return the 'select' button
     */
    public Button getSelect() {
        getComponent();
        return _select;
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(IMObject object) {
        if (object != null) {
            final String summaryKey = "imobject.summary";
            String summary = Messages.get(summaryKey, object.getName(),
                    object.getDescription());
            _summary.setText(summary);
            if (!object.isActive()) {
                _deactivated.setText(Messages.get("imobject.deactivated"));
            } else {
                _deactivated.setText(null);
            }
        } else {
            _summary.setText(null);
            _deactivated.setText(null);
        }
    }

    /**
     * Create the component.
     */
    protected void doLayout() {
        _component = RowFactory.create("Selector.ControlRow");
        doLayout(_component);
    }

    /**
     * Lay out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        container.add(getSummary());
        container.add(getDeactivated());
        if (_buttonStyle == ButtonStyle.LEFT) {
            container.add(getButton(), 0);
        } else if (_buttonStyle == ButtonStyle.RIGHT) {
            container.add(getButton());
        }
    }

    /**
     * Returns the 'select' button, creating it if needed.
     *
     * @return the select button
     */
    protected Button getButton() {
        if (_select == null) {
            _select = ButtonFactory.create("select");
        }
        return _select;
    }

    /**
     * Returns the summary label, creating it if needed.
     *
     * @return the summary label
     */
    protected Label getSummary() {
        if (_summary == null) {
            _summary = LabelFactory.create();
        }
        return _summary;
    }

    /**
     * Returns the 'deactivated' label, creating it if needed.
     *
     * @return the deactivated label
     */
    protected Label getDeactivated() {
        if (_deactivated == null) {
            _deactivated = LabelFactory.create(null, "Selector.Deactivated");
        }
        return _deactivated;
    }

}
