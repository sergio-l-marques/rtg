package com.example.rtg_v1;

import android.app.Application;

public class OsciApp extends Application {

	public int numPointsPerChan=1024;
	
	public int sourceType;
	public final int SOURCE_TYPE_GEN = 1;
	public final int SOURCE_TYPE_NET = 2;
	public final int SOURCE_TYPE_USB = 3;
	
	
    public DataThread dataThread=null;
    public DisplayView displayView=null;

	
	public void onStart(MainActivity mainActivity/*,OsciSurfaceView surfaceView*/) {
	
	}

}
