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
import org.labkey.api.action.RedirectAction;
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
import org.labkey.filetransfer.security.GlobusAuthenticator;
import org.labkey.filetransfer.security.OAuth2Authenticator;
import org.labkey.filetransfer.security.SecurePropertiesDataStore;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

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
//            String endpointPath = form.getEndpointPath();
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
//            if (StringUtils.isEmpty(form.getEndpointPath()))
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

    @RequiresPermission(ReadPermission.class)
    public class TransferAction extends SimpleViewAction
    {

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return null;
        }

        @Override
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            return null;
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class AuthAction extends RedirectAction<AuthForm>
    {
        @Override
        public URLHelper getSuccessURL(AuthForm authForm)
        {
            return new ActionURL(FileTransferController.TransferAction.class, getContainer());
        }

        @Override
        public boolean doAction(AuthForm form, BindException errors) throws Exception
        {
            if (form.getError() != null)
            {
                logger.error("Error in authorizing: " + form.getError());
                return false;
            }
            else if (form.getCode() != null)
            {
                SecurePropertiesDataStore store = new SecurePropertiesDataStore(getUser(), getContainer());

                StoredCredential credential = store.get(null);
                if (credential.getAccessToken() == null || credential.getExpirationTimeMilliseconds() == null || credential.getExpirationTimeMilliseconds() <= 0)
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
            return false;
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
                controller.new TransferAction(),
                controller.new AuthAction()
            );

//            // @RequiresPermission(AdminOperationsPermission.class)
//            assertForAdminOperationsPermission(user,
//                controller.new ConfigurationAction()
//            );
        }
    }
}