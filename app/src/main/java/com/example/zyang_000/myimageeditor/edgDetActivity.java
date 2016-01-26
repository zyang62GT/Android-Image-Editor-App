package com.example.zyang_000.myimageeditor;

import android.content.Context;import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;



public class edgDetActivity extends AppCompatActivity {
    private final static int SELECT_ORIGINAL_PIC=126;
    private Uri imageUri;
    private ImageView imgShow;
    private ImageView imgShow2;
    private Bitmap bitmap;
    private int mPhotoWidth = 0;
    private int mPhotoHeight = 0;
    private final static int CROP_PIC=125;
    private final static int SELECT_PIC=123;
    String picturePath ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edg_det);
        selectFromGallery();
        //initialize imageUri
        imageUri=Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "test.jpg"));
        imgShow=(ImageView)findViewById(R.id.imgShow);
        imgShow2=(ImageView)findViewById(R.id.imgShow2);
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11,
                edgDetActivity.this, mOpenCVCallBack)) {
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
                        imgShow2.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
            case SELECT_PIC:
                if (resultCode==RESULT_OK) {//select oroginal photo from album and crop
                    try {
                        bitmap=BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        imgShow.setImageBitmap(bitmap);
                        imgShow2.setImageBitmap(bitmap);
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

    public void performEdgeDetection(View view){

        Mat mRgba=new Mat(), mIntermediateMat=new Mat();
        Mat mGray=new Mat(), detected_edges=new Mat();
        mPhotoWidth = bitmap.getWidth();
        mPhotoHeight = bitmap.getHeight();

        int edgeThresh = 1;
        int lowThreshold;
        int max_lowThreshold = 100;
        int ratio = 3;
        int kernel_size = 3;
        Bitmap bm = Bitmap.createBitmap(mPhotoWidth, mPhotoHeight, Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(bitmap, mRgba);
        Utils.bitmapToMat(bm, mGray);


        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 2, 2);
        //add Gaussian blur to reduce noise

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
        Imgproc.dilate(mGray, mGray, kernel);


        Imgproc.Canny(mGray, mIntermediateMat, 15, 45);

        Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
        Utils.matToBitmap(mRgba, bitmap);
        imgShow.setImageBitmap(bitmap);
        imgShow.invalidate();
        imgShow2.setImageBitmap(bitmap);
        imgShow2.invalidate();






    }
    public void SaveThePicture(View view) {
        // Make sure the folder is available on SD card

        int pos = picturePath.lastIndexOf("/");
        int pos2 = picturePath.lastIndexOf(".");
        String filename ="test.jpeg";
        if(pos>0){
            filename = picturePath.substring(pos+1,pos2)+".jpeg";
            Toast.makeText(edgDetActivity.this, filename,
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
            Toast.makeText(edgDetActivity.this, "Saved",
                    Toast.LENGTH_SHORT).show();

            fos.flush();
            fos.close();
        } catch (Exception e) {
            Toast.makeText(edgDetActivity.this, "Save error",
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

