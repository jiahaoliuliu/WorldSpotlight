package com.worldspotlightapp.android.maincontroller.modules.citymodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.City;
import com.worldspotlightapp.android.model.Organizer;

import java.util.List;

/**
 * Created by jiahaoliuliu on 2/20/15.
 */
public class CityModuleOrganizersListResponse extends BaseModuleResponse {

    private List<Organizer> mOrganizersList;

    public CityModuleOrganizersListResponse(ParseResponse parseResponse, List<Organizer> organizersList) {
        super(parseResponse);
        this.mOrganizersList = organizersList;
    }

    public List<Organizer> getOrganizersList() {
        return mOrganizersList;
    }
}
