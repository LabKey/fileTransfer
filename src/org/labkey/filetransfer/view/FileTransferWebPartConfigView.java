package org.labkey.filetransfer.view;

import org.apache.log4j.Logger;
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
    private static final Logger logger = Logger.getLogger(FileTransferWebPartConfigView.class);
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

