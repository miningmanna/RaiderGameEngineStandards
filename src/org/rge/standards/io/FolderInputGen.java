package org.rge.standards.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.rge.assets.io.InputGen;

public class FolderInputGen implements InputGen {
	
	public static final String INPUTGEN_TYPE = "dir";
	
	File folder;
	boolean valid;
	
	@Override
	public boolean init(String path) {
		
		if(path == null) {
			valid = false;
			return false;
		}
		
		folder = new File(path);
		if(folder.isFile()) {
			valid = false;
			return false;
		}
		System.out.println("Initiating FolderInputGen at: " + folder.getAbsolutePath());
		
		valid = true;
		return true;
	}
	
	@Override
	public boolean isDead() {
		return !valid;
	}
	
	@Override
	public InputStream getInput(String path) {
		if(!valid)
			return null;
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
