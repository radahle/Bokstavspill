package com.s300373.hioakids;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by RudiAndre on 08.11.2017.
 */

public class QuizFragment extends Fragment {

    GridView bokstavGrid;
    CustomAdapter quizAdapter;
    ImageButton startKnapp, replayKnapp;
    ImageView dinosaurGreen, dinosaurGul, dinosaurBlue;
    Random random;
    ImageView hjerter;
    MediaPlayer mp;
    AsyncTaskStart asyncTaskStart;
    protected static int count;
    RestartListener restartListener;
    int randomLyd;
    long lydLengde;

    List<Integer> svarBilder = new ArrayList<>();
    List<Integer> rundeAlternativ;
    List<List<Integer>> alleSvarAlternativ = new ArrayList<>();
    List<Integer> lydListe = new ArrayList<>();
    List<Integer> feilLydListe = new ArrayList<>();
    List<Integer> riktigLydListe = new ArrayList<>();
    List<Integer> quizSprsml = new ArrayList<>();
    List<Integer> quizSprsmlLyder = new ArrayList<>();
    List<Integer> hjerteListe = new ArrayList<>();

    public interface RestartListener {
        public void restartFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        try {
            restartListener = (RestartListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + ": Må implementere RestartListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        dinosaurGreen = view.findViewById(R.id.dinosaur_green);
        dinosaurGul = view.findViewById(R.id.dinosaur_gul);
        dinosaurBlue = view.findViewById(R.id.dinosaur_blue);

        count = 0;

        setLyder();
        setBokstavBilder();
        setHjerter();

        bokstavGrid = view.findViewById(R.id.gridView);
        hjerter = view.findViewById(R.id.hjerter);

        rundeAlternativ = new ArrayList<>();

        setQuizSprsml();

        startAsyncTask("nesteSprsml");

        rundeAlternativ.addAll(alleSvarAlternativ.get(count));

        Collections.shuffle(rundeAlternativ);

        quizAdapter = new CustomAdapter(getContext(), R.layout.grid_cell, QuizFragment.this, alleSvarAlternativ, rundeAlternativ, svarBilder, quizSprsml);
        bokstavGrid.setAdapter(quizAdapter);

        replayKnapp = view.findViewById(R.id.replayKnapp);
        replayKnapp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startAsyncTask("replay");
            }
        });
        ObjectAnimator replayKnappAnimation = ObjectAnimator.ofPropertyValuesHolder(
                replayKnapp,
                PropertyValuesHolder.ofFloat("scaleX", 1.08f),
                PropertyValuesHolder.ofFloat("scaleY", 1.08f));
        replayKnappAnimation.setDuration(1100);

        replayKnappAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        replayKnappAnimation.setRepeatMode(ObjectAnimator.REVERSE);
        replayKnappAnimation.setInterpolator(new FastOutSlowInInterpolator());

        replayKnappAnimation.start();

