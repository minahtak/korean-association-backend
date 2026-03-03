package com.student.iksu.repository;

import com.student.iksu.entity.Gallery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryRepository extends JpaRepository<Gallery, Long> {
    // 최신순 조회
    Page<Gallery> findAllByOrderByRegDateDesc(Pageable pageable);
}