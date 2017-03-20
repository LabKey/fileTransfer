package org.labkey.filetransfer.view;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ActionButton;
import org.labkey.api.data.ButtonBar;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.util.StringExpression;
import org.labkey.api.util.StringExpressionFactory;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.DataView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.FileTransferController;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.query.FileTransferQuerySchema;
import org.springframework.validation.Errors;

/**
 * Created by susanh on 2/7/17.
 */
public class FileTransferMetadataView extends JspView
{
    public FileTransferMetadataView(ViewContext context)
    {
        super("/org/labkey/filetransfer/view/fileList.jsp");
        setTitle("File Transfer");

        if (context.getUser().isSiteAdmin())
        {
            NavTree setUp = new NavTree("Customize", new ActionURL(FileTransferController.ConfigurationAction.class, context.getContainer()).toString(), null, "fa fa-pencil");
            setCustomize(setUp);
        }
        FileTransferManager manager = FileTransferManager.get();
        if (manager.isMetadataListConfigured(context.getContainer()))
        {
            UserSchema schema = QueryService.get().getUserSchema(context.getUser(), context.getContainer(), FileTransferQuerySchema.NAME);
            QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, FileTransferQuerySchema.FILE_METADATA_TABLE_NAME);
            FileTransferMetadataQueryView view = new FileTransferMetadataQueryView(schema, settings, null);
            setView("metadataList", view);
        }
    }

    private class FileTransferMetadataQueryView extends QueryView
    {
        public FileTransferMetadataQueryView(UserSchema schema, QuerySettings settings, @Nullable Errors errors)
        {
            super(schema, settings, errors);
        }

        @Override
        protected void populateButtonBar(DataView view, ButtonBar bar)
        {
            super.populateButtonBar(view, bar);

            String transferLinkUrl = FileTransferManager.get().getGlobusGenomicsTransferUrl(view.getViewContext().getContainer());
            if (transferLinkUrl != null)
            {
                StringExpression url = StringExpressionFactory.createURL(transferLinkUrl);
                if (url != null)
                    bar.add(new ActionButton("Open Transfer Link", url));
            }
        }
    }
}
