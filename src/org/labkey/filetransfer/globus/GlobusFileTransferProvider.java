package org.labkey.filetransfer.globus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.ViewContext;
import org.labkey.filetransfer.FileTransferController;
import org.labkey.filetransfer.FileTransferManager;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.provider.FileTransferProvider;
import org.labkey.filetransfer.security.OAuth2Authenticator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/15/17.
 */
public class GlobusFileTransferProvider extends FileTransferProvider
{
    public static final String NAME = "Globus";

    public GlobusFileTransferProvider(Container container, User user) throws IOException
    {
       super(container, user, NAME);
    }

    private String getUrlPrefix()
    {
        return settings.getTransferApiUrlPrefix();
    }

    public TransferResult transfer(@NotNull TransferEndpoint source, @NotNull TransferEndpoint destination, @NotNull List<String> fileNames, @Nullable String label) throws Exception
    {
        if (fileNames.isEmpty())
            return null;

        URI submissionIdUri = new URI(settings.getTransferApiUrlPrefix() + "/submission_id");
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

        URI transferUri = new URI(getUrlPrefix() + "/transfer");

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

    public String getBrowseEndpointUrl(Container container)
    {
        ActionURL actionUrl = new ActionURL(FileTransferController.PrepareAction.class, container);
        return settings.getBrowseEndpointUrlPrefix() + "?method=POST&folderlimit=0&filelimit=0&action=" + actionUrl.getURIString();
    }

    @Nullable
    public TransferEndpoint getEndpoint(String endpointId) throws IOException, URISyntaxException
    {
        URI uri = new URI(getUrlPrefix() + "/endpoint/" + endpointId + "?fields=id,display_name,organization");
        return (TransferEndpoint) makeApiGetRequest(uri, TransferEndpoint.class);
    }

    public List<TransferEndpoint> getKnownEndpoints()
    {
        return Collections.emptyList();
    }

    public boolean isTransferApiConfigured()
    {
        return settings.getClientId() != null &&
                settings.getClientSecret() != null &&
                settings.getAuthUrlPrefix() != null &&
                settings.getTransferApiUrlPrefix() != null &&
                settings.getBrowseEndpointUrlPrefix() != null
//              &&  !settings.getEndpoints().isEmpty()
 ;
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

    @Override
    public String getTransferUiUrl(Map<String, String> properties, ViewContext context)
    {
        String baseUrl = settings.getTransferUiUrlPrefix();
        // TODO
//        TransferEndpoint sourceEndpoint = settings.getEndpoint(properties.get("sourceEndpointKey"));
//        if (StringUtils.isNotBlank(baseUrl) && sourceEndpoint != null)
//        {
//            // ex: https://www.globus.org/app/transfer?origin_id=<ENDPOINT_ID>&origin_path=<ENDPOINT_DIR>
//            String transferUrl = baseUrl.trim() + (!baseUrl.trim().endsWith("?") ? "?" : "")
//                    + "origin_id=" + PageFlowUtil.encode(sourceEndpoint.getId());
//
//            String endpointDir = sourceEndpoint.getPath();
//            if (StringUtils.isNotBlank(endpointDir))
//                transferUrl += "&origin_path=" + PageFlowUtil.encode(endpointDir.trim());
//
//            return transferUrl;
//        }

        String endpointId = FileTransferManager.get().getSourceEndpointId(context.getContainer());
        if (StringUtils.isNotBlank(baseUrl) && StringUtils.isNotBlank(endpointId))
        {
            // ex: https://www.globus.org/app/transfer?origin_id=<ENDPOINT_ID>&origin_path=<ENDPOINT_DIR>
            String transferUrl = baseUrl.trim() + (!baseUrl.trim().endsWith("?") ? "?" : "")
                    + "origin_id=" + PageFlowUtil.encode(endpointId.trim());

            String endpointDir = FileTransferManager.get().getSourceEndpointDir(context);

            if (StringUtils.isNotBlank(endpointDir))
                transferUrl += "&origin_path=" + PageFlowUtil.encode(endpointDir.trim());

            return transferUrl;
        }

        return null;
    }

    @Override
    public OAuth2Authenticator getAuthenticator(Container container, User user)
    {
        return new GlobusAuthenticator(user, container);
    }
}
