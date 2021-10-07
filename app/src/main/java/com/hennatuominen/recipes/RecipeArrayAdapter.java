package com.hennatuominen.recipes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;

import java.util.List;

public class RecipeArrayAdapter extends ArrayAdapter<Recipe> {

    private static final String TAG = "MyActivity";
    private final Context contextThis;
    private final CollectionReference mRecipeReference;

    public RecipeArrayAdapter(Context context, int resource, List<Recipe> objects, CollectionReference ref) {
        super(context, resource, objects);
        contextThis = context;
        mRecipeReference = ref;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.recipe_item, parent, false);
        }
        TextView nameTextView = convertView.findViewById(R.id.textViewRecipeName);
        ImageView imageView = convertView.findViewById(R.id.imageViewList);
        TextView sourceTextView = convertView.findViewById(R.id.textViewRecipeSource);
        TextView timeTextView = convertView.findViewById(R.id.textViewPrepTime);
        ImageView webPageButton = convertView.findViewById(R.id.goToWebPageButton);
        ImageView favoriteButton = convertView.findViewById(R.id.recipeFavoriteButton);
        Recipe recipe = getItem(position);


        nameTextView.setVisibility(View.VISIBLE);
        nameTextView.setText(""+recipe.getName());

        imageView.setVisibility(View.VISIBLE);
        Bitmap imageBitmap = convertStringToBitmap(recipe.getImageEncoded());
        imageView.setImageBitmap(imageBitmap);

        sourceTextView.setVisibility(View.VISIBLE);
        if (recipe.getSource() != "") {
            sourceTextView.setText(""+recipe.getSource());
        }

        timeTextView.setVisibility(View.VISIBLE);
        if (recipe.getPreparationTime() != 0) {
            timeTextView.setText(recipe.getPreparationTime() +" min");
        }

        favoriteButton.setVisibility(View.VISIBLE);
        toggleFavorite(recipe, favoriteButton);
        favoriteButton.setOnClickListener(v -> {
            if (recipe.getUserFavorite()) {
                mRecipeReference.document(recipe.getName()).update("userFavorite", false);
                favoriteButton.setImageResource(R.drawable.ic_heart_borders);
            } else {
                mRecipeReference.document(recipe.getName()).update("userFavorite", true);
                favoriteButton.setImageResource(R.drawable.ic_heart_filled_grey);
            }



        });

        webPageButton.setVisibility(View.VISIBLE);
        webPageButton.setOnClickListener(v -> {
            try {
                String recipeUrl = recipe.getUrl();
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse((recipeUrl)));
                contextThis.startActivity(i);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }

        });
        return convertView;
    }

    public Bitmap convertStringToBitmap(String string) {
        byte[] byteArray = null;
        byteArray = Base64.decode(string, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return bitmap;
    }

    public void toggleFavorite(Recipe recipe, ImageView v) {
        if (recipe.getUserFavorite()) {
            v.setImageResource(R.drawable.ic_heart_filled_grey);
        } else {
            v.setImageResource(R.drawable.ic_heart_borders);
        }
    }


}
