package com.bloomframe.server.firebase;

import com.google.firebase.FirebaseApp;

public record FirebaseAppHolder(boolean enabled, FirebaseApp app) {

    public static FirebaseAppHolder disabled() {
        return new FirebaseAppHolder(false, null);
    }

    public static FirebaseAppHolder enabled(FirebaseApp app) {
        return new FirebaseAppHolder(true, app);
    }

    public String projectId() {
        return enabled && app != null ? app.getOptions().getProjectId() : null;
    }
}
