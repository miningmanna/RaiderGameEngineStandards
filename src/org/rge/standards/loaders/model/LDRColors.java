package org.rge.standards.loaders.model;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class LDRColors {
	
	HashMap<Integer, Color> colors;
	public LDRColors() {
		colors = new HashMap<>();
	}
	
	public void generateFromFile(InputStream in) {
		try {
			if(!colors.isEmpty())
				colors.clear();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String line;
			lineloop: while ((line = br.readLine()) != null) {
				line = line.trim();
				if(line.equals("") || !line.startsWith("0"))
					continue;
				
				System.out.println(line);
				
				int code = -1;
				Color color = null;
				
				String[] split = line.split(" ");
				int ioff = 0;
				for(int i = 0; i < split.length; i++) {
					if(split[i].equals("")) {
						ioff--;
						continue;
					}
					int ri = i+ioff;
					
					
					System.out.println("i, ri, split[i] : " + i + ", " + ri + ", " + split[i]);
					switch (ri) {
						case 0:
							if(!split[i].equals("0"))
								continue lineloop;
							break;
						
						case 1:
							if(!split[i].equals("!COLOUR"))
								continue lineloop;
							break;
						
						case 3:
							if(!split[i].equals("CODE"))
								continue lineloop;
							break;
						
						case 4:
							code = Integer.parseInt(split[i]);
							break;
						
						case 5:
							if(!split[i].equals("VALUE"))
								continue lineloop;
							break;
						
						case 6:
							color = new Color(Integer.parseInt(split[i].substring(1), 16));
							break;
						
						default:
							break;
					}
				}
				
				System.out.println("Color?: " + code + " " + color);
				
				if(color == null || code == -1)
					continue;
				
				colors.put(code, color);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Color getColor(int num) {
		return colors.get(num);
	}
	
}
