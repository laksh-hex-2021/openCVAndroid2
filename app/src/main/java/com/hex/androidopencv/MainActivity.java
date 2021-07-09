package com.hex.androidopencv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;

import android.os.Bundle;
import android.view.WindowManager;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    JavaCameraView javaCameraView;
    File cascFile;
    CascadeClassifier faceDetector;
    private Mat mRgba, mGrey;
    //private final Matrix mMatrix = new Matrix();
    private CameraBridgeViewBase mOpenCvCameraView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
       getSupportActionBar().hide();

        javaCameraView = (JavaCameraView)findViewById(R.id.javaCamView);
        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0,this,baseCallback);

        }
        else{

            try {
                baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.setCameraIndex(1);
        //javaCameraView.enableFpsMeter();
    }



    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba=new Mat();
        mGrey=new Mat();

    }

    @Override
    public void onCameraViewStopped() {
        mGrey.release();
        mRgba.release();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba=inputFrame.rgba();
        mGrey=inputFrame.gray();
        int height = mGrey.rows();
        int faceSize = Math.round(height * 0.5F);
        Mat temp = mGrey.clone();
        Core.transpose(mGrey, temp);
        Core.flip(temp, temp, -1);
        MatOfRect rectFaces = new MatOfRect();

        // java detector fast


//        MatOfRect faceDetections=new MatOfRect();
        faceDetector.detectMultiScale(mRgba,rectFaces);
//
        for(Rect rect:rectFaces.toArray()){
            Imgproc.rectangle(mRgba,new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(255,0,0));
            int ch = rect.height/2;
            int cw = rect.width/2;
            Imgproc.line(mRgba,new Point(rect.x+cw,rect.y+ch),new Point(rect.x+cw+2,rect.y+ch+2),new Scalar(255,0,0),3);
        }
        return mRgba;
    }

    private BaseLoaderCallback baseCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    InputStream is=getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    File cascadeDir=getDir("cascade", Context.MODE_PRIVATE);
                    cascFile=new File(cascadeDir,"haarcascade_frontalface_alt2.xml");



                    FileOutputStream fos=new FileOutputStream(cascFile);


                    byte[] buffer=new byte[4096];
                    int bytesRead;

                    while ((bytesRead=is.read(buffer))!=-1){
                        fos.write(buffer,0,bytesRead);
                    }
                    is.close();
                    fos.close();

                    faceDetector=new CascadeClassifier(cascFile.getAbsolutePath());

                    if(faceDetector.empty()){
                        faceDetector=null;
                    }
                    else{
                        cascadeDir.delete();
                    }
                    javaCameraView.enableView();
                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }

        }
    };

}