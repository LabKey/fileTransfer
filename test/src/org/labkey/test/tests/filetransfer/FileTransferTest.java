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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.TestFileUtils;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.FileTransfer;
import org.labkey.test.categories.Git;
import org.labkey.test.pages.filetransfer.FileTransferConfigPage;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.PortalHelper;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

@Category({Git.class, FileTransfer.class})
public class FileTransferTest extends BaseWebDriverTest
{
    private static final String STUDY_A_FOLDER = "StudyAFolder";
    private static final String SOURCE_FOLDER_B = "StudyBListFolder";
    private static final String FILE_TRANSFER_FOLDER_B = "StudyBFileTransferFolder";
    private static final String ABSENT_CONFIG_MSG ="No metadata list currently configured for this container.";
    private static final String STUDY_A_FILE1 = "studyA_RNASeq.fasta";
    private static final String STUDY_A_FILE2 = "studyA_conclusion.pdf";
    private static final String STUDY_B_FILE1 = "studyB_cDNA.seq";
    private static final String STUDY_B_FILE3 = "studyB_rna.fasta";

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
        if (_containerHelper.doesFolderExist(getProjectName(), getProjectName(), STUDY_A_FOLDER))
            _containerHelper.deleteFolder(getProjectName(), STUDY_A_FOLDER);
        if (_containerHelper.doesFolderExist(getProjectName(), getProjectName(), FILE_TRANSFER_FOLDER_B))
            _containerHelper.deleteFolder(getProjectName(), FILE_TRANSFER_FOLDER_B);
        if (_containerHelper.doesFolderExist(getProjectName(), getProjectName(), SOURCE_FOLDER_B))
            _containerHelper.deleteFolder(getProjectName(), SOURCE_FOLDER_B);
        goToProjectHome();
    }

    @Test
    public void testFileTransferSetup()
    {
        log("Verifying file listing with referencing metadata list in the same container as File Transfer web part");
        _containerHelper.createSubfolder(getProjectName(), STUDY_A_FOLDER);

        log("Import StudyAList.lists.zip into " + STUDY_A_FOLDER + " folder");
        File listFile = TestFileUtils.getSampleData("/lists/StudyAList.lists.zip");
        _listHelper.importListArchive(STUDY_A_FOLDER, listFile);
        goToProjectHome();
        clickFolder(STUDY_A_FOLDER);

        log("Adding File Transfer Metadata to home page under " + STUDY_A_FOLDER + " folder");
        portalHelper.addWebPart("File Transfer Metadata");

        log("Verify web part message without exising configuration");
        assertTextPresent(ABSENT_CONFIG_MSG);

        log("Config web part on File Transfer Set-Up page");
        portalHelper.clickWebpartMenuItem("File Transfer", "Customize");
        FileTransferConfigPage configPage = new FileTransferConfigPage(this);
        File studyAPath = TestFileUtils.getSampleData("/StudyA/studyA_figure1.png").getParentFile();
        String containerPath = "/" + getProjectName() + "/" + STUDY_A_FOLDER;
        configPage.saveConfig(studyAPath.getPath(), containerPath, "StudyA", "Filename");

        clickFolder(STUDY_A_FOLDER);
        assertTextNotPresent(ABSENT_CONFIG_MSG);
        DataRegionTable results = new DataRegionTable("query", this);

        List<String> values = results.getColumnDataAsText("Filename");
        String columnValues = String.join(", ",values);
        log("Column Values: " + columnValues);
        assertTrue("File " + STUDY_A_FILE1 + " is not listed" ,values.get(0).equals(STUDY_A_FILE1));
        assertTrue("File " + STUDY_A_FILE2 + " is not listed" ,values.get(1).equals(STUDY_A_FILE2));

        log("Verify the available status for " + STUDY_A_FILE1 + ", " + STUDY_A_FILE2);
        values = results.getColumnDataAsText("Available");
        assertTrue("File " + STUDY_A_FILE1 + " should be available" ,values.get(0).equals("Yes"));
        assertTrue("File " + STUDY_A_FILE2 + " should be unavailable" ,values.get(1).equals("No"));

    }

    @Test
    public void testExternalLookupList()
    {
        log("Verifying file listing with referencing metadata list in a different container from File Transfer web part");
        _containerHelper.createSubfolder(getProjectName(), SOURCE_FOLDER_B);

        log("Import StudyBList.lists.zip into " + SOURCE_FOLDER_B + " folder");
        File listFile = TestFileUtils.getSampleData("/lists/StudyBList.lists.zip");
        _listHelper.importListArchive(SOURCE_FOLDER_B, listFile);
        goToProjectHome();

        _containerHelper.createSubfolder(getProjectName(), FILE_TRANSFER_FOLDER_B);
        log("Adding File Transfer Metadata to home page under " + FILE_TRANSFER_FOLDER_B + " folder");
        portalHelper.addWebPart("File Transfer Metadata");

        log("Config web part on File Transfer Set-Up page");
        portalHelper.clickWebpartMenuItem("File Transfer", "Customize");
        FileTransferConfigPage configPage = new FileTransferConfigPage(this);
        File studyAPath = TestFileUtils.getSampleData("/StudyB/studyB_figure1.png").getParentFile();
        configPage.saveConfig(studyAPath.getPath(), "/" + getProjectName() + "/" + SOURCE_FOLDER_B, "StudyB", "Filename");

        clickFolder(FILE_TRANSFER_FOLDER_B);
        DataRegionTable results = new DataRegionTable("query", this);

        List<String> values = results.getColumnDataAsText("Filename");
        String columnValues = String.join(", ",values);
        log("Column Values: " + columnValues);
        assertTrue("File " + STUDY_B_FILE1 + " is not listed" ,values.get(0).equals(STUDY_B_FILE1));
        assertTrue("File " + STUDY_B_FILE3 + " is not listed" ,values.get(2).equals(STUDY_B_FILE3));

        log("Verify the available status for " + STUDY_B_FILE1 + ", " + STUDY_B_FILE3);
        values = results.getColumnDataAsText("Available");
        assertTrue("File " + STUDY_B_FILE1 + " should be available" ,values.get(0).equals("Yes"));
        assertTrue("File " + STUDY_B_FILE3 + " should be unavailable" ,values.get(2).equals("No"));

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