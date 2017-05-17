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

package org.labkey.filetransfer;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import org.junit.Test;
import org.labkey.api.action.ApiAction;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.action.RedirectAction;
import org.labkey.api.action.ReturnUrlForm;
import org.labkey.api.action.SimpleResponse;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AbstractActionPermissionTest;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.util.TestContext;
import org.labkey.api.util.URLHelper;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.RedirectException;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.model.globus.TransferResult;
import org.labkey.filetransfer.provider.GlobusFileTransferProvider;
import org.labkey.filetransfer.security.GlobusAuthenticator;
import org.labkey.filetransfer.security.OAuth2Authenticator;
import org.labkey.filetransfer.security.SecurePropertiesDataStore;
import org.labkey.filetransfer.view.TransferView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.net.URISyntaxException;

import static org.labkey.api.data.DataRegionSelection.DATA_REGION_SELECTION_KEY;
import static org.labkey.filetransfer.FileTransferManager.ENDPOINT_ID_SESSION_KEY;
import static org.labkey.filetransfer.FileTransferManager.ENDPOINT_PATH_SESSION_KEY;

@Marshal(Marshaller.Jackson)
public class FileTransferController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(FileTransferController.class);
    public static final String NAME = "filetransfer";

    public FileTransferController()
    {
        setActionResolver(_actionResolver);
    }

    // TODO put this back, but use a different base class.  We still want to do the validation.
