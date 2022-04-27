package fr.abitbol.service4night.services;

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
}
