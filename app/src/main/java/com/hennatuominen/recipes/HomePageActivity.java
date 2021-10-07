package com.hennatuominen.recipes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;


import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";
    private static final int GALLERY_REQUEST_CODE = 123;
    private static final int FILTER_REQUEST_CODE = 100;

    private Context context;

    private FirebaseFirestore mFirestore;
    private CollectionReference mRecipeReference;
    private ArrayAdapter mRecipeArrayAdapter;

    private List<Recipe> ownRecipes;
    private List<Recipe> searchedRecipes;
    private List<Recipe> filteredRecipes;
    private List<Recipe> favoriteRecipes;
    private List<Recipe> selectedList;
    private ArrayList<String> filters;
    private ListView mRecipeListView;
    private ImageView imageView;
    private String recipeToDelete;
    private TextView recipeAmountText;
    private boolean showFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        context = this;
        mFirestore = FirebaseFirestore.getInstance();
        mRecipeReference = mFirestore.collection("ht_recipes");

        ownRecipes = new ArrayList<>();
        filteredRecipes = new ArrayList<>();
        filters = new ArrayList<>();
        favoriteRecipes = new ArrayList<>();
        selectedList = new ArrayList<>();
        //selected list is either ownRecipes (all user recipes) or favoriteRecipes
        selectedList = ownRecipes;
        mRecipeListView = findViewById(R.id.listViewRecipes);
        recipeAmountText = findViewById(R.id.textViewRecipeAmount);


        FloatingActionButton fabAdd = findViewById(R.id.floatingButtonAdd);
        fabAdd.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.colorPrimary));
        fabAdd.setOnClickListener(v -> createNewRecipeDialog());

        ImageView buttonFilter = findViewById(R.id.filterButton);
        buttonFilter.setOnClickListener(v -> {
            resetFilters();
            getFilteredRecipes();
        });

        showFavorites = false;
        ImageView buttonFavorites = findViewById(R.id.favoritesButton);
        buttonFavorites.setImageResource(R.drawable.ic_heart_borders);
        buttonFavorites.setOnClickListener(v -> toggleFavorite(buttonFavorites));

        EditText editTextSearch = findViewById(R.id.editTextSearchOwn);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    mRecipeArrayAdapter = new RecipeArrayAdapter(context, R.layout.recipe_item, selectedList, mRecipeReference);
                    recipeAmountText.setText("Recipes ("+selectedList.size()+")");
                } else {
                    searchedRecipes = new ArrayList<>();
                    for (Recipe recipe : selectedList) {
                        if (recipe.getName().contains(s)) {
                            searchedRecipes.add(recipe);
                        }
                    }
                    mRecipeArrayAdapter = new RecipeArrayAdapter(context, R.layout.recipe_item, searchedRecipes, mRecipeReference);
                    recipeAmountText.setText("Searched recipes ("+searchedRecipes.size()+")");
                }
                mRecipeArrayAdapter.notifyDataSetChanged();
                mRecipeListView.setAdapter(mRecipeArrayAdapter);

            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        setListOnLongPressListener();
        changeQuery();
    }

    private void getFilteredRecipes() {
        Intent intent = new Intent(HomePageActivity.this, Popup.class);
        startActivityForResult(intent, FILTER_REQUEST_CODE);
    }


    public void changeQuery() {
        mFirestore.collection("ht_recipes").addSnapshotListener((value, error) -> {
            Log.d(TAG, "Changes in database");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            ownRecipes.clear();
            if(value != null) {
                for (DocumentSnapshot snapshot : value) {
                    Recipe recipe = snapshot.toObject(Recipe.class);
                    if (recipe.getUserEmail().equals(user.getEmail())) {
                        ownRecipes.add(recipe);
                    }

                }
                getFavoriteRecipes();
                Log.d(TAG, "Own recipes size: "+ownRecipes.size());
                mRecipeArrayAdapter = new RecipeArrayAdapter(context, R.layout.recipe_item, selectedList, mRecipeReference);
                mRecipeArrayAdapter.notifyDataSetChanged();
                mRecipeListView.setAdapter(mRecipeArrayAdapter);
                recipeAmountText.setText("Recipes ("+selectedList.size()+")");
                Log.d(TAG, "Array adapter set");
            }
        });
    }

    public void setFavoriteRecipesToArrayAdapter() {
        getFavoriteRecipes();
        mRecipeArrayAdapter = new RecipeArrayAdapter(context, R.layout.recipe_item, favoriteRecipes, mRecipeReference);
        mRecipeArrayAdapter.notifyDataSetChanged();
        mRecipeListView.setAdapter(mRecipeArrayAdapter);
        recipeAmountText.setText("Favorite recipes ("+favoriteRecipes.size()+")");
    }

    public void getFavoriteRecipes() {
        favoriteRecipes.clear();
        if (ownRecipes.size() > 0) {
            for (Recipe recipe : ownRecipes) {
                if (recipe.getUserFavorite()) {
                    favoriteRecipes.add(recipe);
                }
            }
        }
    }

    public void createNewRecipeDialog() {
        try {
            BottomSheetDialog bottomSheetDialog1 = new BottomSheetDialog(
                    HomePageActivity.this, R.style.BottomSheetDialogTheme);
            View bottomSheetView1 = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.layout_bottom_sheet_first, findViewById(R.id.bottomSheetContainer));


            imageView = bottomSheetView1.findViewById(R.id.imageViewRecipe);
            imageView.setImageResource(R.drawable.example_food);

            Button pickImageButton = bottomSheetView1.findViewById(R.id.pickImageButton);
            pickImageButton.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);
            });

            bottomSheetDialog1.setContentView(bottomSheetView1);
            bottomSheetDialog1.show();

            bottomSheetView1.findViewById(R.id.bottomSheetNextButton)
                    .setOnClickListener(v -> {
                        bottomSheetDialog1.hide();
                        BottomSheetDialog bottomSheetDialog2;
                        bottomSheetDialog2 = new BottomSheetDialog(
                                HomePageActivity.this, R.style.BottomSheetDialogTheme);
                        View bottomSheetView2 = LayoutInflater.from(getApplicationContext())
                                .inflate(R.layout.bottom_sheet_second, findViewById(R.id.bottomSheetContainer));
                        bottomSheetDialog2.setContentView(bottomSheetView2);
                        bottomSheetDialog2.show();

                        bottomSheetView2.findViewById(R.id.save_recipe_button)
                                .setOnClickListener(v1 -> {
                                    submitRecipeForm(bottomSheetView2);
                                    bottomSheetDialog1.cancel();
                                    bottomSheetDialog2.cancel();
                                });
                        bottomSheetView2.findViewById(R.id.bottomSheetBackButton)
                                .setOnClickListener(v12 -> {
                                    bottomSheetDialog2.hide();
                                    bottomSheetDialog1.show();
                                });
                        bottomSheetDialog2.findViewById(R.id.bottomSheetCloseButton)
                                .setOnClickListener(v13 -> {
                                    bottomSheetDialog1.cancel();
                                    bottomSheetDialog2.cancel();
                                });
                    });
        } catch (Exception e) {
            Log.d(TAG,  e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageData = data.getData();
            imageView.setImageURI(imageData);
        }
        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                filters = data.getStringArrayListExtra("filters");
                if (filters.size() > 0) {
                    setFilteredRecipesToArrayAdapter(filters);
                }
                Log.d(TAG, "Filters: "+data.getStringArrayListExtra("filters").size());
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }

        }
    }

    public void setFilteredRecipesToArrayAdapter(ArrayList<String> selectedFilters) {
        for (Recipe recipe : selectedList) {
            if (recipe.getCategories().containsAll(selectedFilters)) {
                filteredRecipes.add(recipe);
            }
        }
        mRecipeArrayAdapter = new RecipeArrayAdapter(context, R.layout.recipe_item, filteredRecipes, mRecipeReference);
        mRecipeArrayAdapter.notifyDataSetChanged();
        mRecipeListView.setAdapter(mRecipeArrayAdapter);
        recipeAmountText.setText("Filtered recipes ("+filteredRecipes.size()+")");
    }

    public void resetFilters() {
        filters.clear();
        filteredRecipes.clear();
        mRecipeArrayAdapter = new RecipeArrayAdapter(context, R.layout.recipe_item, selectedList, mRecipeReference);
        mRecipeArrayAdapter.notifyDataSetChanged();
        mRecipeListView.setAdapter(mRecipeArrayAdapter);
        recipeAmountText.setText("Recipes ("+selectedList.size()+")");
    }

    private String imageToUpload() {
        Bitmap img = convertImageViewToBitmap(imageView);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] byteArray = stream.toByteArray();
        String result = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return result;
    }

    public Bitmap convertImageViewToBitmap(ImageView iv) {
        Bitmap bitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        return bitmap;
    }

    public void submitRecipeForm(View bottomSheetView) {
        EditText editName = bottomSheetView.findViewById(R.id.editTextRecipeName);
        EditText editSource = bottomSheetView.findViewById(R.id.editTextRecipeSource);
        EditText editUrl = bottomSheetView.findViewById(R.id.editTextRecipeUrl);
        EditText editTime = bottomSheetView.findViewById(R.id.editTextRecipeTime);

        String name = editName.getText().toString();
        String source = editSource.getText().toString();
        String url = editUrl.getText().toString();
        String time = editTime.getText().toString();

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Recipe recipe = new Recipe(name, userEmail);
        if (!source.equals("")) {
            recipe.setSource(source);
        }
        if (!url.equals("")) {
            recipe.setUrl(url);
        }

        if (!time.trim().equalsIgnoreCase("")) {
            int prepTime = Integer.valueOf(time);
            recipe.setPreparationTime(prepTime);
        }

        ArrayList<String> categories = new ArrayList<>();
        ChipGroup chipGroup = bottomSheetView.findViewById(R.id.chipGroupMain);
        List<Integer> chipIds = chipGroup.getCheckedChipIds();
        for (Integer id : chipIds) {
            Chip chip = chipGroup.findViewById(id);
            categories.add(chip.getText().toString());
        }
        if (categories.size() > 0) {
            recipe.setCategories(categories);
        }

        recipe.setImageEncoded(imageToUpload());

        try {
            mRecipeReference.document(recipe.getName()).set(recipe);
            Log.d(TAG, "New recipe created");
            Toast.makeText(context, "New recipe: "+recipe.getName()+" added", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

    }

    public void removeRecipe(String recipeName) {
        mFirestore.collection("ht_recipes").document(recipeName)
                .delete()
                .addOnSuccessListener(unused -> {
                    Recipe toDelete = null;
                    for (Recipe recipe : ownRecipes) {
                        if (recipe.getName().equals(recipeName)) {
                            toDelete = recipe;
                        }
                    }
                    ownRecipes.remove(toDelete);
                    getFavoriteRecipes();
                    mRecipeArrayAdapter = new RecipeArrayAdapter(context, R.layout.recipe_item, selectedList, mRecipeReference);
                    mRecipeArrayAdapter.notifyDataSetChanged();
                    mRecipeListView.setAdapter(mRecipeArrayAdapter);
                    recipeAmountText.setText("Recipes ("+selectedList.size()+")");
                    Log.d(TAG, "Recipe successfully deleted!");
                    Toast.makeText(context, recipeName+" recipe deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error deleting document", e);
                    Toast.makeText(context, "Error, try again!", Toast.LENGTH_SHORT).show();
                });
        recipeToDelete = "";
    }

    private void setListOnLongPressListener(){
        mRecipeListView.setOnItemLongClickListener((parent, view, position, id) -> {
            recipeToDelete = selectedList.get(position).getName();
            AlertDialog dialog = ConfirmDeletion(recipeToDelete);
            dialog.show();
            return true;
        });
    }

    public AlertDialog ConfirmDeletion(String recipeName) {

        AlertDialog deleteDialogBox = new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete "+recipeName+" recipe?")
                .setPositiveButton("Delete", (dialog, whichButton) -> {
                    removeRecipe(recipeName);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    finish();
                    startActivity(getIntent());
                    dialog.dismiss();
                })
                .create();
        return deleteDialogBox;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logOutItem:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "User logged out");
                    finish();
                });
    }

    public void toggleFavorite(ImageView v) {
        if (!showFavorites) {
            v.setImageResource(R.drawable.ic_heart_filled_grey);
            setFavoriteRecipesToArrayAdapter();
            showFavorites = true;
            selectedList = favoriteRecipes;
        } else {
            v.setImageResource(R.drawable.ic_heart_borders);
            mRecipeArrayAdapter = new RecipeArrayAdapter(context, R.layout.recipe_item, ownRecipes, mRecipeReference);
            mRecipeArrayAdapter.notifyDataSetChanged();
            mRecipeListView.setAdapter(mRecipeArrayAdapter);
            recipeAmountText.setText("Recipes ("+ownRecipes.size()+")");
            showFavorites = false;
            selectedList = ownRecipes;
        }
    }


}