package com.example.notiguard;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.util.Log;
import android.os.Build;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import android.widget.Toast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ActivityCompat;
import android.view.View;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Activity";
    private TextView detectTextView, alarmTextView, ledTextView, statusTextView;
    private DatabaseReference mDatabase;
    private AlertDialog.Builder alertDialogBuilder;
    private static final int NOTIFICATION_REQUEST_CODE=1234;
    private static final String CHANNEL_ID = "MyChannelID";
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();


        // Ánh xạ TextView từ layout
        detectTextView = findViewById(R.id.detectTextView);
        alarmTextView = findViewById(R.id.alarmTextView);
        ledTextView = findViewById(R.id.ledTextView);
        statusTextView = findViewById(R.id.statusTextView);

        // Khởi tạo DatabaseReference cho Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("Noti");
        // Khởi tạo AlertDialog.Builder
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Thông báo"); // Tiêu đề của thông báo
        alertDialogBuilder.setMessage("Đã phát hiện chuyển động"); // Nội dung thông báo

        // Thiết lập nút "OK" trong thông báo
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Đóng thông báo khi người dùng click nút "OK"
                dialogInterface.dismiss();
            }
        });

        // Đọc dữ liệu từ các node 'detect', 'alarm', 'led', 'status'
        mDatabase.child("Detect").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String detectData = dataSnapshot.getValue(String.class);
                    detectTextView.setText(detectData);
                    if ("ON".equals(detectData)) {
                        // Gửi thông báo pop-up
                        showAlertDialog();
                        showNotification();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        mDatabase.child("Alarm").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String alarmData = dataSnapshot.getValue(String.class);
                    alarmTextView.setText(alarmData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        mDatabase.child("LED").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String ledData = dataSnapshot.getValue(String.class);
                    ledTextView.setText(ledData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        mDatabase.child("Status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String statusData = dataSnapshot.getValue(String.class);
                    statusTextView.setText(statusData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }
    private void showAlertDialog(){
        AlertDialog alertDialog=alertDialogBuilder.create();
        alertDialog.show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "Channel for my app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Method to show a pop-up notification
    private void showNotification() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY)
                == PackageManager.PERMISSION_GRANTED) {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.images) // Replace with your notification icon
                    .setContentTitle("Motion Detected")
                    .setContentText("Đã phát hiện chuyển động.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // Show the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            // If permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_NOTIFICATION_POLICY}, NOTIFICATION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, show the notification
                showNotification();
            } else {
                // Permission denied, handle accordingly (e.g., inform the user)
                Toast.makeText(this, "Permission denied. The app cannot show notifications.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
