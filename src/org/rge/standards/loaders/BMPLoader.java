package org.rge.standards.loaders;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.rge.assets.AssetManager;
import org.rge.loaders.TextureRawLoader;

public class BMPLoader implements TextureRawLoader {
	
	public static final String[] TEXTURERAWLOADER_TYPES = { "bmp" };
	
	@Override
	public void init() {
		
	}
	
	public static final String ALPHA_REGEX = "[Aa]\\d{3}.+";
	
	@Override
	public BufferedImage getRawImage(String path, AssetManager am) {
		
		System.out.println("Loading BMP: " + path);
		
		InputStream in = am.getAsset(path);
		if(in == null)
			return null;
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[4096];
			int len = -1;
			while((len = in.read(buffer)) != -1)
				out.write(buffer, 0, len);
			
			in.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		byte[] fileBuffer = out.toByteArray();
		
		if(fileBuffer == null)
			return null;
		
		int alphaIndex = -1;
		System.out.println("Regex: " + ALPHA_REGEX + " matches: '" + path + "' = " + path.matches(ALPHA_REGEX));
		if(path.matches(ALPHA_REGEX)) {
			System.out.println("ALPHA INDEX IN PATH! " + path.substring(1, 3));
			try {
				alphaIndex = Integer.parseInt(path.substring(1, 3));
			} catch (Exception e) {}
		}
		
		BufferedImage img = null;
		
		System.out.println("ALPHAINDEX:  " + alphaIndex);
		
		if(alphaIndex >= 0 && alphaIndex < 256) {
			
			int[] palette = new int[256];
			for(int i = 0; i < palette.length; i++) {
				if(i != alphaIndex)
					palette[i] = getColorFromBMPPalet(fileBuffer, i);
				else
					palette[i] = 0x00FFFFFF;
			}
			
			img = new BufferedImage(getIntLE(fileBuffer, 18), getIntLE(fileBuffer, 22), BufferedImage.TYPE_INT_ARGB);
			int off = getIntLE(fileBuffer, 10);
			for(int i = 0; i < img.getHeight()*img.getWidth(); i++) {
				int x = i % img.getWidth();
				int y = img.getHeight()-Math.floorDiv(i, img.getWidth())-1;
				img.setRGB(x, y, palette[fileBuffer[off+i] & 0x00FF]);
			}
		} else {
			try {
				ByteArrayInputStream buffIn = new ByteArrayInputStream(fileBuffer);
				img = ImageIO.read(buffIn);
				buffIn.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return img;
	}
	
//	private static int getColorFromBMPPalet(InputStream in, int aind) throws IOException {
//		in.skip(54+aind*4);
//		byte[] bc = new byte[4];
//		in.read(bc);
//		int r = 0x000000FF&bc[2];
//		int g = 0x000000FF&bc[1];
//		int b = 0x000000FF&bc[0];
//		Vector3f c = new Vector3f();
//		c.x = r/255.0f;
//		c.y = g/255.0f;
//		c.z = b/255.0f;
//		return b | (g << 8) | (r << 16);
//	}
	
	private static int getColorFromBMPPalet(byte[] fileBuffer, int aind) {
		byte[] bc = new byte[4];
		System.arraycopy(fileBuffer, 54+aind*4, bc, 0, 4);
		int r = 0x000000FF&bc[2];
		int g = 0x000000FF&bc[1];
		int b = 0x000000FF&bc[0];
		return b | (g << 8) | (r << 16) | (0xFF<<24);
	}
	
	private static int getIntLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 4; i++) {
			res = res | (0x000000FF & b[off+3-i]);
			if(i != 3)
				res = res << 8;
		}
		
		return res;
		
	}
	
	@Override
	public void destroy() {
		
	}

	@Override
	public boolean canRead(String path) {
		return path.toUpperCase().endsWith("BMP");
	}
	
}
