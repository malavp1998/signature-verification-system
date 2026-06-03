package com.tensorflow.siamese.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Small helpers for working with embedding vectors.
 */
public final class VectorUtil {

    private VectorUtil() {}

    /**
     * Return an L2-normalized (unit-length) copy of the vector.
     * After this, the vector's Euclidean norm is 1, so cosine distance and
     * Euclidean distance produce the same ranking.
     */
    public static List<Double> l2Normalize(List<Double> v) {
        double sumSq = 0.0;
        for (double x : v) {
            sumSq += x * x;
        }
        double norm = Math.sqrt(sumSq);
        if (norm == 0.0) {
            // avoid divide-by-zero; return a copy of the original (all-zero) vector
            return new ArrayList<>(v);
        }
        List<Double> out = new ArrayList<>(v.size());
        for (double x : v) {
            out.add(x / norm);
        }
        return out;
    }

    /**
     * Cosine distance = 1 - cosine similarity. Matches pgvector's "<=>" operator.
     * Range: 0 (identical direction) .. 2 (opposite direction).
     */
    public static double cosineDistance(List<Double> a, List<Double> b) throws Exception {
        if (a.size() != b.size()) {
            throw new Exception("Both embeddings should be of same length.");
        }
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            na += a.get(i) * a.get(i);
            nb += b.get(i) * b.get(i);
        }
        if (na == 0.0 || nb == 0.0) {
            return 1.0;
        }
        return 1.0 - (dot / (Math.sqrt(na) * Math.sqrt(nb)));
    }
}
