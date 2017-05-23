package org.labkey.filetransfer.view;

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

    public TransferView(FileTransferController.PrepareTransferForm form) throws IOException, URISyntaxException
    {
        super ("/org/labkey/filetransfer/view/fileTransfer.jsp");

        ViewContext context = getViewContext();
        FileTransferManager manager = FileTransferManager.get();

        FileTransferProvider provider = manager.getProvider(context);

        TransferBean bean = new TransferBean();

        TransferEndpoint sourceEndpoint = FileTransferManager.get().getSourceEndpoint(context);
//        sourceEndpoint = provider.getEndpoint(FileTransferManager.get().getSourceEndpointId(context.getContainer()));
        if (sourceEndpoint == null)
        {
            bean.setSource(new TransferEndpoint(FileTransferManager.get().getSourceEndpointId(context.getContainer()),
                FileTransferManager.get().getSourceEndpointDir(context)));
        }
        else
        {
            sourceEndpoint.setPath(FileTransferManager.get().getSourceEndpointDir(context));
            bean.setSource(sourceEndpoint);
        }

        bean.setBrowseEndpointsUrl(provider.getBrowseEndpointUrl(context.getContainer()));
        bean.setProviderName(provider.getName());
        bean.setAuthorized(form.getAuthorized());
        bean.setLabel(form.getLabel());
        bean.setReturnUrl(form.getReturnUrl());

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

        bean.setFileNames(FileTransferManager.get().getFileNames(getViewContext()));

        setModelBean(bean);
    }
}
