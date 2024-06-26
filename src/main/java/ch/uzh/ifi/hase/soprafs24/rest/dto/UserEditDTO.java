package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class UserEditDTO {

    private String name;
    private String username;
    private String address;
    private String latitude;
    private String longitude;
    private String phoneNumber;
    private float radius;


    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public String getLatitude() { return latitude; }

    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }

    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getPhoneNumber() { return phoneNumber; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public float getRadius() { return radius; }

    public void setRadius(float radius) { this.radius = radius; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }
}
