package com.devsquad10.shipping.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShippingHistory is a Querydsl query type for ShippingHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShippingHistory extends EntityPathBase<ShippingHistory> {

    private static final long serialVersionUID = -1429607110L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShippingHistory shippingHistory = new QShippingHistory("shippingHistory");

    public final NumberPath<Double> actDist = createNumber("actDist", Double.class);

    public final NumberPath<Integer> actTime = createNumber("actTime", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath deletedBy = createString("deletedBy");

    public final ComparablePath<java.util.UUID> departureHubId = createComparable("departureHubId", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> destinationHubId = createComparable("destinationHubId", java.util.UUID.class);

    public final NumberPath<Double> estDist = createNumber("estDist", Double.class);

    public final NumberPath<Integer> estTime = createNumber("estTime", Integer.class);

    public final EnumPath<com.devsquad10.shipping.domain.enums.ShippingHistoryStatus> historyStatus = createEnum("historyStatus", com.devsquad10.shipping.domain.enums.ShippingHistoryStatus.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final QShipping shipping;

    public final ComparablePath<java.util.UUID> shippingManagerId = createComparable("shippingManagerId", java.util.UUID.class);

    public final NumberPath<Integer> shippingPathSequence = createNumber("shippingPathSequence", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath updatedBy = createString("updatedBy");

    public QShippingHistory(String variable) {
        this(ShippingHistory.class, forVariable(variable), INITS);
    }

    public QShippingHistory(Path<? extends ShippingHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShippingHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShippingHistory(PathMetadata metadata, PathInits inits) {
        this(ShippingHistory.class, metadata, inits);
    }

    public QShippingHistory(Class<? extends ShippingHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shipping = inits.isInitialized("shipping") ? new QShipping(forProperty("shipping")) : null;
    }

}

