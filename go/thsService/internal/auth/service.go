package auth

import (
	"errors"
	"fmt"
	"net/mail"
	"regexp"
	"strings"
	"time"
	"unicode/utf8"

	"golang.org/x/crypto/bcrypt"
)

var (
	ErrInvalidAccount     = errors.New("account must be a Chinese mobile number or an email address")
	ErrInvalidRealName    = errors.New("real name length must be between 2 and 64 characters")
	ErrInvalidPassword    = errors.New("密码至少6个字符且最多72字节，不限制字符类型")
	ErrInvalidCredentials = errors.New("invalid account or password")
	ErrUserDisabled       = errors.New("user is disabled")
)

var mainlandPhonePattern = regexp.MustCompile(`^1[3-9]\d{9}$`)
var dummyPasswordHash = func() []byte {
	hash, err := bcrypt.GenerateFromPassword([]byte("dummy-password"), bcrypt.DefaultCost)
	if err != nil {
		panic(err)
	}
	return hash
}()

type Service struct {
	repository *Repository
	tokens     *TokenIssuer
	expiresIn  int
}

func NewService(repository *Repository, tokens *TokenIssuer, expiresIn int) *Service {
	return &Service{repository: repository, tokens: tokens, expiresIn: expiresIn}
}

func (s *Service) Register(request RegisterRequest) (AuthData, error) {
	account := strings.TrimSpace(request.Account)
	realName := strings.TrimSpace(request.RealName)

	phone, email, normalizedAccount, err := normalizeAccount(account)
	if err != nil {
		return AuthData{}, err
	}
	if length := utf8.RuneCountInString(realName); length < 2 || length > 64 {
		return AuthData{}, ErrInvalidRealName
	}
	if err := ValidateMemberPassword(request.Password); err != nil {
		return AuthData{}, ErrInvalidPassword
	}

	passwordHash, err := bcrypt.GenerateFromPassword([]byte(request.Password), bcrypt.DefaultCost)
	if err != nil {
		return AuthData{}, fmt.Errorf("hash password: %w", err)
	}

	user := User{
		Phone:        phone,
		Email:        email,
		RealName:     realName,
		PasswordHash: string(passwordHash),
		Status:       UserStatusActive,
	}
	if err := s.repository.Create(&user); err != nil {
		return AuthData{}, err
	}

	accessToken, err := s.tokens.Issue(user.ID)
	if err != nil {
		return AuthData{}, err
	}

	return buildAuthData(user, normalizedAccount, phone != nil, accessToken, s.expiresIn), nil
}

func (s *Service) Login(request LoginRequest) (AuthData, error) {
	account := strings.TrimSpace(request.Account)
	phone, email, normalizedAccount, err := normalizeAccount(account)
	if err != nil {
		bcrypt.CompareHashAndPassword(dummyPasswordHash, []byte(request.Password))
		return AuthData{}, ErrInvalidCredentials
	}

	user, err := s.repository.FindByAccount(phone, email)
	if err != nil {
		if errors.Is(err, ErrUserNotFound) {
			bcrypt.CompareHashAndPassword(dummyPasswordHash, []byte(request.Password))
			return AuthData{}, ErrInvalidCredentials
		}
		return AuthData{}, err
	}
	if err := bcrypt.CompareHashAndPassword([]byte(user.PasswordHash), []byte(request.Password)); err != nil {
		return AuthData{}, ErrInvalidCredentials
	}
	if user.Status != UserStatusActive {
		return AuthData{}, ErrUserDisabled
	}

	loggedInAt := time.Now()
	if err := s.repository.UpdateLastLogin(user.ID, loggedInAt); err != nil {
		return AuthData{}, fmt.Errorf("update last login: %w", err)
	}

	accessToken, err := s.tokens.Issue(user.ID)
	if err != nil {
		return AuthData{}, err
	}

	return buildAuthData(user, normalizedAccount, phone != nil, accessToken, s.expiresIn), nil
}

func buildAuthData(user User, account string, phone bool, accessToken string, expiresIn int) AuthData {
	return AuthData{
		AccessToken: accessToken,
		TokenType:   "Bearer",
		ExpiresIn:   expiresIn,
		User: UserResponse{
			ID:       user.ID,
			Account:  maskAccount(account, phone),
			RealName: user.RealName,
		},
	}
}

func normalizeAccount(account string) (*string, *string, string, error) {
	if mainlandPhonePattern.MatchString(account) {
		return &account, nil, account, nil
	}

	normalizedEmail := strings.ToLower(account)
	parsed, err := mail.ParseAddress(normalizedEmail)
	if err != nil || parsed.Address != normalizedEmail || len(normalizedEmail) > 254 {
		return nil, nil, "", ErrInvalidAccount
	}
	return nil, &normalizedEmail, normalizedEmail, nil
}

// ValidateMemberPassword is shared by registration and administrator actions.
func ValidateMemberPassword(password string) error {
	if !utf8.ValidString(password) || utf8.RuneCountInString(password) < 6 || len(password) > 72 {
		return ErrInvalidPassword
	}
	return nil
}

func NormalizeMemberAccount(account string) (*string, *string, error) {
	phone, email, _, err := normalizeAccount(strings.TrimSpace(account))
	return phone, email, err
}

func maskAccount(account string, phone bool) string {
	if phone {
		return account[:3] + "****" + account[7:]
	}

	parts := strings.SplitN(account, "@", 2)
	local := parts[0]
	if len(local) <= 2 {
		return local[:1] + "***@" + parts[1]
	}
	return local[:2] + "***@" + parts[1]
}
