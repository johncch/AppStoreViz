package com.fifthrevision;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import processing.core.PApplet;
import processing.core.PImage;
import toxi.color.TColor;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public class AppStoreViz extends PApplet {

	public static final String APP_NAME = "App Store Visualization";
	public static final String APP_VERSION = "20110130";
	
	public static final String FILEPATH = "/Users/johncch/Documents/Projects/AppStoreScraperJS2/";
	public static final String DB_NAME = "iTunesAppStore.db";
	public static final String DB_FOLDER = "db/";
	public static final String IMG_FOLDER = "images/";
	public static final String EXT_JPG = ".jpg";
	
	public static int WIDTH = 1096;
	public static int HEIGHT = 1096;
	
	// public static int DRAW_WIDTH = 550;
	public int drawWidth;
	public int drawHeight;
	public static int OFFSET = 0;
	public AppStoreEntry[][] entriesArr;
	
	public int previewCWidth = 20;
	public int previewCHeight = 20;
	
	public ArrayList<AppStoreEntry> entries = new ArrayList<AppStoreEntry>();
	
	/**
	 * Setup and initialization phase of the application
	 */
	@Override
	public void setup() {
		size(WIDTH, HEIGHT, P3D);
		frame.setTitle(APP_NAME + APP_VERSION);
		loadData();
	}
	
	@Override
	public void draw() {
		background(0);
		// int drawHeight = entries.size() / DRAW_WIDTH;
		
		for(int i = 0; i < drawHeight; i++) {
			for(int j = 0; j < drawWidth; j++) {
				// int index = i *  + j;
				if(entriesArr[i][j] == null) break;
				TColor c = entriesArr[i][j].colorOne;
				fill(c.toARGB());
				stroke(c.toARGB());
				rect(OFFSET + j * 2, OFFSET + i * 2, 2, 2);
			}
		}
		
		int tempX = (mouseX - OFFSET) / 2;
		int tempY = (mouseY - OFFSET) / 2;
		if(tempX > 0 && tempX < drawHeight && tempY > 0 && tempY < drawHeight) {
			if(entriesArr[tempY][tempX] != null) {
				AppStoreEntry e = entriesArr[tempY][tempX]; 
				TColor d = e.colorOne;
				fill(d.toARGB());
				stroke(0);
				rect(mouseX - previewCWidth / 2, mouseY - previewCHeight / 2, previewCWidth, previewCHeight);
				PImage img = loadImage(FILEPATH + IMG_FOLDER + entriesArr[tempY][tempX].id + EXT_JPG);
				image(img, mouseX, mouseY);
				fill(0);
				text("H: " + d.hue(), mouseX - 50, mouseY + 10);
				text("S: " + d.saturation(), mouseX - 50, mouseY + 30);
				text("B: " + d.brightness(), mouseX - 50, mouseY + 50);
			}
		}

	}
	
	private int partition(Object[] sortedEntries, int left, int right) { 
		int i = left, j = right; 
		AppStoreEntry tmp; 
		AppStoreEntry pivot = (AppStoreEntry) sortedEntries[(left + right) / 2]; 
		while (i <= j) { 
			while (((AppStoreEntry) sortedEntries[i]).colorOne.toARGB() < pivot.colorOne.toARGB()) 
				i++; 
			while (((AppStoreEntry) sortedEntries[j]).colorOne.toARGB() > pivot.colorOne.toARGB()) 
				j--; 
			if (i <= j) { 
				tmp = (AppStoreEntry) sortedEntries[i]; 
				sortedEntries[i] = sortedEntries[j]; 
				sortedEntries[j] = tmp; 
				i++; 
				j--; 
			} 
		}; 
		return i; 
	} 

	private void quickSort(Object[] sortedEntries, int left, int right) { 
		int index = partition(sortedEntries, left, right); 
		if (left < index - 1) 
			quickSort(sortedEntries, left, index - 1); 
		if (index < right) 
			quickSort(sortedEntries, index, right); 
	}
	
	public void loadData() {
		SQLiteConnection db = new SQLiteConnection(new File(FILEPATH + DB_FOLDER + DB_NAME));
		SQLiteStatement st = null;
		
		try {
			db.openReadonly();
			st = db.prepare("SELECT * FROM entries");
			while(st.step()) {
				AppStoreEntry entry = new AppStoreEntry();
				entry.id = st.columnLong(0);
				entry.url = st.columnString(1);
				entry.name = st.columnString(2);
				entry.price = (float) st.columnDouble(3);
				entry.genre = st.columnString(4);
				entry.category = st.columnString(5);
				entry.released  = st.columnLong(6);
				entry.version = st.columnString(7);
				entry.size = (float) st.columnDouble(8);
				entry.seller = st.columnString(9);
				entry.language = st.columnString(10);
				entry.currRating = st.columnInt(11);
				entry.allRating = st.columnInt(12);
				entry.numCurrRating = st.columnInt(13);
				entry.numAllRating = st.columnInt(14);
				entry.appRating = st.columnInt(15);
				entry.colorOne = TColor.newHex(st.columnString(16));
				entry.colorOneF = (float) st.columnDouble(17);
				entries.add(entry);
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			st.dispose();
			db.dispose();
		}
		
		// Object[] sortedEntries = entries.toArray(); 
		// quickSort(sortedEntries, 0, entries.size() - 1);
		Collections.sort(entries, new Comparator<AppStoreEntry>() {
			@Override
			public int compare(AppStoreEntry o1, AppStoreEntry o2) {
				AppStoreEntry entry1 = (AppStoreEntry) o1;
				AppStoreEntry entry2 = (AppStoreEntry) o2;
				// int val1 = (Integer) entry1.colorOne.toARGB();
				// int val2 = (Integer) entry2.colorOne.toARGB();
				// long val1 = (int)(entry1.colorOne.red() * 255) << 16 | (int)(entry1.colorOne.green() * 255) << 8 | (int)(entry1.colorOne.blue() * 255);
				// long val2 = (int)(entry2.colorOne.red() * 255) << 16 | (int)(entry2.colorOne.green() * 255) << 8 | (int)(entry2.colorOne.blue() * 255);
				// System.out.println(val1 + ", " + val2);
				float val1 = entry1.colorOne.hue();
				float val2 = entry2.colorOne.hue();
				if (val1 == val2)
				  return 0;
				if (val1 < val2)
				  return 1;
				return -1;
			}
		});
		
		// entriesArr = entries.toArray();
		drawHeight = drawWidth = (int) sqrt(entries.size()) + 1;
		System.out.println(drawHeight);
		
		ArrayList<AppStoreEntry> newEntries = new ArrayList<AppStoreEntry>();
		
		int count = 0;
		while(entries.size() > 0) {
			ArrayList<AppStoreEntry> sortEntries = new ArrayList<AppStoreEntry>();
			for(int i = 0; i < drawHeight && entries.size() > 0; i++) {
				sortEntries.add(entries.remove(0));
			}
			Collections.sort(sortEntries, new Comparator<AppStoreEntry>() {
				@Override
				public int compare(AppStoreEntry o1, AppStoreEntry o2) {
					AppStoreEntry entry1 = (AppStoreEntry) o1;
					AppStoreEntry entry2 = (AppStoreEntry) o2;
					float val1 = entry1.colorOne.brightness();
					float val2 = entry2.colorOne.brightness();
					if (val1 == val2)
					  return 0;
					if (val1 < val2)
					  return 1;
					return -1;
				}
				
			});
			newEntries.addAll(sortEntries);
			// System.out.println("test " + ++count);
		}
		
		entriesArr = new AppStoreEntry[drawHeight][drawHeight];
		for(int i = 0; i < drawHeight; i++) {
			// entriesArr[i] = new AppStoreEntry[drawHeight];
			for(int j = 0; j < drawWidth; j++) {
				int index = i * drawWidth + j;
				if(index >= newEntries.size()) break;
				entriesArr[i][j] = (AppStoreEntry) newEntries.get(index);
				// if(index >= sortedEntries.length) break;
				// entriesArr[i][j] = (AppStoreEntry) sortedEntries[index];
			}
		}
	}
	
	/**
	 * Main entry point of the application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		PApplet.main(new String[] { "com.fifthrevision.AppStoreViz"} );
	}

}
