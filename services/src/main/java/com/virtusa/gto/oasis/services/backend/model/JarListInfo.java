package com.virtusa.gto.oasis.services.backend.model;

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
}
