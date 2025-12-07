package kw.manager.core.csv;

public class DataBean {
    private int id;
    private String name;
    private String resourceName;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public String toString() {
        return "DataBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", resourceName='" + resourceName + '\'' +
                '}';
    }
}
