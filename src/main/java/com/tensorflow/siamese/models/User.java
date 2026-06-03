package com.tensorflow.siamese.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_name", columnList = "name"),
    @Index(name = "idx_users_created_at", columnList = "created_at")
})
@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class User {

    @Id
    @GeneratedValue
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @NonNull
    private UUID id;

    @NonNull
    @Column(unique = true, columnDefinition = "VARCHAR(45)")
    private String name;

    private int numImages;

    // Store as JSON (backward compatible). Cast the bound String to jsonb on
    // write, since Postgres will not implicitly convert varchar -> jsonb.
    @Column(columnDefinition = "JSONB")
    @ColumnTransformer(write = "?::jsonb")
    private String embedding;

    // ✅ Store as pgvector for native similarity search.
    // Held as the pgvector text literal "[v1,v2,...]" (same format as the JSON
    // array, which pgvector accepts). The @ColumnTransformer casts the bound
    // String to the vector type on write, since Hibernate 5.3 has no native
    // binding for the pgvector "vector" column type.
    @Column(columnDefinition = "vector(128)")
    @ColumnTransformer(write = "?::vector")
    private String embeddingVector;

    @NonNull
    @CreatedDate
    @Column(name = "created_at")
    private Instant created;

    @LastModifiedDate
    @Column(name = "modified_at")
    private Instant modified;
}
