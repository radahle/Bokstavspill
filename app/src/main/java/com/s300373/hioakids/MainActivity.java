package com.s300373.hioakids;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        QuizFragment.RestartListener {

    ImageButton startKnapp;

    private static final String BACK_STACK_ROOT_TAG = "root_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startKnapp = findViewById(R.id.start);
        startKnapp.setOnClickListener(this);

        ObjectAnimator quizKnappAnimation = ObjectAnimator.ofPropertyValuesHolder(
                startKnapp,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f));
        quizKnappAnimation.setDuration(1000);

        quizKnappAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        quizKnappAnimation.setRepeatMode(ObjectAnimator.REVERSE);
        quizKnappAnimation.setInterpolator(new FastOutLinearInInterpolator());

        quizKnappAnimation.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                openFragmentHandler(new QuizFragment());
                break;
            default:
                break;
        }
    }

    public void openFragmentHandler(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        fragmentManager.beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .addToBackStack(BACK_STACK_ROOT_TAG)
                .commit();
    }

    @Override
    public void restartFragment() {
        openFragmentHandler(new QuizFragment());
    }
}
