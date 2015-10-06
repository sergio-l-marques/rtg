package com.example.rtg_v1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

@SuppressLint("WrongCall")
public class DisplayView extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final int MAX_NUM_CHANNELS=4;

	private SurfaceHolder holder;
    //private DataThread dataThread = null;
    private Paint channelPaint;
    private int numChannels;
    private int pointsPerChannel;
    private Path[] channelPath;
    private float[][] chPoints;
    private int surfaceHeight, surfaceWitdh;
    private float[] channelYoffset;
    private float xFactor, yFactor;
    private boolean[] channelOn;
    
    private boolean previewWindow=true;
    private int numDisplayPerPreviewWindow=4;
    private int xOffset=0;
    
    private Messenger mServiceMessenger;
    
    public DisplayView(Context context, Messenger mServiceMessenger, int pointsPerChannel) {
    	super(context);
          
    	this.mServiceMessenger=mServiceMessenger;
    	
    	//OsciApp osciAppContext=((OsciApp) context);
    	
    	Log.i("RTG", String.format(String.format("DisplayView 1")));
  
    	holder = getHolder();
    	holder.addCallback(this);

    	this.numChannels=0;
    	this.pointsPerChannel=pointsPerChannel;
    	
    	channelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	channelPaint.setColor(Color.WHITE);
    	channelPaint.setStyle(Paint.Style.FILL_AND_STROKE);

    	channelOn=new boolean[DisplayView.MAX_NUM_CHANNELS];

    	channelPath=new Path[DisplayView.MAX_NUM_CHANNELS];
    	chPoints=new float[DisplayView.MAX_NUM_CHANNELS][this.pointsPerChannel];
    	channelYoffset=new float[DisplayView.MAX_NUM_CHANNELS];
    	
    	for (int i=0;i<channelOn.length;i++) {
    		channelOn[i]=false;
    		channelPath[i]=new Path();
    		channelYoffset[i]=0;
    	}
    	Log.i("RTG", String.format("DisplayView 2"));
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	
    	
    	//osciAppContext.dataThread.stopThread();
    	Log.i("RTG", String.format("surfaceDestroyed"));
        // Now that we have the service messenger, lets send our messenger
        Message msg = Message.obtain(null, DataServ.MSG_STOP_SERVICE, 0, 0);
        //msg.replyTo = mClientMessenger;

        /*
         * In case we would want to send extra data, we could use Bundles:
         * Bundle b = new Bundle(); b.putString("key", "hello world");
         * msg.setData(b);
         */

        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    	
    }
	
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	
    	surfaceHeight=holder.getSurfaceFrame().height();
    	surfaceWitdh=holder.getSurfaceFrame().width();
    	xFactor=surfaceWitdh/(float)pointsPerChannel;
    	yFactor=surfaceHeight/(float)256;
    	
    	Log.i("RTG", String.format(String.format("surfaceCreated 1 %d %d %f %f", surfaceHeight, surfaceWitdh, xFactor, yFactor)));
        Message msg = Message.obtain(null, 2, 0, 0);
        msg.obj=this;
        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
	
	 @Override
	 public void surfaceChanged(SurfaceHolder holder, int format,
	               int width, int height) {
		 
		 surfaceHeight=height;//holder.getSurfaceFrame().height();
		 surfaceWitdh=width;//holder.getSurfaceFrame().width();
		 xFactor=surfaceWitdh/(float)pointsPerChannel;
		 yFactor=surfaceHeight/(float)256;
	 }

	 public int getNumPointsPerChannel() {
		 return pointsPerChannel;
	 }

	 public int getNumChannels() {
		 return numChannels;
	 }
	 
	 public boolean getChannelState(int chNum) {
		 return channelOn[chNum];
	 }
	 
	 public void newChannel(int chNum) {
		 for (int i=0;i<this.pointsPerChannel;i++) {
			 chPoints[chNum][i]=0;
		 }
		 channelYoffset[chNum]=256/2;///0/*surfaceHeight/2*/;
		 channelOn[chNum]=true;
		 numChannels++;
	 }

	 public void delChannel(int chNum) {
		 numChannels--;
		 channelOn[chNum]=false;
	 }

	 public void setChannelYoffset(int chNum, byte offset) {
		 channelYoffset[chNum]+=offset;
	 }
	 
	 public void setChannelPersistence(boolean presistFlag) {
	 }

	 public void enablePreviewWindow(boolean enable) {
		 this.previewWindow=enable;
	 }
	 public void setDisplayXOffset(float offset) {
		 
		 if (offset/xFactor>this.pointsPerChannel-(this.pointsPerChannel/numDisplayPerPreviewWindow))
			 this.xOffset=this.pointsPerChannel-(this.pointsPerChannel/numDisplayPerPreviewWindow);
		 else if (offset/xFactor<0/*(this.pointsPerChannel/numDisplayPerPreviewWindow)*/)
			 this.xOffset=0;
		 else 
			 this.xOffset=(int) (offset/xFactor);//(offset*xFactor);
		 
		 
		 Log.i("RTG", String.format(String.format("displaView: setDisplayXOffset %d", this.xOffset)));
		 //this.postInvalidate();
	 }

	 public void setPoints(int chNum, byte[] point) {
		 for (int i=0;i<point.length;i++) {
			 //Log.i("RTG", String.format(String.format("displaView: setPoints %f", (float)point[i])));
			 chPoints[chNum][i]=(float)point[i];
		 }
	 }

	 @Override
	 protected void onDraw(Canvas canvas) {
		 float yFactorAux, xFactorAux, yOffset=0;

		 canvas.drawColor(Color.WHITE);
          
		 channelPaint.setColor(Color.BLUE);
		 channelPaint.setStrokeWidth(1.5f);
		 channelPaint.setStyle(Paint.Style.STROKE);

		 if (this.previewWindow) {
			 
			 for (int i=0;i<channelOn.length;i++) {
				 
				 if (numChannels>0) {
					 canvas.drawRect(this.xOffset*xFactor, 0, this.xOffset*xFactor+(pointsPerChannel/numDisplayPerPreviewWindow)*xFactor, surfaceHeight*1/3, channelPaint);
				 }
				 
				 if (channelOn[i]==true) {

					 if (channelPath[i].isEmpty()==false) channelPath[i].rewind();
		              
					 //Log.i("RTG", String.format(String.format("displaView: chPoints[i][0] %f channelYoffset[i] %f yFactor %f ", chPoints[i][0],(float)channelYoffset[i], (yFactor*1/3))));
					 channelPath[i].moveTo(0, (chPoints[i][0]+(float)chPoints[i][0]+channelYoffset[i])*(yFactor*1/3)+yOffset);
					 for (int j=1;j<pointsPerChannel;j++) {
						 channelPath[i].lineTo(j*xFactor, (chPoints[i][j]+channelYoffset[i])*(yFactor*1/3)+yOffset);
					 }

					 canvas.drawPath(channelPath[i], channelPaint);
				 }
			 }

			 yFactorAux=yFactor*2/3;
			 yOffset=surfaceHeight*1/3;
		 } else {
			 yFactorAux=yFactor;
			 yOffset=0;
		 }

		 xFactorAux=xFactor*numDisplayPerPreviewWindow;
		 
		 for (int i=0;i<channelOn.length;i++) {
 
			 if (channelOn[i]==true) {

				 if (channelPath[i].isEmpty()==false) channelPath[i].rewind();
	              
				 channelPath[i].moveTo(0, (chPoints[i][this.xOffset]+channelYoffset[i])*yFactorAux+yOffset);
				 for (int j=this.xOffset+1;j<this.xOffset+256;j++) {
					 channelPath[i].lineTo((j-this.xOffset)*xFactorAux, (chPoints[i][j]+channelYoffset[i])*yFactorAux+yOffset);
				 }

				 canvas.drawPath(channelPath[i], channelPaint);
			 }
		 }
	 }
}