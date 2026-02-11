import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Class to handle connections to Samba/SMB servers and retrieve files and directories recursively.
 */
public class SambaServerHandler {
    
    private String serverAddress;
    private String username;
    private String password;
    private String domain;
    private NtlmPasswordAuthentication authentication;
    
    /**
     * Constructor to initialize Samba server connection parameters.
     *
     * @param serverAddress  The SMB server address (e.g., "smb://192.168.1.100/share/")
     * @param domain         The domain/workgroup (can be null or empty)
     * @param username       The username for authentication (can be null for guest access)
     * @param password       The password for authentication (can be null for guest access)
     */
    public SambaServerHandler(String serverAddress, String domain, String username, String password) {
        this.serverAddress = serverAddress;
        this.username = username;
        this.password = password;
        this.domain = domain != null ? domain : "";
        
        // Initialize authentication (null means guest access)
        if (username != null && password != null) {
            this.authentication = new NtlmPasswordAuthentication(this.domain, username, password);
        }
    }
    
    /**
     * Connect to the Samba server and test the connection.
     *
     * @return true if connection is successful, false otherwise
     */
    public boolean connect() {
        try {
            SmbFile smbFile = new SmbFile(serverAddress, authentication);
            smbFile.listFiles();
            System.out.println("Successfully connected to Samba server: " + serverAddress);
            return true;
        } catch (SmbAuthException e) {
            System.err.println("Authentication failed: " + e.getMessage());
            return false;
        } catch (SmbException e) {
            System.err.println("Failed to connect to Samba server: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error during connection: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieve all files and directories recursively from the Samba server.
     *
     * @return A list of SambaFile objects containing file/directory information
     * @throws Exception if connection or retrieval fails
     */
    public List<SambaFile> getFilesRecursively() throws Exception {
        List<SambaFile> results = new ArrayList<>();
        SmbFile rootFile = new SmbFile(serverAddress, authentication);
        traverseDirectory(rootFile, "", results);
        return results;
    }
    
    /**
     * Retrieve all files and directories recursively from a specific path.
     *
     * @param path The relative path within the share to start from
     * @return A list of SambaFile objects containing file/directory information
     * @throws Exception if connection or retrieval fails
     */
    public List<SambaFile> getFilesRecursivelyFromPath(String path) throws Exception {
        List<SambaFile> results = new ArrayList<>();
        String fullPath = serverAddress;
        if (!path.isEmpty() && !path.startsWith("/")) {
            fullPath += "/" + path;
        } else if (!path.isEmpty()) {
            fullPath += path;
        }
        
        if (!fullPath.endsWith("/")) {
            fullPath += "/";
        }
        
        SmbFile rootFile = new SmbFile(fullPath, authentication);
        traverseDirectory(rootFile, path, results);
        return results;
    }
    
    /**
     * Recursively traverse directories and collect files and subdirectories.
     *
     * @param currentDirectory The current SmbFile directory being traversed
     * @param relativePath     The relative path from the share root
     * @param results          The list to accumulate results
     */
    private void traverseDirectory(SmbFile currentDirectory, String relativePath, List<SambaFile> results) {
        try {
            SmbFile[] files = currentDirectory.listFiles();
            
            for (SmbFile file : files) {
                String fileName = file.getName().replaceAll("/$", "");  // Remove trailing slash
                String filePath = relativePath.isEmpty() ? fileName : relativePath + "/" + fileName;
                
                SambaFile sambaFile = new SambaFile(
                    fileName,
                    filePath,
                    file.isDirectory(),
                    file.length(),
                    file.lastModified(),
                    file.getPath()
                );
                
                results.add(sambaFile);
                
                // Recursively traverse subdirectories
                if (file.isDirectory()) {
                    traverseDirectory(file, filePath, results);
                }
            }
        } catch (SmbException e) {
            System.err.println("Error traversing directory: " + e.getMessage());
        }
    }
    
    /**
     * Get files only (excluding directories) recursively.
     *
     * @return A list of SambaFile objects representing only files
     * @throws Exception if connection or retrieval fails
     */
    public List<SambaFile> getFilesOnly() throws Exception {
        List<SambaFile> allItems = getFilesRecursively();
        List<SambaFile> filesOnly = new ArrayList<>();
        
        for (SambaFile item : allItems) {
            if (!item.isDirectory()) {
                filesOnly.add(item);
            }
        }
        
        return filesOnly;
    }
    
    /**
     * Get directories only (excluding files) recursively.
     *
     * @return A list of SambaFile objects representing only directories
     * @throws Exception if connection or retrieval fails
     */
    public List<SambaFile> getDirectoriesOnly() throws Exception {
        List<SambaFile> allItems = getFilesRecursively();
        List<SambaFile> directoriesOnly = new ArrayList<>();
        
        for (SambaFile item : allItems) {
            if (item.isDirectory()) {
                directoriesOnly.add(item);
            }
        }
        
        return directoriesOnly;
    }
    
    /**
     * Search for files matching a specific pattern.
     *
     * @param pattern A regex pattern to match file names
     * @return A list of matching SambaFile objects
     * @throws Exception if connection or retrieval fails
     */
    public List<SambaFile> searchFiles(String pattern) throws Exception {
        List<SambaFile> allFiles = getFilesOnly();
        List<SambaFile> matchingFiles = new ArrayList<>();
        
        for (SambaFile file : allFiles) {
            if (file.getName().matches(pattern)) {
                matchingFiles.add(file);
            }
        }
        
        return matchingFiles;
    }
    
    /**
     * Print all files and directories in a formatted manner.
     *
     * @param items The list of SambaFile objects to print
     */
    public void printFileList(List<SambaFile> items) {
        System.out.println("\n=== File Listing ===");
        System.out.printf("%-50s | %-12s | %-15s | %s%n", "Path", "Type", "Size", "Last Modified");
        System.out.println("-".repeat(110));
        
        for (SambaFile item : items) {
            String type = item.isDirectory() ? "Directory" : "File";
            String size = item.isDirectory() ? "-" : formatSize(item.getSize());
            String modified = new Date(item.getLastModified()).toString();
            
            System.out.printf("%-50s | %-12s | %-15s | %s%n", 
                item.getPath(), type, size, modified);
        }
        
        System.out.println("\nTotal items: " + items.size());
    }
    
    /**
     * Format file size for display.
     *
     * @param bytes The size in bytes
     * @return Formatted size string
     */
    private String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * Inner class to represent a file or directory on the Samba server.
     */
    public static class SambaFile {
        private String name;
        private String path;
        private boolean isDirectory;
        private long size;
        private long lastModified;
        private String fullPath;
        
        public SambaFile(String name, String path, boolean isDirectory, long size, long lastModified, String fullPath) {
            this.name = name;
            this.path = path;
            this.isDirectory = isDirectory;
            this.size = size;
            this.lastModified = lastModified;
            this.fullPath = fullPath;
        }
        
        public String getName() { return name; }
        public String getPath() { return path; }
        public boolean isDirectory() { return isDirectory; }
        public long getSize() { return size; }
        public long getLastModified() { return lastModified; }
        public String getFullPath() { return fullPath; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s (%s bytes, modified: %s)", 
                isDirectory ? "DIR" : "FILE", 
                path, 
                size, 
                new Date(lastModified));
        }
    }
    
    /**
     * Example usage of the SambaServerHandler class.
     */
    public static void main(String[] args) {
        // Configure these with your actual Samba server details
        String serverAddress = "smb://mubuntu.local/sambashare/";  // Change to your server
        String domain = "WORKGROUP";                             // Change if needed
        String username = "ubuntu";                                // Change to your username
        String password = "passfromcl";                            // Change to your password
        
        SambaServerHandler handler = new SambaServerHandler(serverAddress, domain, username, password);
        
        // Test connection
        if (!handler.connect()) {
            System.err.println("Failed to connect to Samba server. Exiting.");
            System.exit(1);
        }
        
        try {
            // Get all files and directories
            System.out.println("\n=== Getting all files and directories ===");
            List<SambaFile> allItems = handler.getFilesRecursively();
            handler.printFileList(allItems);
            
            // Get files only
            System.out.println("\n=== Getting files only ===");
            List<SambaFile> filesOnly = handler.getFilesOnly();
            handler.printFileList(filesOnly);
            
            // Get directories only
            System.out.println("\n=== Getting directories only ===");
            List<SambaFile> directoriesOnly = handler.getDirectoriesOnly();
            handler.printFileList(directoriesOnly);
            
            // Search for files matching a pattern (e.g., all .txt files)
            System.out.println("\n=== Searching for .txt files ===");
            List<SambaFile> txtFiles = handler.searchFiles(".*\\.txt$");
            handler.printFileList(txtFiles);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
