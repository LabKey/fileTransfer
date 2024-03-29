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
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.api.view.Portal" %>
<%@ page import="org.labkey.api.view.template.ClientDependencies" %>
<%@ page import="org.labkey.filetransfer.provider.FileTransferProvider" %>
<%@ page import="org.labkey.filetransfer.view.WebPartConfigBean" %>
<%@ page import="java.util.Map" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!
    @Override
    public void addClientDependencies(ClientDependencies dependencies)
    {
        dependencies.add("Ext4");
        dependencies.add("sqv");
        dependencies.add("fileTransfer/fileTransferConfig.css");
    }
%>
<%
    JspView<WebPartConfigBean>  me = (JspView<WebPartConfigBean>) JspView.currentView();
    WebPartConfigBean bean = me.getModelBean();
    Portal.WebPart webPart = bean.getWebPart();
    FileTransferProvider provider = bean.getProvider();
    Map<String, String> properties = webPart.getPropertyMap();
    String title = properties.get("webpart.title") != null ? properties.get("webpart.title"): "File Transfer";
%>
<labkey:errors/>
<div id="SQVPicker"></div>

<script type="text/javascript" nonce="<%=getScriptNonce()%>">

    Ext4.onReady(function()
    {
        var getFieldHoverText = function(id) {
            return '<a id=\'' + id + '\' href=\'#\'><span class=\'labkey-help-pop-up\'>?</span></a>';
        };

        var webPartTitle = Ext4.create('Ext.form.field.Text', {
            name: 'webpart.title',
            labelWidth: 150,
            width: 510,
            padding: '10px 0 25px 0',
            fieldLabel: 'Web Part Title',
            initialValue : <%=qh(title)%>,
            value: <%=qh(title)%>,
            allowBlank: false
        });

        var localDirMsg = <%= StringUtils.isEmpty(provider.getSettings().getFileTransferRoot()) %> ?
            'The file transfer root directory must first be configured in the admin console before proceeding with this configuration step.' :
            'Specify where the files to be transferred in this webpart are available on your local file system. Provide a path relative to the file transfer root directory (set via the admin console).';

        var localFilesDirectoryHeader = Ext4.create('Ext.form.Label', {
            html: '<span style="font-weight: bold">Files Directory</span></br>' + localDirMsg
        });

        var localFilesDirectoryField = Ext4.create('Ext.form.field.Text', {
            name: 'localFilesDirectory',
            labelWidth: 150,
            width: 510,
            padding: '10px 0 25px 0',
            disabled: <%=StringUtils.isEmpty(provider.getSettings().getFileTransferRoot())%>,
            fieldLabel: 'Local Directory' + getFieldHoverText('localFilesDirectory_a'),
            initialValue : <%=q(properties.get("localFilesDirectory"))%>,
            value: <%=q(properties.get("localFilesDirectory"))%>,
            allowBlank: false
        });

        var referenceListHeader = Ext4.create('Ext.form.Label', {
            text: 'Reference List',
            style: 'font-weight: bold;'
        });

        var sqvModel = Ext4.create('LABKEY.sqv.Model', {});

        var containerIdTextField = Ext4.create('Ext.form.field.Text', {
            name: 'containerId',
            hidden: true
        });

        var providerNameField = Ext4.create('Ext.form.field.Text', {
            name: 'fileTransferProvider',
            hidden: true,
            value: <%=q(provider.getName())%>
        });

        var containerComboField = Ext4.create('Ext.form.field.ComboBox', sqvModel.makeContainerComboConfig({
            name: 'listFolder',
            labelWidth: 150,
            fieldLabel: 'Folder' + getFieldHoverText('listFolder_a'),
            editable: false,
            width: 510,
            padding: '10px 0 0 0',
            allowBlank: false,
            initialValue : <%=q(properties.get("listFolder"))%>,
            value : <%=q(properties.get("listFolder"))%>,
            listeners: {
                select: function(combo) {
                    containerIdTextField.setValue(combo.getValue());
                    sqvModel.changeQueryStore(combo.getValue(), 'lists');
                }
            }
        }));

        var schemaComboField = Ext4.create('Ext.form.field.ComboBox', {
            name: 'listSchema',
            labelWidth: 150,
            allowBlank: false,
            fieldLabel: 'Schema',
            value: 'lists',
            disabled: true,
            width: 300,
            padding: '10px 0 0 0'
        });

        var queryComboField = Ext4.create('Ext.form.field.ComboBox', sqvModel.makeQueryComboConfig({
            name: 'listTable',
            forceSelection: true,
            defaultSchema: 'lists',
            fieldLabel: 'List'+ getFieldHoverText('listTable_a'),
            labelWidth: 150,
            allowBlank: false,
            initialValue : <%=q(properties.get("listTable"))%>,
            value : <%=q(properties.get("listTable"))%>,
            width: 300,
            padding: '10px 0 0 0'
        }));

        var columnComboField = Ext4.create('Ext.form.field.ComboBox', sqvModel.makeColumnComboConfig({
            name: 'fileNameColumn',
            fieldLabel: 'File Name Field' + getFieldHoverText('fileNameColumn_a'),
            forceSelection: true,
            labelWidth: 150,
            allowBlank: false,
            initialValue : <%=q(properties.get("fileNameColumn"))%>,
            value : <%=q(properties.get("fileNameColumn"))%>,
            margin: '0, 0, 20, 0',
            width: 300,
            padding: '10px 0 25px 0'
        }));

        var transferSourceHeader = Ext4.create('Ext.form.Label', {
            text: <%=q(provider.getName())%> + ' File Transfer Source',
            style: 'font-weight: bold;'
        });

        var sourceEndpointDirField = Ext4.create('Ext.form.field.Text', {
            name: 'sourceEndpointDir',
            labelWidth: 150,
            width: 510,
            padding: '10px 0 25px 0',
            fieldLabel: 'Endpoint Directory' + getFieldHoverText('sourceEndpointDir_a'),
            initialValue : <%=q(properties.get("sourceEndpointDir"))%>,
            value: <%=q(properties.get("sourceEndpointDir"))%>
        });

        var cancelButton = Ext4.create('Ext.button.Button', {
            text: 'Cancel',
            scope: this,
            handler: function ()
            {
                var url = LABKEY.ActionURL.getParameter('returnUrl');
                if (!url)
                    url = LABKEY.ActionURL.buildURL('project', 'begin.view');

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
                        url: <%=q(webPart.getCustomizePostURL(getViewContext()))%>,
                        method: 'POST',
                        scope: this
                    });
                }
            }
        });

        const attachEvents = function (id, title, details) {
            const element = document.getElementById(id);
            element['onclick'] = function(){ return showHelpDiv(this, title, details); };
            element['onmouseover'] = function(){ return showHelpDiv(this, title, details); };
            element['onmouseout'] = function(){ return hideHelpDivDelay(); };
        }

        Ext4.create('Ext.form.Panel', {
            border : false,
            renderTo : 'SQVPicker',
            cls: 'configFormPanel',
            width: 520,
            items : [
                webPartTitle,
                localFilesDirectoryHeader,
                localFilesDirectoryField,
                referenceListHeader,
                providerNameField,
                containerIdTextField,
                containerComboField,
                schemaComboField,
                queryComboField,
                columnComboField,
                transferSourceHeader,
                sourceEndpointDirField,
                { xtype: 'hidden', name: 'X-LABKEY-CSRF', value: LABKEY.CSRF }
            ],
            buttons: [
                cancelButton,
                saveButton
            ],
            listeners: {
                render: function () {
                    attachEvents('localFilesDirectory_a', 'Local Directory', localDirMsg);
                    attachEvents('listFolder_a', 'Reference List Folder', 'Specify the location of the list that contains the metadata for the files referenced in this webpart.');
                    attachEvents('listTable_a', 'Reference List', 'Specify the name of the list that contains the metadata for the files referenced in this webpart.');
                    attachEvents('fileNameColumn_a', 'Reference List Filed', 'Specify the name of the field in the reference list that contains the names of the files that could be transferred.');
                    attachEvents('sourceEndpointDir_a', 'Endpoint Directory', 'Specify the directory on the transfer service provider endpoint that contains the files for this webpart.');
                }
            }
        });
    });
</script>