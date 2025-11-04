package com.technicalchallenge.rsql;

import cz.jirutka.rsql.parser.ast.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Converts RSQL AST nodes into Spring Data JPA Specifications.
 *
 * @param <T> Entity type (e.g., Trade)
 */
public class GenericRsqlSpecBuilder<T> {

    public Specification<T> createSpecification(Node node) {
        if (node instanceof LogicalNode logicalNode) {
            return buildLogicalSpecification(logicalNode);
        } else if (node instanceof ComparisonNode comparisonNode) {
            return buildComparisonSpecification(comparisonNode);
        }
        return null;
    }

    // Handles AND/OR logic recursively
    private Specification<T> buildLogicalSpecification(LogicalNode logicalNode) {
        List<Specification<T>> specs = logicalNode.getChildren()
                .stream()
                .map(this::createSpecification)// recursion
                .toList(); // 👈 RECURSION HAPPENS HERE
        Specification<T> result = specs.getFirst();
        for (int i = 1; i < specs.size(); i++) {
            if (logicalNode.getOperator() == LogicalOperator.AND) {
                result = result.and(specs.get(i));
            } else if (logicalNode.getOperator() == LogicalOperator.OR) {
                result = result.or(specs.get(i));
            }
        }
        return result;

    }

    private Specification<T> buildComparisonSpecification(ComparisonNode node) {
        return (root, query, cb) -> {
            // Read the pieces of the RSQL comparison node
            String selector = node.getSelector();             // e.g. "book.name"
            String operator = node.getOperator().getSymbol(); // e.g. "==", "=gt=", "=in="
            List<String> args = node.getArguments();          // e.g. ["FXDesk"] or ["NEW","AMENDED"]

//            Path<?> path = getPath(root, selector);


            return null;
        };
    }
}

