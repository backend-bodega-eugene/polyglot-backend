package main

import (
	"fmt"

	"github.com/gin-gonic/gin"
	"github.com/hashicorp/consul/api"
)

func main() {
	config := api.DefaultConfig()
	config.Address = "localhost:8500"

	client, err := api.NewClient(config)
	if err != nil {
		panic(err)
	}

	leader, err := client.Status().Leader()
	if err != nil {
		panic(err)
	}

	fmt.Println("Consul Leader:", leader)
	registration := &api.AgentServiceRegistration{
		ID:      "user-service-9101",
		Name:    "user-service",
		Address: "host.docker.internal",
		Port:    9101,
		Check: &api.AgentServiceCheck{
			HTTP:     "http://host.docker.internal:9101/health",
			Interval: "10s",
			Timeout:  "3s",
		},
	}

	err = client.Agent().ServiceRegister(registration)
	if err != nil {
		panic(err)
	}

	fmt.Println("user-service registered to consul")
	r := gin.Default()

	r.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"service": "user-service",
			"status":  "UP",
		})
	})

	r.Run(":9101")
}
