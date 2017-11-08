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

import org.apache.log4j.Logger;
import org.labkey.api.view.JspView;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.FileTransferController;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.model.TransferBean;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.provider.FileTransferProvider;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by susanh on 5/14/17.
 */
public class TransferView extends JspView<TransferBean>
{
    private static final Logger LOG = Logger.getLogger(FileTransferManager.class);

    public TransferView(FileTransferController.PrepareTransferForm form) throws IOException, URISyntaxException
    {
        super ("/org/labkey/filetransfer/view/fileTransfer.jsp");


        ViewContext context = getViewContext();
        FileTransferManager manager = FileTransferManager.get();
        FileTransferProvider provider = manager.getProvider(context);
        TransferBean bean = new TransferBean();

        bean.setSource(FileTransferManager.get().getSourceEndpoint(context));
        bean.setAuthorized(form.getAuthorized());
        bean.setLabel(form.getLabel());
        bean.setReturnUrl(form.getReturnUrl());
        bean.setErrorCode(form.getErrorCode());
        if (provider != null)
        {
            bean.setBrowseEndpointsUrl(provider.getBrowseEndpointUrl(manager.getContainer(context)));
            bean.setProviderName(provider.getName());

            if (form.getDestinationId() != null)
            {
                TransferEndpoint destinationEndpoint = provider.getEndpoint(form.getDestinationId());
                if (destinationEndpoint == null)
                    bean.setDestination(new TransferEndpoint(form.getDestinationId(), form.getPath()));
                else
                {
                    destinationEndpoint.setPath(form.getPath());
                    bean.setDestination(destinationEndpoint);
                }
            }
            else
            {
                TransferEndpoint destinationEndpoint = FileTransferManager.get().getDestinationEndpoint(context);
                if (destinationEndpoint != null)
                    bean.setDestination(destinationEndpoint);
            }
        }
        else
        {
            bean.setErrorCode(FileTransferManager.ErrorCode.noProvider);
        }

        bean.setFileNames(FileTransferManager.get().getFileNames(getViewContext()));

        setModelBean(bean);
    }
}
