package org.rge.standards.loaders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.rge.assets.AssetManager;

public class LDRContent {
	
	/**public static void main(String[] args) {
		
		FileFinder finder = new FileFinder();
		finder.register(new File("C:\\Users\\USER\\Desktop\\ldraw\\models"));
		finder.register(new File("C:\\Users\\USER\\Desktop\\ldraw\\p"));
		finder.register(new File("C:\\Users\\USER\\Desktop\\ldraw\\parts"));
		
		String name = "car";
		
		LDRContent content = new LDRContent();
		content.transform = new Matrix4f().identity();
		content.absTransform = new Matrix4f().identity();
		content.parseFile(finder.get(name + ".ldr"), finder, content.transform);
		
		System.out.println("-----------------------------------------");
		
		try {
			PrintWriter writer = new PrintWriter(name + ".obj");
			content.printToObj(writer);
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}**/
	int colorNum;
	String name;
	Matrix4f transform;
	Matrix4f absTransform;
	LDRContent[] subContent;
	int quadOrientation;
	int[] triColors;
	float[] verts;  // Position of vertices
	int[] inds; // All triangles;
	
	public ArrayList<LDRContent> getAllEntries() {
		ArrayList<LDRContent> entries = new ArrayList<>();
		putEntries(this, entries);
		return entries;
	}
	
	public void printToObj(PrintWriter writer) {
		
		ArrayList<LDRContent> entries = new ArrayList<>();
		putEntries(this, entries);
		
		int[] indOffsets = new int[entries.size()];
		int off = 0;
		for(int i = 0; i < entries.size(); i++) {
			indOffsets[i] = off;
			off += entries.get(i).verts.length/3;
			entries.get(i).printVerts(writer);
		}
		
		writer.println();
		
		for(int i = 0; i < entries.size(); i++)
			entries.get(i).printFaces(indOffsets[i], writer);
		
	}
	
	public void printVerts(PrintWriter writer) {
		Vector3f temp = new Vector3f();
		System.out.println(verts.length/3);
		for(int i = 0; i < verts.length/3; i++) {
			temp.x = verts[i*3+0];
			temp.y = verts[i*3+1];
			temp.z = verts[i*3+2];
			
			absTransform.transformPosition(temp);
			
			System.out.println("v " + temp.x + " " + temp.y + " " + temp.z);
			writer.println("v " + temp.x + " " + temp.y + " " + temp.z);
		}
	}
	
	public void printFaces(int off, PrintWriter writer) {
		for(int i = 0; i < inds.length/3; i++)
			writer.println("f " + (off+inds[i*3+0]+1) + " " + (off+inds[i*3+1]+1) + " " + (off+inds[i*3+2]+1));
	}
	
	private static void putEntries(LDRContent content, ArrayList<LDRContent> entries) {
		
		entries.add(content);
		for(LDRContent subContent : content.subContent)
			putEntries(subContent, entries);
		
	}
	
	public void parseFile(InputStream in, AssetManager am, Matrix4f parentTransform, int givenColor, boolean inverseOrder) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			
			ArrayList<LDRContent>	_subContent	= new ArrayList<>();
			ArrayList<Float>		_verts		= new ArrayList<>();
			ArrayList<Integer>		_inds		= new ArrayList<>();
			ArrayList<Integer>		_triColors	= new ArrayList<>();
			
			int nextIndNum = 0;
			boolean invertNext = false;
			boolean ccw = true;
			
