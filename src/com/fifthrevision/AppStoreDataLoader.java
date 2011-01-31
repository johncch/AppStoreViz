package com.fifthrevision;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import toxi.color.TColor;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public class AppStoreDataLoader implements Runnable {

	public static final String FILEPATH = "/Users/johncch/Documents/Projects/AppStoreScraperJS2/";
	public static final String DB_NAME = "iTunesAppStore.db";
	public static final String DB_FOLDER = "db/";
	public static final String IMG_FOLDER = "images/";
	public static final String EXT_JPG = ".jpg";
	
	public static final String CACHE_FILE = "cache.db";
	
	public ArrayList<AppStoreEntry> entries = new ArrayList<AppStoreEntry>();
	private IDataLoaderListener listener;
	
	public AppStoreDataLoader(IDataLoaderListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void run() {
		int drawHeight = 0;
		int drawWidth = 0;
		boolean cached = false;
		AppStoreEntry[][] entriesArr = null;
		
		SQLiteConnection cacheDb = new SQLiteConnection(new File(CACHE_FILE));
		SQLiteStatement cacheSt = null;
		try {
			cacheDb.open(true);
			cacheSt = cacheDb.prepare("CREATE TABLE IF NOT EXISTS entries(id integer primary key, url text, name text, price real, genre text, category text, released integer, version text, size real, seller text, language text, currRating integer, allRating integer, numCurrRating integer, numAllRating integer, appRating integer, colorOne text, colorOneF real, colorTwo text, colorTwoF real, colorThree text, colorThreeF real, x integer, y integer)");
			cacheSt.step();
			cacheSt.dispose();
			cacheSt = cacheDb.prepare("CREATE TABLE IF NOT EXISTS main(width int, height int)");
			cacheSt.step();
			cacheSt.dispose();
			cacheSt = cacheDb.prepare("SELECT * from main");
			if(cacheSt.step()) {
				cached = true;
				drawWidth = cacheSt.columnInt(0);
				drawHeight = cacheSt.columnInt(1);
			}
		} catch (SQLiteException e1) {
			e1.printStackTrace();
		} finally {
			cacheSt.dispose();
		}
		
		if(cached) {
			System.out.println("using cached data");
			entriesArr = new AppStoreEntry[drawHeight][drawWidth];
			try {
				cacheSt = cacheDb.prepare("SELECT * FROM entries");
				while(cacheSt.step()) {
					AppStoreEntry entry = new AppStoreEntry();
					entry.id = cacheSt.columnLong(0);
					entry.url = cacheSt.columnString(1);
					entry.name = cacheSt.columnString(2);
					entry.price = (float) cacheSt.columnDouble(3);
					entry.genre = cacheSt.columnString(4);
					entry.category = cacheSt.columnString(5);
					entry.released  = cacheSt.columnLong(6);
					entry.version = cacheSt.columnString(7);
					entry.size = (float) cacheSt.columnDouble(8);
					entry.seller = cacheSt.columnString(9);
					entry.language = cacheSt.columnString(10);
					entry.currRating = cacheSt.columnInt(11);
					entry.allRating = cacheSt.columnInt(12);
					entry.numCurrRating = cacheSt.columnInt(13);
					entry.numAllRating = cacheSt.columnInt(14);
					entry.appRating = cacheSt.columnInt(15);
					entry.colorOne = TColor.newHex(cacheSt.columnString(16));
					entry.colorOneF = (float) cacheSt.columnDouble(17);
					int x = cacheSt.columnInt(22);
					int y = cacheSt.columnInt(23);
					entriesArr[y][x] = entry;
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cacheSt.dispose();
				cacheDb.dispose();
			}
			
			listener.setData(entriesArr, drawWidth, drawHeight);
			return;
		}
		
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
		
		drawHeight = drawWidth = (int) Math.sqrt(entries.size()) + 1;
		try {
			cacheSt = cacheDb.prepare("INSERT INTO main (width, height) VALUES (?, ?)");
			cacheSt.bind(1, drawWidth);
			cacheSt.bind(2, drawHeight);
			cacheSt.step();
		} catch (SQLiteException e1) {
			e1.printStackTrace();
		} finally {
			cacheSt.dispose();
		}
		System.out.println(drawHeight);
		
		Comparator<AppStoreEntry> hueComparator = new Comparator<AppStoreEntry>() {
			@Override
			public int compare(AppStoreEntry o1, AppStoreEntry o2) {
				AppStoreEntry entry1 = (AppStoreEntry) o1;
				AppStoreEntry entry2 = (AppStoreEntry) o2;
				float val1 = entry1.colorOne.hue();
				float val2 = entry2.colorOne.hue();
				if (val1 == val2)
				  return 0;
				if (val1 < val2)
				  return 1;
				return -1;
			}
		};
		
		Comparator<AppStoreEntry> brightnessComparator = new Comparator<AppStoreEntry>() {
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
		};
		
		ArrayList<AppStoreEntry> brightEntries = new ArrayList<AppStoreEntry>();
		ArrayList<AppStoreEntry> darkEntries = new ArrayList<AppStoreEntry>();
		ArrayList<AppStoreEntry> normalEntries = new ArrayList<AppStoreEntry>();
		// ArrayList<AppStoreEntry> newEntries = new ArrayList<AppStoreEntry>();
		
		while(entries.size() > 0) {
			AppStoreEntry e = entries.remove(0);
			TColor c = e.colorOne;
			if(c.saturation() < 0.15f && c.brightness() > 0.85f) {
				brightEntries.add(e);
			} else if(c.brightness() < 0.2f) {
				darkEntries.add(e);
			} else {
				normalEntries.add(e);
			}
		}
		
		Collections.sort(brightEntries, hueComparator);
		Collections.sort(darkEntries, hueComparator);
		Collections.sort(normalEntries, hueComparator);		
		
		int brightCount = brightEntries.size() / drawHeight;
		int darkCount = darkEntries.size() / drawHeight;
		// int normalCount = normalEntries.size() / drawHeight;
		
		entriesArr = new AppStoreEntry[drawHeight][drawHeight];
	
		int brightIndex = 0;
		int darkIndex = 0;
		int normalIndex = 0;
		
		for(int i = 0; i < drawHeight; i++) {
			int brightRandom = (int) (Math.random() * 100) - 50;
			int darkRandom = (int) (Math.random() * 20) - 10;
			
			ArrayList<AppStoreEntry> bTemp = new ArrayList<AppStoreEntry>();
			ArrayList<AppStoreEntry> dTemp = new ArrayList<AppStoreEntry>();
			ArrayList<AppStoreEntry> nTemp = new ArrayList<AppStoreEntry>();
			
			for(int j = 0; j < drawWidth; j++) {
				if(j < brightCount + brightRandom) {
					if(brightIndex < brightEntries.size()) {
						// entriesArr[i][j] = brightEntries.get(brightIndex);
						bTemp.add(brightEntries.get(brightIndex));
						brightIndex++;
					}
				} else if (j > drawWidth - darkCount - darkRandom) {
					if(darkIndex < darkEntries.size()) {
						// entriesArr[i][j] = darkEntries.get(darkIndex);
						dTemp.add(darkEntries.get(darkIndex));
						darkIndex++;
					}
				} else {
					if(normalIndex < normalEntries.size()) {
						// entriesArr[i][j] = normalEntries.get(normalIndex);
						nTemp.add(normalEntries.get(normalIndex));
						normalIndex++;
					}
				}
			}
			
			Collections.sort(bTemp, brightnessComparator);
			Collections.sort(dTemp, brightnessComparator);
			Collections.sort(nTemp, brightnessComparator);
			
			for(int j = 0; j < drawWidth; j++) {
				if(j < brightCount + brightRandom) {
					if(bTemp.size() > 0) {
						entriesArr[i][j] = bTemp.remove(0);
					}
				} else if (j > drawWidth - darkCount - darkRandom) {
					if(dTemp.size() > 0) {
						entriesArr[i][j] = dTemp.remove(0);
					}
				} else {
					if(nTemp.size() > 0) {
						entriesArr[i][j] = nTemp.remove(0);
					}
				}
				if(entriesArr[i][j] != null) {
					AppStoreEntry entry = entriesArr[i][j];
					try {
						cacheSt = cacheDb.prepare("INSERT INTO entries (id, url, name, price, genre, category, released, version, size, seller, language, currRating, allRating, numCurrRating, numAllRating, appRating, colorOne, colorOneF, colorTwo, colorTwoF, colorThree, colorThreeF, x, y) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
						cacheSt.bind(1, entry.id);
						cacheSt.bind(2, entry.url);
						cacheSt.bind(3, entry.name);
						cacheSt.bind(4, entry.price);
						cacheSt.bind(5, entry.genre);
						cacheSt.bind(6, entry.category);
						cacheSt.bind(7, entry.released);
						cacheSt.bind(8, entry.version);
						cacheSt.bind(9, entry.size);
						cacheSt.bind(10, entry.seller);
						cacheSt.bind(11, entry.language);
						cacheSt.bind(12, entry.currRating);
						cacheSt.bind(13, entry.allRating);
						cacheSt.bind(14, entry.numCurrRating);
						cacheSt.bind(15, entry.numAllRating);
						cacheSt.bind(16, entry.appRating);
						cacheSt.bind(17, entry.colorOne.toHex());
						cacheSt.bind(18, entry.colorOneF);
						cacheSt.bind(23, j);
						cacheSt.bind(24, i);
						cacheSt.step();
					} catch (SQLiteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						cacheSt.dispose();
					}
				}
			}
		}
		
		listener.setData(entriesArr, drawWidth, drawHeight);
		cacheDb.dispose();
	}
	
}
