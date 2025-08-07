package stringutils

import (
	"testing"
)

// TestReverseFunctions tests both Reverse and EugeneReverse functions.
// It covers cases with ASCII, spaces, emojis, Chinese characters, and mixed scripts.
func TestReverseFunctions(t *testing.T) {
	tests := []struct {
		name     string // 描述测试用例名称
		input    string // 输入字符串
		expected string // 预期反转结果
	}{
		{"ASCII", "hello", "olleh"},                      // 普通英文
		{"With space", "a b c", "c b a"},                 // 含空格
		{"Emoji", "🐱🐶123", "321🐶🐱"},                   // emoji 混合数字
		{"Chinese", "老胡最帅", "帅最胡老"},               // 中文字符
		{"Mixed", "Go语言123", "321言语oG"},              // 中英混排
		{"Single char", "X", "X"},                        // 单个字符
		{"Empty", "", ""},                                // 空字符串
	}

	for _, tt := range tests {
		t.Run("Reverse_"+tt.name, func(t *testing.T) {
			result := Reverse(tt.input)
			if result != tt.expected {
				t.Errorf("Reverse(%q) = %q; want %q", tt.input, result, tt.expected)
			}
		})

		t.Run("EugeneReverse_"+tt.name, func(t *testing.T) {
			result := EugeneReverse(tt.input)
			if result != tt.expected {
				t.Errorf("EugeneReverse(%q) = %q; want %q", tt.input, result, tt.expected)
			}
		})
	}
}
