package org.labkey.filetransfer.view;

import org.labkey.api.view.JspView;
import org.labkey.api.view.Portal;

/**
 * Created by susanh on 5/9/17.
 */
public class FileTransferConfigView extends JspView<Portal.WebPart>
{
    public FileTransferConfigView(Portal.WebPart webPart)
    {
        super("/org/labkey/filetransfer/view/fileTransferConfig.jsp");
        setModelBean(webPart);
        setShowTitle(true);
    }
}

