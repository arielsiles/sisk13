package com.encens.khipus.util;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utilidades para procesar imagenes antes de persistirlas en la base de datos.
 * Patron: leer bytes, redimensionar manteniendo ratio, re-encodear a JPEG con
 * calidad fija. Resultado tipico: ~50-150 KB para mapas tomados de captura.
 */
public final class ImageUtils {

    private static final LogProvider log = Logging.getLogProvider(ImageUtils.class);

    /** No instanciable. */
    private ImageUtils() {
    }

    /**
     * Lee la imagen, la redimensiona a {@code maxWidth} (preservando ratio) y
     * la recodifica como JPEG con calidad por defecto (~85%). Si la imagen
     * original ya es mas pequena que {@code maxWidth}, conserva su tamano.
     *
     * @param bytes    bytes crudos de la imagen subida.
     * @param maxWidth ancho maximo en pixeles. Recomendado 1024 para mapas.
     * @return bytes JPEG redimensionado.
     * @throws IOException si la imagen no es legible.
     */
    public static byte[] resizeAndCompress(byte[] bytes, int maxWidth) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }
        BufferedImage src = ImageIO.read(new ByteArrayInputStream(bytes));
        if (src == null) {
            throw new IOException("Formato de imagen no soportado");
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int targetW = Math.min(w, maxWidth);
        int targetH = (int) Math.round(h * (double) targetW / (double) w);

        BufferedImage dst = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, targetW, targetH, null);
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
        if (!ImageIO.write(dst, "jpg", out)) {
            throw new IOException("No se pudo codificar la imagen como JPEG");
        }
        byte[] result = out.toByteArray();
        if (log.isDebugEnabled()) {
            log.debug("resizeAndCompress: " + w + "x" + h + " (" + bytes.length
                    + " bytes) -> " + targetW + "x" + targetH + " (" + result.length + " bytes)");
        }
        return result;
    }
}
