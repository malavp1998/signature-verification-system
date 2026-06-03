package com.tensorflow.siamese.repositories;

import com.tensorflow.siamese.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByName(String name);

    /**
     * Find users by similarity to query embedding using pgvector
     * Uses cosine distance (<=> operator) over L2-normalized embeddings
     */
    @Query(value = "SELECT * FROM users WHERE embedding_vector IS NOT NULL " +
                   "ORDER BY embedding_vector <=> CAST(:queryVector AS vector) LIMIT :limit",
        nativeQuery = true)
    List<User> findBySimilarity(
        @Param("queryVector") String queryVector,
        @Param("limit") int limit
    );

    /**
     * Find users within distance threshold using pgvector
     * Uses cosine distance (<=> operator) over L2-normalized embeddings
     */
    @Query(value = "SELECT * FROM users WHERE embedding_vector IS NOT NULL " +
                   "AND (embedding_vector <=> CAST(:queryVector AS vector)) <= :threshold " +
                   "ORDER BY embedding_vector <=> CAST(:queryVector AS vector) LIMIT :limit",
        nativeQuery = true)
    List<User> findBySimilarityWithThreshold(
        @Param("queryVector") String queryVector,
        @Param("threshold") double threshold,
        @Param("limit") int limit
    );

    /**
     * Count users with embeddings
     */
    long countByEmbeddingVectorIsNotNull();
}
