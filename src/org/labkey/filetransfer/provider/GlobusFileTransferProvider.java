package org.labkey.filetransfer.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.StoredCredential;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.view.ActionURL;
import org.labkey.filetransfer.FileTransferController;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.security.SecurePropertiesDataStore;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Created by susanh on 5/15/17.
 */
public class GlobusFileTransferProvider
{
    private StoredCredential credential;

    public GlobusFileTransferProvider(Container container, User user) throws IOException
    {
        SecurePropertiesDataStore store = new SecurePropertiesDataStore(user, container);
        // TODO check if this actually retrieves from the database or if there's more going on
        credential = store.get(null);
    }

    public void transfer(TransferEndpoint source, TransferEndpoint destination, Set<String> fileNames)
    {

    }

    public static String getBrowseEndpointUrl(Container container)
    {
        ActionURL actionUrl = new ActionURL(FileTransferController.PrepareAction.class, container);
        return "https://www.globus.org/app/browse-endpoint?method=POST&folderlimit=0&filelimit=0&action=" + actionUrl.getURIString();
    }

    public TransferEndpoint getEndpoint(String endpointId) throws IOException, URISyntaxException
    {
        String url = "https://transfer.api.globusonline.org/v0.10/endpoint/" + endpointId + "?fields=id,display_name,organization";
        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            URI uri = new URI(url);
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Authorization", "Bearer " + credential.getAccessToken());

            try (CloseableHttpResponse response = httpClient.execute(httpGet))
            {
                ResponseHandler<String> handler = new BasicResponseHandler();
                StatusLine status = response.getStatusLine();

                if (status.getStatusCode() != HttpStatus.SC_OK)
                {
                    String contents = handler.handleResponse(response);
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(contents, TransferEndpoint.class);
                }
            }
        }
        return null;
    }
}
