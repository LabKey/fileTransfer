/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.filetransfer.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.labkey.api.view.JspView;
import org.labkey.api.view.Portal;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.globus.GlobusFileTransferProvider;

import java.io.IOException;

/**
 * Created by susanh on 5/9/17.
 */
public class FileTransferWebPartConfigView extends JspView<WebPartConfigBean>
{
    private static final Logger logger = LogManager.getLogger(FileTransferWebPartConfigView.class);
    public FileTransferWebPartConfigView(Portal.WebPart webPart, ViewContext context)
    {
        super("/org/labkey/filetransfer/view/fileTransferWebPartConfig.jsp");
        WebPartConfigBean bean = new WebPartConfigBean();
        bean.setWebPart(webPart);
        try
        {
            bean.setProvider(new GlobusFileTransferProvider(context.getContainer(), context.getUser()));
        }
        catch (IOException e)
        {
            logger.error("Unable to instantiate provider", e);
        }
        setModelBean(bean);
        setShowTitle(true);
    }
}

