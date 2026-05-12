package org.iesalixar.daw2.womhat.womhat.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Servicio utilitario para generar códigos QR en PNG.
 *
 * Mantiene un esquema visual consistente (alto contraste) para
 * lectura en pantallas y tarjetas impresas.
 */
@Service
public class QrCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeService.class);
    private static final int BACKGROUND_COLOR = 0xFFFFFFFF;
    private static final int FOREGROUND_COLOR = 0xFF111827;

    /**
     * Genera una imagen PNG de QR para contenido textual.
     *
     * @param content texto/URL que codificar
     * @param width ancho de salida en píxeles
     * @param height alto de salida en píxeles
     * @return bytes del PNG resultante
     */
    public byte[] generatePng(String content, int width, int height) {
        if (!StringUtils.hasText(content)) {
            logger.warn("Intento de generar QR con contenido vacío.");
            throw new IllegalArgumentException("QR content cannot be blank.");
        }

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage image = toBufferedImage(matrix);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(image, "PNG", outputStream);
                return outputStream.toByteArray();
            }

        } catch (WriterException | IOException ex) {
            logger.error("Fallo generando QR (width={}, height={}): {}", width, height, ex.getMessage(), ex);
            throw new IllegalStateException("Could not generate the QR code image.", ex);
        }
    }

    /**
     * Convierte la matriz binaria en una imagen RGB con la paleta del proyecto.
     */
    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? FOREGROUND_COLOR : BACKGROUND_COLOR);
            }
        }

        return image;
    }
}
