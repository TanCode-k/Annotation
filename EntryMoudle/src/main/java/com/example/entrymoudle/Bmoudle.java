package com.example.entrymoudle;

import com.example.mannotation.Factory;

@Factory(
        id = "b",
        type = Bmoudle.class
)
public class Bmoudle implements Meal {
    @Override
    public int getPrice() {
        return 2;
    }
}
