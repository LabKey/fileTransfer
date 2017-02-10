package org.labkey.filetransfer.view;

import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.FileTransferController;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.query.FileTransferQuerySchema;

import java.util.List;

/**
 * Created by susanh on 2/7/17.
 */
public class FileTransferMetadataView extends JspView
{
    public FileTransferMetadataView(ViewContext context)
    {
        super("/org/labkey/filetransfer/view/fileList.jsp");
        setTitle("File Transfer");

        if (context.getContainer().hasPermission(context.getUser(), AdminPermission.class))
        {
            NavTree setUp = new NavTree("Set up", new ActionURL(FileTransferController.ConfigurationAction.class, context.getContainer()).toString(), null, "fa fa-pencil");
            setCustomize(setUp);
        }
        FileTransferManager manager = FileTransferManager.get();
        if (manager.isMetadataListConfigured(context.getContainer()))
        {
            List<String> activeFiles = FileTransferManager.get().getActiveFiles(context.getContainer());
            UserSchema schema = QueryService.get().getUserSchema(context.getUser(), context.getContainer(), FileTransferQuerySchema.NAME);
            QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, FileTransferQuerySchema.FILE_METADATA_TABLE_NAME);
            QueryView queryView = schema.createView(getViewContext(), settings, null);

            setView("metadataList", queryView);
        }
    }


}
