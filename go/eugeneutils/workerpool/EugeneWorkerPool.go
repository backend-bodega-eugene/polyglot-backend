package workerpool

import (
	"fmt"
	"sync"
)

type Task struct {
	Handler func(interface{})
	Param   interface{}
}

type WorkerPool struct {
	taskQueue chan Task
	wg        sync.WaitGroup
}

func NewWorkerPool(workerCount int) *WorkerPool {
	pool := &WorkerPool{
		taskQueue: make(chan Task),
	}
	for i := 0; i < workerCount; i++ {
		go pool.worker(i)
	}
	return pool
}

func (p *WorkerPool) worker(id int) {
	for task := range p.taskQueue {
		fmt.Printf("Worker %d 正在处理任务...\n", id)
		task.Handler(task.Param) // 👈 执行带参任务
		p.wg.Done()
	}
}

func (p *WorkerPool) Submit(handler func(interface{}), param interface{}) {
	p.wg.Add(1)
	p.taskQueue <- Task{
		Handler: handler,
		Param:   param,
	}
}

func (p *WorkerPool) Shutdown() {
	p.wg.Wait()
	close(p.taskQueue)
}
