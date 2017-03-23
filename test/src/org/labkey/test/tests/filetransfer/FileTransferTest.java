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

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.ModulePropertyValue;
import org.labkey.test.SortDirection;
import org.labkey.test.TestFileUtils;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.Git;
import org.labkey.test.components.CustomizeView;
import org.labkey.test.pages.filetransfer.FileTransferConfigPage;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.PortalHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category({Git.class})
public class FileTransferTest extends BaseWebDriverTest
{
    private static final String STUDY_A_FOLDER = "StudyAFolder";
    private static final String STUDY_B_FOLDER = "StudyBListFolder";
    private static final String STUDY_B_FILE_TRANSFER_FOLDER = "StudyBFileTransferFolder";
    private static final String STUDY_C_FOLDER = "StudyCFolder";
    private static final String ABSENT_CONFIG_MSG ="No metadata list currently configured for this container.";
    private static final File STUDY_A_LIST_ARCHIVE = TestFileUtils.getSampleData("/lists/StudyAList.lists.zip");
    private static final String STUDY_A_FILE_PDF = "studyA_conclusion.pdf";
    private static final String STUDY_A_FILE_FASTA = "studyA_RNASeq.fasta";
    private static final File STUDY_B_LIST_ARCHIVE = TestFileUtils.getSampleData("/lists/StudyBList.lists.zip");
    private static final String STUDY_B_FILE_PNG = "studyB_figure1.png";
    private static final String STUDY_B_FILE_FASTA = "studyB_rna.fasta";
    private static final Locator.XPathLocator TRANSFER_LINK_BTN = Locator.lkButton("Open Transfer Link");

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
    public void testFileTransferSetup()
    {
        log("Verifying file listing with referencing metadata list in the same container as File Transfer web part");
        createFolderAndImportListArchive(STUDY_A_FOLDER, STUDY_A_LIST_ARCHIVE);
        addFileTransferMetadataWebpart(STUDY_A_FOLDER);

        log("Config web part on File Transfer Customize page");
        String studyPath = TestFileUtils.getSampleData("/StudyA/studyA_figure1.png").getParentFile().getPath();
        String containerPath = "/" + getProjectName() + "/" + STUDY_A_FOLDER;
        DataRegionTable results = initialFileTransferWebpartConfig(studyPath, containerPath, "StudyA", "Filename");
        addSort(results, "Status", true);

        List<String> values = results.getColumnDataAsText("Filename");
        String columnValues = String.join(", ",values);
        log("Column Values: " + columnValues);
        assertTrue("File " + STUDY_A_FILE_PDF + " is not listed" ,values.get(0).equals(STUDY_A_FILE_PDF));
        assertTrue("File " + STUDY_A_FILE_FASTA + " is not listed" ,values.get(1).equals(STUDY_A_FILE_FASTA));

        log("Verify the available status for " + STUDY_A_FILE_PDF + ", " + STUDY_A_FILE_FASTA);
        values = results.getColumnDataAsText("Available");
        assertTrue("File " + STUDY_A_FILE_PDF + " should be unavailable" ,values.get(0).equals("No"));
        assertTrue("File " + STUDY_A_FILE_FASTA + " should be available" ,values.get(1).equals("Yes"));
    }

    @Test
    public void testExternalLookupList()
    {
        log("Verifying file listing with referencing metadata list in a different container from File Transfer web part");
        createFolderAndImportListArchive(STUDY_B_FOLDER, STUDY_B_LIST_ARCHIVE);
        _containerHelper.createSubfolder(getProjectName(), STUDY_B_FILE_TRANSFER_FOLDER);
        addFileTransferMetadataWebpart(STUDY_B_FILE_TRANSFER_FOLDER);

        log("Config web part on File Transfer Customize page");
        String studyPath = TestFileUtils.getSampleData("/StudyB/studyB_figure1.png").getParentFile().getPath();
        String containerPath = "/" + getProjectName() + "/" + STUDY_B_FOLDER;
        DataRegionTable results = initialFileTransferWebpartConfig(studyPath, containerPath, "StudyB", "Filename");
        addSort(results, "Status", true);

        List<String> values = results.getColumnDataAsText("Filename");
        String columnValues = String.join(", ",values);
        log("Column Values: " + columnValues);
        assertTrue("File " + STUDY_B_FILE_FASTA + " is not listed" ,values.get(0).equals(STUDY_B_FILE_FASTA));
        assertTrue("File " + STUDY_B_FILE_PNG + " is not listed" ,values.get(1).equals(STUDY_B_FILE_PNG));

        log("Verify the available status for " + STUDY_B_FILE_FASTA + ", " + STUDY_B_FILE_PNG);
        values = results.getColumnDataAsText("Available");
        assertTrue("File " + STUDY_B_FILE_FASTA + " should be unavailable" ,values.get(0).equals("No"));
        assertTrue("File " + STUDY_B_FILE_PNG + " should be available" ,values.get(1).equals("Yes"));
    }

