package main

import (
	"Myservice/router"
)

func main() {
	r := router.SetupRouter()
	r.Run(":8080")
}
