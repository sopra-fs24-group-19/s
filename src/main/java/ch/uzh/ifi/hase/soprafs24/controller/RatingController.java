package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class RatingController {
    private final RatingService ratingService;

    RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public void createRating(@RequestBody RatingPostDTO ratingPostDTO, @RequestHeader("Authorization") String token) {
        ratingService.createReview(ratingPostDTO, token);
    }
}