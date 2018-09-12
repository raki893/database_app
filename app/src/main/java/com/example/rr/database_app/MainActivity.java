package com.example.rr.database_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rr.database_app.Dbhelper.Dbhelper;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Please Wait...");

        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(displayImageOptions)
                .build();
        ImageLoader.getInstance().init(configuration);

        listView = findViewById(R.id.listview_list);
        new JsonTask().execute("https://rakiandroid.000webhostapp.com/connection/new.php");

    }

    private class JsonTask extends AsyncTask<String, String, List<Dbhelper>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Dbhelper> doInBackground(String... params) {

            HttpURLConnection connection;
            connection = null;
            BufferedReader reader =null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                String finalJSON = builder.toString();

                JSONObject parent_object = new JSONObject(finalJSON);
                JSONArray parent_array = parent_object.getJSONArray("server_response");

                List<Dbhelper> dbhelperss = new ArrayList<>();
                Gson gson = new Gson();
                for (int i = 0; i < parent_array.length(); i++) {
                    JSONObject finalObject = parent_array.getJSONObject(i);
                    Dbhelper dbhelper1 = gson.fromJson(finalObject.toString(), Dbhelper.class);
                    dbhelperss.add(dbhelper1);
                }
                return dbhelperss;
            }catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            finally {
                if (connection !=null){
                    connection.disconnect();
                }
                try{
                    if (reader !=null){
                        reader.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Dbhelper> dbhelpersresult) {
            super.onPostExecute(dbhelpersresult);
            progressDialog.dismiss();
            Dbhelperadapter dbhelperadapter = new Dbhelperadapter(getApplicationContext(),R.layout.person_information,dbhelpersresult);
            listView.setAdapter(dbhelperadapter);
        }
    }

    private class Dbhelperadapter extends ArrayAdapter {



        private List<Dbhelper> dbhelpers;
        private int resourse;
        private LayoutInflater layoutInflater;

        public Dbhelperadapter(Context context, int resourse, List<Dbhelper> object){

            super(context, resourse, object);
            dbhelpers = object;
            this.resourse = resourse;
            layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }



        class ViewHolder{
            private ImageView person_img;
            private TextView textView_name;
            private TextView textView_address;
            private TextView textView_phone;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(resourse,null);
                viewHolder.person_img = convertView.findViewById(R.id.person_image);
                viewHolder.textView_name = convertView.findViewById(R.id.tv_name);
                viewHolder.textView_address = convertView.findViewById(R.id.tv_address);
                viewHolder.textView_phone = convertView.findViewById(R.id.tv_phone);

                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final ProgressBar progressBar = convertView.findViewById(R.id.image_progressbar);
            ImageLoader.getInstance().displayImage(dbhelpers.get(position).getImages(), viewHolder.person_img, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });

            viewHolder.textView_name.setText(dbhelpers.get(position).getNames());
            viewHolder.textView_address.setText(dbhelpers.get(position).getAddreses());
            viewHolder.textView_phone.setText(dbhelpers.get(position).getPhones());
            return convertView;
        }
    }
}
