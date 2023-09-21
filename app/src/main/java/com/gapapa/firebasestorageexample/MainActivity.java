package com.gapapa.firebasestorageexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton btnPlayPause, btnNext, btnPrevious;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private List<StorageReference> fileLinkList;
    private ArrayList<String> fileNameList = new ArrayList<>();
    private ArrayList<Music> musicObjectList = new ArrayList<>();
    private RecyclerView recyclerViewMusic;
    private boolean isPlaying = false;
    private boolean isInitialized = false;
    private boolean isListenerInitialized = false;
    private boolean isFalseCompletionCalled = false;
    private int currentIndex = 0;
    private boolean isListFileRetrieved = false;
    private MusicRecyclerViewAdapter adapter;
    private DatabaseReference realtimeDatabase = FirebaseDatabase.getInstance().getReference();
    private TextView txtSongTitle, txtNoInternetConnection;
    private LottieAnimationView iconNoInternetConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // Status Bar Customization
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.purple_shade));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        recyclerViewMusic = findViewById(R.id.recyclerViewMusic);
        txtSongTitle = findViewById(R.id.txtSongTitle);
        txtNoInternetConnection = findViewById(R.id.txtNoInternetConnection);
        iconNoInternetConnection = findViewById(R.id.iconNoInternetConnection);

        if(isInternetConnectionAvailable()){
            // Get List of Songs
            storageRef.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
                @Override
                public void onComplete(@NonNull Task<ListResult> task) {
                    fileLinkList = task.getResult().getItems();
                    for(int i = 0; i < fileLinkList.size() ; i++){
                        fileNameList.add(fileLinkList.get(i).getName());
                    }
                    // Convert to Music Object + Store to arraylist
                    for(int i = 0 ; i < fileNameList.size() ; i++){
                        // Convert from whole name to music artist and title
                        String[] parts = getTitleAndArtist(fileNameList.get(i).toString());
                        // Store to arraylist
                        musicObjectList.add(new Music(parts[1], parts[0], i, false));
                    }
                    // Prepare Recyclerview
                    adapter = new MusicRecyclerViewAdapter(musicObjectList, getApplicationContext());
                    recyclerViewMusic.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerViewMusic.setAdapter(adapter);
                    isListFileRetrieved = true;

                    // Listener if the song completed
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if(!isFalseCompletionCalled){
                                isFalseCompletionCalled = true;
                            }
                            else{
                                if(currentIndex+1 == fileNameList.size()){
                                    realtimeDatabase.child("currentMusicIndex").setValue(0);
                                }
                                else{
                                    realtimeDatabase.child("currentMusicIndex").setValue(currentIndex+1);
                                }
                            }
                        }
                    });

                    // Realtime Database Things
                    realtimeDatabase.child("currentMusicIndex").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!isListenerInitialized){
                                isListenerInitialized = true;
                                currentIndex = snapshot.getValue(int.class);
                            }
                            else{
                                isInitialized = true;
                                currentIndex = snapshot.getValue(int.class);
                                getCurrentIndexUrl();
                                txtSongTitle.setText(adapter.getWholeName(currentIndex));
                                isPlaying = true;
                                btnPlayPause.setImageResource(R.drawable.icon_pause);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    btnPlayPause.setOnClickListener(v -> {
                        if(!isInitialized){
                            isInitialized = true;
                            if(currentIndex+1 == fileNameList.size()){
                                realtimeDatabase.child("currentMusicIndex").setValue(0);
                            }
                            else{
                                realtimeDatabase.child("currentMusicIndex").setValue(currentIndex+1);
                            }
                            isPlaying = true;
                            btnPlayPause.setImageResource(R.drawable.icon_pause);
                        }
                        if(!isPlaying){
                            isPlaying = true;
                            btnPlayPause.setImageResource(R.drawable.icon_pause);
                            resumeAudio();
                        }
                        else{
                            isPlaying = false;
                            btnPlayPause.setImageResource(R.drawable.icon_play);
                            pauseAudio();
                        }
                    });

                    btnNext.setOnClickListener(v -> {
                        btnPlayPause.setImageResource(R.drawable.icon_pause);
                        playNextAudio();
                    });

                    btnPrevious.setOnClickListener(v -> {
                        btnPlayPause.setImageResource(R.drawable.icon_pause);
                        playPreviousAudio();
                    });
                }
            });
        }
        else{ // If no internet connection
            txtNoInternetConnection.setVisibility(View.VISIBLE);
            iconNoInternetConnection.setVisibility(View.VISIBLE);
            btnPlayPause.setEnabled(false);
            btnPrevious.setEnabled(false);
            btnNext.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public boolean isInternetConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private String[] getTitleAndArtist(String wholeName){
        int hyphenIndex = wholeName.indexOf("-");
        if (hyphenIndex == -1) {
            return new String[]{wholeName, ""};
        } else {
            return new String[]{wholeName.substring(0, hyphenIndex), wholeName.substring(hyphenIndex + 1)};
        }
    }

    private void playURLAudio(String url){
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        else{
            mediaPlayer.stop(); // Stop currently played media
            mediaPlayer.reset(); // Reset the mediaPlayer resources
        }

        // Setting up mediaPlayer
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void resumeAudio(){
        mediaPlayer.start();
    }
    private void pauseAudio(){
        mediaPlayer.pause();
    }
    public void playNextAudio(){
        // isInitialized false indicate if the app cold start
        if(!isInitialized){
            isInitialized = true;
            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.icon_pause);
        }
        else{
            if(currentIndex+1 == fileNameList.size()){
                realtimeDatabase.child("currentMusicIndex").setValue(0);
            }
            else{
                realtimeDatabase.child("currentMusicIndex").setValue(currentIndex += 1);
            }
        }
        getCurrentIndexUrl();
    }

    private void getCurrentIndexUrl(){
        if(isListFileRetrieved){
            // Get URL
            storageRef.child(fileNameList.get(currentIndex).toString()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Log.d("downloadURL", task.getResult().toString());
                    // Passing music URL
                    playURLAudio(task.getResult().toString());
                }
            });
        }
        else{
            Toast.makeText(MainActivity.this, "Something when wrong\nTry again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void playPreviousAudio(){
        // isInitialized false indicate if the app cold start
        if(!isInitialized){
            isInitialized = true;
        }
        else{
            if(currentIndex-1 == -1){
                realtimeDatabase.child("currentMusicIndex").setValue(fileNameList.size()-1);
            }
            else{
                realtimeDatabase.child("currentMusicIndex").setValue(currentIndex - 1);
            }
        }
    }
}