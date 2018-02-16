package com.truiton.mobile.vision.qrcode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "Barcode Scanner API";
    private static final int PHOTO_REQUEST = 10;
    private TextView scanResults;
    private BarcodeDetector detector;
    private Uri imageUri;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    JSONObject getMethodJsonResponse = new JSONObject();
    boolean isTicketChecked;
    boolean isTicketValidated;
    String name;
    String journeyType;
    String fare;
    String source;
    String destination;
    String expiry;
    String objectId;
    Calendar expiryDate = Calendar.getInstance();
    Spinner dropdown;
    String mobno;
    HashMap<String,Integer> stations = new HashMap<>();
    @Override



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);
        Button button2 = (Button) findViewById(R.id.button2);
        scanResults = (TextView) findViewById(R.id.scan_results);
        initializeValues();

        if (savedInstanceState != null) {
            imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
            Log.i("Result",savedInstanceState.getString(SAVED_INSTANCE_RESULT));
            scanResults.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this, new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!objectId.equals("Scan Failed: Found nothing to scan")){
                    System.out.println("Make a call");
                    System.out.println("https://qrnodeapi.herokuapp.com/api/tickets/" + objectId);
                    getObject(objectId);
                }
                else{
                    System.out.println("Don't Make a Call");
                }
                getObject(objectId);
            }
        });


        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
        if (!detector.isOperational()) {
            scanResults.setText("Could not set up the detector!");
            return;
        }

        // Asking for permission to send SMS
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.SEND_SMS)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},1);
            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},1);
            }
        } else{
            //do nothing

        }


    }


    public void initializeValues(){
        stations.put("Borivali",0);
        stations.put("Kandivali",1);
        stations.put("Malad",2);
        stations.put("Goregaon",3);
        stations.put("Jogeshwari",4);
        stations.put("Andheri",5);
        stations.put("VileParle",6);
        stations.put("Santacruz",7);
        stations.put("Khar",8);
        stations.put("Bandra",9);

        //Initializing the dropdown
        dropdown = (Spinner) findViewById(R.id.stationsList);
        ArrayList<String> stationsList = new ArrayList<String>();
        stationsList.addAll(stations.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, stationsList);
        dropdown.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            launchMediaScanIntent();
            try {
                Bitmap bitmap = decodeBitmapUri(this, imageUri);
                if (detector.isOperational() && bitmap != null) {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Barcode> barcodes = detector.detect(frame);
                    for (int index = 0; index < barcodes.size(); index++) {
                        Barcode code = barcodes.valueAt(index);
                        scanResults.setText(scanResults.getText() + code.displayValue + "\n");

                        //Required only if you need to extract the type of barcode
                        int type = barcodes.valueAt(index).valueFormat;
                        switch (type) {
                            case Barcode.CONTACT_INFO:
                                Log.i(LOG_TAG, code.contactInfo.title);
                                System.out.println("1");
                                break;
                            case Barcode.EMAIL:
                                Log.i(LOG_TAG, code.email.address);
                                System.out.println("2");
                                break;
                            case Barcode.ISBN:
                                Log.i(LOG_TAG, code.rawValue);
                                System.out.println("3");
                                break;
                            case Barcode.PHONE:
                                Log.i(LOG_TAG, code.phone.number);
                                System.out.println("4");
                                break;
                            case Barcode.PRODUCT:
                                Log.i(LOG_TAG, code.rawValue);
                                System.out.println("5");
                                break;
                            case Barcode.SMS:
                                Log.i(LOG_TAG, code.sms.message);
                                System.out.println("6");
                                break;
                            case Barcode.TEXT:
                                Log.i(LOG_TAG, code.rawValue);
                                String []infoList = code.rawValue.split(":");
                                int length = infoList.length;
                                objectId = infoList[length-1];
                                System.out.println(objectId);
                                break;
                            case Barcode.URL:
                                Log.i(LOG_TAG, "url: " + code.url.url);
                                System.out.println("8");
                                break;
                            case Barcode.WIFI:
                                Log.i(LOG_TAG, code.wifi.ssid);
                                System.out.println("9");
                                break;
                            case Barcode.GEO:
                                Log.i(LOG_TAG, code.geoPoint.lat + ":" + code.geoPoint.lng);
                                System.out.println("10");
                                break;
                            case Barcode.CALENDAR_EVENT:
                                Log.i(LOG_TAG, code.calendarEvent.description);
                                System.out.println("11");
                                break;
                            case Barcode.DRIVER_LICENSE:
                                Log.i(LOG_TAG, code.driverLicense.licenseNumber);
                                System.out.println("12");
                                break;
                            default:
                                Log.i(LOG_TAG, code.rawValue);
                                System.out.println("13");
                                break;
                        }
                    }
                    if (barcodes.size() == 0) {
                        scanResults.setText("Scan Failed: Found nothing to scan");
                        objectId = "Scan Failed: Found nothing to scan";
                    }
                } else {
                    scanResults.setText("Could not set up the detector!");
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
                        .show();
                Log.e(LOG_TAG, e.toString());
            }
        }

    }


    private void getObject(String id) {

        final String currentDestination = dropdown.getSelectedItem().toString();
        System.out.println("Current Station : " + currentDestination);
        String URL = "https://qrnodeapi.herokuapp.com/api/tickets/" + id;
        RequestQueue requestQueue = Volley.newRequestQueue( this);

        //Works for Get Request
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){
                        Log.i("Rest Response",response.toString());
                        getMethodJsonResponse = response;

                        try {
                            name = getMethodJsonResponse.getString("Name");
                            isTicketChecked = getMethodJsonResponse.getBoolean("isTicketChecked");
                            isTicketValidated = getMethodJsonResponse.getBoolean("isTicketValidated");
                            source = getMethodJsonResponse.getString("source");
                            destination = getMethodJsonResponse.getString("destination");
                            journeyType = getMethodJsonResponse.getString("journeyType");
                            fare = getMethodJsonResponse.getString("fare");
                            expiry = getMethodJsonResponse.getString("expiry");
                            objectId = getMethodJsonResponse.getString("_id");
                            mobno = getMethodJsonResponse.getString("mobno");

                            Log.i("Check Ticket", String.valueOf(isTicketChecked));
                            Log.i("Check Validity",String.valueOf(isTicketValidated));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Checking ticket for the first time
                        if(isTicketChecked == false){
                            isTicketChecked = true;
                            if(stations.get(currentDestination) >= stations.get(source) && stations.get(currentDestination) <= stations.get(destination)) {
                                isTicketValidated = true;
                                updateIsTicketValidated(name, isTicketChecked, isTicketValidated,source,destination, journeyType, fare, expiry,objectId);
                                sendSms(objectId,mobno);
                                Toast.makeText(MainActivity.this, "Ticket Checked and Validated", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Invalid Ticket", Toast.LENGTH_SHORT).show();
                            }
                        }
                        //Ticket already checked
                        else{
                            //sendSms(objectId);
                            Toast.makeText(MainActivity.this, "Ticket Already Checked", Toast.LENGTH_SHORT).show();
                        }



                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.e("Rest Error",error.toString());
                    }
                }
        );
        requestQueue.add(objectRequest);

    }


    private void updateIsTicketValidated(String name,boolean isTicketChecked,boolean isTicketValidated,String source,String destination, String journeyType,String fare,String expiry,String id) {
        RequestQueue queue = Volley.newRequestQueue(this);

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Name", name);
            jsonObject.put("isTicketValidated",isTicketValidated);
            jsonObject.put("isTicketChecked",isTicketChecked);
            jsonObject.put("source", source);
            jsonObject.put("destination", destination);
            jsonObject.put("journeyType", journeyType);
            jsonObject.put("fare",fare);
            jsonObject.put("expiry",expiry);

        } catch (JSONException e) {
            // handle exception
        }


            /*
            {"isTicketChecked":true,"isTicketValidated":true,"_id":"5a7bbb3ce6cb753d7d5f04ee","Name":"Manish Jathan","source":"Goregaon","destination":"Andheri","journeyType":"return","fare":"20","expiry":"2018-02-08T00:00:00.000Z"}
            */
        String url = "https://qrnodeapi.herokuapp.com/api/tickets/" + id;
        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {

                try {
                    Log.i("json", jsonObject.toString());
                    return jsonObject.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        queue.add(putRequest);
    }


    private void sendSms(String objectId,String mobno){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(mobno, null, name + " with ticket " +objectId + " has been validated.", null, null);
            Toast.makeText(getApplicationContext(), "SMS Sent!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }





    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        imageUri = FileProvider.getUriForFile(MainActivity.this,
                BuildConfig.APPLICATION_ID + ".provider", photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, scanResults.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }
    public String getObjectId(){
        return objectId;
    }
}
