package client.files;

import common.Constants;
import common.Message;
import common.MessageType;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReceiver {

    private Socket socket;
    private DataInputStream dataIn;
    private BufferedReader messageIn;
    private String receiverUsername;
    private FileReceiveCallback callback;

    private long totalBytesReceived;
    private long expectedFileSize;
    private boolean transferInProgress;
    @SuppressWarnings("unused")
    private String currentFileName;
    private String receiveDirectory;

    public interface FileReceiveCallback {
        void onFileRequest(String sender, String filename, long fileSize);

        void onProgress(int percentage, long bytesReceived, long totalBytes);

        void onComplete(String filename, String savedPath);

        void onError(String errorMessage);

        void onCancelled();
    }

    public FileReceiver(Socket socket, String receiverUsername) {
        this.socket = socket;
        this.receiverUsername = receiverUsername;
        this.transferInProgress = false;
        this.receiveDirectory = Constants.FILE_TRANSFER_DIRECTORY + receiverUsername + "/";

        createReceiveDirectory();

        try {
            this.dataIn = new DataInputStream(socket.getInputStream());
            this.messageIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error initializing FileReceiver: " + e.getMessage());
        }
    }

    public FileReceiver(Socket socket, String receiverUsername, FileReceiveCallback callback) {
        this(socket, receiverUsername);
        this.callback = callback;
    }

    private void createReceiveDirectory() {
        try {
            Path dirPath = Paths.get(receiveDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                System.out.println("Created receive directory: " + receiveDirectory);
            }
        } catch (IOException e) {
            System.err.println("Error creating receive directory: " + e.getMessage());
            receiveDirectory = "./received_files/";
        }
    }

    public boolean handleFileRequest(Message fileMetadata) {
        if (transferInProgress) {
            notifyError("Another file transfer is already in progress!");
            return false;
        }

        String sender = fileMetadata.getSender();
        String filename = fileMetadata.getFilename();
        long fileSize = fileMetadata.getFileSize();

        if (callback != null) {
            callback.onFileRequest(sender, filename, fileSize);
        }

        int choice = JOptionPane.showConfirmDialog(
                null,
                String.format("Accept file '%s' (%.2f MB) from %s?",
                        filename, fileSize / (1024.0 * 1024.0), sender),
                "Incoming File Transfer",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        return choice == JOptionPane.YES_OPTION;
    }

    public String receiveFile(String filename, long fileSize) {
        if (transferInProgress) {
            notifyError("Another file transfer is already in progress!");
            return null;
        }

        FileOutputStream fileOut = null;
        BufferedOutputStream bufferedOut = null;

        try {
            transferInProgress = true;
            currentFileName = filename;
            expectedFileSize = fileSize;
            totalBytesReceived = 0;

            String savePath = generateUniqueFilePath(receiveDirectory, filename);
            File outputFile = new File(savePath);

            fileOut = new FileOutputStream(outputFile);
            bufferedOut = new BufferedOutputStream(fileOut);

            System.out.println("Receiving file: " + filename + " -> " + savePath);

            long confirmedSize = dataIn.readLong();
            if (confirmedSize != fileSize) {
                throw new IOException("File size mismatch!");
            }

            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
            int bytesRead;

            while (totalBytesReceived < expectedFileSize) {
                int toRead = (int) Math.min(buffer.length, expectedFileSize - totalBytesReceived);
                bytesRead = dataIn.read(buffer, 0, toRead);

                if (bytesRead == -1) {
                    throw new IOException("Unexpected end of stream");
                }

                bufferedOut.write(buffer, 0, bytesRead);
                totalBytesReceived += bytesRead;

                int percentage = (int) ((totalBytesReceived * 100) / expectedFileSize);
                notifyProgress(percentage, totalBytesReceived, expectedFileSize);
            }

            bufferedOut.flush();

            System.out.println("File received successfully: " + formatFileSize(totalBytesReceived));

            if (outputFile.length() != expectedFileSize) {
                throw new IOException("File size verification failed!");
            }

            notifyComplete(filename, savePath);
            return savePath;

        } catch (IOException e) {
            notifyError("Error receiving file: " + e.getMessage());
            return null;
        } finally {
            closeStreams(bufferedOut, fileOut);
            transferInProgress = false;
            currentFileName = null;
        }
    }

    public String receiveFileWithMetadata(Message metadataMessage) {
        String filename = metadataMessage.getFilename();
        long fileSize = metadataMessage.getFileSize();

        if (!handleFileRequest(metadataMessage)) {
            sendRejectionMessage(metadataMessage.getSender(), filename);
            return null;
        }

        sendAcceptanceMessage(metadataMessage.getSender(), filename);

        return receiveFile(filename, fileSize);
    }

    private void sendAcceptanceMessage(String sender, String filename) {
        try {
            Message acceptMsg = new Message(
                    MessageType.FILE_TRANSFER_ACCEPT,
                    receiverUsername,
                    sender,
                    "Accepted: " + filename);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(acceptMsg.toProtocolString());

        } catch (IOException e) {
            System.err.println("Error sending acceptance: " + e.getMessage());
        }
    }

    private void sendRejectionMessage(String sender, String filename) {
        try {
            Message rejectMsg = new Message(
                    MessageType.FILE_TRANSFER_REJECT,
                    receiverUsername,
                    sender,
                    "Rejected: " + filename);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(rejectMsg.toProtocolString());

        } catch (IOException e) {
            System.err.println("Error sending rejection: " + e.getMessage());
        }
    }

    private String generateUniqueFilePath(String directory, String filename) {
        File file = new File(directory + filename);

        if (!file.exists()) {
            return file.getAbsolutePath();
        }

        String nameWithoutExt = filename;
        String extension = "";

        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            nameWithoutExt = filename.substring(0, lastDot);
            extension = filename.substring(lastDot);
        }

        int counter = 1;
        while (true) {
            String newFilename = nameWithoutExt + "_(" + counter + ")" + extension;
            file = new File(directory + newFilename);

            if (!file.exists()) {
                return file.getAbsolutePath();
            }
            counter++;
        }
    }

    public void cancelTransfer() {
        if (transferInProgress) {
            transferInProgress = false;
            notifyCancelled();
            System.out.println("File transfer cancelled by user");
        }
    }

    public boolean isTransferInProgress() {
        return transferInProgress;
    }

    public int getProgress() {
        if (expectedFileSize == 0)
            return 0;
        return (int) ((totalBytesReceived * 100) / expectedFileSize);
    }

    public String getReceiveDirectory() {
        return receiveDirectory;
    }

    public void setReceiveDirectory(String directory) {
        this.receiveDirectory = directory;
        if (!directory.endsWith("/") && !directory.endsWith("\\")) {
            this.receiveDirectory += "/";
        }
        createReceiveDirectory();
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

    private void notifyProgress(int percentage, long bytesReceived, long totalBytes) {
        if (callback != null) {
            SwingUtilities.invokeLater(() -> callback.onProgress(percentage, bytesReceived, totalBytes));
        }
    }

    private void notifyComplete(String filename, String savedPath) {
        if (callback != null) {
            SwingUtilities.invokeLater(() -> callback.onComplete(filename, savedPath));
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

    public void close() {
        closeStreams(dataIn, messageIn);
    }

    public static void testFileWriting() {
        System.out.println("=== FileReceiver Standalone Test ===");

        String testData = "This is test data for FileReceiver.\n" +
                "Testing file writing and directory creation.\n";

        String testDirectory = "test_received_files/";
        String testFilename = "test_received.txt";

        try {
            Path dirPath = Paths.get(testDirectory);
            Files.createDirectories(dirPath);
            System.out.println("Created test directory: " + testDirectory);
        } catch (IOException e) {
            System.err.println("Error creating directory: " + e.getMessage());
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(testDirectory + testFilename);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] data = testData.getBytes();
            bos.write(data);
            bos.flush();

            System.out.println("Wrote test file: " + testDirectory + testFilename);
            System.out.println("Bytes written: " + data.length);

            File testFile = new File(testDirectory + testFilename);
            if (testFile.exists() && testFile.length() == data.length) {
                System.out.println("Test PASSED ✓");
            } else {
                System.out.println("Test FAILED: File verification error");
            }

        } catch (IOException e) {
            System.err.println("Test FAILED: " + e.getMessage());
        }

        try {
            Files.deleteIfExists(Paths.get(testDirectory + testFilename));
            Files.deleteIfExists(Paths.get(testDirectory));
        } catch (IOException e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        testFileWriting();
    }
}
