package main

import (
	"fmt"
	_ "net/http/pprof" // 导入 pprof
)

func main() {

	laohuin := make(chan int, 10)
	for i := 0; i < 100; i++ {
		testroutine(laohuin, i)
		rcount := <-laohuin
		fmt.Printf("count:%d", rcount)

	}
}

var count int

func testroutine(laohuin chan<- int, f int) {

	laohuin <- count

	go func() {
		int1 := 1
		int2 := 2
		int3 := 3
		int4 := 4
		fmt.Printf("-------------%d------------\n", f)
		fmt.Printf("int4:%d,int3:%d,int2:%d,int1:%d", int4, int3, int2, int1)
		fmt.Printf("-------------%d------------\n", f)

	}()
	count++
	//time.Sleep(2 * time.Second)
}
