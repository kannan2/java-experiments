# Samba Server Handler - Java Class

A comprehensive Java utility class for connecting to Samba/SMB servers and retrieving files and directories recursively.

## Features

- **Connect to SMB/Samba servers** with authentication support
- **Recursive file and directory listing** 
- **Filter files or directories** separately
- **Search files** using regex patterns
- **File metadata** including size, modification time, and full path
- **Error handling** with informative messages
- **Formatted output** for easy viewing

## Dependencies

This project uses:
- **JCIFS** (1.3.17) - Java CIFS Client Library for SMB/CIFS protocol support
- **Java 11+**

## Installation

### With Maven

1. Ensure you have Maven installed
2. Run: `mvn clean install`

### Manual Setup

1. Download JCIFS JAR from: http://jcifs.samba.org/
2. Add the JAR to your project's classpath

## Usage

### Basic Example

```java
// Create handler with server credentials
SambaServerHandler handler = new SambaServerHandler(
    "smb://192.168.1.100/shared/",  // Server address
    "WORKGROUP",                      // Domain (optional)
    "username",                       // Username
    "password"                        // Password
);

// Connect to server
if (handler.connect()) {
    // Get all files and directories recursively
    List<SambaFile> items = handler.getFilesRecursively();
    handler.printFileList(items);
}
```

### Available Methods

#### `boolean connect()`
Tests the connection to the Samba server.
- **Returns**: `true` if successful, `false` otherwise

#### `List<SambaFile> getFilesRecursively()`
Retrieves all files and directories from the server root recursively.
- **Returns**: List of `SambaFile` objects
- **Throws**: Exception on connection/retrieval failure

#### `List<SambaFile> getFilesRecursivelyFromPath(String path)`
Retrieves files and directories from a specific path within the share.
- **Parameters**: 
  - `path`: Relative path within the share (e.g., "folder/subfolder")
- **Returns**: List of `SambaFile` objects
- **Throws**: Exception on connection/retrieval failure

#### `List<SambaFile> getFilesOnly()`
Retrieves only files (excludes directories).
- **Returns**: List of `SambaFile` objects representing files only

#### `List<SambaFile> getDirectoriesOnly()`
Retrieves only directories (excludes files).
- **Returns**: List of `SambaFile` objects representing directories only

#### `List<SambaFile> searchFiles(String pattern)`
Searches for files matching a regex pattern.
- **Parameters**:
  - `pattern`: Regex pattern to match file names (e.g., `".*\\.txt$"` for .txt files)
- **Returns**: List of matching `SambaFile` objects
- **Throws**: Exception on connection/retrieval failure

#### `void printFileList(List<SambaFile> items)`
Prints a formatted list of files and directories.
- **Parameters**:
  - `items`: List of `SambaFile` objects to display

### SambaFile Class

Inner class representing a file or directory on the Samba server.

**Properties:**
- `getName()`: File/directory name
- `getPath()`: Relative path from share root
- `isDirectory()`: Whether it's a directory
- `getSize()`: File size in bytes
- `getLastModified()`: Last modification timestamp
- `getFullPath()`: Complete SMB path

## Examples

### List All Files and Directories

```java
SambaServerHandler handler = new SambaServerHandler(
    "smb://192.168.1.100/documents/",
    "WORKGROUP",
    "user",
    "password"
);

if (handler.connect()) {
    List<SambaFile> allItems = handler.getFilesRecursively();
    handler.printFileList(allItems);
}
```

### Get Only Files

```java
List<SambaFile> files = handler.getFilesOnly();
for (SambaFile file : files) {
    System.out.println(file.getName() + " - " + 
                      formatSize(file.getSize()));
}
```

### Search for Specific File Types

```java
// Find all PDF files
List<SambaFile> pdfFiles = handler.searchFiles(".*\\.pdf$");
System.out.println("Found " + pdfFiles.size() + " PDF files");
```

### Access File/Directory Information

```java
List<SambaFile> items = handler.getFilesRecursively();
for (SambaFile item : items) {
    System.out.println("Name: " + item.getName());
    System.out.println("Path: " + item.getPath());
    System.out.println("Is Directory: " + item.isDirectory());
    System.out.println("Size: " + item.getSize() + " bytes");
    System.out.println("Modified: " + 
                      new Date(item.getLastModified()));
}
```

## Configuration Parameters

When creating a new `SambaServerHandler`:

| Parameter | Description | Example |
|-----------|-------------|---------|
| `serverAddress` | SMB server URL with share name | `smb://192.168.1.100/shared/` |
| `domain` | Windows domain or workgroup | `WORKGROUP` or `DOMAIN` |
| `username` | Account username | `user123` |
| `password` | Account password | `password123` |

**Notes:**
- Server address must end with `/`
- For guest access, pass `null` for username and password
- Domain can be `null` for workgroup-based systems

## Authentication Methods

### With Username/Password
```java
new SambaServerHandler(
    "smb://192.168.1.100/shared/",
    "WORKGROUP",
    "username",
    "password"
);
```

### Guest Access
```java
new SambaServerHandler(
    "smb://192.168.1.100/shared/",
    null,
    null,
    null
);
```

## Compilation

### Using javac

```bash
javac -cp jcifs-1.3.17.jar SambaServerHandler.java
java -cp .:jcifs-1.3.17.jar SambaServerHandler
```

### Using Maven

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="SambaServerHandler"
```

## Error Handling

The class handles various error scenarios:

- **SmbAuthException**: Authentication failures (wrong username/password)
- **SmbException**: Connection or access issues
- **General Exceptions**: Unexpected errors during traversal

All methods with potential network operations include try-catch blocks and provide informative error messages.

## Performance Considerations

- Recursive traversal may take time for large directory structures
- Consider filtering results (files/directories only) to reduce memory usage
- Use path-based retrieval for specific folders to improve performance

## Security Notes

- Never hardcode credentials in production code
- Use environment variables or secure vaults for passwords
- Ensure proper SSL/TLS configuration if required by your server
- Validate and sanitize any file paths used in searches

## Supported Platforms

- Windows
- Linux
- macOS
- Any platform with Java 11+ and network access to SMB servers

## License

This is a sample implementation. Modify as needed for your use case.

## Troubleshooting

### Connection Fails
- Verify server address and credentials
- Check network connectivity to SMB server
- Ensure firewall allows SMB (ports 137-139, 445)

### Authentication Errors
- Verify username and password
- Check domain/workgroup name
- Ensure account has access to the share

### File Not Found
- Verify file/directory path is correct
- Check file permissions
- Use absolute share paths in URLs

## Contact & Support

For JCIFS documentation, visit: http://jcifs.samba.org/
