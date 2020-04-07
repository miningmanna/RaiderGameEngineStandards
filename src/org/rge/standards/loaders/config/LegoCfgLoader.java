package org.rge.standards.loaders.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

import org.rge.assets.AssetManager;
import org.rge.assets.Loader;
import org.rge.assets.config.Config;
import org.rge.assets.config.Config.ConfigNode;

public class LegoCfgLoader implements Loader {
	
	public static final String[] LOADER_TYPES				= { "LEGOCFG" };
	public static final Class<?>[] LOADER_RETURN_TYPES	= { Config.class };
	
	@Override
	public void init(AssetManager am) {
		
	}
	
	@Override
	public Object get(String path, AssetManager am) {
		Config res = new Config();
		ConfigNode node = new ConfigNode();
		res.baseNode = node;
		
		boolean error = false;
		try {
			InputStream in = am.getAsset(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			Stack<ConfigNode> nodeStack = new Stack<>();
			String line = null;
			String lastToken = null;
			while((line = br.readLine()) != null) {
				line = line.replace('\t', ' ').trim();
				if(line.length() == 0)
					continue;
				
				int commentStart = line.length()+1;
				int semicolonCommentStart = line.indexOf(';');
				if(semicolonCommentStart == 0)
					continue;
				if(semicolonCommentStart < commentStart && semicolonCommentStart != -1)
					commentStart = semicolonCommentStart;
				
				int doubleSlashCommentStart = line.indexOf("//");
				if(semicolonCommentStart == 0)
					continue;
				if(doubleSlashCommentStart < commentStart && doubleSlashCommentStart != -1)
					commentStart = doubleSlashCommentStart;
				
				line = line.substring(0, commentStart-1).trim();
				if(line.indexOf(' ') == -1) {
					if(line.equals("}")) {
						
						// SUBNODE END
						node = nodeStack.pop();
						
						continue;
					} else if(lastToken == null) {
						
						lastToken = line;
						continue;
						
					}
					if(line.equals("{") && lastToken != null) {
						
						// SUBNODE START
						nodeStack.push(node);
						ConfigNode newNode = new ConfigNode();
						node.setSubNode(lastToken, newNode);
						node = newNode;
						lastToken = null;
						continue;
						
					}
					System.out.println("Invalid line! \"" + line + "\"");
					error = true;
					break;
				}
				
				line = line.replaceAll(" +", " ");
				
				String[] split = line.split(" ");
				if(split.length != 2) {
					System.out.println("Invalid line! \"" + line + "\"");
					error = true;
					break;
				}
				if(split[1].equals("{")) {
					
					// SUBNODE START
					nodeStack.push(node);
					ConfigNode newNode = new ConfigNode();
					node.setSubNode(split[0], newNode);
					node = newNode;
					
				} else {
					
					// VALUE
					node.setValue(split[0], split[1]);
					
				}
			}
			
			br.close();
		} catch(Exception e) {
			return null;
		}
		if(error)
			return null;
		return res;
	}
	
	@Override
	public boolean canRead(String path) {
		return path.toUpperCase().endsWith(".CFG");
	}
	
	@Override
	public void destroy() {
		
	}
	
}
