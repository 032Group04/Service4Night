package fr.abitbol.service4night.services;

import androidx.annotation.Nullable;

public class DumpService extends Service {
    public static final String NAME = "Dumpster";
    public DumpService(){
        super(NAME);
    }

    @Override
    public boolean matchFilter(Service filter) {
        return filter instanceof DumpService;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}
