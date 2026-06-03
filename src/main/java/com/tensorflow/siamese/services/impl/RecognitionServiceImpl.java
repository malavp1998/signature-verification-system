package com.tensorflow.siamese.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tensorflow.siamese.models.User;
import com.tensorflow.siamese.repositories.UserRepository;
import com.tensorflow.siamese.services.EmbeddingService;
import com.tensorflow.siamese.services.RecognitionService;
import com.tensorflow.siamese.services.RecognitionResult;
import com.tensorflow.siamese.util.Pair;
import com.tensorflow.siamese.util.VectorUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecognitionServiceImpl implements RecognitionService {

    private static ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmbeddingService embeddingService;

    /**
     * ✅ SIMPLIFIED: pgvector handles similarity search!
     * Uses PostgreSQL native vector similarity search with HNSW indexing
     */
    @Override
    public RecognitionResult recognise(@NonNull Path imagePath, @NonNull Double maxDistanceThr,
            @NonNull Double firstSecondMarginGap) throws Exception {

        log.info("Starting recognition for image: {}", imagePath);

        // 1. Get query embedding (L2-normalized to match the normalized templates)
        List<Double> imageEmb = VectorUtil.l2Normalize(embeddingService.getEmbeddings(imagePath));

        // 2. Convert to pgvector format (JSON string)
        String queryVector = objectMapper.writeValueAsString(imageEmb);

        // 3. ✅ SIMPLE: Let PostgreSQL do the similarity search!
        //    pgvector query handles:
        //    - Euclidean distance calculation
        //    - HNSW index lookup (fast!)
        //    - Sorting by distance
        List<User> topMatches = userRepository.findBySimilarityWithThreshold(
            queryVector,
            maxDistanceThr,
            10  // Top 10 matches
        );

        log.info("Found {} matches within threshold {}", topMatches.size(), maxDistanceThr);

        // 4. Convert to Pair format (User, Distance)
        List<Pair<User, Double>> userMatches = topMatches.stream()
            .map(user -> {
                try {
                    List<Double> userEmb = objectMapper.readValue(
                        user.embedding(), new TypeReference<List<Double>>() {});
                    double distance = VectorUtil.cosineDistance(imageEmb, userEmb);
                    return new Pair<>(user, distance);
                } catch (Exception e) {
                    throw new RuntimeException("Error calculating distance", e);
                }
            })
            .collect(Collectors.toList());

        // 5. Apply second-best margin threshold
        User bestMatch = null;
        if (!userMatches.isEmpty()) {
            double bestDistance = userMatches.get(0).getValue();

            if (userMatches.size() == 1 ||
                (userMatches.get(1).getValue() - bestDistance) >= firstSecondMarginGap) {
                bestMatch = userMatches.get(0).getKey();
                log.info("Best match found: {} with distance {}", bestMatch.name(), bestDistance);
            } else {
                log.info("Ambiguous match: top distance={}, second={}, gap={}",
                    bestDistance, userMatches.get(1).getValue(),
                    userMatches.get(1).getValue() - bestDistance);
            }
        }

        return new RecognitionResult(userMatches, bestMatch);
    }
}
