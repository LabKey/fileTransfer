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
<%@ page import="org.labkey.api.util.Button" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.api.view.template.ClientDependencies" %>
<%@ page import="org.labkey.filetransfer.FileTransferController" %>
<%@ page import="org.labkey.filetransfer.FileTransferManager" %>
<%@ page import="org.labkey.filetransfer.model.TransferBean" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!
    @Override
    public void addClientDependencies(ClientDependencies dependencies)
    {
        dependencies.add("Ext4");
    }
%>
<%
    JspView<TransferBean>  me = (JspView<TransferBean>) JspView.currentView();
    TransferBean bean = me.getModelBean();
    String returnUrl = bean.getReturnUrl();
    Boolean transferEnabled = bean.getAuthorized() && bean.getSource() != null && bean.getDestination() != null && bean.getTransferResultMsg() == null;
    String notifyMsg = "";
    if (bean.getErrorCode() == FileTransferManager.ErrorCode.noProvider)
        notifyMsg = "No file transfer provider available in the session.";
    else if (bean.getErrorCode() == FileTransferManager.ErrorCode.noTokens)
        notifyMsg = "Unable to retrieve access tokens from provider '" + bean.getProviderName() + "'. The provider's service may be offline or there could be a configuration problem with this module. " +
                "Please contact an administrator.";
    else if (bean.getSource() == null)
        notifyMsg = "Source endpoint has not yet been configured.";
    else if (!bean.getAuthorized())
        notifyMsg = "User has not authorized LabKey Server for file transfer using " + bean.getProviderName() + ".";
    else if (bean.getFileNames().isEmpty())
        notifyMsg = "No files selected.";
    if (!notifyMsg.isEmpty())
        notifyMsg += "  No transfer request will be made.";
    String cancelText = "Back";
    ActionURL transferUrl = new ActionURL(FileTransferController.TransferAction.class, getContainer());
    String browseEndpointsUrl = bean.getBrowseEndpointsUrl();
%>
<labkey:errors/>
<%
    if (transferEnabled)
    {
%>
<script type="text/javascript">
    function makeTransferRequest()
    {
        Ext4.Ajax.request({
            url: <%=q(transferUrl.getLocalURIString())%>,
            method: 'POST',
            jsonData: {
                sourceEndpoint: <%= q(bean.getSource().getId())%>,
                sourcePath: <%= q(bean.getSource().getPath())%>,
                destinationEndpoint: <%= q(bean.getDestination().getId()) %>,
                destinationPath: <%= q(bean.getDestination().getPath()) %>,
                label: <%= q(bean.getLabel()) %>
            },
            success: function (response) {
                var jsonResp = LABKEY.Utils.decode(response.responseText);
                if (jsonResp) {
                    if (jsonResp.success) {
                        Ext4.Msg.show({
                                    title: 'Transfer Request ' + <%=q(bean.getLabel() == null ? "" : "\"" + bean.getLabel() + "\"")%> + ' Made',
                                    msg: jsonResp.data['message'] + '.  Check the <%=h(bean.getProviderName())%> website for status information<%= h(!StringUtils.isEmpty(bean.getLabel()) ? " for the transfer labeled '" + bean.getLabel() + "'" : "")%>.',
                                    icon: Ext4.window.MessageBox.INFO,
                                    buttonText: {
                                        ok: 'OK',
                                        cancel: "BACK"
                                    },
                                    fn: function(btn, text){
                                        if (btn === 'cancel')
                                           window.location = <%=q(returnUrl)%>;
                                        else if (btn === 'ok')
                                            Ext4.get('transferBtn').dom.className = " labkey-disabled-button"
                                    },
                                    buttons: Ext4.Msg.OKCANCEL
                        });
                    }
                    else {
                        var errorHTML = jsonResp.message;
                        Ext4.Msg.alert('Error', errorHTML);
                    }

                }
            },
            failure: function (response) {
                var jsonResp = LABKEY.Utils.decode(response.responseText);
                if (jsonResp && jsonResp.errors) {
                    var errorHTML = jsonResp.errors[0].message;
                    Ext4.Msg.alert('Error', errorHTML);
                }
            }
        });
    }

</script>
<%
    }
%>

<h2><%= h(bean.getProviderName() == null ? "" : bean.getProviderName() ) %> File Transfer</h2>

<br>

<span class="labkey-fileTransfer-notification" id="notification"><%=h(notifyMsg)%></span>
<%
    if (notifyMsg.isEmpty())
    {
%>
Preparing to transfer the following files from directory <%= h(bean.getSource().getPath()) %> on source endpoint '<%= h(bean.getSource().getDisplayName()) %>'.
<ul>
    <%
        for (String filename : bean.getFileNames())
        {
    %>
    <li><%=h(filename)%></li>
    <%
        }
    %>
</ul>
<%
        if (bean.getDestination() == null)
        {
%>
Select destination endpoint.
<%
        }
        else
        {
%>
Click the 'Transfer' button below to initiate the transfer
<%= h(!StringUtils.isEmpty(bean.getLabel()) ? "using label '" + bean.getLabel() + "'" : "") %>
to directory <%=h(bean.getDestination().getPath())%> on destination endpoint '<%=h(bean.getDestination().getDisplayName())%>'
<br>
<b>OR</b> select a different destination endpoint.
<%
        }
        out.write(PageFlowUtil.button("Browse").href(browseEndpointsUrl).toString());
%>
<br><br>
<%
        Button.ButtonBuilder builder=PageFlowUtil.button("Transfer");
        builder.enabled(transferEnabled);
        builder.id("transferBtn");
        if (transferEnabled)
        {
            builder.onClick("makeTransferRequest(); return false;");
        }
        out.write(builder.toString());
%>
<%
    }
    out.write(PageFlowUtil.button(cancelText).href(returnUrl).toString());
%>