        startKnapp = view.findViewById(R.id.startKnapp);
        startKnapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartListener.restartFragment();
            }
        });
        ObjectAnimator startKnappAnimation = ObjectAnimator.ofPropertyValuesHolder(
                startKnapp,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f));
        startKnappAnimation.setDuration(1000);

        startKnappAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        startKnappAnimation.setRepeatMode(ObjectAnimator.REVERSE);
        startKnappAnimation.setInterpolator(new FastOutSlowInInterpolator());

        startKnappAnimation.start();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stoppAsyncTask();
    }

    public void startAsyncTask(String string) {
        stoppAsyncTask();
        asyncTaskStart = new AsyncTaskStart();
        asyncTaskStart.execute(string);
    }

    public void stoppAsyncTask() {
        if(asyncTaskStart != null) {
            asyncTaskStart.cancel(true);
        }
    }

    private class AsyncTaskStart extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {

            if(strings[0].equalsIgnoreCase("nesteSprsml")) {
                hentNesteSprsml();
            } else if(strings[0].equalsIgnoreCase("riktig")) {
                lyd(riktigLydListe, randomLyd);
            } else if(strings[0].equalsIgnoreCase("feil")) {
                randomFeil();
            } else if(strings[0].equalsIgnoreCase("replay")) {
                lyd(lydListe, quizSprsmlLyder.get(count));
            }
            while(mp.isPlaying()) {
                if (isCancelled()) {
                    mp.stop();
                    break;
                }
            }
            return null;
        }
    }

    public void lyd(List<Integer> liste, int lyd) {
        if (mp != null){
            if (mp.isPlaying()||mp.isLooping()) {
                mp.stop();
            }
            mp.release();
            mp = null;
        }
        mp = MediaPlayer.create(getContext(), liste.get(lyd));
        mp.start();
    }

    public void randomRiktig() {
        random = new Random();
        randomLyd = random.nextInt(riktigLydListe.size());
        setLydLengde(riktigLydListe.get(randomLyd));
        startAsyncTask("riktig");
    }

    public void randomFeil() {
        random = new Random();
        int randomLyd = random.nextInt(feilLydListe.size());
        lyd(feilLydListe, randomLyd);
    }

    public void setLydLengde(int resId) {
        Resources res = getActivity().getResources();
        String uriPath = "android.resource://" + getActivity().getPackageName() + "/raw/" + res.getResourceEntryName(resId);
        Uri uri = Uri.parse(uriPath);
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(getActivity(), uri);

        String duration =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        lydLengde = Long.parseLong(duration);
    }

    public void oppdaterHjerter() {
        hjerter.setImageResource(hjerteListe.get(count));
    }

    public void hentNesteSprsml() {
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lyd(lydListe, quizSprsmlLyder.get(count));
    }

    public void setQuizSprsml() {
        random = new Random();
        int indeks;
        for (int i = 0; i < 10; i++) {
            indeks = random.nextInt(svarBilder.size());
            if (!quizSprsml.contains(svarBilder.get(indeks))) {
                quizSprsml.add(svarBilder.get(indeks));
                quizSprsmlLyder.add(indeks);
            } else {
                i--;
            }
        }
        setSvarAlternativer(quizSprsml);
    }

    public void setSvarAlternativer(List<Integer> quizSprsml) {
        random = new Random();
        int indeks;
        for(int i = 0; i < quizSprsml.size(); i++) {
            List<Integer> temp = new ArrayList<>();
            temp.add(quizSprsml.get(i));
            for(int j = 0; j < 3; j++) {
                indeks = random.nextInt(svarBilder.size());
                if (!temp.contains(svarBilder.get(indeks))) {
                    temp.add(svarBilder.get(indeks));
                } else {
                    j--;
                }
            }
            alleSvarAlternativ.add(temp);
        }
    }

    public void setHjerter() {
        hjerteListe.add(R.drawable.hjerter00); hjerteListe.add(R.drawable.hjerter01); hjerteListe.add(R.drawable.hjerter02);
        hjerteListe.add(R.drawable.hjerter03); hjerteListe.add(R.drawable.hjerter04); hjerteListe.add(R.drawable.hjerter05);
        hjerteListe.add(R.drawable.hjerter06); hjerteListe.add(R.drawable.hjerter07); hjerteListe.add(R.drawable.hjerter08);
        hjerteListe.add(R.drawable.hjerter09); hjerteListe.add(R.drawable.hjerter10);
    }

    public void setBokstavBilder() {
        svarBilder.add(R.drawable.a); svarBilder.add(R.drawable.b); svarBilder.add(R.drawable.c); svarBilder.add(R.drawable.d);
        svarBilder.add(R.drawable.e); svarBilder.add(R.drawable.f); svarBilder.add(R.drawable.g); svarBilder.add(R.drawable.h);
        svarBilder.add(R.drawable.i); svarBilder.add(R.drawable.j); svarBilder.add(R.drawable.k); svarBilder.add(R.drawable.l);
        svarBilder.add(R.drawable.m); svarBilder.add(R.drawable.n); svarBilder.add(R.drawable.o); svarBilder.add(R.drawable.p);
        svarBilder.add(R.drawable.q); svarBilder.add(R.drawable.r); svarBilder.add(R.drawable.s); svarBilder.add(R.drawable.t);
        svarBilder.add(R.drawable.u); svarBilder.add(R.drawable.v); svarBilder.add(R.drawable.w); svarBilder.add(R.drawable.x);
        svarBilder.add(R.drawable.y); svarBilder.add(R.drawable.z); svarBilder.add(R.drawable.ae); svarBilder.add(R.drawable.oe);
        svarBilder.add(R.drawable.aa);
    }

    public void setLyder() {
        lydListe.add(R.raw.a); lydListe.add(R.raw.b); lydListe.add(R.raw.c); lydListe.add(R.raw.d);
        lydListe.add(R.raw.e); lydListe.add(R.raw.f); lydListe.add(R.raw.g); lydListe.add(R.raw.h);
        lydListe.add(R.raw.i); lydListe.add(R.raw.j); lydListe.add(R.raw.k); lydListe.add(R.raw.l);
        lydListe.add(R.raw.m); lydListe.add(R.raw.n); lydListe.add(R.raw.o); lydListe.add(R.raw.p);
        lydListe.add(R.raw.q); lydListe.add(R.raw.r); lydListe.add(R.raw.s); lydListe.add(R.raw.t);
        lydListe.add(R.raw.u); lydListe.add(R.raw.v); lydListe.add(R.raw.w); lydListe.add(R.raw.x);
        lydListe.add(R.raw.y); lydListe.add(R.raw.z); lydListe.add(R.raw.ae); lydListe.add(R.raw.oe);
        lydListe.add(R.raw.aa); lydListe.add(R.raw.julie); feilLydListe.add(R.raw.feil1);
        feilLydListe.add(R.raw.feil2); feilLydListe.add(R.raw.feil3); riktigLydListe.add(R.raw.riktig1);
        riktigLydListe.add(R.raw.riktig2); riktigLydListe.add(R.raw.riktig3); riktigLydListe.add(R.raw.riktig4);
    }
}
