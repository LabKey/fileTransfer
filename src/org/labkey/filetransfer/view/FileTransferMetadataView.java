package org.labkey.filetransfer.view;

import org.labkey.api.data.Container;
import org.labkey.api.view.JspView;

/**
 * Created by susanh on 2/7/17.
 */
public class FileTransferMetadataView extends JspView
{
    public FileTransferMetadataView(Container container)
    {
        super("/org/labkey/filetransfer/view/fileList.jsp");
    }
}
