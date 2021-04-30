package com.image.imageProccessing.model;

import javax.persistence.*;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_Sequence")
    @SequenceGenerator(name = "image_Sequence", sequenceName = "image_SEQ")
    private Long id;
    @Column(name = "original_name")
    private String originalName;
    @Column(name = "hashed_name")
    private String hashedName;
    @Column(name = "address")
    private String address;

    public Image() {
    }

    public Image(String originalName, String hashedName, String address) {
        this.originalName = originalName;
        this.hashedName = hashedName;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getHashedName() {
        return hashedName;
    }

    public void setHashedName(String hashedName) {
        this.hashedName = hashedName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
