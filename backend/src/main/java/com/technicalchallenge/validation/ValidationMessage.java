package com.technicalchallenge.validation;

/**
 * Represents a single validation message (either ERROR, WARNING, or INFO)
 * created during trade validation.
 *
 * <p>This is a simple immutable data class.
 *
 * @param type    The category of the message, e.g. ERROR or WARNING.
 * @param message The human-readable description of the rule violation.
 */

public record ValidationMessage(String type, String message) {

}
