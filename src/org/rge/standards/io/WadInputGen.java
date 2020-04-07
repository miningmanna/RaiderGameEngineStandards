package org.rge.standards.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

import org.rge.assets.io.InputGen;

public class WadInputGen implements InputGen {
	
	public static final String INPUTGEN_TYPE = "wad";
	
	HashMap<String, WadStreamInfo> files;
	File wadFile;
	boolean valid;
	
	public WadInputGen() {
		files = new HashMap<>();
	}
	
	@Override
	public boolean init(String path) {
		
		wadFile = new File(path);
		if(!wadFile.exists() || wadFile.isDirectory()) {
			valid = false;
			return false;
		}
		
		RandomAccessFile in = null;
		try {
			in = new RandomAccessFile(wadFile, "r");
			long pos = 0;
			
			byte[] buff = new byte[2048];
			in.read(buff, 0, 8); pos+=8;
			if(!new String(buff, 0, 4).equals("WWAD")) {
				System.err.println("Different wad file type! : " + new String(buff, 0, 4));
				in.close();
				valid = false;
				return false;
			}
			
			int lentries = getIntLE(buff, 4);
			
			String[] names = new String[lentries];
			
			int buffOffset = 0;
			in.read(buff, 0, 2048); pos+=2048;
			for(int i = 0; i < lentries; i++) {
				String name = "";
				for(int j = buffOffset; j < 2048; j++) {
					if(buff[j] == 0) {
						name += new String(buff, buffOffset, j-buffOffset);
						names[i] = name.replace('\\', '/');
						buffOffset = j+1;
						if(buffOffset == 2048) {
							in.read(buff, 0, 2048); pos+=2048;
							buffOffset = 0;
						}
						break;
					}
					if(j == 2047) {
						name += new String(buff, buffOffset, 2048-buffOffset);
						in.read(buff, 0, 2048); pos+=2048;
						buffOffset = 0;
						j = -1;
					}
				}
			}
			for(int i = 0; i < lentries; i++) {
				for(int j = buffOffset; j < 2048; j++) {
					if(buff[j] == 0) {
						buffOffset = j+1;
						if(buffOffset == 2048) {
							in.read(buff, 0, 2048); pos+=2048;
							buffOffset = 0;
						}
						break;
					}
					if(j == 2047) {
						in.read(buff, 0, 2048); pos+=2048;
						buffOffset = 0;
						j = -1;
					}
				}
			}
			in.seek(pos-(2048-buffOffset));
			
			for(int i = 0; i < lentries; i++) {
				in.read(buff, 0, 16);
				WadStreamInfo info = new WadStreamInfo();
				info.length = getIntLE(buff, 8);
				info.offset = getIntLE(buff, 12);
				files.put(names[i], info);
			}
		} catch(Exception e) {
			System.out.println("Failed to load WAD entries");
			valid = false;
			try {
				if(in != null)
					in.close();
			} catch(Exception e2) {}
		}
		
		try {
			in.close();
		} catch(Exception e2) {}
		
		valid = true;
		return true;
	}
	
	@Override
	public boolean isDead() {
		return !valid;
	}
	
	@Override
	public boolean exists(String path) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public InputStream getInput(String path) {
		WadStreamInfo info = files.get(path);
		System.out.println("PATH,INFO: " + path + "," + info);
		if(info == null)
			return null;
		try {
			return new WadStream(new RandomAccessFile(wadFile, "r"), info.offset, info.length);
		} catch (FileNotFoundException e) {}
		return null;
	}
	
	@Override
	public void destroy() {
		
	}
	
	private static class WadStreamInfo {
		long offset, length;
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
	
}
