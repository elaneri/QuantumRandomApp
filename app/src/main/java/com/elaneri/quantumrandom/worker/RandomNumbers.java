package com.elaneri.quantumrandom.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elaneri.quantumrandom.util.VolleySingleton;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RandomNumbers  extends Worker {
    private Data output;
    private static final String MAX_RND = "MAX_RND";
    private static final String CNT_RND = "CNT_RND";
    private static final String TYPE = "TYPE";
    private static final String TYPE_RND = "TYPE_RND";

    private static final String KEY_RESULT = "KEY_RESULT";
    private  static String url ="https://qrng.anu.edu.au/API/jsonI.php?type=uint16";
    private static final String TAG = RandomNumbers.class.getName();

    public RandomNumbers(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String max =  getInputData().getString(MAX_RND);
        String cnt =  getInputData().getString(CNT_RND);
        String type =  getInputData().getString(TYPE_RND);

        if (max.length()==0)max="1";


        //Setup a RequestFuture object
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
//Pass the future into the JsonObjectRequest

        url = url + "&size="+cnt+"&length="+max+"&type="+type;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, future, future);
//Add the request to the Request Queue
        VolleySingleton.getmInstance(getApplicationContext()).addToRequestQueue(request);
        try {
            JSONObject response = future.get(60, TimeUnit.SECONDS);
            Log.d(TAG, response.toString());

            output = new Data.Builder()
                    .putString(KEY_RESULT, response.toString())
                    .build();

            return Result.success(output);

        } catch (InterruptedException e) {
            e.printStackTrace();
            // exception handling
            return Result.failure();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Result.failure();
            // exception handling
        } catch (TimeoutException e) {
            e.printStackTrace();
            return Result.failure();
        }

    }
}
