package org.oucho.radio2.itf;


import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Random;

public interface PlayableItem {
	public String getTitle();
	public String getArtist(); // Return null if no artist available
	public String getPlayableUri();
	public PlayableItem getNext(boolean repeatAll); // Return null if no next item
	public PlayableItem getPrevious(); // Return null if no previous item
	public PlayableItem getRandom(Random random); // Return null if no random item
	public boolean isLengthAvailable();
	public boolean hasImage();
	public Bitmap getImage(); // Return null if no image available
	public ArrayList<Information> getInformation();
}
