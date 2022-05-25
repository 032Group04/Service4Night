package fr.abitbol.service4night.DAO;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.QuerySnapshot;

public class DAOFactory {
    private DAOFactory(){

    }

    public static LocationDAO getLocationDAOOffline(OnCompleteListener<QuerySnapshot> context){
        // TODO : gérer hors ligne ici

        return null;
    }

    public static LocationDAO getLocationDAOOnline(){
        return FirestoreLocationDAO.getInstance();
    }
}
