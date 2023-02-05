package com.example.top100list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FilmDescriptionActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView textView;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_description);

        imageView = findViewById(R.id.imageView3);
        textView = findViewById(R.id.textView6);

        TextView textView1 = findViewById(R.id.textView7);
        textView1.setText(getIntent().getExtras().getString("name"));

        TextView textView2 = findViewById(R.id.textView8);
        textView2.setText("Жанры: " + getIntent().getExtras().getString("genre"));

        DownloadImage downloadImage = new DownloadImage();
        downloadImage.execute(getIntent().getExtras().getString("posterUrl"));

        final String url = "https://kinopoiskapiunofficial.tech/api/v2.2/films/";

        OkHttpHandler okHttpHandlerArr = new OkHttpHandler();
        okHttpHandlerArr.execute(url + getIntent().getExtras().getString("id"));
    }


    private class DownloadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(FilmDescriptionActivity.this);
                mProgressDialog.setTitle("Download poster");
                mProgressDialog.setMessage("Loading...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.show();
            }
        }
        @Override
        protected Bitmap doInBackground(String... URL) {
            String imageURL = URL[0];
            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            imageView.setImageBitmap(result);
            mProgressDialog.dismiss();
        }
    }


    public class OkHttpHandler extends AsyncTask<String, String, String> {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(@NonNull String...params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            builder.addHeader("X-API-KEY", "5d9acf24-f31a-4486-bad4-85dbf224b544");
            Request request = builder.build();

            try {
                try (Response response = client.newCall(request).execute()) {
                    return response.body().string();
                }

            }catch (IOException ioException){
                throw new RuntimeException();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);

                textView.setText(jsonObject.getString("description"));

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }

}