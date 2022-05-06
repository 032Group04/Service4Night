package fr.abitbol.service4night.services;

import androidx.annotation.Nullable;

public class ElectricityService extends Service {
    private double price;
    public static final String NAME = "Electricity";
    public ElectricityService(double _price) {
        super(NAME);
        price = _price;
    }
    public ElectricityService(){
        super(ELECTRICITY_SERVICE);
        price = 0;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean matchFilter(Service filter) {
        return filter instanceof ElectricityService;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);

    }
}
