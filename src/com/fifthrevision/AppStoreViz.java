package com.fifthrevision;

import java.awt.event.KeyEvent;

import processing.core.PApplet;
import processing.core.PImage;
import toxi.color.TColor;

public class AppStoreViz extends PApplet implements IDataLoaderListener {

	private static final long serialVersionUID = 1L;
	public static final String APP_NAME = "App Store Visualization";
	public static final String APP_VERSION = "20110130";
	
	public static final String FILEPATH = "/Users/johncch/Documents/Projects/AppStoreScraperJS2/";
	public static final String DB_NAME = "iTunesAppStore.db";
	public static final String DB_FOLDER = "db/";
	public static final String IMG_FOLDER = "images/";
	public static final String EXT_JPG = ".jpg";
	
	public static int WIDTH = 1024;
	public static int HEIGHT = 768;
	
	public static int ZOOM_THRESHOLD = 32;
	public static int SHIFT_STEPS = 3;
	
	// public static int DRAW_WIDTH = 550;
	public int drawWidth;
	public int drawHeight;
	// public static int OFFSET = 0;
	public AppStoreEntry[][] entriesArr;
	
	public int previewCWidth = 20;
	public int previewCHeight = 20;
		
	public int state = 0;
	public int zoomLevel = 2;
	public int startPointX = 0;
	public int startPointY = 0;
	public int maxStartPointX = 0;
	public int maxStartPointY = 0;
	
	private boolean[] keys = new boolean[526];
	
	/**
	 * Setup and initialization phase of the application
	 */
	@Override
	public void setup() {
		size(WIDTH, HEIGHT, P3D);
		frame.setTitle(APP_NAME + APP_VERSION);
		
		AppStoreDataLoader loader = new AppStoreDataLoader(this);
		Thread t = new Thread(loader);
		t.start();
	}
	
	@Override
	public void draw() {
		background(0);

		if(state == 0) {
			PImage startImg = loadImage("assets/apple.jpg");
			image(startImg, WIDTH / 2 - startImg.width / 2, HEIGHT / 2 - startImg.height / 2 );
		} else {
			int maxHeightLength = (int) ((float) HEIGHT / zoomLevel) + startPointY;
			int maxWidthLength = (int) ((float) WIDTH / zoomLevel) + startPointX;
			
			for(int i = startPointY; i < maxHeightLength; i++) {
				for(int j = startPointX; j < maxWidthLength; j++) {
					if(entriesArr[i][j] == null) break;
					int thisX = (j - startPointX) * zoomLevel;
					int thisY = (i - startPointY) * zoomLevel;
					if(zoomLevel > ZOOM_THRESHOLD) {
						PImage img = loadImage(FILEPATH + IMG_FOLDER + entriesArr[i][j].id + EXT_JPG);
						image(img, thisX, thisY, zoomLevel, zoomLevel);
					} else {
						TColor c = entriesArr[i][j].colorOne;
						fill(c.toARGB());
						stroke(c.toARGB());
						rect(thisX, thisY, zoomLevel, zoomLevel);
					}
				}
			}
			
			int tempX = (mouseX) / 2;
			int tempY = (mouseY) / 2;
			if(tempX > 0 && tempX < drawHeight && tempY > 0 && tempY < drawHeight) {
				if(entriesArr[tempY][tempX] != null) {
					AppStoreEntry e = entriesArr[tempY][tempX]; 
					TColor d = e.colorOne;
					fill(d.toARGB());
					stroke(0);
					rect(mouseX - zoomLevel, mouseY - zoomLevel, zoomLevel * 2, zoomLevel * 2);
					fill(0);
					text("H: " + d.hue(), mouseX - 50, mouseY + 10);
					text("S: " + d.saturation(), mouseX - 50, mouseY + 30);
					text("B: " + d.brightness(), mouseX - 50, mouseY + 50);
					if(zoomLevel < ZOOM_THRESHOLD) {
						PImage img = loadImage(FILEPATH + IMG_FOLDER + entriesArr[tempY][tempX].id + EXT_JPG);
						image(img, mouseX, mouseY);
					}
				}
			}
		}

	}

	public void zoomIn() {
		if(zoomLevel < 256) {
			zoomLevel *= 2;
			calculateMaxStartPoints();
		}
	}
	
	public void zoomOut() {
		if(zoomLevel > 2) {
			zoomLevel /= 2;
			calculateMaxStartPoints();
		}
	}
	
	private void calculateMaxStartPoints() {
		maxStartPointX = drawWidth - (int)((float) WIDTH / zoomLevel) - 1;
		maxStartPointY = drawHeight - (int)((float) HEIGHT / zoomLevel) - 1;
	}
	
	@Override
	public void keyPressed() {
		keys[keyCode] = true;
		System.out.println(keyCode);
		
		if(state > 0) {
			if(checkKey(157) && checkKey(KeyEvent.VK_EQUALS)) {
				zoomIn();
			} else if(checkKey(157) && checkKey(KeyEvent.VK_MINUS)) {
				zoomOut();
			} else if(key == CODED) {
				if(keyCode == UP) {
					if(startPointY > 0) {
						if(checkKey(SHIFT)) {
							if(startPointY >= SHIFT_STEPS) {
								startPointY -= SHIFT_STEPS;
							} else {
								startPointY = 0;
							}
						} else {
							startPointY -= 1;
						}
					}
				} else if (keyCode == DOWN) {
					if(startPointY < maxStartPointY) {
						if(checkKey(SHIFT)) {
							if(startPointY < maxStartPointY - SHIFT_STEPS) {
								startPointY += SHIFT_STEPS;
							} else {
								startPointY = maxStartPointY;
							}
						} else {
							startPointY += 1;
						}
					}					
				} else if (keyCode == LEFT) {
					if(startPointX > 0) {
						if(checkKey(SHIFT)) {
							if(startPointX >= 3) {
								startPointX -= 3;
							} else {
								startPointX = 0;
							}
						} else {
							startPointX -= 1;
						}
					}
				} else if (keyCode == RIGHT) {
					if(startPointX < maxStartPointX) {
						if(checkKey(SHIFT)) {
							if(startPointX < maxStartPointX - SHIFT_STEPS) {
								startPointX += SHIFT_STEPS;
							} else {
								startPointX = maxStartPointX;
							}
						} else {
							startPointX += 1;
						}
					}
				}
			}
		}
	}
			 
	private boolean checkKey(int k) {
	  if (keys.length >= k) {
	    return keys[k];  
	  }
	  return false;
	}
	 
	public void keyReleased() { 
	  keys[keyCode] = false; 
	}
	
	/* 
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
	*/
	
	@Override
	public void setData(AppStoreEntry[][] entries, int width, int height) {
		this.drawWidth = width;
		this.drawHeight = height;
		entriesArr = entries;
		calculateMaxStartPoints();
		state = 1;
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
