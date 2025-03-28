package com.devsquad10.shipping.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QShippingAgent is a Querydsl query type for ShippingAgent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShippingAgent extends EntityPathBase<ShippingAgent> {

    private static final long serialVersionUID = -517522133L;

    public static final QShippingAgent shippingAgent = new QShippingAgent("shippingAgent");

    public final NumberPath<Integer> assignmentCount = createNumber("assignmentCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> createdBy = createComparable("createdBy", java.util.UUID.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> deletedBy = createComparable("deletedBy", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> hubId = createComparable("hubId", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final BooleanPath isTransit = createBoolean("isTransit");

    public final ComparablePath<java.util.UUID> shippingManagerId = createComparable("shippingManagerId", java.util.UUID.class);

    public final StringPath shippingManagerSlackId = createString("shippingManagerSlackId");

    public final NumberPath<Integer> shippingSequence = createNumber("shippingSequence", Integer.class);

    public final EnumPath<com.devsquad10.shipping.domain.enums.ShippingAgentType> type = createEnum("type", com.devsquad10.shipping.domain.enums.ShippingAgentType.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> updatedBy = createComparable("updatedBy", java.util.UUID.class);

    public QShippingAgent(String variable) {
        super(ShippingAgent.class, forVariable(variable));
    }

    public QShippingAgent(Path<? extends ShippingAgent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QShippingAgent(PathMetadata metadata) {
        super(ShippingAgent.class, metadata);
    }

}

