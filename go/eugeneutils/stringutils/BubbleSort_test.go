package stringutils

import (
	"reflect"
	"testing"
)

// TestBubbleSortFunctions tests both BubbleSortInt and EugeneBubbleSortInt functions.
// It checks various integer slices to ensure they are correctly sorted in ascending order.
func TestBubbleSortFunctions(t *testing.T) {
	// Define test cases with input and expected sorted result.
	tests := []struct {
		name     string // Name of the test case (used in subtest name)
		input    []int  // Input slice to sort
		expected []int  // Expected sorted result
	}{
		{"Simple", []int{5, 3, 8, 1}, []int{1, 3, 5, 8}},
		{"Already sorted", []int{1, 2, 3}, []int{1, 2, 3}},
		{"Reversed", []int{9, 7, 5, 3, 1}, []int{1, 3, 5, 7, 9}},
		{"With duplicates", []int{2, 2, 1, 3}, []int{1, 2, 2, 3}},
		{"Empty", []int{}, []int{}},     // Empty slice (not nil)
		{"Single", []int{42}, []int{42}}, // Single-element slice
	}

	// Loop through each test case
	for _, tt := range tests {
		// Subtest for BubbleSortInt
		t.Run("BubbleSortInt_"+tt.name, func(t *testing.T) {
			// Create a copy of the input to avoid mutating the original
			input := make([]int, len(tt.input))
			copy(input, tt.input)

			// Run BubbleSortInt
			BubbleSortInt(input)

			// Compare result with expected
			if !reflect.DeepEqual(input, tt.expected) {
				t.Errorf("BubbleSortInt(%v) = %v; want %v", tt.input, input, tt.expected)
			}
		})

		// Subtest for EugeneBubbleSortInt (Eugene 的冒泡版)
		t.Run("EugeneBubbleSortInt_"+tt.name, func(t *testing.T) {
			// Create a copy of the input
			input := make([]int, len(tt.input))
			copy(input, tt.input)

			// Run EugeneBubbleSortInt
			EugeneBubbleSortInt(input)

			// Compare result with expected
			if !reflect.DeepEqual(input, tt.expected) {
				t.Errorf("EugeneBubbleSortInt(%v) = %v; want %v", tt.input, input, tt.expected)
			}
		})
	}
}
