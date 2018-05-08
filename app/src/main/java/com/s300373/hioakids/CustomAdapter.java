package com.s300373.hioakids;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by RudiAndre on 08.11.2017.
 */

public class CustomAdapter extends ArrayAdapter {

    List<List<Integer>> alleSvarAlternativ;
    List<Integer> svarAlternativ;
    List<Integer> svarBilder;
    List<Integer> quizSprsml;
    QuizFragment fragment;


    public CustomAdapter(@NonNull Context context, int resource, QuizFragment fragment, @NonNull List<List<Integer>> alleSvarAlternativ,
                         List<Integer> svarAlternativ, List<Integer> svarBilder, List<Integer> quizSprsml) {
        super(context, resource, svarAlternativ);
        this.fragment = fragment;
        this.alleSvarAlternativ = alleSvarAlternativ;
        this.svarAlternativ = svarAlternativ;
        this.svarBilder = svarBilder;
        this.quizSprsml = quizSprsml;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater quizInflater = LayoutInflater.from(getContext());
        View customView = quizInflater.inflate(R.layout.grid_cell, parent, false);

        final ImageButton imgBtn = customView.findViewById(R.id.imageButton);
        imgBtn.setImageResource(svarAlternativ.get(position));

        ObjectAnimator imgBtnAnimation = ObjectAnimator.ofPropertyValuesHolder(
                imgBtn,
                PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.1f));
        imgBtnAnimation.setDuration(1000);

        imgBtnAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        imgBtnAnimation.setRepeatMode(ObjectAnimator.REVERSE);
        imgBtnAnimation.setInterpolator(new FastOutSlowInInterpolator());
        imgBtnAnimation.start();

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sjekkBokstav(svarAlternativ.get(position), quizSprsml.get(fragment.count));
            }
        });

        return customView;
    }

    public void sjekkBokstav(int gjettet, int svar) {
        if(gjettet == svar) {
            fragment.count++;
            fragment.oppdaterHjerter();

            if(fragment.count < 10) {
                fragment.randomRiktig();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refresh(alleSvarAlternativ.get(fragment.count));
                            fragment.startAsyncTask("nesteSprsml");
                        }
                    }, fragment.lydLengde);

            } else if(fragment.count == 10) {
                        fragment.lyd(fragment.lydListe, fragment.lydListe.indexOf(R.raw.julie));

                refresh(new ArrayList<Integer>());
                fragment.dinosaurGul.setVisibility(View.INVISIBLE);
                fragment.dinosaurGreen.setVisibility(View.INVISIBLE);
                fragment.dinosaurBlue.setVisibility(View.VISIBLE);
                fragment.replayKnapp.setVisibility(View.INVISIBLE);
                fragment.replayKnapp.setEnabled(false);
                fragment.startKnapp.setVisibility(View.VISIBLE);
                fragment.startKnapp.setEnabled(true);

            }
        } else {
            if(fragment.count < 10) {
                fragment.startAsyncTask("feil");
            }
        }
    }

    public void refresh(List<Integer> nyeAlternativ) {
        Collections.shuffle(nyeAlternativ);
        svarAlternativ.clear();
        svarAlternativ.addAll(nyeAlternativ);
        notifyDataSetChanged();
    }
}
