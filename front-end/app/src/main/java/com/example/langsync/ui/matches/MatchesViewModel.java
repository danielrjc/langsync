package com.example.langsync.ui.matches;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MatchesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    // ChatGPT Usage: No
    public MatchesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}