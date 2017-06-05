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

package org.labkey.test.tests.filetransfer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.SortDirection;
import org.labkey.test.TestFileUtils;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.Git;
import org.labkey.test.components.CustomizeView;
import org.labkey.test.pages.core.admin.FileTransferConfigurationPage;
import org.labkey.test.pages.filetransfer.CustomizeFileTransferPage;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.PortalHelper;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category({Git.class})
public class FileTransferTest extends BaseWebDriverTest
{
    private static final String STUDY_A = "StudyA";
    private static final String STUDY_B = "StudyB";
    private static final String STUDY_A_FOLDER = STUDY_A + "Folder";
    private static final String STUDY_B_FOLDER = STUDY_B + "ListFolder";
    private static final String STUDY_B_FILE_TRANSFER_FOLDER = STUDY_B + "FileTransferFolder";
    private static final String ABSENT_CONFIG_MSG ="No metadata list currently configured for this container.";
    private static final String EXPECTED_ERROR_MSG = "file transfer root directory must first be configured";
    private static final String CUSTOMIZE_FILE_TRANSFER_TITLE ="Customize File Transfer";
    private static final File STUDY_A_LIST_ARCHIVE = TestFileUtils.getSampleData("/lists/StudyA_ITNFiles.lists.zip");
    private static final String STUDY_A_FILE_NOT_THERE = "FileNotPresent.csv";
    private static final String STUDY_A_FILE_CSV1 = "GSE85530.csv";
    private static final String STUDY_A_FILE_CSV2 = "GSE85531.csv";
    private static final String STUDY_A_FILE_FASTQ = "SRR4026474.fastq";
    private static final File STUDY_B_LIST_ARCHIVE = TestFileUtils.getSampleData("/lists/StudyB_ITNFiles.lists.zip");
    private static final String STUDY_B_FILE_FASTQ1 = "SRR4026483.fastq";
    private static final String STUDY_B_FILE_FASTQ2 = "SRR4026487.fastq";
    private static final String STUDY_B_FILE_FASTQ3 = "SRR4026495.fastq";
    private static final String STUDY_B_FILE_FASTQ4 = "SRR4026496.fastq";
    private static final String STUDY_B_FILE_NOT_THERE = "ThisFileIsNotHere.csv";
    private static final String CLIENT_ID = "43b53bd7-57d2-4c9a-bogus-1b6e5fe76a07";
    private static final String CLIENT_SECRET = "r25Bf/9+SimpleTestNotHere+oqpQlrT+SSMHye1S0=";
    private static final String AUTH_URL_PREFIX = "https://auth.globusnothere.org/v2/oauth2";
    private static final String BROWSER_ENDPOINT_URL = "https://www.nothereglobussite.org/app/browse-endpoint";
    private static final String API_URL_PREFIX = "https://transfer.fakeforlabkeytest.globusonline.org/v0.10";
    private static final String UI_URL_PREFIX = "https://www.globusfakeforlabkey.org/app/transfer";
    private static final String ENDPOINT_ID = "32dcfc4e-2380-11e7-bc59-testnothere";
    private static final String ENDPOINT_NAME = "ITN on Globus";

    private static final Locator.XPathLocator OPEN_TRANSFER_LINK_BTN = Locator.lkButton("Open Transfer Link");
    private static final Locator.XPathLocator TRANSFER_BTN = Locator.tag("a").withPredicate("contains(@class, 'labkey-button') or contains(@class, 'labkey-menu-button') or contains(@class, 'labkey-disabled-button')").withText("Transfer");

