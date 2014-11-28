package org.monroe.team.runit.app.uc.entity;

public class ApplicationData {

    public final String name;
    public final String packageName;

    public ApplicationData(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationData data = (ApplicationData) o;

        if (name != null ? !name.equals(data.name) : data.name != null) return false;
        if (packageName != null ? !packageName.equals(data.packageName) : data.packageName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        return result;
    }

    public String getUniqueName() {
        return packageName+":"+name;
    }
}
