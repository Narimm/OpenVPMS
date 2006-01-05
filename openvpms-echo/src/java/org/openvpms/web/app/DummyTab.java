package org.openvpms.web.app;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;

import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.Styles;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class DummyTab extends Column {

    public void DummyTab() {
        setStyleName(Styles.DEFAULT);
        Label label = LabelFactory.create("placeholder");
        add(label);
    }

}
