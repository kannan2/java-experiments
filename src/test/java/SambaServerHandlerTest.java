import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for SambaServerHandler class.
 */
public class SambaServerHandlerTest {

    private SambaServerHandler handler;
    private static final String TEST_SERVER = "smb://testserver.local/share/";
    private static final String TEST_DOMAIN = "TESTDOMAIN";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpass";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConstructorWithValidParameters() {
        handler = new SambaServerHandler(TEST_SERVER, TEST_DOMAIN, TEST_USERNAME, TEST_PASSWORD);
        assertNotNull("Handler should be created", handler);
    }

    @Test
    public void testConstructorWithNullDomain() {
        handler = new SambaServerHandler(TEST_SERVER, null, TEST_USERNAME, TEST_PASSWORD);
        assertNotNull("Handler should be created with null domain", handler);
    }

    @Test
    public void testConstructorWithGuestAccess() {
        handler = new SambaServerHandler(TEST_SERVER, TEST_DOMAIN, null, null);
        assertNotNull("Handler should be created for guest access", handler);
    }

    @Test
    public void testConnectWithInvalidServerAddress() {
        handler = new SambaServerHandler("invalid-address", TEST_DOMAIN, TEST_USERNAME, TEST_PASSWORD);
        boolean result = handler.connect();
        assertFalse("Connection should fail with invalid address", result);
    }

    @Test
    public void testConnectWithServerAddressWithoutTrailingSlash() {
        handler = new SambaServerHandler("smb://testserver.local/share", TEST_DOMAIN, TEST_USERNAME, TEST_PASSWORD);
        // This will fail to connect but should validate the address format
        boolean result = handler.connect();
        // Expected to fail since we can't actually connect, but format validation should pass
        assertFalse("Connection should fail (no real server)", result);
    }

    @Test
    public void testSambaFileCreation() {
        SambaServerHandler.SambaFile file = new SambaServerHandler.SambaFile(
            "testfile.txt",
            "folder/testfile.txt",
            false,
            1024L,
            System.currentTimeMillis(),
            "smb://server/share/folder/testfile.txt"
        );

        assertEquals("File name should match", "testfile.txt", file.getName());
        assertEquals("File path should match", "folder/testfile.txt", file.getPath());
        assertFalse("Should not be a directory", file.isDirectory());
        assertEquals("File size should match", 1024L, file.getSize());
        assertNotNull("Full path should not be null", file.getFullPath());
    }

    @Test
    public void testSambaFileIsDirectory() {
        SambaServerHandler.SambaFile directory = new SambaServerHandler.SambaFile(
            "testfolder",
            "testfolder",
            true,
            0L,
            System.currentTimeMillis(),
            "smb://server/share/testfolder/"
        );

        assertTrue("Should be a directory", directory.isDirectory());
        assertEquals("Directory size should be 0", 0L, directory.getSize());
    }

    @Test
    public void testSambaFileToString() {
        long timestamp = System.currentTimeMillis();
        SambaServerHandler.SambaFile file = new SambaServerHandler.SambaFile(
            "test.txt",
            "folder/test.txt",
            false,
            2048L,
            timestamp,
            "smb://server/share/folder/test.txt"
        );

        String fileString = file.toString();
        assertNotNull("toString should not return null", fileString);
        assertTrue("toString should contain FILE", fileString.contains("FILE"));
        assertTrue("toString should contain path", fileString.contains("folder/test.txt"));
        assertTrue("toString should contain size", fileString.contains("2048"));
    }

    @Test
    public void testSambaDirectoryToString() {
        SambaServerHandler.SambaFile directory = new SambaServerHandler.SambaFile(
            "folder",
            "folder",
            true,
            0L,
            System.currentTimeMillis(),
            "smb://server/share/folder/"
        );

        String dirString = directory.toString();
        assertNotNull("toString should not return null", dirString);
        assertTrue("toString should contain DIR", dirString.contains("DIR"));
    }

    @Test
    public void testMultipleHandlerInstances() {
        SambaServerHandler handler1 = new SambaServerHandler(
            "smb://server1/share/", "DOMAIN1", "user1", "pass1"
        );
        SambaServerHandler handler2 = new SambaServerHandler(
            "smb://server2/share/", "DOMAIN2", "user2", "pass2"
        );

        assertNotNull("First handler should be created", handler1);
        assertNotNull("Second handler should be created", handler2);
        assertNotSame("Handlers should be different instances", handler1, handler2);
    }

    @Test
    public void testPrintFileListWithEmptyList() {
        handler = new SambaServerHandler(TEST_SERVER, TEST_DOMAIN, TEST_USERNAME, TEST_PASSWORD);
        List<SambaServerHandler.SambaFile> emptyList = new java.util.ArrayList<>();

        // Should not throw exception with empty list
        try {
            handler.printFileList(emptyList);
        } catch (Exception e) {
            fail("Should not throw exception with empty list: " + e.getMessage());
        }
    }

    @Test
    public void testPrintFileListWithMultipleItems() {
        handler = new SambaServerHandler(TEST_SERVER, TEST_DOMAIN, TEST_USERNAME, TEST_PASSWORD);
        List<SambaServerHandler.SambaFile> files = new java.util.ArrayList<>();

        files.add(new SambaServerHandler.SambaFile(
            "file1.txt", "file1.txt", false, 1024L, System.currentTimeMillis(), "smb://server/file1.txt"
        ));
        files.add(new SambaServerHandler.SambaFile(
            "folder1", "folder1", true, 0L, System.currentTimeMillis(), "smb://server/folder1/"
        ));

        // Should not throw exception
        try {
            handler.printFileList(files);
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testSambaFileGetters() {
        String name = "document.pdf";
        String path = "docs/document.pdf";
        boolean isDir = false;
        long size = 50000L;
        long modified = System.currentTimeMillis();
        String fullPath = "smb://server/share/docs/document.pdf";

        SambaServerHandler.SambaFile file = new SambaServerHandler.SambaFile(
            name, path, isDir, size, modified, fullPath
        );

        assertEquals("getName() should return correct value", name, file.getName());
        assertEquals("getPath() should return correct value", path, file.getPath());
        assertEquals("isDirectory() should return correct value", isDir, file.isDirectory());
        assertEquals("getSize() should return correct value", size, file.getSize());
        assertEquals("getLastModified() should return correct value", modified, file.getLastModified());
        assertEquals("getFullPath() should return correct value", fullPath, file.getFullPath());
    }

    @Test
    public void testConstructorWithEmptyStrings() {
        handler = new SambaServerHandler(TEST_SERVER, "", "", "");
        assertNotNull("Handler should be created with empty strings", handler);
    }

    @Test
    public void testServerAddressValidation() {
        // Test with address not starting with smb://
        handler = new SambaServerHandler("http://wrongprotocol/share/", TEST_DOMAIN, TEST_USERNAME, TEST_PASSWORD);
        boolean result = handler.connect();
        assertFalse("Should fail with wrong protocol", result);
    }
}
