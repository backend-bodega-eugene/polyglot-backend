package admin

import (
	"os"
	"regexp"
	"strings"
	"testing"

	"gorm.io/gorm"
	"ths-service/internal/alipay"
)

func testManagementCodeMigration(t *testing.T, db *gorm.DB) {
	t.Helper()
	if e := db.Exec("UPDATE alipay_records SET payload=JSON_SET(payload,'$.manageCode','SIM-I-000000000093') WHERE kind='insurance' AND user_id=93").Error; e != nil {
		t.Fatal(e)
	}
	apply := func(direction string) {
		t.Helper()
		b, e := os.ReadFile("../../migrations/000021_alipay_management_code." + direction + ".sql")
		if e != nil {
			t.Fatal(e)
		}
		for _, statement := range strings.Split(string(b), ";") {
			if strings.TrimSpace(statement) != "" {
				if e := db.Exec(statement).Error; e != nil {
					t.Fatal(e)
				}
			}
		}
	}
	apply("up")
	s := alipay.NewService(db)
	a, e := s.Summary(t.Context(), 93)
	if e != nil {
		t.Fatal(e)
	}
	b, e := s.Summary(t.Context(), 93)
	if e != nil {
		t.Fatal(e)
	}
	if !regexp.MustCompile(`^110500[1-9]{9}$`).MatchString(a.ManageCode) || a.ManageCode != b.ManageCode {
		t.Fatalf("invalid or unstable management code: %s / %s", a.ManageCode, b.ManageCode)
	}
	apply("down")
	restored, e := s.Summary(t.Context(), 93)
	if e != nil || restored.ManageCode != "SIM-I-000000000093" {
		t.Fatalf("rollback: %+v %v", restored, e)
	}
}
