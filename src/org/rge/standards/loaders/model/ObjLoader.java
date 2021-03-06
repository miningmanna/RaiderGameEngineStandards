package org.rge.standards.loaders.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.rge.assets.AssetManager;
import org.rge.assets.Loader;
import org.rge.assets.models.Model.RawData;
import org.rge.assets.models.Model.RawData.RawSurface;
import org.rge.assets.models.Verts;

public class ObjLoader implements Loader {
	
	public static final String[] LOADER_TYPES				= { "obj" };
	public static final Class<?>[] LOADER_RETURN_TYPES	= { RawData.class };
	
	@Override
	public void init(AssetManager am) {
		
	}
	
	// TODO: fix loader: no surfaces to draw
	
	@Override
	public RawData get(String path, AssetManager am) {
		
		InputStream in = am.getAsset(path);
		if(in == null)
			return null;
		
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(in));
		
		Verts verts = new Verts();
		verts.pos = 0;
		verts.dimension = 3;
		
		Verts vTex = new Verts();
		vTex.pos = 1;
		vTex.dimension = 2;
		
		ArrayList<Float> vertsRawList = new ArrayList<>();
		ArrayList<Float> texVertsRawList = new ArrayList<>();
		ArrayList<Integer> indsRawList = new ArrayList<>();
		
		String line;
		try {
			lineLoop: while((line = lineReader.readLine()) != null) {
				
				if(line.length() == 0)
					continue;
				
				if(line.startsWith("v ")) {
					
					String[] split = line.split(" ");
					if(split.length != 4) {
						System.out.println("Vert has an incorrect amount of components! " + (split.length-1) + " components");
						continue;
					}
					
					float[] components = new float[3];
					for(int i = 1; i < split.length; i++) {
						try {
							components[i-1] = Float.parseFloat(split[i]);
						} catch(Exception e) {
							System.err.println("Couldnt parse component: " + i);
							e.printStackTrace();
							continue lineLoop;
						}
					}
					
					for(int i = 0; i < components.length; i++)
						vertsRawList.add(components[i]);
					
					continue;
				}
				
				if(line.startsWith("vt ")) {
					String[] split = line.split(" ");
					if(split.length != 3) {
						System.out.println("Tex vert has an incorrect amount of components! " + (split.length-1) + " components");
						continue;
					}
					
					float[] components = new float[2];
					for(int i = 1; i < split.length; i++) {
						try {
							components[i-1] = Float.parseFloat(split[i]);
						} catch(Exception e) {
							System.err.println("Couldnt parse component: " + i);
							e.printStackTrace();
							continue lineLoop;
						}
					}
					
					for(int i = 0; i < components.length; i++)
						texVertsRawList.add(components[i]);
					
					continue;
					
				}
				
				if(line.charAt(0) == 'f') {
					
					String[] split = line.split(" ");
					if(split.length != 4) {
						System.out.println("Face has too many vertices! " + (split.length-1) + " vertices");
						continue;
					}
					
					int[] indices = new int[3];
					for(int i = 1; i < split.length; i++) {
						try {
							if(split[i].contains("/"))
								split[i] = split[i].split("/")[0];
							
							indices[i-1] = Integer.parseInt(split[i])-1;
						} catch(Exception e) {
							System.err.println("Couldnt parse index: " + i);
							e.printStackTrace();
							continue lineLoop;
						}
					}
					
					for(int i = 0; i < indices.length; i++)
						indsRawList.add(indices[i]);
					
					continue;
				}
				
			}
		} catch (IOException e) {
			System.err.println("Couldnt load OBJ model");
			e.printStackTrace();
			return null;
		}
		
		verts.rawVerts = new float[vertsRawList.size()];
		for(int i = 0; i < verts.rawVerts.length; i++)
			verts.rawVerts[i] = vertsRawList.get(i);
		
		vTex.rawVerts = new float[texVertsRawList.size()];
		for(int i = 0; i < vTex.rawVerts.length; i++)
			vTex.rawVerts[i] = texVertsRawList.get(i);
		
		
		RawData res = new RawData();
		res.verts = new Verts[] { verts, vTex };
		
		res.rawInds = new int[indsRawList.size()];
		for(int i = 0; i < res.rawInds.length; i++)
			res.rawInds[i] = indsRawList.get(i);
		
		RawSurface s = new RawSurface();
		s.additiveColor = false;
		s.doubleSided = true;
		s.isTranslucent = false;
		s.indOffset = 0;
		s.indLength = res.rawInds.length;
		
		res.surfaces = new RawSurface[] { s };
		
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
	
	@Override
	public boolean canRead(String path) {
		return path.toUpperCase().endsWith(".OBJ");
	}
	
}
