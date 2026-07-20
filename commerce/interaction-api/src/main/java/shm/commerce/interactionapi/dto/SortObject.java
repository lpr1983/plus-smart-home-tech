package shm.commerce.interactionapi.dto;

public class SortObject {
    private String direction;
    private String nullHandling;
    private Boolean ascending;
    private String property;
    private Boolean ignoreCase;

    public SortObject() {
    }

    public SortObject(String direction, String nullHandling, Boolean ascending,
                      String property, Boolean ignoreCase) {
        this.direction = direction;
        this.nullHandling = nullHandling;
        this.ascending = ascending;
        this.property = property;
        this.ignoreCase = ignoreCase;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getNullHandling() {
        return nullHandling;
    }

    public void setNullHandling(String nullHandling) {
        this.nullHandling = nullHandling;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Boolean getIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(Boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
}
