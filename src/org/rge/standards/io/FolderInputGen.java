package org.rge.standards.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.rge.assets.io.InputGen;

public class FolderInputGen implements InputGen {
	
	public static final String INPUTGEN_TYPE = "dir";
	
	File folder;
	
	@Override
	public void init(String path) {
		
		if(path == null)
			return;
		
		folder = new File(path);
		System.out.println("Initiating FolderInputGen at: " + folder.getAbsolutePath());
		
	}
	
	@Override
	public InputStream getInput(String path) {
		System.out.println("Trying to get: " + path);
		File f = new File(folder, path);
		if(!f.exists())
			return null;
		try {
			return new FileInputStream(f);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	@Override
	public void destroy() {
		// Empty
	}

	@Override
	public boolean exists(String path) {
		File f = new File(folder, path);
		return (f.exists() && f.isFile());
	}
	
}