//    @RequiresPermission(AdminPermission.class)
//    public class ConfigurationAction extends FormViewAction<FileTransferConfigForm>
//    {
//        @Override
//        public NavTree appendNavTrail(NavTree root)
//        {
//            root.addChild("File Transfer: Customize");
//            return root;
//        }
//
//        @Override
//        public void validateCommand(FileTransferConfigForm form, Errors errors)
//        {
//            String endpointPath = form.getEndpointLocalPath();
//            FileContentService service = ServiceRegistry.get().getService(FileContentService.class);
//            if (service != null && !service.isValidProjectRoot(endpointPath))
//            {
//                errors.reject(ERROR_MSG, "File root '" + endpointPath + "' does not appear to be a valid directory accessible to the server at " + getViewContext().getRequest().getServerName() + ".");
//            }
//        }
//
//        @Override
//        public ModelAndView getView(FileTransferConfigForm form, boolean reshow, BindException errors) throws Exception
//        {
//            Map<String, String> map = PropertyManager.getProperties(getContainer(), FileTransferManager.FILE_TRANSFER_CONFIG_PROPERTIES);
//            form.setEndpointPath(map.get(FileTransferManager.ENDPOINT_DIRECTORY));
//            form.setLookupContainer(map.get(FileTransferManager.REFERENCE_FOLDER));
//            form.setQueryName(map.get(FileTransferManager.REFERENCE_LIST));
//            form.setColumnName(map.get(FileTransferManager.REFERENCE_COLUMN));
//            form.setSourceEndpointDir(map.get(FileTransferManager.SOURCE_ENDPOINT_DIRECTORY));
//            return new JspView<>("/org/labkey/filetransfer/view/fileTransferConfig.jsp", form, errors);
//        }
//
//        @Override
//        public boolean handlePost(FileTransferConfigForm form, BindException errors) throws Exception
//        {
//            if (StringUtils.isEmpty(form.getEndpointLocalPath()))
//                return false;
//
//            if (errors.hasErrors())
//                return false;
//
//            FileTransferManager.get().saveFileTransferConfig(form, getContainer());
//            return true;
//        }
//
//        @Override
//        public URLHelper getSuccessURL(FileTransferConfigForm form)
//        {
//            if (form.getReturnUrl() != null)
//                return new ActionURL(form.getReturnUrl());
//            else
//                return PageFlowUtil.urlProvider(ProjectUrls.class).getBeginURL(getContainer());
//        }
//    }


    /**
     * This action stores certain properties in the session and then redirects to the authentication provider's
     * authorization UI.  This redirect contains a parameter indicating the action to return to when authentication is
     * complete.
     */
    @RequiresPermission(ReadPermission.class)
    public class AuthAction extends SimpleViewAction<TransferSelectionForm>
    {
        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return null;
        }

        @Override
        public ModelAndView getView(TransferSelectionForm form, BindException errors) throws Exception
        {
            HttpSession session = getViewContext().getRequest().getSession();
            session.setAttribute(DATA_REGION_SELECTION_KEY, form.getDataRegionSelectionKey());
            session.setAttribute("fileTransferContainer", getContainer().getId());
            session.setAttribute(FileTransferManager.WEB_PART_ID_SESSION_KEY, form.getWebPartId());
            session.setAttribute(FileTransferManager.RETURN_URL_SESSION_KEY, form.getReturnUrl());
            OAuth2Authenticator authenticator = new GlobusAuthenticator(getUser(), getContainer());
            throw new RedirectException(authenticator.getAuthorizationUrl());
        }
    }

    public static class TransferSelectionForm extends ReturnUrlForm
    {
        private String dataRegionSelectionKey;
        private Integer webPartId;

        public String getDataRegionSelectionKey()
        {
            return dataRegionSelectionKey;
        }

        public void setDataRegionSelectionKey(String dataRegionSelectionKey)
        {
            this.dataRegionSelectionKey = dataRegionSelectionKey;
        }

        public Integer getWebPartId()
        {
            return webPartId;
        }

        public void setWebPartId(Integer webPartId)
        {
            this.webPartId = webPartId;
        }
    }

    /**
     * This action is the target of the authorization action from the file transfer provider.  If authorization has
     * been granted, the code provided from that authorization will be used to retrieve credentials to be used in
     * initiating transfer requests.  In any case, this action redirects to a page where we give feedback to the user
     * and display items for next steps, which are either to return to the page where the initial selection of files was
     * made, choose a destination endpoint, or initiate a transfer of the selected files.
     */
    @RequiresPermission(ReadPermission.class)
    public class TokensAction extends RedirectAction<AuthForm>
    {
        Boolean authorized = true;

        @Override
        public URLHelper getSuccessURL(AuthForm authForm)
        {
            ActionURL url = new ActionURL(PrepareAction.class, getContainer()).addParameter("authorized", authorized);
            String returnUrl = (String) getViewContext().getSession().getAttribute(FileTransferManager.RETURN_URL_SESSION_KEY);
            if (returnUrl != null)
                try
                {
                    url.addReturnURL(new URLHelper(returnUrl));
                }
                catch (URISyntaxException e)
                {
                    logger.error("Invalid URI syntax for returnUrl " + returnUrl + " returning to container start URL", e);
                    url.addReturnURL(getViewContext().getContainer().getStartURL(getUser()));
                }
            return url;
        }

        @Override
        public boolean doAction(AuthForm form, BindException errors) throws Exception
        {
            if (form.getError() != null)
            {
                // not an error because the user may have simply chosen not to authorize the client.
                logger.info("Error in authorizing: " + form.getError());
                this.authorized = false;
                return true;
            }
            else if (form.getCode() != null)
            {
                SecurePropertiesDataStore store = new SecurePropertiesDataStore(getUser(), getContainer());

                // TODO check if this actually retrieves from the database or if there's more going on
                StoredCredential credential = store.get(null);
//                if (credential.getAccessToken() == null || credential.getExpirationTimeMilliseconds() == null || credential.getExpirationTimeMilliseconds() <= 0)
                {
                    OAuth2Authenticator authenticator = new GlobusAuthenticator(getUser(), getContainer());

                    Credential c = authenticator.getTokens(form.getCode());
                    if (c.getAccessToken() != null)
                    {
                        store.set(store.getId(), new StoredCredential(c));
                        return true;
                    }
                }
            }
            return true;
        }

        @Override
        public void validateCommand(AuthForm target, Errors errors)
        {
        }
    }

    public static class AuthForm
    {
        private String _code;
        private String _error;

        public String getCode()
        {
            return _code;
        }

        public void setCode(String code)
        {
            _code = code;
        }

        public String getError()
        {
            return _error;
        }

        public void setError(String error)
        {
            _error = error;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class TransferAction extends ApiAction<TransferRequestForm>
    {
        @Override
        public Object execute(TransferRequestForm form, BindException errors) throws Exception
        {
            GlobusFileTransferProvider provider = new GlobusFileTransferProvider(getContainer(), getUser());
            TransferEndpoint source = new TransferEndpoint(FileTransferManager.get().getSourceEndpointId(getContainer()),
                    FileTransferManager.get().getSourceEndpointDir(getViewContext()));
            TransferEndpoint destination = new TransferEndpoint(form.getDestinationEndpoint(), form.getDestinationPath());
            try
            {
                TransferResult result = provider.transfer(source, destination, FileTransferManager.get().getFileNames(getViewContext()), form.getLabel());
                return success(result);
            }
            catch (Exception e)
            {
                return new SimpleResponse(false, e.getMessage());
            }

        }
    }

    public static class TransferRequestForm
    {
        private String destinationEndpoint;
        private String destinationPath;
        private String label;

        public String getDestinationEndpoint()
        {
            return destinationEndpoint;
        }

        public void setDestinationEndpoint(String destinationEndpoint)
        {
            this.destinationEndpoint = destinationEndpoint;
        }

        public String getDestinationPath()
        {
            return destinationPath;
        }

        public void setDestinationPath(String destinationPath)
        {
            this.destinationPath = destinationPath;
        }

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class PrepareAction extends SimpleViewAction<PrepareTransferForm>
    {
        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return null;
        }

        @Override
        public ModelAndView getView(PrepareTransferForm form, BindException errors) throws Exception
        {
            // stash these values in the session so we can display them when the user returns to this page.
            if (form.getDestinationId() != null && form.getPath() != null)
            {
                getViewContext().getSession().setAttribute(ENDPOINT_ID_SESSION_KEY, form.getDestinationId());
                getViewContext().getSession().setAttribute(ENDPOINT_PATH_SESSION_KEY, form.getPath());
            }
            if (form.getReturnUrl() == null)
            {
                String returnUrl = (String) getViewContext().getSession().getAttribute(FileTransferManager.RETURN_URL_SESSION_KEY);
                if (returnUrl == null)
                    returnUrl = getContainer().getStartURL(getUser()).getLocalURIString();
                form.setReturnUrl(returnUrl);
            }
            return new TransferView(getUser(), getContainer(), form);
        }
    }

    public static class PrepareTransferForm extends ReturnUrlForm
    {
        private Boolean authorized = true;
        private String endpoint_id; // parameter names dictated by https://docs.globus.org/api/helper-pages/browse-endpoint/
        private String path;
        private String label;

        public Boolean getAuthorized()
        {
            return authorized;
        }

        public void setAuthorized(Boolean authorized)
        {
            this.authorized = authorized;
        }

        // alias for endpoint_id
        public String getDestinationId()
        {
            return endpoint_id;
        }

        public void setDestinationId(String destinationId)
        {
            endpoint_id = destinationId;
        }

        public String getEndpoint_id()
        {
            return endpoint_id;
        }

        public void setEndpoint_id(String endpoint_id)
        {
            this.endpoint_id = endpoint_id;
        }

        public String getPath()
        {
            return path;
        }

        public void setPath(String path)
        {
            this.path = path;
        }

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }
    }


    public static class TestCase extends AbstractActionPermissionTest
    {
        @Test
        public void testActionPermissions()
        {
            User user = TestContext.get().getUser();
            assertTrue(user.isInSiteAdminGroup());

            FileTransferController controller = new FileTransferController();

            // @RequiresPermission(ReadPermission.class)
            assertForReadPermission(user,
                controller.new PrepareAction(),
                controller.new TokensAction()
            );

//            // @RequiresPermission(AdminOperationsPermission.class)
//            assertForAdminOperationsPermission(user,
//                controller.new ConfigurationAction()
//            );
        }
    }
}