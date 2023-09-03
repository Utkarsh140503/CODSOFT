package com.utkarsh.notify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.app.NotificationCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int NOTIFICATION_PERMISSION_CODE = 2;
    private EditText editText;
    private Button showNotificationBtn;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    CircleImageView selectImageBtn;
    LinearLayout developerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        showNotificationBtn = findViewById(R.id.showNotificationBtn);
        developerLayout = findViewById(R.id.developerLayout);

        developerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String portfolioUrl = "https://utkarsh140503.github.io/Portfolio/#";
                Uri uri = Uri.parse(portfolioUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        ImageButton clearImageBtn = findViewById(R.id.clearImageBtn);
        clearImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearImagePreview();
            }
        });

        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        showNotificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNotificationDataComplete()) {
                    if (checkNotificationPermission()) {
                        showConfirmationDialog();
                    }
                } else {
                    showIncompleteDataAlert();
                }
            }
        });
    }

    private boolean isNotificationDataComplete() {
        return !editText.getText().toString().isEmpty() && selectedImageBitmap != null;
    }

    private void showIncompleteDataAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Incomplete Notification Data")
                .setMessage("The information provided for the notification is not complete. Continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showNotification();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Notification")
                .setMessage("Are you sure you want to send this notification?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showNotification();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "notification_channel";
            int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showNotification();
            }
        }
    }

    private void clearImagePreview() {
        selectedImageBitmap = null;

        // Reset the CircularImageView to display placeholder image
        CircleImageView imagePreview = findViewById(R.id.imagePreview);
        imagePreview.setImageResource(R.drawable.notify);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                selectedImageBitmap = BitmapFactory.decodeFileDescriptor(
                        getContentResolver().openFileDescriptor(selectedImageUri, "r").getFileDescriptor());

                // Set the selected image to the ImageView for preview
                ImageView imagePreview = findViewById(R.id.imagePreview);
                imagePreview.setImageBitmap(selectedImageBitmap);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void showNotification() {
        String notificationText = editText.getText().toString();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        String channelId = "notification_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Notify Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the intent to open the developer's website
        Intent openWebsiteIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://utkarsh140503.github.io/Portfolio/"));
        PendingIntent openWebsitePendingIntent = PendingIntent.getActivity(
                this,
                0,
                openWebsiteIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Create the intent to remove the notification
        Intent removeNotificationIntent = new Intent(this, NotificationReceiver.class);
        removeNotificationIntent.setAction("REMOVE_NOTIFICATION");
        PendingIntent removeNotificationPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                removeNotificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Bitmap notificationImage = selectedImageBitmap;
        if (notificationImage == null) {
            notificationImage = BitmapFactory.decodeResource(getResources(), R.drawable.def);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.fire)
                .setContentTitle("Notify App Notification")
                .setContentText(notificationText)
                .setLargeIcon(notificationImage)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(notificationImage)
                        .bigLargeIcon(null))
                .addAction(R.drawable.ic_launcher_foreground, "Say Hi to the Developer!", openWebsitePendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Ok", removeNotificationPendingIntent);

        Notification notification = builder.build();
        notificationManager.notify(1, notification);
    }

}
