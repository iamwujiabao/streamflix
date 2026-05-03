package com.streamflix.repository;

import com.streamflix.entity.PlaylistVideo;
import com.streamflix.entity.PlaylistVideo.PlaylistVideoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistVideoRepository extends JpaRepository<PlaylistVideo, PlaylistVideoId> {

    @Query("""
           SELECT pv FROM PlaylistVideo pv
           WHERE pv.playlistId = :playlistId
           ORDER BY pv.position ASC
           """)
    List<PlaylistVideo> findByPlaylist(@Param("playlistId") Long playlistId);

    @Query("SELECT COALESCE(MAX(pv.position), 0) FROM PlaylistVideo pv WHERE pv.playlistId = :pid")
    Integer findMaxPosition(@Param("pid") Long playlistId);
}
