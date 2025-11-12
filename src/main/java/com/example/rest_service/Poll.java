package com.example.rest_service;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polls")
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String question;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "poll_options", joinColumns = @JoinColumn(name = "poll_id"))
    @Column(name = "option_text")
    private List<String> options = new ArrayList<>(); // something like Yes or No or over or under

    //Had to brush up on this again heres my source
    // https://www.w3schools.com/java/java_enums.asp
    @Enumerated(EnumType.STRING)
    private PollStatus status = PollStatus.PENDING;

    private String category;
    private Integer totalBets = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime endsAt;

    public Poll(){}

    public Poll(String question, List<String> options, String category, LocalDateTime endsAt){
        this.question = question;
        this.options = options;
        this.category = category;
        this.endsAt = endsAt;
        this.status = PollStatus.PENDING;
    }

    //getters and setters

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getQuestion(){
        return question;
    }

    public void setQuestion(String question){
        this.question = question;
    }

    public List<String> getOptions(){
        return options;
    }

    public void setOptions(List<String> options){
        this.options = options;
    }

    public PollStatus getStatus() {
        return status;
    }

    public void setStatus(PollStatus status) {
        this.status = status;
    }

    public String getCategory(){
        return category;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public Integer getTotalBets(){
        return totalBets;
    }

    public void setTotalBets(Integer totalBets){
        this.totalBets = totalBets;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public LocalDateTime getEndsAt(){
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt){
        this.endsAt = endsAt;
    }
}