package com.example.adas_project;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;

import android.util.Log;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Arrays;

import android.Manifest;
import android.widget.ImageView;
import android.widget.Toast;


import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    AbstractMQTTHelper mqttHelper;
    ImageView mImageView;
    Spinner spino;
    String[] courses = {"image1", "image2", "image3"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitializeMQTT();
        InitializeSpinner();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String selectedItem = courses[position];
        Log.d("MainActivity", "Selected item: " + selectedItem);

        InputStream imageStream;

        switch (selectedItem) {
            case "image1":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.image1);
                imageStream = getResources().openRawResource(R.raw.image1);
                buttonClick(imageStream);
                break;
            case "image2":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.image2);
                imageStream = getResources().openRawResource(R.raw.image2);
                buttonClick(imageStream);
                break;
            case "image3":
                mImageView = findViewById(R.id.imageView2);
                mImageView.setImageResource(R.drawable.image3);
                imageStream = getResources().openRawResource(R.raw.image3);
                buttonClick(imageStream);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Handle no item selected
    }

    private void buttonClick(InputStream huuhaa) {
        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> convertAndSend(huuhaa));
    }


    private void convertAndSend(InputStream imageStream){
        try {
            byte[] imageBytes = convertStreamToByteArray(imageStream);
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("image", encodedImage); // Set the encoded image

            // Convert JSON object to string
            String message = jsonMessage.toString();
            String topic = "javalta"; // Topic to which you want to send the message

            mqttHelper.sendMessage(topic, message.getBytes()); // Convert message to byte array and send
            Log.d("MainActivity", "Image message sent successfully");

        } catch (JSONException | MqttException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private byte[] convertStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }



    private void InitializeMQTT() {
        mqttHelper = new AbstractMQTTHelper(
                "tcp://broker.hivemq.com:1883", // URI of the MQTT broker
                "1234", // Client name
                new String[]{"javalta"}) { // Topics to subscribe to
            @Override
            public void onMessage(String topic, String message) {
                try {
                    // Parse the JSON message
                    JSONObject jsonObject = new JSONObject(message);

                    // Extract the base64 encoded image string
                    String base64Image = jsonObject.getString("image");

                    // Decode base64 string to byte array
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);

                    // Convert byte array to Bitmap (if needed)
                     Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    // Now you can handle the decoded data as needed
                    // Example: Update UI with the decoded data
                    // runOnUiThread(() -> updateUI(decodedMessage));

                    mImageView = findViewById(R.id.imageView);
                    mImageView.post(() -> mImageView.setImageBitmap(decodedBitmap));

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }



    private void InitializeSpinner() {
        spino = findViewById(R.id.spinner);
        spino.setOnItemSelectedListener(this);

        ArrayAdapter<String> ad = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courses);

        ad.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spino.setAdapter(ad);
    }



    public static void convertBase64ToImage(String base64Image, String outputPath) {
        try {
            // Decode the Base64 string
            byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);

            // Write the byte array to an image file
            try (OutputStream outputStream = new FileOutputStream(outputPath)) {
                outputStream.write(imageBytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}






