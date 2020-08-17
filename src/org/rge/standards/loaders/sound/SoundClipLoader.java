package org.rge.standards.loaders.sound;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.rge.assets.AssetManager;
import org.rge.assets.Loader;
import org.rge.assets.audio.SoundClip.RawSoundClip;

public class SoundClipLoader implements Loader {
	
	public static final String[] LOADER_TYPES				= { "wav", "ogg" };
	public static final Class<?>[] LOADER_RETURN_TYPES	= { RawSoundClip.class, AudioInputStream.class };
	@Override
	public void init(AssetManager am) {
		
	}
	
	
	@Override
	public Object get(String path, AssetManager am) {
		
		if(path.toUpperCase().endsWith(".WAV"))
			return loadSoundClip(path, am);
		if(path.toUpperCase().endsWith(".OGG"))
			return loadSoundStream(path, am);
		
		return null;
	}
	
	private RawSoundClip loadSoundClip(String path, AssetManager am) {
		System.out.println("WHAT THE FUCK?");
		
		InputStream dataIn = am.getAsset(path);
		
		dataIn = new BufferedInputStream(dataIn);
		AudioInputStream in = null;
		try {
			in = AudioSystem.getAudioInputStream(dataIn);
		} catch (Exception e1) {
			e1.printStackTrace();
			try { dataIn.close(); } catch(Exception e2) {}
			return null;
		}
		if(in == null)
			return null;
		
		AudioFormat enc = new AudioFormat(in.getFormat().getSampleRate(), 16, 2, true, false);
		in = AudioSystem.getAudioInputStream(enc, in);
		
		long size = in.getFrameLength()*4;
		
		if(size < 0)
			size = 44100*4*5;
		byte[] b = new byte[(int) size];
		int read = 0, l = 0;
		try {
			while((l = in.read(b, read, b.length-read)) > 0) {
				read += l;
				System.out.println(read);
			}
		} catch (IOException e) {
			e.printStackTrace();
			try { in.close(); } catch(Exception e2) {}
			return null;
		}
		
		System.out.println("WAVLOADER: " + read + "/" + size);
		
		RawSoundClip res = new RawSoundClip();
		res.data = b;
		
		res.sampleRate = (int) in.getFormat().getSampleRate();
		return res;
	}
	
	private AudioInputStream loadSoundStream(String path, AssetManager am) {
		InputStream dataIn = am.getAsset(path);
		
		dataIn = new BufferedInputStream(dataIn);
		AudioInputStream in = null;
		try {
			in = AudioSystem.getAudioInputStream(dataIn);
		} catch (Exception e1) {
			e1.printStackTrace();
			try { dataIn.close(); } catch(Exception e2) {}
			System.out.println("OFFLOADER NULL1");
			return null;
		}
		if(in == null) {
			System.out.println("OFFLOADER NULL2");
			return null;
		}
		
		AudioFormat enc = new AudioFormat(in.getFormat().getSampleRate(), 16, 2, true, false);
		in = AudioSystem.getAudioInputStream(enc, in);
		
		return in;
	}
	
	@Override
	public boolean canRead(String path) {
		return path.toUpperCase().endsWith(".WAV") || path.toUpperCase().endsWith(".OGG");
	}
	@Override
	public void destroy() {
	}
	
}
