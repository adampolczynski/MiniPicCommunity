package hitech.example.admin.volleyrequests;

import android.content.Context;
import android.util.Log;


import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 2016-12-10.
 */

public class Utils { // wymaga uporzadkowania // class needs sorting

    private final static String host = "http://zupelnieniepotrzebnie.comxa.com/";
    private final static String TAG = "Utils";

    interface VolleyCallback {
        public void onSuccess(String response);
        public void onError();
    }
    interface JSONCallback {
        public void onSuccess(JSONObject response);
    }
    public static void request_JSONgetComments (int id, Context context, final JSONCallback callback) {
        String url = host + "get_comments.php?id="+Integer.toString(id);
        JsonObjectRequest JsonRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("not-typical-header0", "value0123");
                return params;
            }
        };
        JsonRequest.setRetryPolicy(
                new DefaultRetryPolicy(8000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Singleton.getInstance(context).addToRequestQueue(JsonRequest);
    }
    public static void request_JSONgetBigImg (int id, Context context, final JSONCallback callback) {
        String url = host + "get_main_img.php?id="+Integer.toString(id)+"&user="+ImgDataHolder.name;
        JsonObjectRequest JsonRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("not-typical-header0", "value0123");
                return params;
            }
        };
        JsonRequest.setRetryPolicy(
                new DefaultRetryPolicy(8000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Singleton.getInstance(context).addToRequestQueue(JsonRequest);
    }
    public static void request_commentImg (int id, String comment, String name, Context mContext, final VolleyCallback callback) {

        String url = host+"comment_img.php?id="+Integer.toString(id)+"&comment="+comment+"&name="+name;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        callback.onSuccess(response.substring(0, response.length()-152));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("not-typical-header0", "value0123");
                return params;
            }
        };
        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Singleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }
    public static void request_rateImg (int id, int rate, String comment, String name, Context mContext, final VolleyCallback callback) {

        String url = host+"rate_img.php?id="+Integer.toString(id)+"&rate="+Integer.toString(rate)+"&comment="+comment+"&name="+name;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        callback.onSuccess(response.substring(0, response.length()-152));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("not-typical-header0", "value0123");
                return params;
            }
        };
        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Singleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }
    public static void request_getBinImgs (Context mContext, final VolleyCallback callback) {

        String url = host+"get_bin_data.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response.substring(0, response.length()-152));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("not-typical-header0", "value0123");
                return params;
            }
        };
        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Singleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }
    protected static void request_sendBmps (final Context context, final MultipartEntityBuilder entityB, final VolleyCallback callback) {

        String url = host+"store_bin_data.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess(response.substring(0, response.length()-152));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("onErrorResponse: "+error);
                callback.onError();
            }
        }) {

            @Override
            public byte[] getBody() throws AuthFailureError {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    entityB.build().writeTo(bos);

                } catch (IOException e) {
                    System.out.println("Exception" + e);
                }
                return bos.toByteArray();
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("not-typical-header0", "value0123");
                return params;
            }
        };
        stringRequest.setRetryPolicy(
                new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Singleton.getInstance(context).addToRequestQueue(stringRequest);
    }


}
