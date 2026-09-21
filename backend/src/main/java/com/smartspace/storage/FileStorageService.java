package com.smartspace.storage;

import com.smartspace.common.exception.DomainException;
import com.smartspace.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageLocation;

    public FileStorageService(@Value("${app.storage.path:./uploads}") String storagePath) throws IOException {
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(this.storageLocation);
    }

    public FileMetadata store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "File is empty");
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] magic = new byte[8];
            inputStream.mark(8);
            if (inputStream.read(magic) < 4) {
                throw new DomainException(ErrorCode.VALIDATION_ERROR, "Invalid file content");
            }
            inputStream.reset();

            String extension = detectExtension(magic, file.getOriginalFilename());
            if (extension == null) {
                throw new DomainException(ErrorCode.VALIDATION_ERROR, "Unsupported file format");
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            Path targetLocation = this.storageLocation.resolve(newFilename);

            if (extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png")) {
                // Strip EXIF by reading and rewriting the image
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    throw new DomainException(ErrorCode.VALIDATION_ERROR, "Invalid image data");
                }
                String formatName = extension.substring(1).equals("jpg") ? "jpeg" : extension.substring(1);
                File targetFile = targetLocation.toFile();
                ImageIO.write(image, formatName, targetFile);
            } else {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            String sha256 = calculateHash(targetLocation);
            return new FileMetadata(newFilename, sha256);
        } catch (IOException e) {
            throw new DomainException(ErrorCode.INTERNAL_ERROR, "Failed to store file");
        }
    }

    private String detectExtension(byte[] magic, String originalFilename) {
        if (magic[0] == (byte) 0xFF && magic[1] == (byte) 0xD8 && magic[2] == (byte) 0xFF) {
            return ".jpg";
        }
        if (magic[0] == (byte) 0x89 && magic[1] == (byte) 0x50 && magic[2] == (byte) 0x4E && magic[3] == (byte) 0x47) {
            return ".png";
        }
        if (magic[0] == (byte) 0x25 && magic[1] == (byte) 0x50 && magic[2] == (byte) 0x44 && magic[3] == (byte) 0x46) {
            return ".pdf";
        }
        return null;
    }

    private String calculateHash(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(Files.readAllBytes(file));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static class FileMetadata {
        private final String path;
        private final String sha256;

        public FileMetadata(String path, String sha256) {
            this.path = path;
            this.sha256 = sha256;
        }

        public String getPath() {
            return path;
        }

        public String getSha256() {
            return sha256;
        }
    }
}
