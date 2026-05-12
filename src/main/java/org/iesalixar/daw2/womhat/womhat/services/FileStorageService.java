package org.iesalixar.daw2.womhat.womhat.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Servicio encargado de guardar y borrar ficheros subidos por el usuario.
 *
 * En este proyecto se usa, por ejemplo, para la imagen de perfil.
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    /**
     * Directorio raíz externo donde se guardan los uploads.
     */
    @Value("${app.upload-root}")
    private String uploadRootPath;

    /**
     * Subcarpeta relativa donde guardamos archivos públicos.
     */
    private static final String UPLOADS_SUBDIR = "uploads";

    /**
     * Guarda un fichero y devuelve la ruta web relativa.
     *
     * @param file archivo subido
     * @return ruta web relativa o null si falla
     */
    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.warn("Se intentó guardar un fichero nulo o vacío.");
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);

            String uniqueFileName = UUID.randomUUID().toString();
            if (!extension.isBlank()) {
                uniqueFileName += "." + extension;
            }

            Path uploadsDir = Paths.get(uploadRootPath).resolve(UPLOADS_SUBDIR);
            Files.createDirectories(uploadsDir);

            Path filePath = uploadsDir.resolve(uniqueFileName);
            Files.write(filePath, file.getBytes());

            logger.info("Archivo guardado correctamente en {}", filePath);

            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            logger.error("Error al guardar el archivo: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Elimina un fichero del sistema.
     *
     * @param filePathOrWebPath nombre de archivo o ruta web relativa
     */
    public void deleteFile(String filePathOrWebPath) {
        if (filePathOrWebPath == null || filePathOrWebPath.isBlank()) {
            logger.warn("Se intentó borrar un fichero sin ruta.");
            return;
        }

        try {
            String fileName = normalizeFileName(filePathOrWebPath);
            Path uploadsDir = Paths.get(uploadRootPath).resolve(UPLOADS_SUBDIR);
            Path filePath = uploadsDir.resolve(fileName);

            Files.deleteIfExists(filePath);
            logger.info("Archivo eliminado correctamente: {}", filePath);

        } catch (IOException e) {
            logger.error("Error al eliminar el archivo {}: {}", filePathOrWebPath, e.getMessage(), e);
        }
    }

    /**
     * Extrae la extensión del archivo.
     */
    private String getFileExtension(String fileName) {
        if (fileName != null) {
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0 && lastDot < fileName.length() - 1) {
                return fileName.substring(lastDot + 1);
            }
        }
        return "";
    }

    /**
     * Normaliza una ruta para quedarse solo con el nombre del archivo.
     */
    private String normalizeFileName(String filePathOrWebPath) {
        String value = filePathOrWebPath.trim();

        if (value.startsWith("/uploads/")) {
            value = value.substring("/uploads/".length());
        }

        int lastSlash = value.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < value.length() - 1) {
            value = value.substring(lastSlash + 1);
        }

        return value;
    }
}