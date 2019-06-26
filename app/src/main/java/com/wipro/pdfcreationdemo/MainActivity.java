package com.wipro.pdfcreationdemo;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import electrophile.mutils.MiniPermissionUtils;

public class MainActivity extends AppCompatActivity {
    private boolean pdfGenerated = false;
    private String path;
    private String signature_pdf_ = "abc";
    private Bitmap bitmap;
    private String imagesUri;
    private String signature_img_ = "abc";
    private File myPath;
    private int totalHeight;
    private int totalWidth;
    private Uri uri;
    private Button generateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        generateBtn = findViewById(R.id.generate_btn);
        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MiniPermissionUtils permissionUtils = new MiniPermissionUtils(getApplicationContext());
                if (permissionUtils.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    pdfGenerated = true;
                    Toast.makeText(MainActivity.this, "Generating the PDF.....", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            takeScreenShot();
                            Toast.makeText(MainActivity.this, "Generated Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }, 1000);
                } else
                    permissionUtils.requestPermission(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        });

    }


    private void takeScreenShot() {

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.Signature/");

        if (!folder.exists()) {
            boolean success = folder.mkdir();
        }

        path = folder.getAbsolutePath();
        path = path + "/" + signature_pdf_ + System.currentTimeMillis() + ".pdf";// path where pdf will be stored

        LinearLayout parentView = findViewById(R.id.root); // parent view
        totalHeight = parentView.getHeight();// parent view height
        totalWidth = parentView.getWidth();// parent view width

        //Save bitmap to  below path
        String extr = Environment.getExternalStorageDirectory() + "/.Signature/";
        File file = new File(extr);
        if (!file.exists())
            file.mkdir();
        String fileName = signature_img_ + ".jpg";
        myPath = new File(extr, fileName);
        imagesUri = myPath.getPath();
        FileOutputStream fos = null;
        bitmap = getBitmapFromView(parentView, totalHeight, totalWidth);
        try {
            fos = new FileOutputStream(myPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
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
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);


        Bitmap bitmap = Bitmap.createScaledBitmap(this.bitmap, this.bitmap.getWidth(), this.bitmap.getHeight(), true);
        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);
        File filePath = new File(path);
        uri = Uri.fromFile(filePath);
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
        target.setDataAndType(Uri.fromFile(new File(path)), "application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No Application to Open this file", Toast.LENGTH_SHORT).show();
        }
    }

    public void onShareClick(View v) {
        if (pdfGenerated) {
            Resources resources = getResources();
            Intent emailIntent = new Intent();
            emailIntent.setAction(Intent.ACTION_SEND);
            emailIntent.setType("application/pdf");
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            Intent openInChooser = Intent.createChooser(emailIntent, resources.getString(R.string.share_chooser_text));
            startActivity(openInChooser);
        } else Toast.makeText(this, "PDF NOT GENERATED", Toast.LENGTH_SHORT).show();
    }
}
