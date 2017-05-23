package org.labkey.filetransfer.provider;

import com.google.api.client.auth.oauth2.StoredCredential;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.globus.TransferResult;
import org.labkey.filetransfer.security.OAuth2Authenticator;
import org.labkey.filetransfer.security.SecurePropertiesDataStore;
import org.labkey.filetransfer.config.FileTransferSettings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/22/17.
 */
public abstract class FileTransferProvider
{
    protected String name;
    protected StoredCredential credential;
    protected FileTransferSettings settings;

    public FileTransferProvider(Container container, User user, String name) throws IOException
    {
        SecurePropertiesDataStore store = new SecurePropertiesDataStore(user, container);
        // TODO check if this actually retrieves from the database or if there's more going on
        credential = store.get(null);
        this.name = name;
        settings = new FileTransferSettings(this.name);
    }

    public String getName()
    {
        return name;
    }

    public FileTransferSettings getSettings()
    {
        return settings;
    }

    public abstract TransferResult transfer(@NotNull TransferEndpoint source, @NotNull TransferEndpoint destination, @NotNull List<String> fileNames, @Nullable String label) throws Exception;

    public abstract String getBrowseEndpointUrl(Container container);

    public abstract TransferEndpoint getEndpoint(String endpointId) throws IOException, URISyntaxException;

    public abstract List<TransferEndpoint> getKnownEndpoints();

    public abstract boolean isTransferApiConfigured();

    public abstract String getTransferUiUrl(Map<String, String> properties, ViewContext context);

    public abstract OAuth2Authenticator getAuthenticator(Container container, User user);

}
