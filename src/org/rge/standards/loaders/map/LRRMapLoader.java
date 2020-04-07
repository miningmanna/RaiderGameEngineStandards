package org.rge.standards.loaders.map;

import java.io.IOException;
import java.io.InputStream;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.rge.assets.Loader;

public class LRRMapLoader implements Loader {
	
	public static final String[] LOADER_TYPES				= { "MAP" };
	public static final Class<?>[] LOADER_RETURN_TYPES	= { LuaTable.class };
	
	@Override
	public void init(org.rge.assets.AssetManager am) {
	}

	@Override
	public Object get(String path, org.rge.assets.AssetManager am) {
		
		InputStream is = am.getAsset(path);
		if(is == null)
			return null;
		
		
		LuaTable res = null;
		try {
			
			int[][] data = loadMapDataStream(is);
			
			LuaTable _res = new LuaTable(data[0].length, 0);
			for(int x = 0; x < data[0].length; x++) {
				LuaTable column = new LuaTable(data.length, 0);
				_res.set(1+x, column);
				for(int y = 0; y < data.length; y++)
					column.set(1+y, LuaValue.valueOf(data[y][x]));
			}
			
			res = _res;
			
		} catch(Exception e) {
			try {
				is.close();
			} catch (IOException e2) {}
		}
		
		try {
			is.close();
		} catch (IOException e) {}
		
		return res;
	}
	
	public static int[][] loadMapDataStream(InputStream in) throws IOException {
		in.skip(4);
		byte[] buff = new byte[4];
		in.read(buff);
		int len = getIntLE(buff, 0);
		buff = new byte[len];
		in.read(buff);
		in.close();
		
		int w = getIntLE(buff, 0);
		int h = getIntLE(buff, 4);
		int[][] res = new int[h][w];
		
		int offset = 6;
		for(int i = 0; i < h; i++)
			for(int j = 0; j < w; j++)
				res[i][j] = getShortLE(buff, (offset += 2));
		
		return res;
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
	
	private static int getShortLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 2; i++) {
			res = res | (0x000000FF & b[off+1-i]);
			if(i != 1)
				res = res << 8;
		}
		
