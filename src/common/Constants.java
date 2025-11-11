package common;

/**
 * Application constants (server address/port etc.)
 */
public class Constants {
	// Default server host and port used by the client and server
	public static final String SERVER_IP = "127.0.0.1";
	public static final int SERVER_PORT = 12345;
	// WebSocket port for browser clients (separate from raw TCP server)
	public static final int WS_PORT = 8080;
	// HTTP port for file uploads/downloads
	// 8000 was conflicting on some systems; use 9000 by default
	public static final int FILE_HTTP_PORT = 9000;
	// Directory (relative to project root) where uploaded files are stored
	public static final String FILES_DIR = "uploaded_files";
}
