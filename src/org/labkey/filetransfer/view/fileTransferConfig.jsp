<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%
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
%>

<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.api.view.template.ClientDependencies" %>
<%@ page import="org.labkey.filetransfer.view.FileTransferConfigForm" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!
    @Override
    public void addClientDependencies(ClientDependencies dependencies)
    {
        dependencies.add("fileTransfer/fileTransferConfig.css");
    }
%>
<%
    JspView<FileTransferConfigForm>  me = (JspView<FileTransferConfigForm>) JspView.currentView();
    FileTransferConfigForm bean = me.getModelBean();

%>
<labkey:errors/>
<h2><%= h(bean.getName()) %> File Transfer</h2>
<div id="transferForm"></div>

<script type="text/javascript">

    Ext4.onReady(function()
    {
        var getFieldHoverText = function(title, details) {
            return '<a href="#" onclick="return showHelpDiv(this, \'' + title + '\', \'' + details + '\');" '
                    + 'onmouseover="return showHelpDivDelay(this, \'' + title + '\', \'' + details + '\');" '
                    + 'onmouseout="return hideHelpDivDelay();"><span class="labkey-help-pop-up">?</span></a>';
        };

        var clientDataHeader = Ext4.create('Ext.form.Label', {
            text: 'Client Registration Data',
            style: 'font-weight: bold; font-size:large'
        });

        var clientId = Ext4.create('Ext.form.field.Text', {
            name: "clientId",
            labelWidth: 200,
            width: 535,
            padding: '10px 0 0 25px',
            hidden: false,
            disabled: false,
            fieldLabel: "Client Id" + getFieldHoverText('Client Id', 'The id assigned by the file transfer provider to identify this application as its client.'),
            initialValue : <%=q(bean.getClientId())%>,
            value: <%=q(bean.getClientId())%>,
            allowBlank: false
        });

        var clientSecret = Ext4.create('Ext.form.field.Text', {
            name: "clientSecret",
            labelWidth: 200,
            width: 535,
            padding: '10px 0 0 25px',
            hidden: false,
            disabled: false,
            fieldLabel: "Client Secret" + getFieldHoverText('Client Secret', 'The secret associated with the Client Id used for authenticating requests to the file transfer provider.'),
            initialValue : <%=q(bean.getClientSecret())%>,
            value: <%=q(bean.getClientSecret())%>,
            allowBlank: false
        });

        var serviceUrlsHeader = Ext4.create('Ext.form.Label', {
            text: 'Service URLs',
            style: 'font-weight: bold; font-size: large',
            padding: '25px 0 0 0'
        });

        var authUrl = Ext4.create('Ext.form.field.Text', {
            name: "authUrlPrefix",
            labelWidth: 200,
            width: 535,
            padding: '10px 0 0 25px',
            hidden: false,
            disabled: false,
            fieldLabel: "Authorization URL Prefix" + getFieldHoverText('Authorization URL Prefix', 'The prefix to the authorization service used for obtaining authorization codes and for requesting tokens. (e.g., https://auth.globus.org/v2/oauth2)'),
            initialValue : <%=q(bean.getAuthUrlPrefix())%>,
            value: <%=q(bean.getAuthUrlPrefix())%>,
            allowBlank: false
        });

        var transferApiUrl =  Ext4.create('Ext.form.field.Text', {
            name: "transferApiUrlPrefix",
            labelWidth: 200,
            width: 535,
            padding: '10px 0 0 25px',
            hidden: false,
            disabled: false,
            fieldLabel: "Transfer API URL Prefix" + getFieldHoverText('Transfer API URL Prefix', 'The prefix to the transfer API used for making transfer requests. (e.g, https://transfer.api.globusonline.org/v0.10)'),
            initialValue : <%=q(bean.getTransferApiUrlPrefix())%>,
            value: <%=q(bean.getTransferApiUrlPrefix())%>,
            allowBlank: false
        });

        var transferUiUrl =  Ext4.create('Ext.form.field.Text', {
            name: "transferUiUrlPrefix",
            labelWidth: 200,
            width: 535,
            padding: '10px 0 0 25px',
            hidden: false,
            disabled: false,
            fieldLabel: "Transfer UI URL Prefix" + getFieldHoverText('Transfer UI URL Prefix', 'The prefix to the UI page where transfer requests can be made. (e.g., https://www.globus.org/app/transfer)'),
            initialValue : <%=q(bean.getTransferUiUrlPrefix())%>,
            value: <%=q(bean.getTransferUiUrlPrefix())%>,
            allowBlank: true
        });

        var browseEndpointUrl =  Ext4.create('Ext.form.field.Text', {
            name: "browseEndpointUrlPrefix",
            labelWidth: 200,
            width: 535,
            padding: '10px 0 0 25px',
            hidden: false,
            disabled: false,
            fieldLabel: "Browse Endpoint URL Prefix" + getFieldHoverText('Browse Endpoint URL Prefix', 'The prefix to the helper page where users can select endpoints (e.g., https://www.globus.org/app/browse-endpoint)'),
            initialValue : <%=q(bean.getBrowseEndpointUrlPrefix())%>,
            value: <%=q(bean.getBrowseEndpointUrlPrefix())%>,
            allowBlank: false
        });

        var endpointsHeader = Ext4.create('Ext.form.Label', {
            text: 'Source Endpoint',
            style: 'font-weight: bold; font-size: large'
        });


        var endpointId = Ext4.create('Ext.form.field.Text', {
                    name: "sourceEndpointId",
                    labelWidth: 200,
                    width: 535,
                    padding: '10px 0 0 25px',
                    hidden: false,
                    disabled: false,
                    fieldLabel: "Endpoint Id" + getFieldHoverText('Source Endpoint Id', 'The unique id assigned by the file transfer provider to the source endpoint for transfer requests.'),
                    initialValue: <%=q(bean.getSourceEndpointId())%>,
                    value: <%=q(bean.getSourceEndpointId())%>,
                    allowBlank: true
                });

        var endpointName = Ext4.create('Ext.form.field.Text', {
                    name: 'sourceEndpointDisplayName',
                    labelWidth: 200,
                    width: 535,
                    padding: '10px 0 0 25px',
                    hidden: false,
                    disabled: false,
                    fieldLabel: "Endpoint Name" + getFieldHoverText('Source Endpoint Name', 'The display name for the source endpoint.'),
                    initialValue: <%=q(bean.getSourceEndpointDisplayName())%>,
                    value: <%=q(bean.getSourceEndpointDisplayName())%>,
                    allowBlank: true
                });

        var endpointLocalFileRoot = Ext4.create("Ext.form.field.Text", {
            name: "sourceEndpointLocalDir",
            labelWidth: 200,
            width: 535,
            padding: '10px 0 0 25px',
            hidden: false,
            disabled: false,
            fieldLabel: "File Transfer Root Directory" + getFieldHoverText('Source Endpoint File Transfer Root Directory', 'Specify the root directory on the '
                    + 'local file system where the files to be transferred from this endpoint are available.'),
            value: <%=text(qh(bean.getSourceEndpointLocalDir()))%>,
            allowBlank: true
        });


        var cancelButton = Ext4.create('Ext.button.Button', {
            text: 'Cancel',
            scope: this,
            handler: function ()
            {
                var url = LABKEY.ActionURL.getParameter("returnUrl");
                if (!url)
                    url = LABKEY.ActionURL.buildURL("admin", "showAdmin.view");

                window.location = url;
            }
        });

        var saveButton = Ext4.create('Ext.button.Button', {
            text: 'Save',
            scope: this,
            handler: function (btn)
            {
                var form = btn.up('form').getForm();
                if (form.isValid()) {
                    form.standardSubmit = true;
                    form.submit({
                        url: LABKEY.ActionURL.buildURL('filetransfer', 'configuration.view'),
                        method: 'POST',
                        scope: this
                    });
                }
            }
        });

        Ext4.create('Ext.form.Panel', {
            border : false,
            renderTo : 'transferForm',
            cls: 'configFormPanel',
            width: 575,
            items : [
                clientDataHeader,
                clientId,
                clientSecret,
                serviceUrlsHeader,
                authUrl,
                browseEndpointUrl,
                transferApiUrl,
                transferUiUrl,
                endpointsHeader,
                endpointId,
                endpointName,
                endpointLocalFileRoot,
                { xtype: 'hidden', name: 'X-LABKEY-CSRF', value: LABKEY.CSRF }
            ],
            buttons: [
                cancelButton,
                saveButton
            ]
        });
    });
</script>