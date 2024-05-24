package com.openclassrooms.tourguide.dto;

public class AttractionDto {
   private String  attractionName;
   private double latitude;
   private double longitude;
   private  double latitudeUser;
   private double longitudeUser;
   private double distance;

    public double getLatitudeUser() {
        return latitudeUser;
    }

    public void setLatitudeUser(double latitudeUser) {
        this.latitudeUser = latitudeUser;
    }

    public double getLongitudeUser() {
        return longitudeUser;
    }

    public void setLongitudeUser(double longitudeUser) {
        this.longitudeUser = longitudeUser;
    }
    private double rewardPoints;

    public double getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(double rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public AttractionDto(String attractionName, double latitude, double longitude, double latitudeUser, double longitudeUser, double distance, double rewardPoints) {
        this.attractionName = attractionName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.latitudeUser = latitudeUser;
        this.longitudeUser = longitudeUser;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }

    public AttractionDto() {

    }
}
