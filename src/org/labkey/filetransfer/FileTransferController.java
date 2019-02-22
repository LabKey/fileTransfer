/*
 * Copyright (c) 2017-2018 LabKey Corporation
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
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.labkey.api.action.FormViewAction;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.action.MutatingApiAction;
import org.labkey.api.action.OldRedirectAction;
import org.labkey.api.action.ReturnUrlForm;
import org.labkey.api.action.SimpleResponse;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.admin.AdminUrls;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.security.AdminConsoleAction;
import org.labkey.api.security.RequiresNoPermission;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AbstractActionPermissionTest;
import org.labkey.api.security.permissions.AdminOperationsPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.util.TestContext;
import org.labkey.api.util.URLHelper;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.RedirectException;
import org.labkey.filetransfer.config.FileTransferSettings;
import org.labkey.filetransfer.globus.GlobusFileTransferProvider;
import org.labkey.filetransfer.globus.TransferResult;
import org.labkey.filetransfer.model.TransferEndpoint;
import org.labkey.filetransfer.provider.FileTransferProvider;
import org.labkey.filetransfer.security.OAuth2Authenticator;
import org.labkey.filetransfer.security.SecurePropertiesDataStore;
import org.labkey.filetransfer.view.FileTransferConfigForm;
import org.labkey.filetransfer.view.TransferView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.net.URISyntaxException;

import static org.labkey.api.data.DataRegionSelection.DATA_REGION_SELECTION_KEY;
import static org.labkey.filetransfer.FileTransferManager.ENDPOINT_ID_SESSION_KEY;
import static org.labkey.filetransfer.FileTransferManager.ENDPOINT_PATH_SESSION_KEY;
import static org.labkey.filetransfer.FileTransferManager.FILE_TRANSFER_CONTAINER;

@Marshal(Marshaller.Jackson)
public class FileTransferController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(FileTransferController.class);
    public static final String NAME = "filetransfer";

    public FileTransferController()
    {
        setActionResolver(_actionResolver);
    }

    public static ActionURL getComplianceSettingsURL()
    {
        return new ActionURL(ConfigurationAction.class, ContainerManager.getRoot());
    }

    @AdminConsoleAction(AdminOperationsPermission.class)
    public class ConfigurationAction extends FormViewAction<FileTransferConfigForm>
    {
        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            root.addChild("File Transfer Configuration");
            return root;
        }

        @Override
        public void validateCommand(FileTransferConfigForm form, Errors errors)
        {
            if (form.getSourceEndpointLocalDir() == null)
                return;
            File file = new File(form.getSourceEndpointLocalDir());
            if (form.getSourceEndpointLocalDir() != null)
            {
                if (!file.isDirectory())
                    errors.rejectValue("sourceEndpointLocalDir", ERROR_MSG, "Directory '" + form.getSourceEndpointLocalDir() + "' does not exist");
                else if (!file.canRead())
                    errors.rejectValue("sourceEndpointLocalDir", ERROR_MSG, "Directory '" + form.getSourceEndpointLocalDir() + "' is not readable");
            }
        }

        @Override
        public ModelAndView getView(FileTransferConfigForm form, boolean reshow, BindException errors) throws Exception
        {
            FileTransferSettings settings = new FileTransferSettings(GlobusFileTransferProvider.NAME);
            form.setName(settings.getProviderName());
            form.setClientSecret(settings.getClientSecret());
            form.setClientId(settings.getClientId());

            TransferEndpoint sourceEndpoint = settings.getEndpoint();
            form.setSourceEndpointLocalDir(sourceEndpoint.getLocalDirectory());
            form.setSourceEndpointId(sourceEndpoint.getId());
            form.setSourceEndpointDisplayName(sourceEndpoint.getDisplayName());

            form.setAuthUrlPrefix(settings.getAuthUrlPrefix());
            form.setBrowseEndpointUrlPrefix(settings.getBrowseEndpointUrlPrefix());
            form.setTransferApiUrlPrefix(settings.getTransferApiUrlPrefix());
            form.setTransferUiUrlPrefix(settings.getTransferUiUrlPrefix());

            return new JspView<>("/org/labkey/filetransfer/view/fileTransferConfig.jsp", form, errors);
        }

        @Override
        public boolean handlePost(FileTransferConfigForm form, BindException errors) throws Exception
        {
            if (!StringUtils.isEmpty(form.getSourceEndpointLocalDir()))
            {
                File file = new File(form.getSourceEndpointLocalDir());
                if (!file.isDirectory() || !file.canWrite())
                    return false;
            }

            FileTransferSettings settings = new FileTransferSettings("Globus");
            settings.saveProperties(form);

            return true;
        }

        @Override
        public URLHelper getSuccessURL(FileTransferConfigForm form)
        {
            if (form.getReturnUrl() != null)
                return new ActionURL(form.getReturnUrl());
            else
                return PageFlowUtil.urlProvider(AdminUrls.class).getAdminConsoleURL();
        }
    }


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
            session.setAttribute(FILE_TRANSFER_CONTAINER, getContainer().getId());
            session.setAttribute(FileTransferManager.WEB_PART_ID_SESSION_KEY, form.getWebPartId());
            session.setAttribute(FileTransferManager.RETURN_URL_SESSION_KEY, form.getReturnUrl());
            FileTransferProvider provider = FileTransferManager.get().getProvider(getViewContext());
            if (provider != null)
            {
                OAuth2Authenticator authenticator = provider.getAuthenticator(getContainer(), getUser());
                throw new RedirectException(authenticator.getAuthorizationUrl());
            }
            else
            {
                errors.reject("No File Transfer Provider defined for this web part");
                return null;
            }
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
    @RequiresNoPermission
    public class TokensAction extends OldRedirectAction<AuthForm>
    {
        Boolean authorized = false;
        FileTransferManager.ErrorCode errorCode = null;

        @Override
        public URLHelper getSuccessURL(AuthForm authForm)
        {
            ActionURL url = new ActionURL(PrepareAction.class, FileTransferManager.get().getContainer(getViewContext())).addParameter("authorized", authorized);
            if (errorCode != null)
                url.addParameter("errorCode", errorCode.toString());
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
        public boolean doAction(AuthForm form, BindException errors)
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
                SecurePropertiesDataStore store = new SecurePropertiesDataStore(getUser(), FileTransferManager.get().getContainer(getViewContext()));

                FileTransferProvider provider = FileTransferManager.get().getProvider(getViewContext());
                if (provider != null)
                {
                    OAuth2Authenticator authenticator = provider.getAuthenticator(getContainer(), getUser());

                    Credential c = authenticator.getTokens(form.getCode());
                    if (c == null || c.getAccessToken() == null)
                    {
                        errorCode = FileTransferManager.ErrorCode.noTokens;
                        return true;
                    }
                    else
                    {
                        store.set(store.getId(), new StoredCredential(c));
                        this.authorized = true;
                        return true;
                    }
                }
                else
                    errorCode = FileTransferManager.ErrorCode.noProvider;
            }
            return true;
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
    public class TransferAction extends MutatingApiAction<TransferRequestForm>
    {
        @Override
        public Object execute(TransferRequestForm form, BindException errors)
        {
            FileTransferProvider provider = FileTransferManager.get().getProvider(getViewContext());
            if (provider == null)
                return new SimpleResponse(false, "Count not find File Transfer Provider in this session.");
            TransferEndpoint source = new TransferEndpoint(form.getSourceEndpoint(), form.getSourcePath());
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
        private String sourceEndpoint;
        private String sourcePath;
        private String destinationEndpoint;
        private String destinationPath;
        private String label;

        public String getSourceEndpoint()
        {
            return sourceEndpoint;
        }

        public void setSourceEndpoint(String sourceEndpoint)
        {
            this.sourceEndpoint = sourceEndpoint;
        }

        public String getSourcePath()
        {
            return sourcePath;
        }

        public void setSourcePath(String sourcePath)
        {
            this.sourcePath = sourcePath;
        }

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
            HttpSession session = getViewContext().getSession();
            // stash these values in the session so we can display them when the user returns to this page.
            if (form.getDestinationId() != null && form.getPath() != null)
            {
                session.setAttribute(ENDPOINT_ID_SESSION_KEY, form.getDestinationId());
                session.setAttribute(ENDPOINT_PATH_SESSION_KEY, form.getPath());
            }
            if (form.getReturnUrl() == null)
            {
                String returnUrl = (String) session.getAttribute(FileTransferManager.RETURN_URL_SESSION_KEY);
                if (returnUrl == null)
                {
                    Container returnContainer = FileTransferManager.get().getContainer(getViewContext());
                    if (returnContainer == null)
                    {
                        returnContainer = getContainer();
                        if (returnContainer.isRoot())
                            returnContainer = ContainerManager.getHomeContainer();
                    }
                    returnUrl = returnContainer.getStartURL(getUser()).getLocalURIString();
                }
                form.setReturnUrl(returnUrl);
            }
            return new TransferView(form);
        }
    }

    public static class PrepareTransferForm extends ReturnUrlForm
    {
        private Boolean authorized = true;
        private String endpoint_id; // parameter names dictated by https://docs.globus.org/api/helper-pages/browse-endpoint/
        private String path;
        private String label;
        private FileTransferManager.ErrorCode errorCode;

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

        public FileTransferManager.ErrorCode getErrorCode()
        {
            return errorCode;
        }

        public void setErrorCode(FileTransferManager.ErrorCode errorCode)
        {
            this.errorCode = errorCode;
        }
    }


    public static class TestCase extends AbstractActionPermissionTest
    {
        @Test
        public void testActionPermissions()
        {
            User user = TestContext.get().getUser();
            assertTrue(user.hasSiteAdminPermission());

            FileTransferController controller = new FileTransferController();

            // @RequiresPermission(ReadPermission.class)
            assertForReadPermission(user,
                controller.new PrepareAction(),
                controller.new TransferAction(),
                controller.new AuthAction()
            );

            // @AdminConsoleAction
            // @RequiresPermission(AdminOperationsPermission.class)
            assertForAdminPermission(ContainerManager.getRoot(), user,
                    controller.new ConfigurationAction()
            );
        }
    }
}