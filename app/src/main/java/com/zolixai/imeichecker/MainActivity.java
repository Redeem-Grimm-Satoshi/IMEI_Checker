package com.zolixai.imeichecker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;


/*

Author: Redeem_Grimm
Description: Scraps IMEI Codes From Orange Servers Checking If They're Valid For The Orange Free Net Activation Or Not.

 */

public class MainActivity extends AppCompatActivity {


    //Global V
    EditText getCode;
    MaterialButton checkCode;
    TextView displayCode,networkName;
    int i = 0;
    int sim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);


        //Global V ID
        getCode = findViewById(R.id.getCode);
        checkCode = findViewById(R.id.checkCode);
        displayCode = findViewById(R.id.displayCode);
        networkName=findViewById(R.id.sim_name);

        //get sim name
        String getSimName=manager.getNetworkOperatorName();

        //Set the name to textview
        networkName.setText("You're Using: " + getSimName);

        //Dialog Box for Instructions
        MaterialAlertDialogBuilder dialogBuilder=new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("IMEI Checker 1.1 Info");
        dialogBuilder.setMessage("Instructions\n\n 1) error code: -1 means request didn't reach server. \n\n 2) Valid IMEI Codes are displayed in the box below \n\n\nDeveloper: Redeem_Grimm");
        dialogBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog=dialogBuilder.create();
        alertDialog.show();






        //When Button Clicked It Starts Checking
        checkCode.setOnClickListener(view -> {
            scrapIMEI();
        });


    }


    //start the process
    void scrapIMEI(){
        //String code = getCode.getText().toString();
        String[] IMEI = getCode.getText().toString().split("\n");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 234);
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
                TelephonyManager manager2 = manager.createForSubscriptionId(2);


                TelephonyManager managerMain = (sim == 0) ? manager : manager2;
                StringBuilder builder = new StringBuilder();

                for (i = 0; i < IMEI.length; i++) {
                    if (IMEI[i].equalsIgnoreCase("")) return;


                    managerMain.sendUssdRequest("#195*1*" + IMEI[i] + "#", new TelephonyManager.UssdResponseCallback() {
                        @Override
                        public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                            super.onReceiveUssdResponse(telephonyManager, request, response);
                            String result = response.toString();
                            if (result.contains("Veuillez rentrer le numero de telephone du beneficiaire et Validez")) {
                                builder.append(IMEI[i] + "\n");
                                getCode.setText(IMEI[i] + "");

                                //Set Toast
                                Toast.makeText(MainActivity.this, "IMEI Validated!", Toast.LENGTH_SHORT);
                            } else {

                                //Set Toast
                                Toast.makeText(MainActivity.this, "IMEI Invalid!", Toast.LENGTH_SHORT);

                            }


                            Log.e("TAG", "onReceiveUssdResponse:  Ussd Response = " + response.toString().trim());
                            Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();


                        }


                        @Override
                        public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                            super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);

                            getCode.setText(" ");

                            Log.e("TAG", "onReceiveUssdResponseFailed: " + " " + failureCode + request);

                            try {
                                Toast.makeText(MainActivity.this, failureCode + " ", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                System.out.println("Ooops! Request Didn't Reach Server");
                            }

                        }
                    }, new Handler());

                    displayCode.setText(builder.toString());
                }
            }
        }

    }
}













