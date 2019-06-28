package foodapp.example.com.smsimage;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

import java.io.File;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.IOException;

import java.io.InputStream;

import java.util.List;

import java.util.Map;

import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import java.util.zip.Deflater;

import java.util.zip.Inflater;

public class CompressionUtils {

    private static final Logger LOG = Logger.getLogger(CompressionUtils.class.getSimpleName());

    public static byte[] compress(byte[] data) throws IOException {

        Deflater deflater = new Deflater();

        deflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

        deflater.finish();

        byte[] buffer = new byte[1024];

        while (!deflater.finished()) {

            int count = deflater.deflate(buffer); // returns the generated code... index

            outputStream.write(buffer, 0, count);

        }

        outputStream.close();

        byte[] output = outputStream.toByteArray();

        LOG.info("Original: " + data.length / 1024 + " Kb");

        LOG.info("Compressed: " + output.length / 1024 + " Kb");

        return output;

    }


    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {

        Inflater inflater = new Inflater();

        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

        byte[] buffer = new byte[1024];

        while (!inflater.finished()) {

            int count = inflater.inflate(buffer);

            outputStream.write(buffer, 0, count);

        }

        outputStream.close();

        byte[] output = outputStream.toByteArray();

        LOG.info("Original: " + data.length);

        LOG.info("Compressed: " + output.length);

        return output;

    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

}