			lineLoop: while((line = br.readLine()) != null) {
				line = line.trim();
				if(line.equals(""))
					continue;
				
				int num = line.charAt(0) - '0';
				switch (num) {
					case 0:
						// COMMENT OR META-COMMAND
						{
							String[] _content = line.substring(0).trim().split(" ");
							String[] content = new String[_content.length];
							int contentLen = 0;
							int ioff = 0;
							for(int i = 0; i < _content.length; i++) {
								if(_content[i].equals("")) {
									ioff--;
									continue;
								}
								contentLen++;
								content[i+ioff] = _content[i];
							}
							
							if(contentLen < 2)
								continue;
							
							if(content[1] == null)
								continue lineLoop;
							if(content[1].equals("BFC")) {
								
								
								if(content[2].equals("INVERTNEXT")) {
									invertNext = true;
									continue lineLoop;
								}
								
								if(content[2].equals("CERTIFY")) {
									
									if(content[3] != null) {
										ccw = !content[3].equals("CW");
									} else {
										ccw = true;
									}
									continue lineLoop;
									
								}
								
								if(content[2].equals("CW")) {
									ccw = false;
									continue lineLoop;
								}
								
								if(content[2].equals("CCW")) {
									ccw = true;
									continue lineLoop;
								}
								
							}
						}
						break;
					
					case 1:
						// SUBFILE
						{
							float[] m = new float[16];
							m[15] = 1;
							String[] split = line.split(" ");
							int color = Integer.parseInt(split[1]);
							int ioff = 0;
							for(int i = 0; i < 3; i++) {
								if(split[2+ioff+i].equals("")) {
									i--;
									ioff++;
									continue;
								}
								m[12+i] = Float.parseFloat(split[2+ioff+i]);
							}
							for(int i = 0; i < 9; i++) {
								if(split[5+ioff+i].equals("")) {
									i--;
									ioff++;
									continue;
								}
								int col = i%3;
								int row = Math.floorDiv(i, 3);
								m[col*4+row] = Float.parseFloat(split[5+ioff+i]);
							}
							String fileName = split[14+ioff];
							System.out.println("SUBFILE: " + fileName);
							LDRContent sub = new LDRContent();
							sub.transform = new Matrix4f().set(m);
							sub.absTransform = new Matrix4f();
							parentTransform.mul(sub.transform, sub.absTransform);
							InputStream subIn = am.getAsset(fileName);
							if(subIn == null)
								continue;
							
							if(sub.transform.determinant3x3() < 0)
								invertNext = !invertNext;
							
							sub.parseFile(subIn, am, sub.absTransform, (givenColor < 0 ? color : givenColor), inverseOrder^invertNext);
							System.out.println("Finished parsing: " + fileName);
							invertNext = false;
							subIn.close();
							_subContent.add(sub);
						}
						break;
					
					case 2:
						// LINE - IGNORED
						break;
					
					case 3:
						// TRIANGLE
						{
							String[] split = line.split(" ");
							int color = Integer.parseInt(split[1]);
							int ioff = 0;
							for(int i = 0; i < 9; i++) {
								if(split[2+ioff+i].equals("")) {
									i -= 1;
									ioff++;
									continue;
								}
								_verts.add(Float.parseFloat(split[2+ioff+i]));
							}
							
							for(int i = 0; i < 3; i++) {
								int mi = ((ccw^inverseOrder) ? i : (i*2)%3);
								if(givenColor < 0)
									_triColors.add(color);
								else
									_triColors.add(givenColor);
								_inds.add(nextIndNum+mi);
							}
							nextIndNum += 3;
						}
						break;
					
					case 4:
						// QUAD
						{
							int[] pattern = {
									nextIndNum, nextIndNum+1, nextIndNum+2,
									nextIndNum+2, nextIndNum+3, nextIndNum+0
							};
							if((!ccw)^inverseOrder)
								pattern = new int[] {
										nextIndNum+2, nextIndNum+1, nextIndNum,
										nextIndNum+3, nextIndNum+2, nextIndNum
								};
							String[] split = line.split(" ");
							int color = Integer.parseInt(split[1]);
							int ioff = 0;
							for(int i = 0; i < 4*3; i++) {
								if(split[2+ioff+i].equals("")) {
									i -= 1;
									ioff++;
									continue;
								}
								
								float f = Float.parseFloat(split[2+ioff+i]);
								_verts.add(f);
								
							}
							if(givenColor < 0) {
								_triColors.add(color);
								_triColors.add(color);
							} else {
								_triColors.add(givenColor);
								_triColors.add(givenColor);
							}
							for(int i = 0; i < pattern.length; i++)
								_inds.add(pattern[i]);
							nextIndNum += 4;
						}
						break;
					
					case 5:
						// OPTIONAL LINE - IGNORED
						break;
					
					default:
						System.out.println("Unknown case! : " + line);
						break;
				}
				
			}
			
			verts = new float[_verts.size()];
			for(int i = 0; i < verts.length; i++)
				verts[i] = _verts.get(i);
			
			inds = new int[_inds.size()];
			for(int i = 0; i < inds.length; i++)
				inds[i] = _inds.get(i);
			
			triColors = new int[_triColors.size()];
			for(int i = 0; i < triColors.length; i++)
				triColors[i] = _triColors.get(i);
			
			subContent = new LDRContent[_subContent.size()];
			for(int i = 0; i < _subContent.size(); i++)
				subContent[i] = _subContent.get(i);
			
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
