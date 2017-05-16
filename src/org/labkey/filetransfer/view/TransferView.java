package org.labkey.filetransfer.view;

import org.labkey.api.view.JspView;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.model.TransferBean;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.provider.GlobusFileTransferProvider;

/**
 * Created by susanh on 5/14/17.
 */
public class TransferView extends JspView<TransferBean>
{

    public TransferView(Boolean authorized, String destinationEndpointId, String destinationPath)
    {
        super ("/org/labkey/filetransfer/view/fileTransfer.jsp");

        ViewContext context = getViewContext();
//        TransferEndpoint sourceEndpoint = null;
//        try
//        {
//            sourceEndpoint = OAuth2Authenticator.getEndpoint(FileTransferManager.get().getSourceEndpointId(container));
//            sourceEndpoint.setPath(FileTransferManager.get().getSourceEndpointDir(container));
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
        TransferBean bean = new TransferBean();
        bean.setBrowseEndpointUrl(GlobusFileTransferProvider.getBrowseEndpointUrl(context.getContainer()));
        bean.setProviderName("Globus");
        bean.setAuthorized(authorized);

        bean.setSource(new TransferEndpoint(FileTransferManager.get().getSourceEndpointId(context.getContainer()),
                FileTransferManager.get().getSourceEndpointDir(context.getContainer())));
//        bean.setSource(sourceEndpoint);
        if (destinationEndpointId != null)
            bean.setDestination(new TransferEndpoint(destinationEndpointId, destinationPath));

        bean.setFileNames(FileTransferManager.get().getFileNames(getViewContext()));
        setModelBean(bean);
    }
}
