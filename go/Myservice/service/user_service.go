package service

import (
	"Myservice/model"
	"strconv"
)

var users = make(map[int]model.User)
var idCounter = 1

func CreateUser(name string) model.User {
	user := model.User{
		ID:   idCounter,
		Name: name,
	}
	users[idCounter] = user
	idCounter++
	return user
}

func GetUsers() []model.User {
	result := []model.User{}
	for _, u := range users {
		result = append(result, u)
	}
	return result
}

func GetUser(id string) (model.User, bool) {
	uid, _ := strconv.Atoi(id)
	user, ok := users[uid]
	return user, ok
}

func DeleteUser(id string) {
	uid, _ := strconv.Atoi(id)
	delete(users, uid)
}
