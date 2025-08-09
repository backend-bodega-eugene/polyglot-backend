// package main

// import (
// 	"eugene-go/gset"
// 	"fmt"
// )

// func main() {
// 	// 创建非并发安全的 int 集合
// 	a := gset.New[int](false, 1, 2, 3)
// 	b := gset.New[int](false, 3, 4, 5)

// 	// 基本操作
// 	a.Add(10)
// 	fmt.Println(a.Has(2)) // true
// 	a.Remove(1)
// 	fmt.Println(a.ToSlice()) // [2 3 10]

// 	// 集合运算
// 	u := gset.Union(a, b)
// 	i := gset.Intersect(a, b)
// 	d := gset.Diff(a, b)

// 	fmt.Println("Union:", u.ToSlice())     // 并集
// 	fmt.Println("Intersect:", i.ToSlice()) // 交集
// 	fmt.Println("Diff:", d.ToSlice())      // 差集
// }
