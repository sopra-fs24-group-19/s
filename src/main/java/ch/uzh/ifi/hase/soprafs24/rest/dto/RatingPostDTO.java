package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class RatingPostDTO {

    private int stars;

    private long reviewedId;

    private long reviewerId;

    private String comment;

    private long taskId;

    public void setStars(int stars) { this.stars = stars; }

    public int getStars() { return stars; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public long getReviewerId() { return reviewerId; }

    public void setReviewerId(long reviewerId) { this.reviewerId = reviewerId; }

    public long getReviewedId() { return reviewedId; }

    public void setReviewedId(long reviewedId) { this.reviewedId = reviewedId; }

    public long getTaskId() { return taskId; }

    public void setTaskId(long taskId) { this.taskId = taskId; }
}
