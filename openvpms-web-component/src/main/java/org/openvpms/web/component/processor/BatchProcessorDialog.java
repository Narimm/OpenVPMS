/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.processor;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.web.component.dialog.PopupDialog;


/**
 * Dialog to display an {@link ProgressBarProcessor}.
 *
 * @author Tim Anderson
 */
public class BatchProcessorDialog extends PopupDialog {

    /**
     * The processor.
     */
    private final ProgressBarProcessor processor;


    /**
     * Constructs a {@code BatchProcessorDialog}.
     *
     * @param title     the dialog title
     * @param processor the processor
     */
    public BatchProcessorDialog(String title, ProgressBarProcessor processor) {
        super(title, CANCEL);
        setModal(true);
        Grid grid = new Grid();
        grid.setWidth(new Extent(100, Extent.PERCENT));
        grid.setHeight(new Extent(100, Extent.PERCENT));
        Component content = processor.getComponent();
        GridLayoutData data = new GridLayoutData();
        data.setAlignment(Alignment.ALIGN_CENTER);
        content.setLayoutData(data);
        grid.add(content);
        getLayout().add(grid);
        this.processor = processor;
    }

    /**
     * Shows the dialog, and starts the processor.
     */
    public void show() {
        super.show();
        processor.process();
    }

    /**
     * Cancels the operation.
     * <p/>
     * This implementation cancels the processor.
     */
    @Override
    protected void doCancel() {
        processor.cancel();
        super.doCancel();
    }
}
