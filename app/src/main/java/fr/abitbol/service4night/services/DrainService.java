package fr.abitbol.service4night.services;

public class DrainService extends Service {

    private boolean blackWater;

    public DrainService(boolean _blackWater) {
        super(DRAINAGE_SERVICE);
        blackWater = _blackWater;
    }

    public boolean isBlackWater() {
        return blackWater;
    }
}
