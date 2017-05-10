package org.labkey.filetransfer.view;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ActionButton;
import org.labkey.api.data.ButtonBar;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.list.ListDefinition;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.util.StringExpression;
import org.labkey.api.util.StringExpressionFactory;
import org.labkey.api.view.DataView;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.query.FileTransferMetadataTable;
import org.labkey.filetransfer.query.FileTransferQuerySchema;
import org.labkey.filetransfer.security.GlobusAuthenticator;
import org.labkey.filetransfer.security.OAuth2Authenticator;
import org.springframework.validation.Errors;

import java.util.Map;

/**
 * Created by susanh on 5/9/17.
 */
public class FileTransferMetadataQueryView extends QueryView
{
    Map<String, String> properties;

    public FileTransferMetadataQueryView(Map<String, String> propertyMap, UserSchema schema, QuerySettings settings, @Nullable Errors errors)
    {
        super(schema, settings, errors);
        this.properties = propertyMap;
    }

    @Override
    protected TableInfo createTable()
    {
        if (this.properties != null)
        {
            ListDefinition listDef = FileTransferManager.get().getMetadataList(this.properties);
            return listDef == null ? null : new FileTransferMetadataTable(properties, listDef.getTable(getUser()), new FileTransferQuerySchema(getUser(), getContainer()));
        }
        return null;
    }

    @Override
    protected void populateButtonBar(DataView view, ButtonBar bar)
    {
        super.populateButtonBar(view, bar);

        Container container = view.getViewContext().getContainer();
        FileTransferManager manager = FileTransferManager.get();
        if (manager.isTransferConfigured(container))
        {
            OAuth2Authenticator authenticator = new GlobusAuthenticator(getUser(), getContainer());
            String transferUrl;
            // TODO direct to a controller action that will capture the query parameters
//            if (authenticator.isAuthorized())
//                transferUrl = new ActionURL(FileTransferController.TransferAction.class, view.getViewContext().getContainer()).toString();
//            else
                transferUrl = authenticator.getAuthorizationUrl();
            if (transferUrl != null)
            {
                StringExpression url = StringExpressionFactory.createURL(transferUrl);
                ActionButton transferBtn = new ActionButton("Transfer", url);
                transferBtn.setTarget("_blank");
                // TODO add in the script to save the transfer data from the query view.

                bar.add(transferBtn);
            }
        }
        String transferLinkUrl = manager.getGlobusTransferUiUrl(view.getViewContext().getContainer());
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
