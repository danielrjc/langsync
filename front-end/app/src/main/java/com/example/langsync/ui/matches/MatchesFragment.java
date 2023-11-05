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
import androidx.lifecycle.ViewModelProvider;

import com.example.langsync.R;
import com.example.langsync.databinding.FragmentMatchesBinding;
import com.example.langsync.util.AuthenticationUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

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

    private String userId;
    private final AuthenticationUtilities utilities = new AuthenticationUtilities(getContext());


    // ChatGPT Usage: No
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("loggedUserId", null);

        MatchesViewModel matchesViewModel =
                new ViewModelProvider(this).get(MatchesViewModel.class);

        binding = FragmentMatchesBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        matchCard = root.findViewById(R.id.match_card);
        langsyncSpinner = root.findViewById(R.id.langsync_spinner);
        loadingView = root.findViewById(R.id.loading_view);
        noMatchesText = root.findViewById(R.id.no_matches_text);
        interestedLanguages = root.findViewById(R.id.match_interested);
        proficientLanguages = root.findViewById(R.id.match_proficiencies);
        TextView interests = root.findViewById(R.id.match_interests);

        dislikeMatch = root.findViewById(R.id.dislike_match);
        dislikeMatch.setOnClickListener(v ->  matchCardAnim(-1));
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
                if(response.isSuccessful()){
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
                                        matchName.setText(match.getString("displayName"));
                                        interestedLanguages.setText(match.getJSONArray("interestedLanguages")
                                                .toString()
                                                .replace(",", ", ")
                                                .replace("[", "")
                                                .replace("]", "")
                                                .replace("\"", ""));
                                        proficientLanguages.setText(match.getJSONArray("proficientLanguages")
                                                .toString()
                                                .replace(",", ", ")
                                                .replace("[", "")
                                                .replace("]", "")
                                                .replace("\"", ""));
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    langsyncSpinner.clearAnimation();
                                    loadingView.setVisibility(View.GONE);
                                    langsyncSpinner.setVisibility(View.GONE);
                                } else {
                                    langsyncSpinner.clearAnimation();
                                    matchName.setText("");
                                    interestedLanguages.setText("");
                                    proficientLanguages.setText("");
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
                        matchName.setText(Objects.requireNonNull(matches.get(0)).getString("displayName"));
                        interestedLanguages.setText(matches.get(0)
                                .getJSONArray("interestedLanguages")
                                .toString()
                                .replace(",", ", ")
                                .replace("[", "")
                                .replace("]", "")
                                .replace("\"", ""));
                        proficientLanguages.setText(matches.get(0)
                                .getJSONArray("proficientLanguages")
                                .toString()
                                .replace(",", ", ")
                                .replace("[", "")
                                .replace("]", "")
                                .replace("\"", ""));
                    }
                }
                if(matches.isEmpty()) {
                    matchName.setText("");
                    interestedLanguages.setText("");
                    proficientLanguages.setText("");
                    matchCard.setVisibility(View.GONE);
                    noMatchesText.setVisibility(View.VISIBLE);
                    loadingView.setVisibility(View.VISIBLE);
                    langsyncSpinner.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ChatGPT Usage: No
    private void createMatch(){
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
                if(response.isSuccessful()){
                    Log.d(TAG, "Match created");
//                    utilities.showToast("Match created");
                }
            }
        });
    }

    // ChatGPT Usage: No
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}