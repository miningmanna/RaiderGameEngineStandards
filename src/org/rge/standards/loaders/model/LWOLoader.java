package org.rge.standards.loaders.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import org.rge.assets.AssetManager;
import org.rge.assets.Loader;
import org.rge.assets.models.Model.RawData;
import org.rge.assets.models.Model.RawData.RawSurface;
import org.rge.assets.models.Texture.TextureRawInfo;
import org.rge.assets.models.Verts;
import org.rge.standards.loaders.model.LwobFileData.Surface;
import org.rge.standards.loaders.model.LwobFileData.Surface.TextureData;

public class LWOLoader implements Loader {
	
	public static final String[] LOADER_TYPES				= { "lwo" };
	public static final Class<?>[] LOADER_RETURN_TYPES	= { RawData.class };
	
	@Override
	public void init(AssetManager am) {
		
	}
	
	@Override
	public RawData get(String path, AssetManager am) {
		
		String prefix = "";
		int slashIndex = path.lastIndexOf('/');
		if(slashIndex != -1)
			prefix = path.substring(0, slashIndex+1);
		System.out.println("PATH:   " + path);
		System.out.println("PREFIX: " + prefix);
		
		InputStream in = am.getAsset(path);
		if(in == null)
			return null;
		
		LwobFileData data = null;
		try {
			data = LwobFileData.getLwobFileData(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(data == null)
			return null;
		
		RawData res = new RawData();
		
		int[] tempInds  = new int[data.iv.length];
		int[] tempPolEdgeCounts = new int[data.ipolsurf.length];
		int[] tempPolygonSurfaceIndices = new int[data.ipolsurf.length];
		
//		ctm.v = new float[ctm.iv.length*3];
//		ctm.texs = new String[lfd.surfs.length];
//		ctm.alphaPix = new Vector3f[lfd.surfs.length];
//		ctm.additive = new boolean[lfd.surfs.length];
//		ctm.ipolsurf = new int[lfd.ipolsurf.length];
//		ctm.polliv = new int[lfd.polliv.length];
//		ctm.doubleSided = new boolean[lfd.surfs.length];
//		ctm.sequenced = new boolean[lfd.surfs.length];
		
		res.verts = new Verts[4];
		
		Verts verts = new Verts();
		verts.dimension = 3;
		verts.pos = 0;
		verts.rawVerts = new float[tempInds.length*verts.dimension];
		res.verts[0] = verts;
		
		Verts cVerts = new Verts();
		cVerts.dimension = 4;
		cVerts.pos = 1;
		cVerts.rawVerts = new float[tempInds.length*cVerts.dimension];
		res.verts[1] = cVerts;
		
		Verts tVerts = new Verts();
		tVerts.dimension = 2;
		tVerts.pos = 2;
		tVerts.rawVerts = new float[tempInds.length*tVerts.dimension];
		res.verts[2] = tVerts;
		
		Verts nVerts = new Verts();
		nVerts.dimension = 3;
		nVerts.pos = 3;
		nVerts.rawVerts = new float[tempInds.length*nVerts.dimension];
		res.verts[3] = nVerts;
		
		// Load indices from lwob to ctm while ordering the surfaces
		int _ivoffset = 0;
		int polIndex = 0;
		for(int i = 0; i < data.surfs.length; i++) {
			int offset = 0;
			for(int j = 0; j < data.ipolsurf.length; j++) {
				
				if(data.ipolsurf[j] == i) {
					for(int k = 0; k < data.polliv[j]; k++) {
						tempInds[_ivoffset+k] = data.iv[offset+k];
					}
					tempPolygonSurfaceIndices[polIndex] = data.ipolsurf[j];
					tempPolEdgeCounts[polIndex] = data.polliv[j];
//					if(uvData != null) {
//						for(int k = 0; k < lfd.polliv[j]; k++) {
//							ivt[_ivoffset+k] = uvData.ivt[offset+k];
//						}
//					}
					polIndex++;
					_ivoffset += data.polliv[j];
				}
				offset += data.polliv[j];
			}
			
		}
		
		for(int i = 0; i < tempInds.length; i++) {
			for(int j = 0; j < 3; j++) {
				verts.rawVerts[i*3+j] = data.v[tempInds[i]*3+j];
//				if(uvData != null) {
//					ctm.vt[i*3+j] = uvData.vt[ivt[i]*3+j];
//				}
			}
			tempInds[i] = i;
			
		}
		
		int index = 0;
		for(int i = 0; i < tempPolygonSurfaceIndices.length; i++) {
			
			Surface s = data.surfs[tempPolygonSurfaceIndices[i]];
			
			for(int j = 0; j < tempPolEdgeCounts[i]; j++) {
				cVerts.rawVerts[tempInds[index+j]*4]		= s.color.x;
				cVerts.rawVerts[tempInds[index+j]*4+1]	= s.color.y;
				cVerts.rawVerts[tempInds[index+j]*4+2]	= s.color.z;
				cVerts.rawVerts[tempInds[index+j]*4+3] = 1;
			}
			index += tempPolEdgeCounts[i];
		}
		
		int vi = 0;
		TextureData td = null;
		float[] ctr = new float[3];
		float[] siz = new float[3];
		for(int i = 0; i < tempPolygonSurfaceIndices.length; i++) {
			Surface s = data.surfs[tempPolygonSurfaceIndices[i]];
			if(s == null) {
				System.out.println("Surface null");
				vi += tempPolEdgeCounts[i];
				continue;
			}
			if(s.texData[0].texFile == null) {
				vi += tempPolEdgeCounts[i];
				continue;
			}
			td = s.texData[0];
			System.out.println("Texture " + tempPolygonSurfaceIndices[i] + ": " + td.texFile);
			ctr[0] = td.tctr.x;
			ctr[1] = td.tctr.y;
			ctr[2] = td.tctr.z;
			siz[0] = td.tsiz.x;
			siz[1] = td.tsiz.y;
			siz[2] = td.tsiz.z;
			int itx = 0;
			int ity = 1;
			float ssx = 1, ssy = -1;
			if(td.projAxis.y != 0) {
				itx = 0;
				ity = 2;
			} else if(td.projAxis.x != 0) {
				itx = 2;
				ity = 1;
				ssx = 1;
				ssy = -1;
			}
			for(int j = 0; j < tempPolEdgeCounts[i]; j++) {
				tVerts.rawVerts[tempInds[vi+j]*2]		= ((verts.rawVerts[tempInds[vi+j]*3 + itx] - ctr[itx]) / (ssx*siz[itx]))+0.5f;
				tVerts.rawVerts[tempInds[vi+j]*2+1]		= ((verts.rawVerts[tempInds[vi+j]*3 + ity] - ctr[ity]) / (ssy*siz[ity]))+0.5f;
			}
			
			vi += tempPolEdgeCounts[i];
		}
		
		res.surfaces = new RawSurface[data.surfs.length];
		for(int i = 0; i < data.surfs.length; i++) {
			RawSurface rawSurf = new RawSurface();
			res.surfaces[i] = rawSurf;
			Surface s = data.surfs[i];
			
			rawSurf.additiveColor = (s.flags & 0x00000200) != 0;
			rawSurf.isTranslucent = rawSurf.additiveColor;
			rawSurf.doubleSided = (s.flags & 0x00000100) != 0;
			
			String texFilePath = s.texData[0].texFile;
			if(texFilePath == null) {
				rawSurf.tex = null;
				continue;
			}
			System.out.println("Texture for a surface (orig path): " + texFilePath);
			texFilePath = texFilePath.replaceAll("\\\\", "/");
			int lastIndex = texFilePath.lastIndexOf('/');
			if(lastIndex != -1)
				texFilePath = texFilePath.substring(lastIndex+1);
			
			TextureRawInfo texInfo = new TextureRawInfo();
			if(s.texData[0].sequenced) {
				
				texInfo.animated = true;
				
				ArrayList<String> _texs = new ArrayList<>();
				
				int lzeros = 1;
				String texprefix = texFilePath.substring(0, texFilePath.lastIndexOf('.'));
				String texextension = texFilePath.substring(texFilePath.lastIndexOf('.')+1);
				System.out.println("TEXPREFIX:    " + texprefix);
				System.out.println("TEXEXTENSION: " + texextension);
				int oneIndex = texprefix.lastIndexOf("1");
				while(texprefix.charAt(oneIndex-1) == '0') {
					oneIndex--;
					lzeros++;
				}
				
				texprefix = texprefix.substring(0, (texprefix.length()) - lzeros);
				
				int texNum = 0;
				int texOffset = 0;
				
				boolean getTexs = true;
				for(int j = 0; j < 9999; j++) {
					String pat = texprefix + String.format("%0" + lzeros + "d", texOffset+texNum) + "." + texextension;
					System.out.println("LOOKING FOR: " + pat);
					if(am.exists(pat)) {
						break;
					}
					texOffset++;
				}
				if(texOffset == 9999)
					System.out.println("over 9000?????");
				while(getTexs) {
					String texFile = texprefix + String.format("%0" + lzeros + "d", texOffset+texNum) + "." + texextension;
					if(!am.exists(texFile)) {
						break;
					}
					_texs.add(texFile);
					texNum++;
				}
				
				System.out.println("FOUND TEXTURES: " + texNum);
				
				String[] texs = new String[texNum];
				float[] times = new float[texNum];
				for(int j = 0; j < texNum; j++) {
					texs[j] = _texs.get(j);
					times[j] = j / 25.0f;
				}
				
				texInfo.files = texs;
				texInfo.times = times;
				texInfo.runlen = texNum / 25.0f;
				
			} else {
				
				texInfo.animated = false;
				texInfo.files = new String[] { texFilePath };
				
			}
			
			rawSurf.tex = texInfo;
			
		}
		
		int[] pattern = {0, 1, 3, 1, 2, 3};
		
		LinkedList<Integer> _iv = new LinkedList<>();
		LinkedList<Integer> _ipolsurf = new LinkedList<>();
		int _lpol = 0;
		int ivOffset = 0;
		for(int i = 0; i < tempPolEdgeCounts.length; i++) {
			if(tempPolEdgeCounts[i] == 4) {
				for(int j = 0; j < pattern.length; j++) {
					_iv.add(tempInds[ivOffset+pattern[j]]);
				}
				_ipolsurf.add(tempPolygonSurfaceIndices[i]);
				_ipolsurf.add(tempPolygonSurfaceIndices[i]);
				_lpol += 2;
			} else {
				for(int j = 0; j < 3; j++) {
					_iv.add(tempInds[ivOffset+j]);
				}
				_ipolsurf.add(tempPolygonSurfaceIndices[i]);
				_lpol += 1;
			}
			ivOffset += tempPolEdgeCounts[i];
		}
		
		res.rawInds = new int[_lpol*3];
//		ctm.ipolsurf = new int[_lpol];
//		ctm.polliv = new int[_lpol];
//		ctm.surfLen = new int[ctm.texs.length];
		
		int indOffset = 0;
		int curisurf = 0;
		int surflen = 0;
		for(int i = 0; i < _lpol; i++) {
			int isurf = _ipolsurf.pop();
			if(isurf > curisurf) {
				res.surfaces[curisurf].indOffset = indOffset;
				res.surfaces[curisurf].indLength = surflen;
				indOffset += surflen;
				surflen = 3;
				curisurf++;
			} else {
				surflen += 3;
			}
			res.rawInds[i*3]		= _iv.pop();
			res.rawInds[i*3+1]	= _iv.pop();
			res.rawInds[i*3+2]	= _iv.pop();
			
			calcNormal(verts.rawVerts, nVerts.rawVerts, res.rawInds[i*3], res.rawInds[i*3+1], res.rawInds[i*3+2]);
		}
		res.surfaces[curisurf].indOffset = indOffset;
		res.surfaces[curisurf].indLength = surflen;
		
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	@Override
	public void destroy() {
		
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
	public boolean canRead(String path) {
		return path.toUpperCase().endsWith(".LWO");
	}
	
}
