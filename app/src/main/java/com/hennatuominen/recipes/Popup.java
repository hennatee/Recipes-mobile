package com.hennatuominen.recipes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class Popup extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(width, height);

        Button filterButton = findViewById(R.id.popupApplyFiltersButton);
        filterButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK,
                    new Intent().putStringArrayListExtra("filters", getSelectedFilters()));
            finish();
        });

        ImageView closeButton = findViewById(R.id.popupCloseButton);
        closeButton.setOnClickListener(v -> finish());
    }

    public ArrayList<String> getSelectedFilters() {
        ArrayList<String> selectedFilters = new ArrayList<>();

        ChipGroup chipGroup1 = findViewById(R.id.chipGroupMainIngredient);
        List<Integer> chipIds1 = chipGroup1.getCheckedChipIds();
        for (Integer id : chipIds1) {
            Chip chip = chipGroup1.findViewById(id);
            selectedFilters.add(chip.getText().toString());
        }
        ChipGroup chipGroup2 = findViewById(R.id.chipGroupMeal);
        List<Integer> chipIds2 = chipGroup2.getCheckedChipIds();
        for (Integer id : chipIds2) {
            Chip chip = chipGroup2.findViewById(id);
            selectedFilters.add(chip.getText().toString());
        }
        ChipGroup chipGroup3 = findViewById(R.id.chipGroupSides);
        List<Integer> chipIds3 = chipGroup3.getCheckedChipIds();
        for (Integer id : chipIds3) {
            Chip chip = chipGroup3.findViewById(id);
            selectedFilters.add(chip.getText().toString());
        }
        ChipGroup chipGroup4 = findViewById(R.id.chipGroupSpecial);
        List<Integer> chipIds4 = chipGroup4.getCheckedChipIds();
        for (Integer id : chipIds4) {
            Chip chip = chipGroup4.findViewById(id);
            selectedFilters.add(chip.getText().toString());
        }
        return selectedFilters;
    }
}
