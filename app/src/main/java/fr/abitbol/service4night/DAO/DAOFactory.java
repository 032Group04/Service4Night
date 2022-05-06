package fr.abitbol.service4night.DAO;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.QuerySnapshot;

public class DAOFactory {
    private DAOFactory(){

    }
    public static LocationDAO getDAO(OnCompleteListener<QuerySnapshot> context){
        // TODO : gérer hors ligne ici

        return new FirestoreLocationDAO(context);
    }

    public static LocationDAO getDAO(OnCompleteListener<Void> context, boolean listened){
        return new FirestoreLocationDAO(context,listened);
    }
}
