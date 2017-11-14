package com.example.alex.quick;

/**
 * Created by Alex on 2017/5/27.
 */

public class Place {
    private double latitude;
    private double longitude;
    private String id;
    private String name;
    private String place_id;
    private String price_level;
    private String rating;
    private String photo_reference;
    private String vicinity;
    private String type;



    private Boolean isOpen;

    public Place(double latitude, double longitude, String id, String name, String place_id, String price_level, String rating, String photo_reference, String vicinity, String type, Boolean isOpen) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.name = name;
        this.place_id = place_id;
        this.price_level = price_level;
        this.rating = rating;
        this.photo_reference = photo_reference;
        this.vicinity = vicinity;
        this.type = type;
        this.isOpen = isOpen;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getPrice_level() {
        return price_level;
    }

    public void setPrice_level(String price_level) {
        this.price_level = price_level;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPhoto_reference() {
        return photo_reference;
    }

    public void setPhoto_reference(String photo_reference) {
        this.photo_reference = photo_reference;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Boolean open) {
        isOpen = open;
    }
}
