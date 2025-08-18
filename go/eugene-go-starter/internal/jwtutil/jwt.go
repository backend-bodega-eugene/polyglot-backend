// internal/jwtutil/jwt.go
package jwtutil

import (
	"crypto/rand"
	"encoding/hex"
	"errors"
	"time"

	"eugene-go-starter/pkg/config"

	"github.com/golang-jwt/jwt/v5"
)

type Claims struct {
	UserID   uint64 `json:"uid"`
	SiteID   uint64 `json:"sid"`
	Username string `json:"uname"`
	//Role     string `json:"role,omitempty"`
	jwt.RegisteredClaims
}

type Service struct {
	cfg config.JWT
}

func New(cfg config.JWT) *Service { return &Service{cfg: cfg} }

func (s *Service) key() []byte { return []byte(s.cfg.Secret) }

func (s *Service) newJTI() (string, error) {
	var b [16]byte
	_, err := rand.Read(b[:])
	if err != nil {
		return "", err
	}
	return hex.EncodeToString(b[:]), nil
}

// GenerateAccessToken 返回 token、过期时间、jti
func (s *Service) GenerateAccessToken(uid, sid uint64, uname string) (string, time.Time, string, error) {
	now := time.Now()
	exp := now.Add(s.cfg.AccessTTL)
	jti, err := s.newJTI()
	if err != nil {
		return "", time.Time{}, "", err
	}

	claims := Claims{
		UserID:   uid,
		SiteID:   sid,
		Username: uname,
		//	Role:     role,
		RegisteredClaims: jwt.RegisteredClaims{
			Issuer:    s.cfg.Issuer,
			Audience:  jwt.ClaimStrings{s.cfg.Audience},
			IssuedAt:  jwt.NewNumericDate(now),
			NotBefore: jwt.NewNumericDate(now.Add(-s.cfg.AllowClockSkew)),
			ExpiresAt: jwt.NewNumericDate(exp),
			ID:        jti,
			Subject:   uname,
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	ss, err := token.SignedString(s.key())
	return ss, exp, jti, err
}

func (s *Service) Parse(tokenString string) (*Claims, error) {
	tok, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(t *jwt.Token) (interface{}, error) {
		if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return s.key(), nil
	}, jwt.WithAudience(s.cfg.Audience), jwt.WithIssuer(s.cfg.Issuer), jwt.WithLeeway(s.cfg.AllowClockSkew))
	if err != nil {
		return nil, err
	}
	if !tok.Valid {
		return nil, errors.New("invalid token")
	}
	c, ok := tok.Claims.(*Claims)
	if !ok {
		return nil, errors.New("claims cast failed")
	}
	return c, nil
}

// 刷新：仅示例，实际可加“仅在即将过期 X 分钟内允许刷新”的策略
func (s *Service) Refresh(oldToken string) (string, time.Time, string, error) {
	c, err := s.Parse(oldToken)
	if err != nil {
		return "", time.Time{}, "", err
	}
	return s.GenerateAccessToken(c.UserID, c.SiteID, c.Username)
}
