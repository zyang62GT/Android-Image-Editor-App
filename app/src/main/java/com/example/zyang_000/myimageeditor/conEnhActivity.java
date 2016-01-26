package com.example.zyang_000.myimageeditor;

import android.content.Intent;
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


public class conEnhActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_con_enh);
        selectFromGallery();
        //initialize imageUri
        imageUri=Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "test.jpg"));
        imgShow=(ImageView)findViewById(R.id.imgShow);
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

    public void performContrastEnhancement(View view){
        mPhotoWidth = bitmap.getWidth();
        mPhotoHeight = bitmap.getHeight();

        int[] pix = new int[mPhotoWidth * mPhotoHeight];
        bitmap.getPixels(pix, 0, mPhotoWidth, 0, 0, mPhotoWidth, mPhotoHeight);

        double angle = (3.14159d * (double)60) / 180.0d;
        int S = (int)(256.0d * Math.sin(angle));
        int C = (int)(256.0d * Math.cos(angle));

        int r, g, b, index;
        int RY, BY, RYY, GYY, BYY, R, G, B, Y;
        long RYTotal = 0, BYTotal = 0, YTotal=0, pixCount=0;
        int RYmean=0, BYmean=0, Ymean=0;





        for (int y = 0; y < mPhotoHeight; y++) {
            for (int x = 0; x < mPhotoWidth; x++) {
                index = y * mPhotoWidth + x;
                r = (pix[index] >> 16) & 0xff;
                g = (pix[index] >> 8) & 0xff;
                b = pix[index] & 0xff;
                RY = (70 * r - 59 * g - 11 * b) / 100;
                BY = (-30 * r - 59 * g + 89 * b) / 100;
                Y = (30 * r + 59 * g + 11 * b) / 100;
                //convert from RGB to YCbCr

                RYTotal += RY;
                BYTotal += BY;
                YTotal += Y;
                pixCount += 1;
            }
        }
        RYmean = (int)(RYTotal/pixCount);
        RYmean = (RYmean < 16) ? 16 : ((RYmean > 240) ? 240 : RYmean);
        BYmean = (int)(BYTotal/pixCount);
        BYmean = (BYmean < 16) ? 16 : ((BYmean > 240) ? 240 : BYmean);
        Ymean = (int)(YTotal/pixCount);
        Ymean = (Ymean < 16) ? 16 : ((Ymean > 235) ? 235 : Ymean);


        for (int y = 0; y < mPhotoHeight; y++) {
            for (int x = 0; x < mPhotoWidth; x++) {
                index = y * mPhotoWidth + x;
                r = (pix[index] >> 16) & 0xff;
                g = (pix[index] >> 8) & 0xff;
                b = pix[index] & 0xff;
                RY = (70 * r - 59 * g - 11 * b) / 100;
                BY = (-30 * r - 59 * g + 89 * b) / 100;
                Y = (30 * r + 59 * g + 11 * b) / 100;
                /*if(RY<80){
                    RY = RYmean+(int)((RY-RYmean)*0.5);
                    RY = (RY < 16) ? 16 : ((RY > 240) ? 240 : RY);
                }else if(RY>100){
                    RY = RYmean+(int)((RY-RYmean)*1.5);
                    RY = (RY < 16) ? 16 : ((RY > 240) ? 240 : RY);
                }else{

                }
                if(BY<80){
                    BY = BYmean+(int)((BY-BYmean)*0.5);
                    BY = (BY < 16) ? 16 : ((BY > 240) ? 240 : BY);
                }else if(RY>100){
                    BY = BYmean+(int)((BY-BYmean)*1.5);
                    BY = (RY < 16) ? 16 : ((BY > 240) ? 240 : BY);
                }else{

                }*/
                //add variable gain filter
                if(Y<80){
                    Y = Ymean+(int)((Y-Ymean)*0.3);
                    Y = (Y < 16) ? 16 : ((Y > 235) ? 235 : Y);
                }else {
                    Y = Ymean+(int)((Y-Ymean)*1.8);
                    Y = (Y < 16) ? 16 : ((Y > 235) ? 235 : Y);
                }
                RYY = RY;
                BYY = BY;
                GYY = (-51 * RYY - 19 * BYY) / 100;
                R = Y + RYY;
                R = (R < 0) ? 0 : ((R > 255) ? 255 : R);
                G = Y + GYY;
                G = (G < 0) ? 0 : ((G > 255) ? 255 : G);
                B = Y + BYY;
                B = (B < 0) ? 0 : ((B > 255) ? 255 : B);
                pix[index] = 0xff000000 | (R << 16) | (G << 8) | B;
            }
        }









        Bitmap bm = Bitmap.createBitmap(mPhotoWidth, mPhotoHeight, Bitmap.Config.ARGB_8888);
        bm.setPixels(pix, 0, mPhotoWidth, 0, 0, mPhotoWidth, mPhotoHeight);
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
            Toast.makeText(conEnhActivity.this, filename,
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
            Toast.makeText(conEnhActivity.this, "Saved",
                    Toast.LENGTH_SHORT).show();

            fos.flush();
            fos.close();
        } catch (Exception e) {
            Toast.makeText(conEnhActivity.this, "Save error",
                    Toast.LENGTH_SHORT).show();
            Log.e("MyLog", e.toString());
        }
    }

}

