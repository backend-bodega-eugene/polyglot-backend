// dto/user_dto.go
package dto

type CreateUserRequest struct {
	Name  string `json:"name"`
	Email string `json:"email"`
}

type CreateUserResponse struct {
	ID int `json:"id"`
}
