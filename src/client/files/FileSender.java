package client.files;

import common.Constants;
import common.Message;
import common.MessageType;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.UUID;

public class FileSender {

    private Socket socket;
    private DataOutputStream dataOut;
    private PrintWriter messageOut;
    private String senderUsername;
    private FileTransferCallback callback;

    private long totalBytesSent;
    private long fileSize;
    private boolean transferInProgress;
    private String currentFileId;

    public interface FileTransferCallback {
        void onProgress(int percentage, long bytesSent, long totalBytes);

        void onComplete(String filename, long totalBytes);

        void onError(String errorMessage);

        void onCancelled();
    }

    public FileSender(Socket socket, String senderUsername) {
        this.socket = socket;
        this.senderUsername = senderUsername;
        this.transferInProgress = false;

        try {
            this.dataOut = new DataOutputStream(socket.getOutputStream());
            this.messageOut = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error initializing FileSender: " + e.getMessage());
        }
    }

    public FileSender(Socket socket, String senderUsername, FileTransferCallback callback) {
        this(socket, senderUsername);
        this.callback = callback;
    }

    public File selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Send");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            if (!selectedFile.exists()) {
                showError("File does not exist!");
                return null;
            }

            if (!selectedFile.canRead()) {
                showError("Cannot read file. Check permissions.");
                return null;
            }

            if (selectedFile.length() > Constants.MAX_FILE_SIZE) {
                showError(String.format("File too large! Max size: %.2f MB",
                        Constants.MAX_FILE_SIZE / (1024.0 * 1024.0)));
                return null;
            }

            return selectedFile;
        }

        return null;
    }

    public boolean sendFile(File file, String receiverUsername) {
        if (transferInProgress) {
            showError("Another file transfer is already in progress!");
            return false;
        }

        if (file == null || !file.exists()) {
            notifyError("Invalid file selected");
            return false;
        }

        try {
            transferInProgress = true;
            fileSize = file.length();
            totalBytesSent = 0;
            currentFileId = generateFileId();

            if (!sendFileMetadata(file, receiverUsername)) {
                transferInProgress = false;
                return false;
            }

            boolean success = sendFileData(file);

            if (success) {
                notifyComplete(file.getName(), fileSize);
            }

            return success;

        } catch (Exception e) {
            notifyError("File transfer failed: " + e.getMessage());
            return false;
        } finally {
            transferInProgress = false;
            currentFileId = null;
        }
    }

    private boolean sendFileMetadata(File file, String receiverUsername) {
        try {
            Message metadataMsg = new Message(
                    MessageType.FILE_METADATA,
                    senderUsername,
                    receiverUsername,
                    file.getName(),
                    file.length(),
                    currentFileId);

            messageOut.println(metadataMsg.toProtocolString());
            messageOut.flush();

            System.out.println("Sent file metadata: " + file.getName() +
                    " (" + formatFileSize(file.length()) + ")");

            return true;

        } catch (Exception e) {
            System.err.println("Error sending file metadata: " + e.getMessage());
            return false;
        }
    }

    private boolean sendFileData(File file) {
        FileInputStream fileIn = null;
        BufferedInputStream bufferedIn = null;

        try {
            fileIn = new FileInputStream(file);
            bufferedIn = new BufferedInputStream(fileIn);

            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
            int bytesRead;

            System.out.println("Starting file transfer: " + file.getName());

            dataOut.writeLong(fileSize);
            dataOut.flush();

            while ((bytesRead = bufferedIn.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
                dataOut.flush();

                totalBytesSent += bytesRead;

                int percentage = (int) ((totalBytesSent * 100) / fileSize);
                notifyProgress(percentage, totalBytesSent, fileSize);

                Thread.sleep(1);
            }

            System.out.println("File transfer complete: " + formatFileSize(totalBytesSent) + " sent");

            Message completeMsg = new Message(
                    MessageType.FILE_COMPLETE,
                    senderUsername,
                    file.getName() + " sent successfully");
            messageOut.println(completeMsg.toProtocolString());
            messageOut.flush();

            return true;

        } catch (IOException e) {
            notifyError("IO Error during file transfer: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            notifyError("File transfer interrupted");
            Thread.currentThread().interrupt();
            return false;
        } finally {
            closeStreams(bufferedIn, fileIn);
        }
    }

    public String calculateChecksum(File file) {
        try (FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis)) {

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            System.err.println("Error calculating checksum: " + e.getMessage());
            return null;
        }
    }

    public void cancelTransfer() {
        if (transferInProgress) {
            transferInProgress = false;
            notifyCancelled();

            try {
                Message cancelMsg = new Message(
                        MessageType.FILE_ERROR,
                        senderUsername,
                        "File transfer cancelled");
                messageOut.println(cancelMsg.toProtocolString());
                messageOut.flush();
            } catch (Exception e) {
                System.err.println("Error sending cancellation: " + e.getMessage());
            }
        }
    }

    public boolean isTransferInProgress() {
        return transferInProgress;
    }

    public int getProgress() {
        if (fileSize == 0)
            return 0;
        return (int) ((totalBytesSent * 100) / fileSize);
    }

    private String generateFileId() {
        return UUID.randomUUID().toString();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }

    private void closeStreams(Closeable... streams) {
        for (Closeable stream : streams) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    System.err.println("Error closing stream: " + e.getMessage());
                }
            }
        }
    }

    private void notifyProgress(int percentage, long bytesSent, long totalBytes) {
        if (callback != null) {
            SwingUtilities.invokeLater(() -> callback.onProgress(percentage, bytesSent, totalBytes));
        }
    }

    private void notifyComplete(String filename, long totalBytes) {
        if (callback != null) {
            SwingUtilities.invokeLater(() -> callback.onComplete(filename, totalBytes));
        }
    }

    private void notifyError(String errorMessage) {
        if (callback != null) {
            SwingUtilities.invokeLater(() -> callback.onError(errorMessage));
        }
    }

    private void notifyCancelled() {
        if (callback != null) {
            SwingUtilities.invokeLater(() -> callback.onCancelled());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "File Transfer Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void close() {
        closeStreams(dataOut, messageOut, socket);
    }

    public static void testFileReading() {
        System.out.println("=== FileSender Standalone Test ===");

        File testFile = new File("test_file.txt");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("This is a test file for FileSender.\n");
            writer.write("Testing file reading and chunking functionality.\n");
            System.out.println("Created test file: " + testFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error creating test file: " + e.getMessage());
            return;
        }

        try (FileInputStream fis = new FileInputStream(testFile);
                BufferedInputStream bis = new BufferedInputStream(fis)) {

            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = bis.read(buffer)) != -1) {
                totalBytes += bytesRead;
                System.out.println("Read chunk: " + bytesRead + " bytes");
            }

            System.out.println("Total bytes read: " + totalBytes);
            System.out.println("File size: " + testFile.length());
            System.out.println("Test PASSED ✓");

        } catch (IOException e) {
            System.err.println("Test FAILED: " + e.getMessage());
        }

        testFile.delete();
    }

    public static void main(String[] args) {
        testFileReading();
    }
}