		return res;
		
	}

	@Override
	public boolean canRead(String path) {
		if(!path.toLowerCase().endsWith(".map"))
			return false;
		
		return true;
	}

	@Override
	public void destroy() {
	}
	
	/*private static final String[] CFG_KEYS = {
			"TerrainMap",
			"PredugMap",
			"CryoreMap",
			"BlockPointersMap",
			"SurfaceMap",
			"PathMap",
			"EmergeMap",
			"ErodeMap",
			"FallinMap"
	};
	
	public int[][][] maps;
	public int width, height;
	
	
	public static LRRMapLoader getMapData() throws Exception {
		
		LRRMapLoader res = new LRRMapLoader();
		res.maps = new int[9][][];
		
		for(int i = 0; i < CFG_KEYS.length; i++) {
			String f = cfg.getOptValue(CFG_KEYS[i], null);
			if(f != null) {
				res.loadData(i, f, am);
			}
		}
		res.ensureAllData();
		
		return res;
		
	}
	
	private void loadData(int mapType, String path, AssetManager am) throws Exception {
		
		InputStream in = am.getAsset(path);
		if(in == null)
			return;
		int[][] data = loadMapDataStream(in);
		in.close();
		if(width == 0 && height == 0) {
			height = data.length;
			width = data[0].length;
		} else {
			if(data.length != height || data[0].length != width)
				throw new Exception("Different map sizes!");
		}
		
		maps[mapType] = data;
		
	}
	
	@SuppressWarnings("unused")
	private void loadData(int mapType, File f) throws Exception {
		
		InputStream in = new FileInputStream(f);
		int[][] data = loadMapDataStream(in);
		in.close();
		if(width == 0 && height == 0) {
			height = data.length;
			width = data[0].length;
		} else {
			if(data.length != height || data[0].length != width)
				throw new Exception("Different map sizes!");
		}
		
		maps[mapType] = data;
		
	}
	
	private void ensureAllData() {
		for(int i = 0; i < 9; i++) {
			if(maps[i] == null)
				maps[i] = new int[height][width];
		}
	}
	
	public static int[][] loadMapDataFile(File f) {
		FileInputStream in = null;
		int[][] res = null;
		try {
			in = new FileInputStream(f);
			res = loadMapDataStream(in);
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	public static int[][] loadMapDataStream(InputStream in) throws IOException {
		in.skip(4);
		byte[] buff = new byte[4];
		in.read(buff);
		int len = getIntLE(buff, 0);
		buff = new byte[len];
		in.read(buff);
		in.close();
		
		int w = getIntLE(buff, 0);
		int h = getIntLE(buff, 4);
		int[][] res = new int[h][w];
		
		int offset = 6;
		for(int i = 0; i < h; i++)
			for(int j = 0; j < w; j++)
				res[i][w-1-j] = getShortLE(buff, (offset += 2));
		
		return res;
	}
	
	public static void printAllValues(int[][] map) {
		
		ArrayList<Integer> vals = new ArrayList<>();
		
		for(int i = 0; i < map.length; i++)
			for(int j = 0; j < map[i].length; j++)
				if(!vals.contains(map[i][j]))
					vals.add(map[i][j]);
		
		for(int i : vals)
			System.out.println("vals: " + i);
		
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
	
	private static int getShortLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 2; i++) {
			res = res | (0x000000FF & b[off+1-i]);
			if(i != 1)
				res = res << 8;
		}
		
		return res;
		
	}
	
	public static void main(String[] args) {
		
		File f = new File("dugg_22.map");
		int[][] cave = loadMapDataFile(f);
		f = new File("surf_22.map");
		int[][] surf = loadMapDataFile(f);
		
		boolean[][] utilBuffer = new boolean[surf.length][surf[0].length];
		for(int i = 0; i < utilBuffer.length; i++)
			for(int j = 0; j < utilBuffer[i].length; j++)
				 utilBuffer[i][j] = false;
		
		int[] cliffTypes = new int[] { 1, 2, 3, 4, 8, 10, 11 };
		
		int h = cave.length;
		int w = cave[0].length;
		
//		ArrayList<Point> todo = new ArrayList<>();
//		
//		for(int z = 0; z < h; z++) {
//			for(int x = 0; x < w; x++) {
//				if(cave[z][x] != 2 && !contains(cliffTypes, surf[z][x])) {
//					System.out.println("ADDING: " + x + " " + z + " " + surf[z][x] + " " + cave[z][x]);
//					utilBuffer[z][x] = true;
//					todo.add(new Point(x, z));
//				}
//			}
//		}
//		
//		while(todo.size() > 0) {
//			Point p = todo.get(0);
//			todo.remove(0);
//			for(int i = -1; i < 2; i++) {
//				for(int j = -1; j < 2; j++) {
//					if((p.x+j < w) && (p.x+j >= 0) && (p.z+i < h) && (p.z+i >= 0) && !(i == 0 && j == 0)) {
//						if(!utilBuffer[p.z+i][p.x+j] && !contains(cliffTypes, surf[p.z+i][p.x+j])) {
//							System.out.println("ADDING: " + p + " " + j + " " + i + " " + cave[p.z+i][p.x+j] + " " + surf[p.z+i][p.x+j]);
//							cave[p.z+i][p.x+j] = 1;
////							surf[p.z+i][p.x+j] = 6;
//							utilBuffer[p.z+i][p.x+j] = true;
//							todo.add(new Point(p.x+j, p.z+i));
//						}
//					}
//				}
//			}
//		}
		
		ArrayList<Point> todo = new ArrayList<>();
		
		for(int z = 0; z < h; z++) {
			for(int x = 0; x < w; x++) {
				if(cave[z][x] != 0 && !contains(cliffTypes, surf[z][x])) {
					utilBuffer[z][x] = true;
					todo.add(new Point(x, z));
				}
			}
		}
		
		while(todo.size() > 0) {
			Point p = todo.get(0);
			todo.remove(0);
			for(int i = -1; i < 2; i++) {
				for(int j = -1; j < 2; j++) {
					if((p.x+j < w) && (p.x+j >= 0) && (p.z+i < h) && (p.z+i >= 0) && !(i == 0 && j == 0)) {
						if(!utilBuffer[p.z+i][p.x+j] && !contains(cliffTypes, surf[p.z+i][p.x+j])) {
							surf[p.z+i][p.x+j] = 20;
							utilBuffer[p.z+i][p.x+j] = true;
						}
					}
				}
			}
		}
		
		JFrame frame = new JFrame("ass");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MapVis(surf));
		frame.setVisible(true);
		
		
	}
	
	public static class Point {
		int x, z;
		public Point(int x, int z) {
			this.x = x;
			this.z = z;
		}
	}
	
	public static boolean contains(int[] a, int val) {
		for(int i = 0; i < a.length; i++)
			if(a[i] == val)
				return true;
		return false;
	}*/
	
}
