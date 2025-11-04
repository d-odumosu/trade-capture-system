package com.technicalchallenge.rsql;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Utility for parsing RSQL queries into Specifications.
 */
@Component
public class RsqlParserService {

    public <T> Specification<T> parse(String search) {
        Node rootNode = new RSQLParser().parse(search); // this takes the raw RSQL text and builds the AST
        //rootNode = the AST (Abstract Syntax Tree) built from RSQL text.
        return rootNode.accept(new CustomRsqlVisitor<>()); //converting the AST into a Specification
    }
}
