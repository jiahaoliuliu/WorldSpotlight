package com.worldspotlightapp.android.maincontroller.modules.organizermodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Organizer;

/**
 * Created by jiahaoliuliu on 15/9/14.
 */
public class OrganizerModuleOrganizerResponse extends BaseModuleResponse {

    private Organizer mOrganizer;

    public OrganizerModuleOrganizerResponse(ParseResponse parseResponse, Organizer organizer) {
        super(parseResponse);
        this.mOrganizer = organizer;
    }

    public Organizer getOrganizer() {
        return mOrganizer;
    }
}
