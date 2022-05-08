package fr.abitbol.service4night.services;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.abitbol.service4night.DAO.LocationDAO;

public abstract class Service implements Parcelable {

    public static final String WATER_SERVICE = "Water";
    public static final String ELECTRICITY_SERVICE = "Electricity";
    public static final String DUMPSTER_SERVICE = "Dumpster";
    public static final String INTERNET_SERVICE = "Internet";
    public static final String DRAINAGE_SERVICE = "Drainage";
    public static final String BATHROOM_SERVICE = "Bathroom";
    private static final String TAG = "Service logging";
    private String label;
    public Service(String _label){
        label = _label;
    }

    public String getLabel() {
        return label;
    }
    //TODO : reflechir a une interface Filterable
    public abstract boolean matchFilter(Service filter);
    public abstract Map<Object,Object> getAsMap();
    public abstract Bundle getAsBundle();
    @Override
    public boolean equals(@Nullable Object obj) {
        Log.i(TAG, "equals called in Service class");
        if (obj == null){
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj.getClass().isAssignableFrom(Service.class))) {
            return false;
        } else {
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
        public static Map<String,Service> buildServices(Bundle bundle){
            Log.i(TAG, "buildServices: bundle version called");
            Map<String,Service> services = new HashMap<>();
            if (bundle.containsKey(Service.WATER_SERVICE)){
                Log.i(TAG, "buildServices: building water service'");
                services.put(Service.WATER_SERVICE,(WaterService)bundle.getParcelable(WATER_SERVICE));
            }else Log.i(TAG, "buildServices: no water service");
            if (bundle.containsKey(Service.ELECTRICITY_SERVICE)){
                Log.i(TAG, "buildServices: building elec service'");
                services.put(Service.ELECTRICITY_SERVICE,(ElectricityService)bundle.getParcelable(ELECTRICITY_SERVICE));
            }else Log.i(TAG, "buildServices: no elec service");
            if (bundle.containsKey(Service.INTERNET_SERVICE)){
                Log.i(TAG, "buildServices: building internet service'");
                services.put(Service.INTERNET_SERVICE,(InternetService)bundle.getParcelable(INTERNET_SERVICE));
            }else Log.i(TAG, "buildServices: no internet service");
            if (bundle.containsKey(Service.DRAINAGE_SERVICE)){
                Log.i(TAG, "buildServices: building drain service'");

                services.put(Service.DRAINAGE_SERVICE,(DrainService)bundle.getParcelable(DRAINAGE_SERVICE));
            }else Log.i(TAG, "buildServices: no drain service");
            if (bundle.containsKey(Service.DUMPSTER_SERVICE)){
                Log.i(TAG, "buildServices: building Dumpster service'");
                services.put(Service.DUMPSTER_SERVICE,new DumpService());
            }else Log.i(TAG, "buildServices: no dump service");
            return services;
        }
        public static Map<String,Service> buildServices(List<Service> serviceList){
            Log.i(TAG, "buildServices: List version called");
            Map<String,Service> services = new HashMap<>();
            for (Service s : serviceList){
                if (s instanceof WaterService){
                    Log.i(TAG, "buildServices: found instance of WaterService");
                    services.put(s.getLabel(),(WaterService) s);
                    continue;
                }
                if (s instanceof ElectricityService){
                    Log.i(TAG, "buildServices: found instance of ElectricityService");
                    services.put(s.getLabel(),(ElectricityService) s);
                    continue;
                }
                if (s instanceof DrainService){
                    Log.i(TAG, "buildServices: found instance of DrainService");
                    services.put(s.getLabel(),(DrainService) s);
                    continue;
                }
                if (s instanceof DumpService){
                    Log.i(TAG, "buildServices: found instance of DumpService");
                    services.put(s.getLabel(),(DumpService) s);
                    continue;
                }
                if (s instanceof InternetService){
                    Log.i(TAG, "buildServices: found instance of InternetService");
                    services.put(s.getLabel(),(InternetService) s);

                }
            }
            return services;
        }
    }


}
