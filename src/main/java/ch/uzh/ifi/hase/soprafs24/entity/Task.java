package ch.uzh.ifi.hase.soprafs24.entity;
import javax.persistence.*;
import java.io.Serializable;
import ch.uzh.ifi.hase.soprafs24.constant.TaskStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@Entity
@Table(name = "TASK")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private int duration;

    @Column(nullable = true)
    private String latitude;

    @Column(nullable = true)
    private String longitude;

    @Enumerated
    @Column(nullable=false)
    private TaskStatus status;

    @ManyToOne
    @JoinColumn(name = "creatorId", referencedColumnName = "id")
    private User creator;

    @ManyToOne
    @JoinColumn(name = "helperId", referencedColumnName = "id")
    private User helper;

    @ManyToMany(mappedBy = "applications")
    private List<User> candidates = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos;

    @OneToMany(mappedBy = "task")
    private List<Rating> ratings;

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public int getPrice() { return price; }

    public void setPrice(int price) { this.price = price; }

    public TaskStatus getStatus() { return status; }

    public void setStatus(TaskStatus status) { this.status = status; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public String getLatitude() { return latitude; }

    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }

    public void setLongitude(String longitude) { this.longitude = longitude; }

    public Date getDate() { return date; }

    public void setDate(Date date) { this.date = date; }

    public User getCreator() { return creator; }

    public void setCreator(User creator) { this.creator = creator; }

    public User getHelper() { return helper; }

    public void setHelper(User helper) { this.helper = helper; }

    public List<User> getCandidates() { return candidates; }

    public void setCandidates(List<User> candidates) { this.candidates = candidates; }

    public int getDuration() { return duration; }

    public void setDuration(int duration) { this.duration = duration; }

    public void addCandidate(User user) {
        if (!candidates.contains(user)) {
            candidates.add(user);
            user.getApplications().add(this);
        }
    }

    public boolean hasCandidate(User candidate) {
        return candidates.contains(candidate);
    }


}