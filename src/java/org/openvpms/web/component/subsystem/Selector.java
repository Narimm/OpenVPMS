package org.openvpms.web.component.subsystem;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.util.Messages;


/**
 * Component that provides a 'select' button and object summary.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class Selector {

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
     * The component.
     */
    private Component _component;

    /**
     * Construct a new <code>Selector</code>.
     */
    public Selector() {
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
        _select = ButtonFactory.create("select");
        _summary = LabelFactory.create();
        _deactivated = LabelFactory.create(null, "Selector.Deactivated");

        _component = RowFactory.create("Selector.ControlRow", _select, _summary,
                _deactivated);
    }

}
