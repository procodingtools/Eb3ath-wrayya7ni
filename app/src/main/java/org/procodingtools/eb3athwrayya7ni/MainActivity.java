package org.procodingtools.eb3athwrayya7ni;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lib.folderpicker.FolderPicker;
import me.zhanghai.android.effortlesspermissions.AfterPermissionDenied;
import me.zhanghai.android.effortlesspermissions.EffortlessPermissions;
import me.zhanghai.android.effortlesspermissions.OpenAppDetailsDialogFragment;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class MainActivity extends AppCompatActivity {


    private EditText messageET;
    private Button sendBtn;
    private Button pickFileBtn;
    private Button pickSmsTextBtn;
    private List<String> numbers;
    private List<String> errorNumber;
    private int numberPosition;
    private SendingSmsDialogFragment dialog;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_FILE_REQUEST_CODE = 6;
    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.SEND_SMS
    };

    private boolean successShown;
    private static final int NUMBERS_FILE_REQUEST_CODE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //starting init views
        initViews();

        //start setting listeners
        setListeners();

        numbers = new ArrayList<>();
        errorNumber = new ArrayList<>();

        numberPosition = 0;
        successShown = false;
    }

    //setting listeners
    private void setListeners() {
        pickFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FolderPicker.class).putExtra("pickFiles",true);
                startActivityForResult(intent, NUMBERS_FILE_REQUEST_CODE);
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numbers.isEmpty()){
                    Toast.makeText(MainActivity.this, getString(R.string.select_numbers), Toast.LENGTH_LONG).show();
                }else{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    dialog = SendingSmsDialogFragment.newInstance(getString(R.string.sending),
                            getString(R.string.sent_numbers,(numberPosition+1)+"" ,numbers.size()+""));
                    dialog.show(fragmentManager,"tag");
                    dialog.setCancelable(false);
                    sendSMS(numbers.get(numberPosition));
                }

                successShown = false;
            }
        });

        pickSmsTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FolderPicker.class).putExtra("pickFiles",true);
                startActivityForResult(intent, SMS_FILE_REQUEST_CODE);
            }
        });
    }

    //init views
    private void initViews() {
        messageET = findViewById(R.id.text_message);
        sendBtn = findViewById(R.id.send_btn);
        pickFileBtn = findViewById(R.id.pick_file_btn);
        pickSmsTextBtn = findViewById(R.id.pick_sms_text);
    }



     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == NUMBERS_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            String folderLocation = intent.getExtras().getString("data");
            try {
                FileInputStream file = new FileInputStream(folderLocation);
                String data = IOUtils.toString(file);
                data.replaceAll("\\s+","");
                parseNumbers(data);
            } catch (FileNotFoundException e) {
                Log.e("File exception",e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if (requestCode == SMS_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            String folderLocation = intent.getExtras().getString("data");
            try {
                FileInputStream file = new FileInputStream(folderLocation);
                String data = IOUtils.toString(file);
                messageET.setText(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    //parsing numbers
    private void parseNumbers(String data) {
        String[] item = data.split(",");
        for (int i = 0; i<item.length; i++)
            numbers.add(item[i]);
    }



    void sendSMS(final String phoneNumber) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        //Toast.makeText(getBaseContext(), "SMS sent",
                          //      Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        if (numberPosition < numbers.size()-1) {
                            numberPosition++;
                            sendSMS(numbers.get(numberPosition));
                            dialog.update(getString(R.string.sent_numbers,numberPosition+"" ,numbers.size()+""));
                        }else{
                            dialog.dismiss();
                            showSuccess();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        errorNumber.add(numbers.get(numberPosition));
                        if (numberPosition < numbers.size()-1) {
                            numberPosition++;
                            sendSMS(numbers.get(numberPosition));
                            dialog.update(getString(R.string.sent_numbers,numberPosition+"" ,numbers.size()+""));
                        }else {
                            dialog.dismiss();
                            showSuccess();
                        }
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, messageET.getText().toString(), sentPI, deliveredPI);
    }

    private void showSuccess() {

        if (!successShown) {
            successShown = true;
            final AwesomeSuccessDialog success = new AwesomeSuccessDialog(MainActivity.this);
            success.setMessage(getString(R.string.sent))
                    .setDoneButtonText(getString(R.string.ok))
                    .setDoneButtonClick(new Closure() {
                        @Override
                        public void exec() {
                            success.hide();
                        }
                    }).show();
        }
        numberPosition = 0;
    }


    //_________________________________________Permissions overrides____________________________________
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Dispatch to our library.
        EffortlessPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,
                this);
    }


    // Call back to the same method so that we'll check and proceed.
    @AfterPermissionGranted(PERMISSION_REQUEST_CODE)
    private void saveFile() {
        if (EffortlessPermissions.hasPermissions(this, PERMISSIONS)) {
            // We've got the permission.

        } else if (EffortlessPermissions.somePermissionPermanentlyDenied(this,
                PERMISSIONS)) {
            // Some permission is permanently denied so we cannot request them normally.
            OpenAppDetailsDialogFragment.show(
                    R.string.grant_permission,
                    R.string.open_settings, this);
        } else  {
            // Request the permissions.
            EffortlessPermissions.requestPermissions(this,
                    R.string.grant_permission,
                    PERMISSION_REQUEST_CODE, PERMISSIONS);
        }
    }

    @AfterPermissionDenied(PERMISSION_REQUEST_CODE)
    private void onSaveFilePermissionDenied() {
        // User denied at least some of the required permissions, report the error.
        Toast.makeText(this, R.string.app_name, Toast.LENGTH_SHORT).show();
    }

    //____________________________________End permissions overrides_________________________________
}
