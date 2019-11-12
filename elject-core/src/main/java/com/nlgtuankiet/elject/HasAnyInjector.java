package com.nlgtuankiet.elject;

import dagger.android.AndroidInjector;

public interface HasAnyInjector {
    AndroidInjector<Object> anyInjector();
}
