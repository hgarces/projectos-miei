import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ObjectTexture {

	private File textureFile;
	int width;
	int height;
	private byte[] im;
	
	public ObjectTexture(File t) {
		textureFile = t;
		try {
			setDataBuffer(textureFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setDataBuffer(File f) throws IOException {
		BufferedImage img = null;
		img = ImageIO.read(textureFile);
		WritableRaster raster = img.getRaster();
		width = raster.getWidth();
		height = raster.getHeight();
		DataBuffer buf = raster.getDataBuffer();
		switch( buf.getDataType() ) {
		case DataBuffer.TYPE_BYTE:
			 	 DataBufferByte bb = (DataBufferByte) buf;
			 	 im = bb.getData();
			 	break;
		case DataBuffer.TYPE_UNDEFINED:
			 	 // Report error here!
			 	 break;
		}
	}
	
	public byte[] getTextureBuffer() {
		return im;
	}
}
