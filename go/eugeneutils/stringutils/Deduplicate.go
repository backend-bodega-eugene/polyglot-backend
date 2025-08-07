package stringutils

// DeduplicateString removes duplicate characters from a string while preserving their original order.
// It supports full Unicode characters, including emojis and Chinese characters.
// The function returns a new string containing only the first occurrence of each character.
func DeduplicateString(s string) string {
	seen := make(map[rune]bool)
	var result []rune

	for _, ch := range s {
		if !seen[ch] {
			seen[ch] = true
			result = append(result, ch)
		}
	}
	return string(result)
}

// EugeneDeduplicateString is Eugene's personalized version of string deduplication.
// Like DeduplicateString, it removes duplicate runes from the input string while keeping their order.
// The naming style is custom, but the core logic is equivalent.
func EugeneDeduplicateString(s string) string {
	isSee := make(map[rune]bool)
	var results []rune

	for _, cha := range s {
		if !isSee[cha] {
			isSee[cha] = true
			results = append(results, cha)
		}
	}
	return string(results)
}
