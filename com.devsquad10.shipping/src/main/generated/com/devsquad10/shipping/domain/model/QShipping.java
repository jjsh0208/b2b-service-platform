package com.devsquad10.shipping.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShipping is a Querydsl query type for Shipping
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShipping extends EntityPathBase<Shipping> {

    private static final long serialVersionUID = -1210498566L;

    public static final QShipping shipping = new QShipping("shipping");

    public final StringPath address = createString("address");

    public final ComparablePath<java.util.UUID> companyShippingManagerId = createComparable("companyShippingManagerId", java.util.UUID.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.util.Date> deadLine = createDateTime("deadLine", java.util.Date.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath deletedBy = createString("deletedBy");

    public final ComparablePath<java.util.UUID> departureHubId = createComparable("departureHubId", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> destinationHubId = createComparable("destinationHubId", java.util.UUID.class);

    public final ListPath<ShippingHistory, QShippingHistory> historyList = this.<ShippingHistory, QShippingHistory>createList("historyList", ShippingHistory.class, QShippingHistory.class, PathInits.DIRECT2);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> orderId = createComparable("orderId", java.util.UUID.class);

    public final StringPath recipientName = createString("recipientName");

    public final StringPath recipientSlackId = createString("recipientSlackId");

    public final StringPath requestDetails = createString("requestDetails");

    public final EnumPath<com.devsquad10.shipping.domain.enums.ShippingStatus> status = createEnum("status", com.devsquad10.shipping.domain.enums.ShippingStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath updatedBy = createString("updatedBy");

    public QShipping(String variable) {
        super(Shipping.class, forVariable(variable));
    }

    public QShipping(Path<? extends Shipping> path) {
        super(path.getType(), path.getMetadata());
    }

    public QShipping(PathMetadata metadata) {
        super(Shipping.class, metadata);
    }

}

