package com.example.entrymoudle;

import com.example.mannotation.Factory;

@Factory(
        id = "A",
        type = Amoudle.class
)
public class Amoudle implements Meal {
    @Override
    public int getPrice() {
        return 1;
    }
}
