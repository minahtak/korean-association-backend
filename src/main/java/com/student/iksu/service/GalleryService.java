package com.student.iksu.service;

import com.student.iksu.dto.request.GalleryFormDto;
import com.student.iksu.entity.Gallery;
import com.student.iksu.entity.Likes;
import com.student.iksu.entity.Member; // ★ 여기 Member 임포트 추가됨!
import com.student.iksu.repository.GalleryRepository;
import com.student.iksu.repository.LikesRepository;
import com.student.iksu.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;
    private final LikesRepository likesRepository;
    private final MemberRepository memberRepository;

    // 1. 목록 조회
    @Transactional(readOnly = true)
    public Page<Gallery> getGalleryList(Pageable pageable, String username) {
        Page<Gallery> page = galleryRepository.findAll(pageable);

        if (username != null) {
            Member member = memberRepository.findByUsername(username).orElse(null);
            if (member != null) {
                page.forEach(gallery -> {
                    // 이제 Gallery 엔티티에 isLiked 필드가 있으므로 에러 안 남
                    boolean liked = likesRepository.existsByGalleryAndMember(gallery, member);
                    gallery.setLiked(liked);
                });
            }
        }
        return page;
    }

    // 2. 갤러리 저장 (간소화됨)
    public void saveGallery(GalleryFormDto dto) {
        Gallery gallery = new Gallery();
        gallery.setTitle(dto.getTitle());
        gallery.setContent(dto.getContent());
        gallery.setWriter(dto.getWriter());
        gallery.setRegDate(LocalDateTime.now());
        gallery.setViewCount(0);
        gallery.setLikes(0);

        // 프론트에서 받은 URL 리스트를 콤마로 연결해서 저장
        if (dto.getGoogleDriveLinks() != null && !dto.getGoogleDriveLinks().isEmpty()) {
            String imageUrls = String.join(",", dto.getGoogleDriveLinks());
            gallery.setImageUrls(imageUrls);
        }

        galleryRepository.save(gallery);
    }

    // 3. 상세 조회
    @Transactional(readOnly = true)
    public Gallery findById(Long id, String username) {
        Gallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        if (username != null) {
            Member member = memberRepository.findByUsername(username).orElse(null);
            if (member != null) {
                gallery.setLiked(likesRepository.existsByGalleryAndMember(gallery, member));
            }
        }
        return gallery;
    }

    // 단순 조회 (내부용)
    public Gallery findById(Long id) {
        return galleryRepository.findById(id).orElseThrow();
    }

    // 4. 조회수 증가
    public void updateViewCount(Long id) {
        Gallery gallery = findById(id);
        gallery.setViewCount(gallery.getViewCount() + 1);
    }

    // 5. 좋아요 토글
    public boolean toggleLike(Long galleryId, String username) {
        Gallery gallery = findById(galleryId);
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Optional<Likes> existingLike = likesRepository.findByGalleryAndMember(gallery, member);

        if (existingLike.isPresent()) {
            likesRepository.delete(existingLike.get());
            gallery.setLikes(Math.max(0, gallery.getLikes() - 1));
            return false;
        } else {
            Likes newLike = new Likes(gallery, member);
            likesRepository.save(newLike);
            gallery.setLikes(gallery.getLikes() + 1);
            return true;
        }
    }

    // 6. 수정
    public void update(Long id, GalleryFormDto dto) {
        Gallery gallery = findById(id);

        String embedUrls = null;
        if (dto.getGoogleDriveLinks() != null && !dto.getGoogleDriveLinks().isEmpty()) {
            embedUrls = String.join(",", dto.getGoogleDriveLinks());
        } else if (dto.getImageUrls() != null) {
            embedUrls = dto.getImageUrls();
        }

        gallery.updateGallery(dto.getTitle(), dto.getContent(), embedUrls);
    }

    // 7. 삭제
    public void delete(Long id) {
        galleryRepository.deleteById(id);
    }

    // 8. 수정 폼용 DTO 조회
    @Transactional(readOnly = true)
    public GalleryFormDto getGalleryDtl(Long id) {
        Gallery gallery = findById(id);
        GalleryFormDto dto = new GalleryFormDto();
        dto.setId(gallery.getId());
        dto.setTitle(gallery.getTitle());
        dto.setContent(gallery.getContent());
        dto.setWriter(gallery.getWriter());
        dto.setImageUrls(gallery.getImageUrls());
        dto.setGoogleDriveLinks(gallery.getImageUrlList());
        return dto;
    }
}