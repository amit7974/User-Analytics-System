package com.example.analytics.repository;

import com.example.analytics.entity.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    long count();

    @Query("SELECT COUNT(DISTINCT e.userId) FROM Event e")
    long countDistinctUsers();

    @Query("""
            SELECT e FROM Event e
            WHERE (:event IS NULL OR e.event = :event)
              AND (:from IS NULL OR e.timestamp >= :from)
              AND (:to IS NULL OR e.timestamp <= :to)
            ORDER BY e.timestamp DESC
            """)
    List<Event> findFiltered(@Param("event") String event,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to,
                              Pageable pageable);

    @Query("""
            SELECT e.userId AS userId, COUNT(e) AS total
            FROM Event e
            WHERE (:event IS NULL OR e.event = :event)
              AND (:from IS NULL OR e.timestamp >= :from)
              AND (:to IS NULL OR e.timestamp <= :to)
            GROUP BY e.userId
            ORDER BY total DESC
            """)
    List<UserEventCount> countEventsPerUser(@Param("event") String event,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    @Query("""
            SELECT e.event AS eventName, COUNT(e) AS total
            FROM Event e
            WHERE (:event IS NULL OR e.event = :event)
              AND (:from IS NULL OR e.timestamp >= :from)
              AND (:to IS NULL OR e.timestamp <= :to)
            GROUP BY e.event
            ORDER BY total DESC
            """)
    List<EventNameCount> countByEventName(@Param("event") String event,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);

    List<Event> findByUserId(String userId);

    interface UserEventCount {
        String getUserId();
        Long getTotal();
    }

    interface EventNameCount {
        String getEventName();
        Long getTotal();
    }
}
