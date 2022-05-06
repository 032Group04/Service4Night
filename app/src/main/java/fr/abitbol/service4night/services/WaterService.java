package fr.abitbol.service4night.services;

import androidx.annotation.Nullable;

import java.util.Map;

public class WaterService extends Service {
    private boolean drinkable;
    private double price;
    public static final String NAME = "Water";
    public static final String FILTER_DRINKABLE = "drinkable";
    public WaterService(double _price, boolean _drinkable) {
        super(NAME);
        price = _price;
        drinkable = _drinkable;
    }
    public WaterService(boolean _drinkable){
        super(NAME);
        drinkable = _drinkable;
        price = 0;
    }

    public boolean isDrinkable() {
        return drinkable;
    }

    public void setDrinkable(boolean drinkable) {
        this.drinkable = drinkable;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }



    @Override
    public boolean matchFilter(Service filter) {
        if (!(filter instanceof WaterService)){
            return false;
        }
        if (((WaterService) filter).isDrinkable() != drinkable){
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!( super.equals(obj))){
            return false;
        }
        return ((WaterService) obj).drinkable == drinkable;
    }
}
