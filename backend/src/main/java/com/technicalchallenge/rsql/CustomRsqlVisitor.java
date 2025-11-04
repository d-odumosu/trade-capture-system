package com.technicalchallenge.rsql;

import cz.jirutka.rsql.parser.ast.*;
import org.springframework.data.jpa.domain.Specification;

/**
 * Custom RSQL Visitor that converts parsed RSQL AST nodes into Spring Data JPA Specifications.
 * @param <T> the entity type (e.g., Trade)
 */
public class CustomRsqlVisitor<T> implements RSQLVisitor<Specification<T>, Void> {

    private final GenericRsqlSpecBuilder<T> builder;

    public CustomRsqlVisitor() {
        this.builder = new GenericRsqlSpecBuilder<>();
    }

    @Override
    public Specification<T> visit(AndNode node, Void param) {
        return builder.createSpecification(node);
    }

    @Override
    public Specification<T> visit(OrNode node, Void param) {
        return builder.createSpecification(node);
    }

    @Override
    public Specification<T> visit(ComparisonNode node, Void param) {
        return builder.createSpecification(node);
    }
}