    @Test
    public void testServiceUrlProperties()
    {
        String projectPath = "/" + getProjectName();
        String containerPath = projectPath + "/" + STUDY_C_FOLDER;
        String projectServiceBaseUrl = WebTestHelper.buildURL("filetransfer", projectPath, "begin");
        String containerServiceBaseUrl = WebTestHelper.buildURL("filetransfer", containerPath, "begin");

        log("Verifying File Transfer web part 'Open Transfer Link' button");
        createFolderAndImportListArchive(STUDY_C_FOLDER, STUDY_A_LIST_ARCHIVE);
        addFileTransferMetadataWebpart(STUDY_C_FOLDER);

        log("Initial config of File Transfer webpart, verify 'Open Transfer Link' not shown");
        String studyPath = TestFileUtils.getSampleData("/StudyA/studyA_figure1.png").getParentFile().getPath();
        DataRegionTable results = initialFileTransferWebpartConfig(studyPath, containerPath, "StudyA", "Filename");
        assertElementNotPresent(TRANSFER_LINK_BTN);

        log("Fail test now if the 'Site Default' is set for the FileTransfer module properties.");
        String siteServiceBaseUrl = getModulePropertyValue(new ModulePropertyValue("FileTransfer", "/", "FileTransferServiceBaseUrl", null));
        String siteSourceEndpointId = getModulePropertyValue(new ModulePropertyValue("FileTransfer", "/", "FileTransferSourceEndpointId", null));
        if (!StringUtils.isEmpty(siteServiceBaseUrl) || !StringUtils.isEmpty(siteSourceEndpointId))
            fail("FileTransfer module properties 'Site Default' values have been set which will cause issues with this test. Please clear those values and run the test again.");

        log("Set project level module properties, verify 'Open Transfer Link' href");
        String sourceEndpointId = "projectOrigin";
        setFileTransferModuleProperties(projectPath, projectServiceBaseUrl, sourceEndpointId);
        verifyTransferFileLinkBtnHref(STUDY_C_FOLDER, projectServiceBaseUrl + "?origin_id=" + sourceEndpointId);

        log("Clear project level endpoint ID module properties, verify 'Open Transfer Link' not shown");
        setFileTransferModuleProperties(projectPath, projectServiceBaseUrl, null);
        clickFolder(STUDY_C_FOLDER);
        assertElementNotPresent(TRANSFER_LINK_BTN);

        log("Set container level module properties, verify 'Open Transfer Link' href");
        sourceEndpointId = "containerOrigin";
        setFileTransferModuleProperties(containerPath, containerServiceBaseUrl, sourceEndpointId);
        verifyTransferFileLinkBtnHref(STUDY_C_FOLDER, containerServiceBaseUrl + "?origin_id=" + sourceEndpointId);

        log("Set File Transfer webpart endpoint directory, verify 'Open Transfer Link' href");
        String sourceEndpointDir = "studyA/files location/dir";
        portalHelper.clickWebpartMenuItem("File Transfer", "Customize");
        FileTransferConfigPage configPage = new FileTransferConfigPage(this);
        configPage.setSourceEndpointDir(sourceEndpointDir);
        configPage.save();
        verifyTransferFileLinkBtnHref(STUDY_C_FOLDER, containerServiceBaseUrl + "?origin_id=" + sourceEndpointId
                + "&origin_path=" + sourceEndpointDir.replaceAll(" ", "%20").replaceAll("/", "%2F"));
    }

    private void createFolderAndImportListArchive(String container, File listArchive)
    {
        _containerHelper.createSubfolder(getProjectName(), container);
        _listHelper.importListArchive(container, listArchive);
    }

    private void addFileTransferMetadataWebpart(String container)
    {
        log("Adding File Transfer Metadata to home page under " + container + " folder");
        clickFolder(container);
        portalHelper.addWebPart("File Transfer Metadata");

        log("Verify web part message without existing configuration");
        waitForElement(Locator.tagContainingText("td", ABSENT_CONFIG_MSG));
    }

    private DataRegionTable initialFileTransferWebpartConfig(String localFolderPath, String referenceListPath, String listName, String fieldName)
    {
        portalHelper.clickWebpartMenuItem("File Transfer", "Customize");
        FileTransferConfigPage configPage = new FileTransferConfigPage(this);
        configPage.saveConfig(localFolderPath, referenceListPath, listName, fieldName);

        DataRegionTable results = new DataRegionTable("query", this);
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

    private void setFileTransferModuleProperties(String containerPath, String serviceBaseUrl, String sourceEndpointId)
    {
        List<ModulePropertyValue> properties = new ArrayList<>();
        properties.add(new ModulePropertyValue("FileTransfer", containerPath, "FileTransferServiceBaseUrl", serviceBaseUrl));
        properties.add(new ModulePropertyValue("FileTransfer", containerPath, "FileTransferSourceEndpointId", sourceEndpointId));
        setModuleProperties(properties);
    }

    private void verifyTransferFileLinkBtnHref(String container, String expectedHref)
    {
        clickFolder(container);
        waitForElement(TRANSFER_LINK_BTN);
        assertEquals("Unexpected generated transfer link URL", expectedHref, getButtonHref(TRANSFER_LINK_BTN, false));
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