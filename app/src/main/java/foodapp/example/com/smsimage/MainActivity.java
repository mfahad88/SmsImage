package foodapp.example.com.smsimage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView txt_sms;
    Button btn_sent,btn_browse,btn_refresh;
    EditText edt_number;
    ImageView imageView;
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_LOAD_CAMERA = 7;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_sent=findViewById(R.id.btn_sent);
        btn_browse=findViewById(R.id.btn_browse);
        btn_refresh=findViewById(R.id.btn_refresh);
        edt_number=findViewById(R.id.edt_number);
        imageView=findViewById(R.id.imageView);
        txt_sms = findViewById(R.id.txt_sms);
        txt_sms.setMovementMethod(new ScrollingMovementMethod());
        requestMultiplePermissions();
    }

    public void pickImage() {

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);

    }

    private void requestMultiplePermissions(){

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
//                        Manifest.permission.READ_PHONE_STATE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            btn_refresh.setOnClickListener(MainActivity.this);
                            btn_browse.setOnClickListener(MainActivity.this);
                            btn_sent.setOnClickListener(MainActivity.this);



                        }
                        if(!report.areAllPermissionsGranted()){
                            Toast toast=Toast.makeText(getApplicationContext(),"To continue allow permission",Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER,0,0);
                            toast.show();
                            finish();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            // openSettingsDialog();
                        }
                    }


                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();

                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();

    }

    private void openSettingsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Required Permissions");
        builder.setMessage("This app require permission to use awesome feature. Grant them in app settings.");
        builder.setPositiveButton("Take Me To SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    public List<Sms> getAllSms(String folderName) {
        List<Sms> lstSms = new ArrayList<Sms>();
        Sms objSms = new Sms();
        Uri message = Uri.parse("content://sms/"+folderName);
        ContentResolver cr = getApplicationContext().getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        // mActivity.startManagingCursor(c);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                String msg = c.getString(c.getColumnIndexOrThrow("body"));

                if(msg.length()>3){
                    if(msg.substring(0,3).equalsIgnoreCase("pic")){
                        objSms = new Sms();
                        objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                        objSms.setAddress(c.getString(c
                                .getColumnIndexOrThrow("address")));
                        objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")).substring(4,c.getString(c.getColumnIndexOrThrow("body")).length()));
//                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                        objSms.setReadState(c.getString(c.getColumnIndex("read")));
                        objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                        imageView.setImageBitmap(StringToBitMap(c.getString(c.getColumnIndexOrThrow("body")).substring(4,c.getString(c.getColumnIndexOrThrow("body")).length())));
                        lstSms.add(objSms);



                    }
                }
                c.moveToNext();
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS in " + folderName);
        // }
        c.close();

        return lstSms;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_refresh){
            for(Sms sms:getAllSms("inbox")) {
                try {
                    byte[] decoded=CompressionUtils.decompress(sms.getMsg().getBytes());
                    byte[] decoded_decompressed=Base64.decode(decoded,Base64.DEFAULT);
                    Bitmap bitmap=BitmapFactory.decodeByteArray(decoded_decompressed,0,decoded_decompressed.length);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DataFormatException e) {
                    e.printStackTrace();
                }
                txt_sms.append(sms.toString()+"\n");
                txt_sms.append("------------------------------------------");

            }
        }if(v.getId()==R.id.btn_sent){
            if(!TextUtils.isEmpty(edt_number.getText().toString())){
                try {
                    Bitmap img=CompressionUtils.getResizedBitmap(((BitmapDrawable)imageView.getDrawable()).getBitmap(),8); // get Drawable from imageview Bitmap and resize
                    String encoded_msg=Base64.encodeToString(CompressionUtils.compress(getBitmapAsByteArray(img)),Base64.DEFAULT);// resized img to compress and base64
                    sendSMS(edt_number.getText().toString(), "pic"+encoded_msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                sendSMS(edt_number.getText().toString(), "pic");
                imageView.setImageBitmap(null);
                edt_number.getText().clear();
            }
        }if(v.getId()==R.id.btn_browse){
            pickImage();
        }
    }

    @SuppressLint("NewApi")
    public void sendSMS(final String phoneNo, final String msg) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SmsManager smsManager = SmsManager.getDefault();
                    ArrayList<String> list=smsManager.divideMessage(msg);
                    Log.e("Complete Message", String.valueOf(list.size()));
                    int counter=0;
                    for(String str:list) {
                        counter++;
                        Log.e("Message",str);
                        Log.e("Length", String.valueOf(str.length()));
                        Log.e("Counter", String.valueOf(counter));
                    }
                    smsManager.sendTextMessage(phoneNo,null,msg,null,null);



                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Message Sent",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }).start();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getApplicationContext().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }if(requestCode == RESULT_LOAD_CAMERA && resultCode == RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            imageView.setImageBitmap(bitmap);
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 2, outputStream);
        return outputStream.toByteArray();
    }



    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,15, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        Log.e("temp--->",temp);
        return temp;
    }


    public Bitmap StringToBitMap(String encodedString){
        try {
//            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);

            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Log.e("Encoded--->",String.valueOf(encodeByte));
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
