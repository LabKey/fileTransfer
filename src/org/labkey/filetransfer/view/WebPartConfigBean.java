package org.labkey.filetransfer.view;

import org.labkey.api.view.Portal;
import org.labkey.filetransfer.provider.FileTransferProvider;

/**
 * Created by susanh on 5/23/17.
 */
public class WebPartConfigBean
{
    private FileTransferProvider provider;
    private Portal.WebPart webPart;

    public Portal.WebPart getWebPart()
    {
        return webPart;
    }

    public void setWebPart(Portal.WebPart webPart)
    {
        this.webPart = webPart;
    }

    public FileTransferProvider getProvider()
    {
        return provider;
    }

    public void setProvider(FileTransferProvider provider)
    {
        this.provider = provider;
    }
}
