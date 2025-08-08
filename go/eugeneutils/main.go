package main

import (
	"encoding/json"
	"eugene-go/dto"
	"fmt"
	"math/rand"
	"time"
)

// 模拟一个网页爬取函数
func fetchURL(id int, ch chan<- string) {
	// 模拟延迟 1~3 秒
	delay := time.Duration(rand.Intn(3)+1) * time.Second
	time.Sleep(delay)

	ch <- fmt.Sprintf("Worker %d finished after %v", id, delay)
}

func main() {

	// Go struct → JSON 字符串
	//user := dto.User{1, "eugene", "laohu@example.com"}
	user := dto.User{2, "this is eugene", "fff"}
	jsonData, _ := json.Marshal(user)
	fmt.Println(string(jsonData)) // {"name":"老胡","age":35,"email":"laohu@example.com"}

	// JSON 字符串 → Go struct
	jsonStr := `{"name":"Eugene","id":18,"email":"eugene@example.com"}`
	var u dto.User
	json.Unmarshal([]byte(jsonStr), &u)
	fmt.Printf("名字：%s，年龄：%d，邮箱：%s\n", u.Name, u.ID, u.Email)
	// rand.Seed(time.Now().UnixNano())

	// numTasks := 5
	// resultChan := make(chan string) // 用于接收任务结果
	// timeout := 2 * time.Second      // 我们最多只等2秒

	// for i := 1; i <= numTasks; i++ {
	// 	go fetchURL(i, resultChan) // 并发执行多个任务
	// }

	// for i := 0; i < numTasks; i++ {
	// 	select {
	// 	case res := <-resultChan:
	// 		fmt.Println("✅ 收到结果:", res)
	// 	case <-time.After(timeout):
	// 		fmt.Println("⏰ 超时了，有任务太慢了，跳过！")
	// 	}
	// }
	fmt.Println("👻 ---------------------- 👻")
	defer fmt.Println("⏰ ---------------------⏰ ")
	thiseugenech := make(chan string)
	go eugnex(thiseugenech)
	//thisrecieveeugenech := make(chan string)
	go recieveugenex(thiseugenech)
	time.Sleep(3 * 1000)

}
func eugnex(eugenech chan<- string) {

	eugenech <- "go away ,eugene"
}
func recieveugenex(eugenech <-chan string) {
	recieve := <-eugenech
	fmt.Println(recieve)
}

// func eugnex(eugenech chan<- string) {
//     eugenech <- "go away ,eugene"
// }

// func recieveugenex(eugenech <-chan string) {
//     recieve := <-eugenech
//     fmt.Println(recieve)
// }
