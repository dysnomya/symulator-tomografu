package com.github.dysnomya.tomograf;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.UIDUtils;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class DicomUtil {

    public static Attributes readDicomAttributes(File file) throws Exception {

        try (DicomInputStream dis = new DicomInputStream(file)) {
            return dis.readDataset(-1);
        }
    }

    public static void saveDicomImage(File inputFile, File file, BufferedImage image) throws Exception {
        int width = image.getWidth();
        int height = image.getHeight();

        byte[] pixelData = new byte[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                pixelData[y * width + x] = (byte) r;
            }
        }

        Attributes attributes = readDicomAttributes(inputFile);
        Attributes modified = new Attributes(attributes);  // copy

        modified.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        modified.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());

        modified.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        modified.setString(Tag.ImageType, VR.CS, "DERIVED", "SECONDARY");

        modified.setInt(Tag.Rows, org.dcm4che3.data.VR.US, image.getHeight());
        modified.setInt(Tag.Columns, org.dcm4che3.data.VR.US, image.getWidth());
        modified.setBytes(Tag.PixelData, VR.OW, pixelData);

        Attributes fmi = modified.createFileMetaInformation(UID.ImplicitVRLittleEndian);

        try (DicomOutputStream dos = new DicomOutputStream(file)) {
            dos.writeDataset(fmi, modified);
        }
    }
}
