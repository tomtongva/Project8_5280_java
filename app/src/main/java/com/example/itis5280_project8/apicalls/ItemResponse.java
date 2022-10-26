package com.example.itis5280_project8.apicalls;

import com.google.gson.annotations.SerializedName;

public class ItemResponse {
    @SerializedName("items")
    private Item[] itemsArray;

    public Item[] getItemsArray() {
        return itemsArray;
    }

    public void setItemsArray(Item[] itemsArray) {
        this.itemsArray = itemsArray;
    }
}
