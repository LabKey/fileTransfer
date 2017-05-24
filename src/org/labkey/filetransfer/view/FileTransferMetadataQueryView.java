package org.labkey.filetransfer.view;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ActionButton;
import org.labkey.api.data.ButtonBar;
import org.labkey.api.data.DataRegionSelection;
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.list.ListDefinition;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.util.StringExpression;
import org.labkey.api.util.StringExpressionFactory;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.DataView;
import org.labkey.api.view.Portal;
import org.labkey.filetransfer.FileTransferController;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.provider.FileTransferProvider;
import org.labkey.filetransfer.provider.Registry;
import org.labkey.filetransfer.query.FileTransferMetadataTable;
import org.labkey.filetransfer.query.FileTransferQuerySchema;
import org.springframework.validation.Errors;

import java.util.Map;

import static org.labkey.filetransfer.FileTransferManager.FILE_TRANSFER_PROVIDER;

/**
 * Created by susanh on 5/9/17.
 */
public class FileTransferMetadataQueryView extends QueryView
{
    Portal.WebPart webPart;
    Map<String, String> properties;
    ListDefinition listDef;
    FileTransferProvider provider;

    public FileTransferMetadataQueryView(Portal.WebPart webPart, UserSchema schema, QuerySettings settings, @Nullable Errors errors)
    {
        super(schema, settings, errors);
        this.webPart = webPart;
        this.properties = webPart.getPropertyMap();
        if (this.properties != null)
        {
            listDef = FileTransferManager.get().getMetadataList(this.properties);
            provider = Registry.get().getProvider(getContainer(), getUser(), this.properties.get(FILE_TRANSFER_PROVIDER));
        }
        if (!FileTransferManager.get().isValidTransferDirectory(this.properties, getViewContext()))
            listDef = null;
    }

    @Override
    protected TableInfo createTable()
    {
        return listDef == null ? null : new FileTransferMetadataTable(properties, listDef.getTable(getUser()), new FileTransferQuerySchema(getUser(), getContainer()), getViewContext());
    }

    @Override
    protected void populateButtonBar(DataView view, ButtonBar bar)
    {
        super.populateButtonBar(view, bar);
        if (provider == null)
            return;

        if (provider.isTransferApiConfigured())
        {
            String transferUrl = new ActionURL(FileTransferController.AuthAction.class, view.getViewContext().getContainer())
                    .addParameter(DataRegionSelection.DATA_REGION_SELECTION_KEY, view.getDataRegion().getSelectionKey())
                    .addParameter("webPartId", webPart.getRowId())
                    .addReturnURL(view.getViewContext().getActionURL())
                    .toString();
            if (transferUrl != null)
            {
                StringExpression url = StringExpressionFactory.createURL(transferUrl);
                ActionButton transferBtn = new ActionButton("Transfer", url);
                transferBtn.setRequiresSelection(true);
                bar.add(transferBtn);
            }
        }
        String transferLinkUrl = provider.getTransferUiUrl(properties, getViewContext());
        if (transferLinkUrl != null)
        {
            StringExpression url = StringExpressionFactory.createURL(transferLinkUrl);
            if (url != null)
            {
                ActionButton transferLinkBtn = new ActionButton("Open Transfer Link", url);
                transferLinkBtn.setTarget("_blank");
                bar.add(transferLinkBtn);
            }
        }
    }
}
