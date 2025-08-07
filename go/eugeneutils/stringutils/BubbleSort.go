package stringutils

// BubbleSortInt performs an in-place bubble sort on a slice of integers.
// The input slice will be sorted in ascending order.
// It modifies the original slice directly.
func BubbleSortInt(nums []int) {
	n := len(nums)
	for i := 0; i < n-1; i++ {
		for j := 0; j < n-i-1; j++ {
			if nums[j] > nums[j+1] {
				nums[j], nums[j+1] = nums[j+1], nums[j]
			}
		}
	}
}

// EugeneBubbleSortInt is Eugene's personalized implementation of bubble sort.
// It also performs in-place sorting of the input slice in ascending order.
// Functionally identical to BubbleSortInt, but uses different variable naming for style.
func EugeneBubbleSortInt(nums []int) {
	number := len(nums)
	for index := 0; index < number-1; index++ {
		for jack := 0; jack < number-index-1; jack++ {
			if nums[jack] > nums[jack+1] {
				nums[jack], nums[jack+1] = nums[jack+1], nums[jack]
			}
		}
	}
}
