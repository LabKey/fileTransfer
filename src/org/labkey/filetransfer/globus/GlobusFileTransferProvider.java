/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.filetransfer.globus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.old.JSONObject;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/15/17.
 */
public class GlobusFileTransferProvider extends FileTransferProvider
{
    public static final Logger logger = LogManager.getLogger(GlobusFileTransferProvider.class);
    public static final String NAME = "Globus";

    public GlobusFileTransferProvider()
    {
        super(NAME);
    }

    public GlobusFileTransferProvider(Container container, User user) throws IOException
    {
       super(container, user, NAME);
    }

    private String getUrlPrefix()
    {
        return settings.getTransferApiUrlPrefix();
    }

    @Override
    public TransferResult transfer(@NotNull TransferEndpoint source, @NotNull TransferEndpoint destination, @NotNull List<String> fileNames, @Nullable String label) throws Exception
    {
        if (fileNames.isEmpty())
            return null;

        URI submissionIdUri = new URI(settings.getTransferApiUrlPrefix() + "/submission_id");
        SubmissionId submissionId = (SubmissionId) makeApiGetRequest(submissionIdUri, SubmissionId.class);

        if (submissionId == null || submissionId.getValue() == null)
            throw new Exception("Could not retrieve submission id from uri " + submissionIdUri + ".  The provider's service may be down or there could be a configuration problem.  Please contact an administrator.");

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
                HttpClientResponseHandler<String> handler = new BasicHttpClientResponseHandler();
//                StatusLine status = response.getStatusLine();
                // TODO check what happens in the error cases
                String contents = handler.handleResponse(response);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(contents, TransferResult.class);
            }
        }
    }

    @Override
    public String getBrowseEndpointUrl(Container container)
    {
        ActionURL actionUrl = new ActionURL(FileTransferController.PrepareAction.class, container);
        return settings.getBrowseEndpointUrlPrefix() + "?method=POST&folderlimit=0&filelimit=0&action=" + actionUrl.getURIString();
    }

    @Override
    @Nullable
    public TransferEndpoint getEndpoint(String endpointId) throws IOException, URISyntaxException
    {
        URI uri = new URI(getUrlPrefix() + "/endpoint/" + endpointId + "?fields=id,display_name,organization");
        return (TransferEndpoint) makeApiGetRequest(uri, TransferEndpoint.class);
    }

    @Override
    public List<TransferEndpoint> getKnownEndpoints()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isTransferApiConfigured()
    {
        return settings.getClientId() != null &&
                settings.getClientSecret() != null &&
                settings.getAuthUrlPrefix() != null &&
                settings.getTransferApiUrlPrefix() != null &&
                settings.getBrowseEndpointUrlPrefix() != null &&
                settings.getEndpoint() != null &&
                !StringUtils.isEmpty(settings.getEndpoint().getId());
    }

    private Object makeApiGetRequest(URI uri, Class clazz) throws IOException
    {
        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Authorization", "Bearer " + credential.getAccessToken());

            try (CloseableHttpResponse response = httpClient.execute(httpGet))
            {
                BasicHttpClientResponseHandler handler = new BasicHttpClientResponseHandler();

                if (response.getCode() == HttpStatus.SC_OK)
                {
                    String contents = handler.handleResponse(response);
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(contents, clazz);
                }
            }
            catch (UnknownHostException e)
            {
                logger.error("Could not make request using uri " + uri, e);
            }
        }
        return null;
    }

    @Override
    public String getTransferUiUrl(Map<String, String> properties, ViewContext context)
    {
        String baseUrl = settings.getTransferUiUrlPrefix();

        TransferEndpoint sourceEndpoint = FileTransferManager.get().getSourceEndpoint(properties);
        if (StringUtils.isNotBlank(baseUrl) && sourceEndpoint != null)
        {
            // ex: https://www.globus.org/app/transfer?origin_id=<ENDPOINT_ID>&origin_path=<ENDPOINT_DIR>
            String transferUrl = baseUrl.trim() + (!baseUrl.trim().endsWith("?") ? "?" : "")
                    + "origin_id=" + PageFlowUtil.encode(sourceEndpoint.getId());

            String endpointDir = sourceEndpoint.getPath();
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
