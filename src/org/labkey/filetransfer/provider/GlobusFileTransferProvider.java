package org.labkey.filetransfer.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.StoredCredential;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.view.ActionURL;
import org.labkey.filetransfer.FileTransferController;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.model.globus.SubmissionId;
import org.labkey.filetransfer.model.globus.TransferResult;
import org.labkey.filetransfer.security.SecurePropertiesDataStore;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by susanh on 5/15/17.
 */
public class GlobusFileTransferProvider
{
    private static final Logger logger = Logger.getLogger(GlobusFileTransferProvider.class);
    private StoredCredential credential;
    private static final String urlPrefix = "https://transfer.api.globusonline.org/v0.10";

    public GlobusFileTransferProvider(Container container, User user) throws IOException
    {
        SecurePropertiesDataStore store = new SecurePropertiesDataStore(user, container);
        // TODO check if this actually retrieves from the database or if there's more going on
        credential = store.get(null);
    }

    public TransferResult transfer(@NotNull TransferEndpoint source, @NotNull TransferEndpoint destination, @NotNull List<String> fileNames, @Nullable String label) throws Exception
    {
        if (fileNames.isEmpty())
            return null;

        URI submissionIdUri = new URI(urlPrefix + "/submission_id");
        SubmissionId submissionId = (SubmissionId) makeApiGetRequest(submissionIdUri, SubmissionId.class);

        if (submissionId == null || submissionId.getValue() == null)
            throw new Exception("Could not retrieve submission id from uri " + submissionIdUri);

        JSONObject transferObject = new JSONObject();
        transferObject.put("DATA_TYPE", "transfer");
        transferObject.put("submission_id", submissionId.getValue());
        transferObject.put("source_endpoint", source.getId());
        transferObject.put("destination_endpoint", destination.getId());
        if (label != null)
            transferObject.put("label", label);

        List<JSONObject> items = new ArrayList<>();
        for (String fileName : fileNames)
        {
            JSONObject item = new JSONObject();
            item.put("DATA_TYPE", "transfer_item");
            item.put("source_path", source.getPath() + "/" + fileName);
            item.put("destination_path", destination.getPath()  + fileName);
            items.add(item);
        }
        transferObject.put("DATA", items);

        URI transferUri = new URI(urlPrefix + "/transfer");

        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpPost httpPost = new HttpPost(transferUri);
            httpPost.setHeader("Authorization", "Bearer " + credential.getAccessToken());

            httpPost.setEntity(new StringEntity(JSONObject.valueToString(transferObject), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost))
            {
                ResponseHandler<String> handler = new BasicResponseHandler();
//                StatusLine status = response.getStatusLine();
                // TODO check what happens in the error cases
                String contents = handler.handleResponse(response);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(contents, TransferResult.class);
            }
        }
    }

    public static String getBrowseEndpointUrl(Container container)
    {
        ActionURL actionUrl = new ActionURL(FileTransferController.PrepareAction.class, container);
        return "https://www.globus.org/app/browse-endpoint?method=POST&folderlimit=0&filelimit=0&action=" + actionUrl.getURIString();
    }

    @Nullable
    public TransferEndpoint getEndpoint(String endpointId) throws IOException, URISyntaxException
    {
        URI uri = new URI(urlPrefix + "/endpoint/" + endpointId + "?fields=id,display_name,organization");
        return (TransferEndpoint) makeApiGetRequest(uri, TransferEndpoint.class);
    }

    private Object makeApiGetRequest(URI uri, Class clazz) throws IOException
    {
        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Authorization", "Bearer " + credential.getAccessToken());

            try (CloseableHttpResponse response = httpClient.execute(httpGet))
            {
                ResponseHandler<String> handler = new BasicResponseHandler();
                StatusLine status = response.getStatusLine();

                if (status.getStatusCode() == HttpStatus.SC_OK)
                {
                    String contents = handler.handleResponse(response);
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(contents, clazz);
                }
            }
        }
        return null;
    }
}
