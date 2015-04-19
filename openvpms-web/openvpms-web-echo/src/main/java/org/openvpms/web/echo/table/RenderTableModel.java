/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.table;

import nextapp.echo2.app.table.TableModel;

/**
 * A table model that is notified prior to the table being rendered, and again when the render is complete.
 * <p/>
 * This enables caching to be used whilst the rendering is in progress.
 *
 * @author Tim Anderson
 */
public interface RenderTableModel extends TableModel {

    /**
     * Invoked prior to the table being rendered.
     */
    void preRender();

    /**
     * Invoked after the table has been rendered.
     */
    void postRender();

}
