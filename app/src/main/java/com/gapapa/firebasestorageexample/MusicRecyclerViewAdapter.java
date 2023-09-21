package com.gapapa.firebasestorageexample;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.MusicViewHolder> {
    private ArrayList<Music> arrayListMusic;
    private DatabaseReference realtimeDatabase = FirebaseDatabase.getInstance().getReference();
    private Context context;
    public MusicRecyclerViewAdapter(ArrayList<Music> arrayListMusic, Context context) {
        this.arrayListMusic = arrayListMusic;
        this.context = context;
    }

    @NonNull
    @Override
    public MusicRecyclerViewAdapter.MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicRecyclerViewAdapter.MusicViewHolder holder, int position) {
        final Music music = arrayListMusic.get(position);

        // Set tiap elemen
        // Title
        holder.txtMusicTitle.setText(music.getTitle().replace(".mp3", ""));
        // Artist
        holder.txtMusicArtist.setText(music.getArtist());

        // Operation
        holder.container.setOnClickListener(v -> {
            // If item clicked
            realtimeDatabase.child("currentMusic").setValue(music.getWholeName());
            realtimeDatabase.child("currentMusicIndex").setValue(music.getIndex());
        });
    }

    @Override
    public int getItemCount() {
        return arrayListMusic.size();
    }

    public void updateMusicArray(ArrayList<Music> newList){
        arrayListMusic.clear();
        arrayListMusic = newList;
        notifyDataSetChanged();
    }

    public String getWholeName(int index){
        return arrayListMusic.get(index).getWholeName();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder{
        TextView txtMusicTitle, txtMusicArtist;
        LottieAnimationView iconMusic;
        ConstraintLayout container;
        CardView containerCard;
        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMusicTitle = itemView.findViewById(R.id.txtMusicTitle);
            txtMusicArtist = itemView.findViewById(R.id.txtMusicArtist);
            iconMusic = itemView.findViewById(R.id.iconMusic);
            container = itemView.findViewById(R.id.container);
            containerCard = itemView.findViewById(R.id.containerCard);
        }
    }
}
