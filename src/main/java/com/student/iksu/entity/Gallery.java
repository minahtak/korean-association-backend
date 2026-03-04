package com.student.iksu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
// import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Gallery {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String writer;

    @Column(length = 5000)
    private String imageUrls;

    private LocalDateTime regDate;
    private int viewCount = 0;
    private int likes = 0;

    @OneToMany(mappedBy = "gallery", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("regDate asc")
    // @SQLRestriction("parent_id is null")
    @Where(clause = "parent_id is null")
    @JsonIgnoreProperties({"gallery", "info", "material", "parent"})
    private List<Comment> comments = new ArrayList<>();


    @Transient
    @JsonProperty("isLiked") // JSON으로 나갈 때 이름을 "isLiked"로 고정합니다.
    private boolean isLiked = false;

    public void updateGallery(String title, String content, String imageUrls) {
        this.title = title;
        this.content = content;
        if(imageUrls != null) this.imageUrls = imageUrls;
    }

    public List<String> getImageUrlList() {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(imageUrls.split(","));
    }
}