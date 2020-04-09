package org.rge.standards.loaders.model;

import java.awt.Color;
import java.io.InputStream;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.rge.assets.AssetManager;
import org.rge.assets.Loader;
import org.rge.assets.models.Model.RawData;
import org.rge.assets.models.Model.RawData.RawSurface;
import org.rge.assets.models.Verts;

public class LDRLoader implements Loader {
	
	public static final String[]	LOADER_TYPES			= { "ldr" };
	public static final Class<?>[]	LOADER_RETURN_TYPES	= { RawData.class };
	
	private LDRColors colors;
	
	@Override
	public void init(AssetManager am) {
		colors = new LDRColors();
	}

	@Override
	public RawData get(String path, AssetManager am) {
		
		LDRContent ldrFile = new LDRContent();
		ldrFile.transform = new Matrix4f().identity();
		ldrFile.absTransform = new Matrix4f().identity();
		boolean succes = false;
		try {
			InputStream in = am.getAsset(path);
			ldrFile.parseFile(in, am, ldrFile.absTransform, -1, false);
			in.close();
			in = am.getAsset("LDConfig.ldr");
			colors.generateFromFile(in);
			in.close();
			succes = true;
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(!succes)
			return null;
		
		ArrayList<LDRContent> entries = ldrFile.getAllEntries();
		
		RawData data = new RawData();
		RawSurface surf = new RawSurface();
		surf.doubleSided = true;
		surf.isTranslucent = false;
		data.surfaces = new RawSurface[] { surf };
		data.verts = new Verts[3];
		
		Verts v = new Verts();
		v.dimension = 3;
		v.pos = 0;
		data.verts[0] = v;
		
		Verts vc = new Verts();
		vc.dimension = 4;
		vc.pos = 1;
		data.verts[1] = vc;
		
		Verts vn = new Verts();
		vn.dimension = 3;
		vn.pos = 2;
		data.verts[2] = vn;
		
		int vLen = 0;
		int iLen = 0;
		for(int i = 0; i < entries.size(); i++) {
			LDRContent c = entries.get(i);
			iLen += c.inds.length;
			vLen += c.verts.length;
		}
		
		data.rawInds = new int[iLen];
		v.rawVerts = new float[vLen];
		vc.rawVerts = new float[4*(vLen/3)];
		vn.rawVerts = new float[vLen];
		
		
		Vector3f temp = new Vector3f();
		
		int vOff = 0;
		int dataIOff = 0;
		for(int i = 0; i < entries.size(); i++) {
			LDRContent c = entries.get(i);
			for(int j = 0; j < c.verts.length/3; j++) {
				temp.x = c.verts[j*3+0];
				temp.y = c.verts[j*3+1];
				temp.z = c.verts[j*3+2];
				c.absTransform.transformPosition(temp);
				v.rawVerts[vOff + j*3 +0] = temp.x;
				v.rawVerts[vOff + j*3 +1] = temp.y;
				v.rawVerts[vOff + j*3 +2] = temp.z;
			}
			int iOff = vOff/3;
			for(int j = 0; j < c.inds.length; j++) {
				data.rawInds[dataIOff + j] = iOff + c.inds[j];
				System.out.println(data.rawInds[dataIOff+j]);
				if(j%3 == 0) {
					Color color = colors.getColor(c.triColors[j/3]);
					for(int g = 0; g < 3; g++) {
						vc.rawVerts[(iOff + c.inds[j+g])*4 + 0] = color.getRed()/255.0f;
						vc.rawVerts[(iOff + c.inds[j+g])*4 + 1] = color.getGreen()/255.0f;
						vc.rawVerts[(iOff + c.inds[j+g])*4 + 2] = color.getBlue()/255.0f;
						vc.rawVerts[(iOff + c.inds[j+g])*4 + 3] = 1;
					}
				}
			}
			
			dataIOff += c.inds.length;
			vOff += c.verts.length;
		}
		
		for(int i = 0; i < data.rawInds.length; i += 3)
			calcNormal(v.rawVerts, vn.rawVerts, data.rawInds[i], data.rawInds[i+1], data.rawInds[i+2]);
		
		v.flip(1);
		vn.flip(1);
		
		surf.indOffset = 0;
		surf.indLength = data.rawInds.length;
		
		System.out.println("Returning data: " + data);
		
//		try {
//			PrintWriter writer = new PrintWriter("WUUT.obj");
//			ldrFile.printToObj(writer);
//			writer.close();
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
		
		return data;
	}
	
	private static void calcNormal(float[] verts, float[] norms, int p0, int p1, int p2) {
		
		// a is a vector from p0 to p1
		float[] a = new float[3];
		for(int i = 0; i < 3; i++)
			a[i] = verts[p1*3+i] - verts[p0*3+i];
		
		// b is a vector from p0 to p2
		float[] b = new float[3];
		for(int i = 0; i < 3; i++)
			b[i] = verts[p2*3+i] - verts[p0*3+i];
		
		for(int i = 0; i < 3; i++)
			norms[p0*3+i] = norms[p1*3+i] = norms[p2*3+i]
					= ((a[(i+1)%3]*b[(i+2)%3]) - (b[(i+1)%3]*a[(i+2)%3]));
		
	}
	
	@Override
	public void destroy() {
		
	}
	
	@Override
	public boolean canRead(String path) {
		return path.toUpperCase().endsWith(".LDR");
	}
	
}
