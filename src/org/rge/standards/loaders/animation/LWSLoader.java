package org.rge.standards.loaders.animation;

import java.io.IOException;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.rge.assets.AssetManager;
import org.rge.assets.Loader;
import org.rge.node.Move;
import org.rge.standards.loaders.animation.LwsFileData.ObjectKeyFrames;
import org.rge.lua.compat.Matrix4;

public class LWSLoader implements Loader {
	
	public static final String[] LOADER_TYPES				= { "LWS" };
	public static final Class<?>[] LOADER_RETURN_TYPES	= { LuaTable.class };
	
	@Override
	public void init(AssetManager am) {
		
	}
	
	@Override
	public Object get(String path, AssetManager am) {
		
		LuaTable res = new LuaTable();
		LuaTable moveSet = new LuaTable();
		LuaTable models = new LuaTable();
		LuaTable nodeNames = new LuaTable();
		
		res.set("moveSet", moveSet);
		res.set("models", models);
		res.set("nodes", nodeNames);
		
		LwsFileData data = null;
		try {
			data = LwsFileData.getLwsFileData(am.getAsset(path));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		float runLen = (data.lastFrame - data.firstFrame)/data.framesPerSecond;
		
		LuaTable[] moveSetNodes = new LuaTable[data.objFiles.length];
		LuaTable[] modelsNodes = new LuaTable[data.objFiles.length];
		LuaTable[] nodeNameNodes = new LuaTable[data.objFiles.length];
		
		for(int i = 0; i < data.objFiles.length; i++) {
			
			ObjectKeyFrames frames = data.frames[i];
			
			LuaTable moveSetNode = new LuaTable();
			moveSetNodes[i] = moveSetNode;
			Move move = new Move();
			moveSetNode.set("move", move.getEngineReference());
			move.runLen = runLen;
			move.loop = false;
			move.keys = frames.transforms;
			
			move.times = new float[frames.lframes];
			for(int j = 0; j < move.times.length; j++) {
				move.times[j] = (frames.frames[j] - data.firstFrame) / data.framesPerSecond;
				
				// update lua values on the fly
				move.luaTimes.set(j+1, LuaValue.valueOf(move.times[j]));
				move.luaKeys.set(j+1, ((Matrix4) move.keys[j]).getEngineReference());
			}
			
			LuaTable modelsNode = new LuaTable();
			modelsNodes[i] = modelsNode;
			if(data.objFiles[i] != null)
				modelsNode.set("model", LuaValue.valueOf(data.objFiles[i]));
			
			LuaTable nodeNameNode = new LuaTable();
			nodeNameNodes[i] = nodeNameNode;
			nodeNameNode.set("name", LuaValue.valueOf(data.nodeNames[i]));
			
		}
		
		for(int i = 0; i < modelsNodes.length; i++) {
			
			int parent = data.parent[i];
			System.out.println("PARENT: " + data.parent[i]);
			if(parent == -1) {
				
				moveSet.set(data.nodeNames[i], moveSetNodes[i]);
				models.set(data.nodeNames[i], modelsNodes[i]);
				nodeNames.set(data.nodeNames[i], nodeNameNodes[i]);
				
			} else {
				System.out.println("NAME: " + data.nodeNames[parent]);
				moveSetNodes[parent].set(data.nodeNames[i], moveSetNodes[i]);
				modelsNodes[parent].set(data.nodeNames[i], modelsNodes[i]);
				nodeNameNodes[parent].set(data.nodeNames[i], nodeNameNodes[i]);
				
			}
				
		}
		
		res.set("toDrawNodeFunctionString",
		"\r\n" + 
		"local function genNodeTree(root, nodeNames, models)\r\n" + 
		"	\r\n" + 
		"	for k,v in ipairs(nodeNames) do\r\n" + 
		"		if k ~= \"name\" then\r\n" + 
		"			local node = rge.newDrawNode()\r\n" + 
		"			node.name(k)\r\n" + 
		"			if models[k].model ~= nil then\r\n" + 
		"				node.model(rge.get(models[k].model))\r\n" + 
		"			end\r\n" + 
		"			root.addSubNode(node)\r\n" + 
		"			genNodeTree(node, v, models[k])\r\n" + 
		"		end\r\n" + 
		"	end\r\n" + 
		"end\r\n" + 
		"\r\n" + 
		"local root = rge.newDrawNode()\r\n" + 
		"\r\n" + 
		"genNodeTree(root, lws.nodes, lws.models)\r\n" + 
		"\r\n" + 
		"root.setMoves(lws.moveSet)\r\n" + 
		"\r\n" + 
		"return root");
		
//		InputStream in = null;
//		try {
//			
//			in = am.getAsset(path);
//			BufferedReader br = new BufferedReader(new InputStreamReader(in));
//			
//			String line = br.readLine();
//			if(line == null || !line.equals("LWSC")) {
//				in.close();
//				return null;
//			}
//			line = br.readLine();
//			if(line == null) {
//				in.close();
//				return null;
//			}
//			
//			int ff = 0, lf = 0, parent = -1;
//			float fps = 30;
//			
//			ArrayList<Matrix4> mats = null;
//			ArrayList<Float> times = null;
//			
//			HashMap<String, Integer> index = new HashMap<>();
//			ArrayList<Integer> parents = new ArrayList<>();
//			ArrayList<LuaTable> moveSetArray = new ArrayList<>();
//			ArrayList<LuaTable> modelsArray = new ArrayList<>();
//			ArrayList<LuaTable> NodeNamesArray = new ArrayList<>();
//						
//			while((line = br.readLine()) != null) {
//				
//				if(line.startsWith("FirstFrame")) {
//					
//					ff = Integer.parseInt(line.split(" ")[1]);
//					
//				} else if(line.startsWith("LastFrame")) {
//					
//					lf = Integer.parseInt(line.split(" ")[1]);
//					
//				} else if(line.startsWith("FramesPerSecond")) {
//					
//					fps = Float.parseFloat(line.split(" ")[1]);
//					
//				} else if(line.startsWith("LoadObject")) {
//					
//					if(mats != null) {
//						
//						LuaTable moveSetInfo = new LuaTable();
//						Move move = new Move();
//						move.loop = false;
//						move.runLen = (lf - ff)/fps;
//						float[] ts = new float[times.size()];
//						for(int i = 0; i < times.size(); i++)
//							ts[i] = times.get(i);
//						move.times = ts;
//						
//						Matrix4f[] keys = new Matrix4f[mats.size()];
//						for(int i = 0; i < mats.size(); i++)
//							keys[i] = mats.get(i);
//						move.keys = keys;
//						
//						moveSetInfo.set("move", move.getEngineReference());
//						moveSetArray.add(moveSetInfo);
//						parents.add(parent);
//						
//					}
//					
//					mats = new ArrayList<>();
//					times = new ArrayList<>();
//					
//					line = line.substring(line.indexOf(' ')+1);
//					if(line.startsWith("\\\\"))
//						line = line.substring(line.lastIndexOf('\\'));
//					
//					LuaTable modelInfo = new LuaTable();
//					modelInfo.set("model", line);
//					modelsArray.add(modelInfo);
//					
//					LuaTable nodeNameInfo = new LuaTable();
//					int i = 0;
//					if(index.containsKey(line))
//						i = index.get(line);
//					index.put(line, i+1);
//					
//					nodeNameInfo.set("name", line + index);
//					NodeNamesArray.add(nodeNameInfo);
//					
//				} else if(line.startsWith("AddNullObject")) {
//					
//					if(mats != null) {
//						
//						LuaTable moveSetInfo = new LuaTable();
//						Move move = new Move();
//						move.loop = false;
//						move.runLen = (lf - ff)/fps;
//						float[] ts = new float[times.size()];
//						for(int i = 0; i < times.size(); i++)
//							ts[i] = times.get(i);
//						move.times = ts;
//						
//						Matrix4f[] keys = new Matrix4f[mats.size()];
//						for(int i = 0; i < mats.size(); i++)
//							keys[i] = mats.get(i);
//						move.keys = keys;
//						
//						moveSetInfo.set("move", move.getEngineReference());
//						moveSetArray.add(moveSetInfo);
//						parents.add(parent);
//						
//					}
//					
//					line = line.substring(line.indexOf(' ')+1);
//					if(line.startsWith("\\\\"))
//						line = line.substring(line.lastIndexOf('\\'));
//					
//					LuaTable modelInfo = new LuaTable();
//					modelsArray.add(modelInfo);
//					
//					LuaTable nodeNameInfo = new LuaTable();
//					int i = 0;
//					if(index.containsKey(line))
//						i = index.get(line);
//					index.put(line, i+1);
//					
//					nodeNameInfo.set("name", line + index);
//					NodeNamesArray.add(nodeNameInfo);
//					
//				} else if(line.startsWith("ObjectMotion")) {
//					
//					
//					
//				}
//				
//			}
//			
//			br.close();
//			
//		} catch(Exception e) {
//			try {
//				if(in != null)
//					in.close();
//			} catch(Exception e2) {}
//			return null;
//		}
		
		return res;
	}
	
	@Override
	public boolean canRead(String path) {
		return path.toUpperCase().endsWith(".LWS");
	}
	
	@Override
	public void destroy() {
		
	}
	
}
