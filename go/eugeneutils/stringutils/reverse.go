package stringutils

// Reverse 反转字符串 s，支持 Unicode 字符（如中文、emoji 等）。
// 返回一个新的字符串，其字符顺序与输入字符串相反。
// 例如：Reverse("老胡最帅") 返回 "帅最胡老"。
func Reverse(s string) string {
	runes := []rune(s) // 将字符串转换为 rune 切片，避免中文/emoji 出现乱码
	for i, j := 0, len(runes)-1; i < j; i, j = i+1, j-1 {
		runes[i], runes[j] = runes[j], runes[i]
	}
	return string(runes)
}

// EugeneReverse 是 Reverse 的自定义实现版本，用不同变量命名方式实现相同逻辑。
// 同样支持 Unicode 字符，确保多字节字符不会被截断。
func EugeneReverse(s string) string {
	runes := []rune(s)
	for index, jack := 0, len(runes)-1; index < jack; index, jack = index+1, jack-1 {
		runes[index], runes[jack] = runes[jack], runes[index]
	}
	return string(runes)
}
