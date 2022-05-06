package fr.abitbol.service4night.services;

import androidx.annotation.Nullable;

public class DrainService extends Service {
    public static final String NAME = "Drainage";
    private boolean blackWater;

    public DrainService(boolean _blackWater) {
        super(NAME);
        blackWater = _blackWater;
    }

    public boolean isBlackWater() {
        return blackWater;
    }

    @Override
    public boolean matchFilter(Service filter) {
        if (!(filter instanceof DrainService)){
            return false;
        }
        return ((DrainService) filter).isBlackWater() == blackWater;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!( super.equals(obj))){
            return false;
        }
        return ((DrainService) obj).blackWater == blackWater;
    }
}
