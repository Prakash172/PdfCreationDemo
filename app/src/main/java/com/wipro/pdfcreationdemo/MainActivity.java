package com.wipro.pdfcreationdemo;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Permission;

import electrophile.mutils.MiniPermissionUtils;

public class MainActivity extends AppCompatActivity {

    private String path;
    private String signature_pdf_ = "abc";
    private Bitmap b;
    private String imagesUri;
    private String signature_img_ = "abc";
    private File myPath;
    private int totalHeight;
    private int totalWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        MiniPermissionUtils permissionUtils = new MiniPermissionUtils(this);
        if(permissionUtils.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    takeScreenShot();
                }
            },1000);
        }else permissionUtils.requestPermission(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
    }


    private void takeScreenShot() {

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Signature/");

        if (!folder.exists()) {
            boolean success = folder.mkdir();
        }

        path = folder.getAbsolutePath();
        path = path + "/" + signature_pdf_ + System.currentTimeMillis() + ".pdf";// path where pdf will be stored

        LinearLayout parentView = findViewById(R.id.root); // parent view
        totalHeight = parentView.getHeight();// parent view height
        totalWidth = parentView.getWidth();// parent view width

        //Save bitmap to  below path
        String extr = Environment.getExternalStorageDirectory() + "/Signature/";
        File file = new File(extr);
        if (!file.exists())
            file.mkdir();
        String fileName = signature_img_ + ".jpg";
        myPath = new File(extr, fileName);
        imagesUri = myPath.getPath();
        FileOutputStream fos = null;
        b = getBitmapFromView(parentView, totalHeight, totalWidth);
        try {
            fos = new FileOutputStream(myPath);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        createPdf();// create pdf after creating bitmap and saving

    }

    private Bitmap getBitmapFromView(View u, int totalHeight, int totalWidth) {
        Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        u.draw(canvas);
        return bitmap;
    }

    private void createPdf() {

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(b.getWidth(), b.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);


        Bitmap bitmap = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);
        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);
        File filePath = new File(path);
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();
        openPdf(path);// You can open pdf after complete
    }

    private void openPdf(String path) {
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(new File(path)),"application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Instruct the user to install a PDF reader here, or something
        }
    }
}
