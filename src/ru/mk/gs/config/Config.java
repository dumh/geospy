package ru.mk.gs.config;

import java.net.URL;
import java.util.List;

/**
 * @author mkasumov
 */
public class Config implements Cloneable {

    private URL url;
    private String credentials;
    private String mySubject;
    private List<String> trackedSubjects;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getMySubject() {
        return mySubject;
    }

    public void setMySubject(String mySubject) {
        this.mySubject = mySubject;
    }

    public List<String> getTrackedSubjects() {
        return trackedSubjects;
    }

    public void setTrackedSubjects(List<String> trackedSubjects) {
        this.trackedSubjects = trackedSubjects;
    }

    @Override
    protected final Config clone() {
        try {
            return (Config) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
}
