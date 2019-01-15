package io.github.isuru.oasis.services.services.backend.model;

import java.util.List;

public class JarListInfo {

    private String address;
    private List<FlinkJar> files;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<FlinkJar> getFiles() {
        return files;
    }

    public void setFiles(List<FlinkJar> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "JarListInfo{" +
                "address='" + address + '\'' +
                ", files=" + files +
                '}';
    }
}
