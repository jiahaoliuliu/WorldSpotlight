package com.worldspotlightapp.android.maincontroller.modules.organizermodule;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.organizermodule.response.OrganizerModuleOrganizerResponse;
import com.worldspotlightapp.android.model.Organizer;

import java.util.Observer;

/**
 * Created by jiahaoliuliu on 15/9/13.
 */
public class OrganizerModuleObserver extends AbstractOrganizerModuleObservable {

    @Override
    public void retrieveOrganizerInfo(Observer observer, String organizerObjectId) {
        addObserver(observer);

        ParseQuery<Organizer> organizerQuery = ParseQuery.getQuery(Organizer.class);
        organizerQuery.getInBackground(organizerObjectId, new GetCallback<Organizer>() {
            @Override
            public void done(Organizer organizer, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    OrganizerModuleOrganizerResponse organizerModuleOrganizerResponse =
                            new OrganizerModuleOrganizerResponse(parseResponse, organizer);
                    setChanged();
                    notifyObservers(organizerModuleOrganizerResponse);
                } else {
                    OrganizerModuleOrganizerResponse organizerModuleOrganizerResponse =
                            new OrganizerModuleOrganizerResponse(parseResponse, null);
                    setChanged();
                    notifyObservers(organizerModuleOrganizerResponse);
                }
            }
        });
    }
}
