package com.example.langsync.ui.matches;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.langsync.R;
import com.example.langsync.databinding.FragmentMatchesBinding;
import com.example.langsync.util.JSONObjectUtilities;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MatchesFragment extends Fragment {

    private static final String TAG = "MatchesFragment";
    private FragmentMatchesBinding binding;
    private CardView matchCard;
    private CardView loadingView;
    private ImageView dislikeMatch;
    private ImageView likeMatch;
    private ImageView langsyncSpinner;

    ArrayList<JSONObject> matches = new ArrayList<>();
    private TextView matchName;
    private TextView noMatchesText;
    private TextView interestedLanguages;
    private TextView proficientLanguages;
    private TextView matchAge;
    private TextView matchInterests;
    private ImageView matchProfileImage;

    private String userId;

    // ChatGPT Usage: No
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("loggedUserId", null);

        binding = FragmentMatchesBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        matchCard = root.findViewById(R.id.match_card);
        langsyncSpinner = root.findViewById(R.id.langsync_spinner);
        loadingView = root.findViewById(R.id.loading_view);
        noMatchesText = root.findViewById(R.id.no_matches_text);
        interestedLanguages = root.findViewById(R.id.match_interested);
        proficientLanguages = root.findViewById(R.id.match_proficiencies);
        matchAge = root.findViewById(R.id.match_age);
        matchInterests = root.findViewById(R.id.match_interests);
        matchProfileImage = root.findViewById(R.id.match_img);

        dislikeMatch = root.findViewById(R.id.dislike_match);
        dislikeMatch.setOnClickListener(v -> matchCardAnim(-1));
        likeMatch = root.findViewById(R.id.like_match);
        likeMatch.setOnClickListener(v -> matchCardAnim(1));

        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setDuration(3000);
        langsyncSpinner.startAnimation(rotateAnimation);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getString(R.string.base_url) + "recommendations/" + userId)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "Error getting recommendations");
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject responseBody = new JSONObject(response.body().string());
                        JSONArray recommendedUsersList = responseBody.getJSONArray("recommendedUsersList");
                        for (int i = 0; i < recommendedUsersList.length(); i++) {
                            Log.d(TAG, recommendedUsersList.getJSONObject(i).toString());
                            matches.add(recommendedUsersList.getJSONObject(i));
                        }
                        requireActivity().runOnUiThread(() -> {
                            matchName = matchCard.findViewById(R.id.match_name);
                            if (!matches.isEmpty()) {
                                try {
                                    JSONObject match = matches.get(0);
                                    Log.d(TAG, match.toString());
                                    fillMatchInformation(match);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "Error setting match info");
                                }
                                langsyncSpinner.clearAnimation();
                                loadingView.setVisibility(View.GONE);
                                langsyncSpinner.setVisibility(View.GONE);
                            } else {
                                langsyncSpinner.clearAnimation();
                                matchName.setText("");
                                interestedLanguages.setText("");
                                proficientLanguages.setText("");
                                matchAge.setText("");
                                matchInterests.setText("");
                                matchProfileImage.setImageResource(R.drawable.placeholder_match_img_foreground);
                                matchCard.setVisibility(View.GONE);
                                likeMatch.setVisibility(View.GONE);
                                dislikeMatch.setVisibility(View.GONE);
                                noMatchesText.setVisibility(View.VISIBLE);
                                loadingView.setVisibility(View.VISIBLE);
                                langsyncSpinner.setVisibility(View.VISIBLE);
                            }
                        });
                    } catch (JSONException | IOException e) {
                        Log.d(TAG, "Error getting recommendations");
                        Log.d(TAG, e.toString());
                        e.printStackTrace();
                    }
                }
            }
        });
        return root;
    }

    // ChatGPT Usage: No
    private void matchCardAnim(int direction) {
        matchCard.animate().translationXBy(direction * 500.0f).alpha(0.0f).setDuration(500).withEndAction(() -> {
            matchName = matchCard.findViewById(R.id.match_name);
            matchCard.animate().translationX(0.0f).alpha(1.0f).setDuration(300);
            if (direction == 1) {
                createMatch();
            }
            try {
                if (!matches.isEmpty()) {
                    matches.remove(0);
                    if (!matches.isEmpty()) {
                        JSONObject match = matches.get(0);
                        fillMatchInformation(match);
                    }
                }
                if (matches.isEmpty()) {
                    matchName.setText("");
                    interestedLanguages.setText("");
                    proficientLanguages.setText("");
                    matchAge.setText("");
                    matchInterests.setText("");
                    matchProfileImage.setImageResource(R.drawable.placeholder_match_img_foreground);
                    matchCard.setVisibility(View.GONE);
                    noMatchesText.setVisibility(View.VISIBLE);
                    loadingView.setVisibility(View.VISIBLE);
                    langsyncSpinner.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "Error setting match info");
            }
        });
    }

    private void fillMatchInformation(JSONObject match) throws JSONException {
        JSONArray interestedLanguagesObj = match.getJSONArray("interestedLanguages");
        JSONArray proficientLanguagesObj = match.getJSONArray("proficientLanguages");
        JSONObject matchInterestsObj = match.getJSONObject("interests");
        String pictureUrl = null;
        try {
            pictureUrl = match.getString("picture");
        } catch (Exception e) {
            Log.d(TAG, "User has no profile picture");
        }

        matchName.setText(match.getString("displayName"));
        matchAge.setText(match.getString("age"));
        interestedLanguages.setText(JSONObjectUtilities.jsonArrayToString(interestedLanguagesObj));
        proficientLanguages.setText(JSONObjectUtilities.jsonArrayToString(proficientLanguagesObj));
        matchInterests.setText(JSONObjectUtilities.getInterestsString(matchInterestsObj));

        setBadges(match.getJSONArray("badges"));

        if (pictureUrl != null && !pictureUrl.isEmpty()) {
            Picasso.get().load(pictureUrl).into(matchProfileImage);
        } else {
            matchProfileImage.setImageResource(R.drawable.placeholder_match_img_foreground);
        }
    }

    private void setBadges(JSONArray badges) throws JSONException {
        ImageView matchBadge = matchCard.findViewById(R.id.matching_badge);
        ImageView sessionBadge = matchCard.findViewById(R.id.session_badge);

        ArrayList<String> badgeIds = new ArrayList<>();
        badgeIds.add(getResources().getString(R.string.bronze_match_badge_id));
        badgeIds.add(getResources().getString(R.string.silver_match_badge_id));
        badgeIds.add(getResources().getString(R.string.gold_match_badge_id));
        badgeIds.add(getResources().getString(R.string.bronze_session_badge_id));
        badgeIds.add(getResources().getString(R.string.silver_session_badge_id));
        badgeIds.add(getResources().getString(R.string.gold_session_badge_id));

        Log.d(TAG, "Badges: " + badges.toString());
        Log.d(TAG, "BadgeIds: " + badgeIds);

        ArrayList<String> badgesList = new ArrayList<>();
        for (int i = 0; i < badges.length(); i++) {
            badgesList.add(badges.getString(i));
        }

        if (badgesList.size() == 0) {
            matchBadge.setVisibility(View.GONE);
            sessionBadge.setVisibility(View.GONE);
            return;
        }

        if (badgesList.contains(badgeIds.get(0))) {
            matchBadge.setImageResource(R.drawable.bronze_match_badge);
        } else if (badgesList.contains(badgeIds.get(1))) {
            matchBadge.setImageResource(R.drawable.silver_match_badge);
        } else if (badgesList.contains(badgeIds.get(2))) {
            matchBadge.setImageResource(R.drawable.gold_match_badge);
        } else {
            matchBadge.setVisibility(View.GONE);
        }

        if (badgesList.contains(badgeIds.get(3))) {
            sessionBadge.setImageResource(R.drawable.bronze_session_badge);
        } else if (badgesList.contains(badgeIds.get(4))) {
            sessionBadge.setImageResource(R.drawable.silver_session_badge);
        } else if (badgesList.contains(badgeIds.get(5))) {
            sessionBadge.setImageResource(R.drawable.gold_session_badge);
        } else {
            sessionBadge.setVisibility(View.GONE);
        }

    }

    // ChatGPT Usage: No
    private void createMatch() {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sourceUserId", userId);
            jsonObject.put("targetUserId", matches.get(0).getString("_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(getString(R.string.base_url) + "matches")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "Error creating match");
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Match created");
                }
            }
        });
    }

    // ChatGPT Usage: No
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        matches.clear();
        binding = null;
    }
}