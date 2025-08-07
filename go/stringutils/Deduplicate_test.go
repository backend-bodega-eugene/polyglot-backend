package stringutils

import (
	"testing"
)

// TestDeduplicateString tests both DeduplicateString and EugeneDeduplicateString functions.
// It ensures that duplicated characters are removed while preserving the original order.
// Includes cases with ASCII, whitespace, emojis, Chinese characters, and edge cases.
func TestDeduplicateString(t *testing.T) {
	tests := []struct {
		name     string // Descriptive test case name
		input    string // Input string to process
		expected string // Expected result after deduplication
	}{
		{"Basic ASCII", "hello", "helo"},
		{"With spaces", "a a b b", "a b"},
		{"All unique", "abcde", "abcde"},
		{"With emojis", "🐱🐱🐶🐶", "🐱🐶"},
		{"With Chinese", "老胡老胡最帅最帅", "老胡最帅"},
		{"Empty string", "", ""},
	}

	for _, tt := range tests {
		// Subtest for the standard version
		t.Run("DeduplicateString_"+tt.name, func(t *testing.T) {
			result := DeduplicateString(tt.input)
			if result != tt.expected {
				t.Errorf("DeduplicateString(%q) = %q; want %q", tt.input, result, tt.expected)
			}
		})

		// Subtest for Eugene's version
		t.Run("EugeneDeduplicateString_"+tt.name, func(t *testing.T) {
			result := EugeneDeduplicateString(tt.input)
			if result != tt.expected {
				t.Errorf("EugeneDeduplicateString(%q) = %q; want %q", tt.input, result, tt.expected)
			}
		})
	}
}
