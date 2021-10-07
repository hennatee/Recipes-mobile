package com.hennatuominen.recipes;

import java.util.ArrayList;

public class Recipe {

    private String name;
    private String source;
    private String url;
    private String imageEncoded;
    private ArrayList<String> categories;
    private int preparationTime;
    private String userEmail;
    private Boolean userFavorite;
    // jatkokehitystä varten
    private Boolean privateUse;

    public Recipe(String name, String userEmail) {
        this.name = name;
        this.userEmail = userEmail;
        userFavorite = false;
        privateUse = true;
    }

    public Recipe() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageEncoded() {
        return imageEncoded;
    }

    public void setImageEncoded(String imageEncoded) {
        this.imageEncoded = imageEncoded;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public String getUserEmail() {
        return userEmail;
    }


    public Boolean getUserFavorite() {
        return userFavorite;
    }


}
