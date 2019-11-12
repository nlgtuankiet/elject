package com.nlgtuankiet.elject;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

public class Eljection {
    public static void injectWith(@NonNull Context context, @NonNull Object target) {
        ((HasAnyInjector) context.getApplicationContext()).anyInjector().inject(target);
    }

    public static void inject(@NonNull Activity activity) {
        injectWith(activity, activity);
    }

    public static void inject(@NonNull View view) {
        injectWith(view.getContext(), view);
    }
}
