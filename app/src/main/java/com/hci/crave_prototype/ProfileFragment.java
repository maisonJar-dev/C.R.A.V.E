package com.hci.crave_prototype;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        Button editButton = view.findViewById(R.id.editButton);
        Button settingsButton = view.findViewById(R.id.settingsButton);

        editButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        return view;
    }
}
