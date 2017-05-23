package flyapp.its_on;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by HLiu on 17/08/2014.
 */
public class ImageHandler {

    // constructor
    public ImageHandler() {

    }

    private static final int BUFFER_IO_SIZE = 8000;
    public Bitmap loadImageFromUrl(final String url) {
        try {
            BufferedInputStream bis = new BufferedInputStream(new URL(url).openStream(), BUFFER_IO_SIZE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos, BUFFER_IO_SIZE);
            copy(bis, bos);
            bos.flush();
            return  BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
        } catch (IOException e) {
            // handle it properly
            return null;
        }
    }

    private void copy(final InputStream bis, final OutputStream baos) throws IOException {
        byte[] buf = new byte[256];
        int l;
        while ((l = bis.read(buf)) >= 0) baos.write(buf, 0, l);
    }

    private InputStream inputStream;

    public void UploadImage(Bitmap bitmap, String imageName, final String url)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream); //compress to which format you want.
        byte [] byte_arr = stream.toByteArray();
        String image_str = Base64.encodeBytes(byte_arr);
        final ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("image",image_str));
        nameValuePairs.add(new BasicNameValuePair("image_name",imageName));

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try{
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    String the_string_response = convertResponseToString(response);

                }catch(Exception e){
                    System.out.println("Error in http connection "+e.toString());
                }
            }
        });
        t.start();
    }


    public String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException {

        String res = "";
        StringBuffer buffer = new StringBuffer();
        inputStream = response.getEntity().getContent();
        final int contentLength = (int) response.getEntity().getContentLength(); //getting content length…..

        if (contentLength < 0){
        }
        else{
            byte[] data = new byte[512];
            int len = 0;
            try
            {
                while (-1 != (len = inputStream.read(data)) )
                {
                    buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer…..
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                inputStream.close(); // closing the stream…..
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            res = buffer.toString();     // converting stringbuffer to string…..
        }
        return res;
    }

    public Bitmap decodeImageFromGallery(Intent intent, Context context){
        Bitmap image;
        Uri selectedImageUri = intent.getData();
        if (Build.VERSION.SDK_INT < 19) {
            String selectedImagePath;
            selectedImagePath = getImagePath(selectedImageUri, context);
            return resizeImageToScreen(BitmapFactory.decodeFile(selectedImagePath), context);
        }
        else {
            ParcelFileDescriptor parcelFileDescriptor;
            try {
                parcelFileDescriptor = context.getContentResolver().openFileDescriptor(selectedImageUri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                return resizeImageToScreen(image, context);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
    }

    private String getImagePath(Uri uri, Context context) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    private Bitmap resizeImageToScreen(Bitmap image, Context context){

        WindowManager wm = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        int resizeWidth, resizeHeight;
        resizeWidth=displayMetrics.widthPixels;
        resizeHeight=(int)(((double)resizeWidth/(double)image.getWidth())*(double)image.getHeight());

        return Bitmap.createScaledBitmap(image, resizeWidth, resizeHeight, true);
    }


    public Bitmap grayFilterNonTransparentPixels(Bitmap image){
        final Paint paint = new Paint();
        Canvas c;

        final int overlayColor = Color.RED;

        final Bitmap bm1 = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        c = new Canvas(bm1);
        paint.setColorFilter(new PorterDuffColorFilter(overlayColor, PorterDuff.Mode.OVERLAY));
        c.drawBitmap(image, 0, 0, paint);

        final Bitmap bm2 = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        c = new Canvas(bm2);
        paint.setColorFilter(new PorterDuffColorFilter(overlayColor, PorterDuff.Mode.SRC_ATOP));
        c.drawBitmap(image, 0, 0, paint);

        paint.setColorFilter(null);
        paint.setXfermode(new AvoidXfermode(overlayColor, 0, AvoidXfermode.Mode.TARGET));
        c.drawBitmap(bm1, 0, 0, paint);

        return bm2;
    }


}
