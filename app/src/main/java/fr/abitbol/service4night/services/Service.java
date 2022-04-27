package fr.abitbol.service4night.services;

import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import fr.abitbol.service4night.Location;

public abstract class Service {

    public static final String WATER_SERVICE = "Water";
    public static final String ELECTRICITY_SERVICE = "Electricity";
    public static final String DUMPSTER_SERVICE = "Dumpster";
    public static final String INTERNET_SERVICE = "Internet";
    public static final String DRAINAGE_SERVICE = "Drainage";
    public static final String BATHROOM_SERVICE = "Bathroom";
    private String label;
    public Service(String _label){
        label = _label;


    }

    public String getLabel() {
        return label;
    }
    @Override
    public boolean equals(@Nullable Object obj) {

        if (this == obj){
            return true;
        }
        if (!(obj.getClass().isAssignableFrom(Service.class) )){
            return false;
        }
        else{
            return label.equals(((Service) obj).getLabel());
        }


    }

    @NonNull
    @Override
    public String toString() {
        return (label + " service type.");
    }

    public static class Builder{
        private static final String TAG = "Service.Builder logging";
        public static Map<String,Service> buildServices(Map<String,Map<String,Object>> map){
            Map<String,Service> services = new HashMap<>();
            if (map.containsKey(Service.WATER_SERVICE)){
                Map<String,Object> waterMap = map.get(Service.WATER_SERVICE);
                services.put(Service.WATER_SERVICE,new WaterService(((double)waterMap.get("price")),((boolean) waterMap.get("drinkable"))));

            }
            if (map.containsKey(Service.ELECTRICITY_SERVICE)){
                services.put(Service.ELECTRICITY_SERVICE,new ElectricityService(((double)map.get(Service.ELECTRICITY_SERVICE).get("price"))));
            }
            if (map.containsKey(Service.INTERNET_SERVICE)){
                Map<String,Object> interMap = map.get(Service.INTERNET_SERVICE);
                String connection = (String) interMap.get("connectionType");
                Log.i(TAG, "buildServices: connection type = "+ connection);
                services.put(Service.INTERNET_SERVICE,new InternetService(InternetService.ConnectionType.valueOf(connection),((double)interMap.get("price")) ));
            }
            if (map.containsKey(Service.DRAINAGE_SERVICE)){
                Map<String,Object> drainMap = map.get(Service.DRAINAGE_SERVICE);
                services.put(Service.DRAINAGE_SERVICE,new DrainService((Boolean) drainMap.get("blackWater")));
            }
            if (map.containsKey(Service.DUMPSTER_SERVICE)){
                services.put(Service.DUMPSTER_SERVICE,new DumpService());
            }
            return services;
        }
    }

}