    public PortalHelper portalHelper = new PortalHelper(this);

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        _containerHelper.deleteProject(getProjectName(), afterTest);
    }

    @BeforeClass
    public static void setupProject()
    {
        FileTransferTest init = (FileTransferTest)getCurrentTest();
        init.doSetup();
    }

    private void doSetup()
    {
        _containerHelper.createProject(getProjectName(), null);
        _containerHelper.enableModule("FileTransfer");
    }

    @Before
    public void preTest()
    {
        goToProjectHome();
    }

    @Test
    public void testGlobusFileTransfer()
    {
        testFileTransferSetup();
        testExternalLookupList();
        testFileTransferConfigProperties();
    }

    public void testFileTransferSetup()
    {
        log("Verifying file listing with referencing metadata list in the same container as File Transfer web part");

        log("Create the folder and import the data.");
        createFolderAndImportListArchive(STUDY_A_FOLDER, STUDY_A_LIST_ARCHIVE);
        addFileTransferWebpart(STUDY_A_FOLDER);

        String sourceEndpointDir = TestFileUtils.getSampleData("/StudyA/" + STUDY_A_FILE_CSV1).getParentFile().getParentFile().getPath();
        String containerPath = "/" + getProjectName() + "/" + STUDY_A_FOLDER;

        log("Validate message is shown if configuration is not done first.");
        CustomizeFileTransferPage customizeFileTransferPage = new CustomizeFileTransferPage(this);

        String errorMsg = customizeFileTransferPage.getErrorMessage();
        assertTrue("Error message did not contain expected text: '" + EXPECTED_ERROR_MSG + "'.", errorMsg.toLowerCase().contains(EXPECTED_ERROR_MSG));

        log("Set the File Transfer Configuration values.");
        FileTransferConfigurationPage ftConfigPageAdmin = FileTransferConfigurationPage.beginAt(this);
        ftConfigPageAdmin.setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setAuthUrlPrefix(AUTH_URL_PREFIX)
                .setBrowseEndpointUrlPrefix(BROWSER_ENDPOINT_URL)
                .setTransferApiUrlPrefix(API_URL_PREFIX)
                .setTransferUiUrlPrefix(UI_URL_PREFIX)
                .setSourceEndpointId(ENDPOINT_ID)
                .setSourceEndpointName(ENDPOINT_NAME)
                .setSourceEndpointDir(sourceEndpointDir)
                .clickSave();

        log("Go back to the project and customize the web part.");
        goToProjectHome();
        clickFolder(STUDY_A_FOLDER);

        PortalHelper portalHelper = new PortalHelper(this);
        portalHelper.clickWebpartMenuItem("File Transfer", "Customize");

        log("Set the customization values.");
        customizeFileTransferPage = new CustomizeFileTransferPage(this);
        customizeFileTransferPage.setLocalDirectory(STUDY_A)
                .setFolder(containerPath)
                .setList(STUDY_A);
        // There is a stupid bug in our list test code that doesn't wait for the drop down to clear.
        // So putting in a wait for to work around that.
        sleep(500);
        customizeFileTransferPage.setFileNameField("Filename")
                .setEndpointDirectory("/");

        customizeFileTransferPage.clickSave();

        DataRegionTable results = getFileDataRegion("metadataList_1");
        addSort(results, "Status", true);

        List<String> values = results.getColumnDataAsText("Filename");
        String columnValues = String.join(", ",values);
        log("Column Values: " + columnValues);
        assertTrue("File " + STUDY_A_FILE_NOT_THERE + " is not listed" ,values.get(0).equals(STUDY_A_FILE_NOT_THERE));
        assertTrue("File " + STUDY_A_FILE_CSV2 + " is not listed" ,values.get(1).equals(STUDY_A_FILE_CSV2));
        assertTrue("File " + STUDY_A_FILE_CSV1 + " is not listed" ,values.get(2).equals(STUDY_A_FILE_CSV1));
        assertTrue("File " + STUDY_A_FILE_FASTQ + " is not listed" ,values.get(3).equals(STUDY_A_FILE_FASTQ));

        log("Verify the available status for " + STUDY_A_FILE_NOT_THERE + ", " + STUDY_A_FILE_CSV2 + ", " + STUDY_A_FILE_CSV1 + " and " + STUDY_A_FILE_FASTQ);
        values = results.getColumnDataAsText("Available");
        assertTrue("File " + STUDY_A_FILE_NOT_THERE + " should be unavailable" ,values.get(0).equals("No"));
        assertTrue("File " + STUDY_A_FILE_CSV2 + " should be available" ,values.get(1).equals("Yes"));
        assertTrue("File " + STUDY_A_FILE_CSV1 + " should be available" ,values.get(2).equals("Yes"));
        assertTrue("File " + STUDY_A_FILE_FASTQ + " should be available" ,values.get(3).equals("Yes"));

        log("Validate that the 'Open Transfer Link' has the correct href.");
        verifyOpenTransferLinkBtnHref(UI_URL_PREFIX + "?origin_id=" + ENDPOINT_ID + "&origin_path=%2F");

        log("Select a file then validate that the 'Transfer' button is as expected.");
        results.checkCheckbox(1);
        assertTrue("Transfer button was not enabled.", isTransferBtnEnabled());
    }

    public void testExternalLookupList()
    {
        log("Verifying file listing with referencing metadata list in a different container from File Transfer web part");
        goToProjectHome();
        createFolderAndImportListArchive(STUDY_B_FOLDER, STUDY_B_LIST_ARCHIVE);
        _containerHelper.createSubfolder(getProjectName(), STUDY_B_FILE_TRANSFER_FOLDER);
        addFileTransferWebpart(STUDY_B_FILE_TRANSFER_FOLDER);

        log("Config web part on File Transfer Customize page");
        String containerPath = "/" + getProjectName() + "/" + STUDY_B_FOLDER;

        CustomizeFileTransferPage customizeFileTransferPage = new CustomizeFileTransferPage(this);

        customizeFileTransferPage.setLocalDirectory(STUDY_B)
                .setFolder(containerPath)
                .setList(STUDY_B);
        // There is a stupid bug in our list test code that doesn't wait for the drop down to clear.
        // So putting in a wait for to work around that.
        sleep(500);
        customizeFileTransferPage.setFileNameField("Filename")
                .setEndpointDirectory("/");
        customizeFileTransferPage.clickSave();

        DataRegionTable results = getFileDataRegion("metadataList_3");
        addSort(results, "Status", true);

        List<String> values = results.getColumnDataAsText("Filename");
        String columnValues = String.join(", ",values);
        log("Column Values: " + columnValues);
        assertTrue("File " + STUDY_B_FILE_NOT_THERE + " is not listed" ,values.get(0).equals(STUDY_B_FILE_NOT_THERE));
        assertTrue("File " + STUDY_B_FILE_FASTQ4 + " is not listed" ,values.get(1).equals(STUDY_B_FILE_FASTQ4));
        assertTrue("File " + STUDY_B_FILE_FASTQ3 + " is not listed" ,values.get(2).equals(STUDY_B_FILE_FASTQ3));
        assertTrue("File " + STUDY_B_FILE_FASTQ2 + " is not listed" ,values.get(3).equals(STUDY_B_FILE_FASTQ2));
        assertTrue("File " + STUDY_B_FILE_FASTQ1 + " is not listed" ,values.get(4).equals(STUDY_B_FILE_FASTQ1));

        log("Verify the available status for " + STUDY_B_FILE_NOT_THERE + ", " + STUDY_B_FILE_FASTQ4 + ", " + STUDY_B_FILE_FASTQ3 + ", " + STUDY_B_FILE_FASTQ2 + " and " + STUDY_B_FILE_FASTQ1);
        values = results.getColumnDataAsText("Available");
        assertTrue("File " + STUDY_B_FILE_NOT_THERE + " should be unavailable" ,values.get(0).equals("No"));
        assertTrue("File " + STUDY_B_FILE_FASTQ4 + " should be available" ,values.get(1).equals("Yes"));
        assertTrue("File " + STUDY_B_FILE_FASTQ3 + " should be available" ,values.get(2).equals("Yes"));
        assertTrue("File " + STUDY_B_FILE_FASTQ2 + " should be available" ,values.get(3).equals("Yes"));
        assertTrue("File " + STUDY_B_FILE_FASTQ1 + " should be available" ,values.get(4).equals("Yes"));

        log("Validate that the 'Open Transfer Link' has the correct href.");
        verifyOpenTransferLinkBtnHref(UI_URL_PREFIX + "?origin_id=" + ENDPOINT_ID + "&origin_path=%2F");

        log("Validate that the 'Transfer button is not enabled if no files are selected.");
        Assert.assertFalse("Transfer button was enabled it should not be.", isTransferBtnEnabled());

        log("Select all the files then validate that the 'Transfer' button is as expected.");
        results.checkAll();
        assertTrue("Transfer button was not enabled.", isTransferBtnEnabled());
    }

    public void testFileTransferConfigProperties()
    {

        log("Validate that required fields generate an error message.");

        FileTransferConfigurationPage ftConfigPageAdmin = FileTransferConfigurationPage.beginAt(this);

        // Clearing the text fields happens quickly so adding sleep to give the attribute a chance to be updated.
        ftConfigPageAdmin.setClientId("");
        sleep(500);
        assertTrue("There was no alert message after setting the Client Id to an empty string.", ftConfigPageAdmin.getClientIdAlert().length() > 0);
        ftConfigPageAdmin.setClientId(CLIENT_ID);

        ftConfigPageAdmin.setClientSecret("");
        sleep(500);
        assertTrue("There was no alert message after setting the Client Secret to an empty string.", ftConfigPageAdmin.getClientSecretAlert().length() > 0);
        ftConfigPageAdmin.setClientSecret(CLIENT_SECRET);

        ftConfigPageAdmin.setAuthUrlPrefix("");
        sleep(500);
        assertTrue("There was no alert message after setting the Authorization URL Prefix to an empty string.", ftConfigPageAdmin.getAuthUrlPrefixAlert().length() > 0);
        ftConfigPageAdmin.setAuthUrlPrefix(AUTH_URL_PREFIX);

        ftConfigPageAdmin.setBrowseEndpointUrlPrefix("");
        sleep(500);
        assertTrue("There was no alert message after setting the Browse Endpoint URL Prefix to an empty string.", ftConfigPageAdmin.getBrowseEndpointUrlPrefixAlert().length() > 0);
        ftConfigPageAdmin.setBrowseEndpointUrlPrefix(BROWSER_ENDPOINT_URL);

        ftConfigPageAdmin.setTransferApiUrlPrefix("");
        sleep(500);
        assertTrue("There was no alert message after setting the Transfer API URL Prefix to an empty string.", ftConfigPageAdmin.getTransferApiUrlPrefixAlert().length() > 0);
        ftConfigPageAdmin.setTransferApiUrlPrefix(API_URL_PREFIX);

        ftConfigPageAdmin.clickCancel();

    }

    private void createFolderAndImportListArchive(String container, File listArchive)
    {
        _containerHelper.createSubfolder(getProjectName(), container);
        _listHelper.importListArchive(container, listArchive);
    }

    private void addFileTransferWebpart(String container)
    {
        log("Adding File Transfer to home page under " + container + " folder");
        clickFolder(container);
        portalHelper.addWebPart("File Transfer");

        log("Verify web part message without existing configuration");
        waitForElement(Locator.tagContainingText("span", CUSTOMIZE_FILE_TRANSFER_TITLE));
    }

    private DataRegionTable getFileDataRegion(String regionName)
    {
        DataRegionTable results = new DataRegionTable(regionName, this);
        assertElementNotPresent(Locator.tagContainingText("td", ABSENT_CONFIG_MSG));
        return results;
    }

    private void addSort(DataRegionTable table, String columnName, boolean isDescending)
    {
        CustomizeView helper = table.getCustomizeView();
        helper.openCustomizeViewPanel();
        helper.addSort(columnName, isDescending ? SortDirection.DESC : SortDirection.ASC);
        helper.saveCustomView();
    }

    private void verifyOpenTransferLinkBtnHref(String expectedHref)
    {
        waitForElement(OPEN_TRANSFER_LINK_BTN);
        assertEquals("Unexpected generated transfer link URL", expectedHref, getButtonHref(OPEN_TRANSFER_LINK_BTN, false));
    }

    private boolean isTransferBtnEnabled()
    {
        waitForElement(TRANSFER_BTN);
        String classAttr = getAttribute(TRANSFER_BTN, "class");
        if(classAttr.toLowerCase().contains("labkey-disabled-button"))
            return false;
        else
            return true;
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    protected String getProjectName()
    {
        return "FileTransferTest Project";
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Collections.singletonList("FileTransfer");
    }
}