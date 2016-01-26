package com.example.zyang_000.myimageeditor;

import android.content.Context;import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.io.File;
import java.io.FileNotFoundException;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;


public class disRemActivity extends AppCompatActivity {
    private final static int SELECT_ORIGINAL_PIC=126;
    private Uri imageUri;
    private ImageView imgShow;
    private Bitmap bitmap;
    private int mPhotoWidth = 0;
    private int mPhotoHeight = 0;
    String picturePath ="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dis_rem);
        selectFromGallery();
        //initialize imageUri
        imageUri=Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "test.jpg"));
        imgShow=(ImageView)findViewById(R.id.imgShow);

        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11,
                disRemActivity.this, mOpenCVCallBack)) {
            Log.e("TEST", "Cannot connect to OpenCV Manager");
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case SELECT_ORIGINAL_PIC:
                if (resultCode==RESULT_OK) {//select oroginal photo from album
                    try {
                        Uri selectedImage = data.getData(); //get the uri returned by system
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);//look for the certain photo in system
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        picturePath = cursor.getString(columnIndex);  //get photo path
                        cursor.close();
                        bitmap= BitmapFactory.decodeFile(picturePath);
                        imgShow.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void selectFromGallery() {
        // TODO Auto-generated method stub
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_PICK);//Pick an item from the data
        intent.setType("image/*");//select from all pictures
        startActivityForResult(intent, SELECT_ORIGINAL_PIC);
    }

    public static int dp2px(Context context, int dp)
    {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void performDistortionRemoval(View view){
        mPhotoWidth = bitmap.getWidth();
        mPhotoHeight = bitmap.getHeight();
        //bitmap2 = bitmap;

        int[] pix = new int[mPhotoWidth * mPhotoHeight];
        int[] new_pix = new int[mPhotoWidth * mPhotoHeight];
        bitmap.getPixels(pix, 0, mPhotoWidth, 0, 0, mPhotoWidth, mPhotoHeight);
        //System.arraycopy(pix, 0, new_pix, 0, mPhotoWidth * mPhotoHeight );




        int  g, b, index;
        int RY, BY, RYY, GYY, BYY, R, G, B, Y;
        long RYTotal = 0, BYTotal = 0, YTotal=0, pixCount=0;
        int RYmean=0, BYmean=0, Ymean=0;

        double k_1=-0.0000001, k_2 =0.0, p_1=0.0, p_2=0.0, r=0.0;
        int new_x = 0,  new_y = 0;





        for (int n = 0; n < mPhotoHeight; n++) {
            for (int m = 0; m < mPhotoWidth; m++) {
                int x=0, new_m = 0;
                int y=0, new_n = 0;
                x = m - (int)Math.floor((double)(mPhotoWidth/2));
                y = -n + (int)Math.floor((double)(mPhotoHeight/2));
                //convert from index to cartesian
                index = n * mPhotoWidth + m;
                int new_index = index;
                r=Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
                //new_x = (int)(x*(1+k_1*Math.pow(r, 2)+k_2*Math.pow(r, 4))+2*p_1*x*y+p_2*(Math.pow(r, 2)+2*Math.pow(x, 2)));
                //new_y = (int)(y*(1+k_1*Math.pow(r, 2)+k_2*Math.pow(r, 4))+2*p_2*x*y+p_1*(Math.pow(r, 2)+2*Math.pow(y, 2)));
                new_x = (int)(((double)x)/(1.0+k_1*Math.pow(r, 2)));
                new_y = (int)(((double)y)/(1.0+k_1*Math.pow(r, 2)));
                //perform distortion removal
                new_m=(int)Math.floor((double)(mPhotoWidth/2))+new_x;
                new_n=(int)Math.floor((double)(mPhotoHeight/2))-new_y;
                if(new_m>=mPhotoWidth){
                    new_m=mPhotoWidth-1;
                }
                if(new_n>=mPhotoHeight){
                    new_n=mPhotoHeight-1;
                }
                if (new_m < 0) {
                    new_m = 0;
                }
                if(new_n<0){
                    new_n=0;
                }
                new_index = new_n * mPhotoWidth + new_m;
                if(new_index>=mPhotoHeight*mPhotoWidth){
                    new_index = mPhotoHeight*mPhotoWidth-1;
                }else if(new_index<0){
                    new_index = 0;
                }
                new_pix[new_index] = pix[index];
            }
        }
        for(int m =1; m<mPhotoWidth-1;m++){
            for(int n=1; n<mPhotoHeight-1;n++){
                index = n*mPhotoWidth+m;
                //perform basic denoising to remove white lines
                int temp_index=0;
                if(new_pix[index]==0){
                    temp_index = (n-1)*mPhotoWidth+m-1;
                    if(new_pix[temp_index]!=0){
                        new_pix[index]=new_pix[temp_index];
                        continue;
                    }
                    temp_index = (n-1)*mPhotoWidth+m;
                    if(new_pix[temp_index]!=0){
                        new_pix[index]=new_pix[temp_index];
                        continue;
                    }
                    temp_index = (n-1)*mPhotoWidth+m+1;
                    if(new_pix[temp_index]!=0){
                        new_pix[index]=new_pix[temp_index];
                        continue;
                    }
                    temp_index = (n)*mPhotoWidth+m-1;
                    if(new_pix[temp_index]!=0){
                        new_pix[index]=new_pix[temp_index];
                        continue;
                    }
                    temp_index = (n)*mPhotoWidth+m+1;
                    if(new_pix[temp_index]!=0){
                        new_pix[index]=new_pix[temp_index];
                        continue;
                    }
                    temp_index = (n+1)*mPhotoWidth+m-1;
                    if(new_pix[temp_index]!=0){
                        new_pix[index]=new_pix[temp_index];
                        continue;
                    }
                    temp_index = (n+1)*mPhotoWidth+m;
                    if(new_pix[temp_index]!=0){
                        new_pix[index]=new_pix[temp_index];
                        continue;
                    }
                    temp_index = (n+1)*mPhotoWidth+m+1;
                    if(new_pix[temp_index]!=0){
                        new_pix[index]=new_pix[temp_index];
                        continue;
                    }

                }
            }
        }

        Bitmap bm = Bitmap.createBitmap(mPhotoWidth, mPhotoHeight, Bitmap.Config.ARGB_8888);
        bm.setPixels(new_pix, 0, mPhotoWidth, 0, 0, mPhotoWidth, mPhotoHeight);

        if (null != bitmap) {
            bitmap.recycle();
        }

        bitmap = bm;

        // Put the updated bitmap into the main view
        imgShow.setImageBitmap(bitmap);
        imgShow.invalidate();



        pix = null;

    }




    public void SaveThePicture(View view) {
        // Make sure the folder is available on SD card
        int pos = picturePath.lastIndexOf("/");
        int pos2 = picturePath.lastIndexOf(".");
        String filename ="test.jpeg";
        if(pos>0){
            filename = picturePath.substring(pos+1,pos2)+".jpeg";
            Toast.makeText(disRemActivity.this, filename,
                    Toast.LENGTH_SHORT).show();
        }

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/GoProEditor";
        File sddir = new File(path);
        File newFile = new File(path+"/"+filename);


        if (!sddir.exists()) {
            sddir.mkdirs();
        }



        try {
            FileOutputStream fos = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
            Toast.makeText(disRemActivity.this, newFile.toString(),
                    Toast.LENGTH_SHORT).show();

            fos.flush();
            fos.close();
        } catch (Exception e) {
            Toast.makeText(disRemActivity.this, "Save error",
                    Toast.LENGTH_SHORT).show();
            Log.e("MyLog", e.toString());
        }
    }




    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    //your code


                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };






}

