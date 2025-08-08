package test

import (
	"eugene-go/workerpool"
	"fmt"
	"testing"
	"time"
)

// func TestWorkerPool(t *testing.T) {
// 	pool := workerpool.NewWorkerPool(3)

// 	for i := 0; i < 10; i++ {
// 		num := i
// 		pool.Submit(func() {
// 			fmt.Printf("任务 %d 正在执行\n", num)
// 			time.Sleep(1 * time.Second)
// 			fmt.Printf("任务 %d 执行完成\n", num)
// 		})
// 	}

// 	pool.Shutdown()
// }
func TestWorkerPoolWithParams(t *testing.T) {
	pool :=workerpool.NewWorkerPool(3)

	for i := 0; i < 10; i++ {
		num := i
		// pool.Submit(func(v interface{}) {
		// 	n := v.(int)
		// 	fmt.Printf("处理参数：%d\n", n)
		// 	time.Sleep(1 * time.Second)
		// 	fmt.Printf("参数 %d 处理完毕\n", n)
		// }, num)
		pool.Submit(eugeneFunc,num)
		pool.Submit(eugeneFuncString,"hey ,go! i am Eugene")
	}

	pool.Shutdown()
}
func eugeneFunc(v interface{}){

		fmt.Printf("处理参数：%d\n", v.(int))
			time.Sleep(1 * time.Second)
			fmt.Printf("参数 %d 处理完毕\n", v.(int))

}
func eugeneFuncString(v interface{}){

		fmt.Printf("处理参数：%s\n", v.(string))
			time.Sleep(1 * time.Second)
			fmt.Printf("参数 %s 处理完毕\n", v.(string))

}
// func TestWorkerPool(t *testing.T) {
// 	eugenPool := workerpool.NewWorkerPool(100)

// 	for index := 0; index < 100; index++ {
// 		eugenPool.Submit(TestThread(index))
// 	}

// }
// func TestThread(num int) {
// 	fmt.Printf("任务 %d 正在执行\n", num)
// 	time.Sleep(3 * time.Second)
// 	fmt.Printf("任务 %d 执行完成\n", num)
// }
