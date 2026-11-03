package config

import "testing"

func TestSwaggerEnvironmentOverride(t *testing.T) {
	for _, test := range []struct {
		name, environment, swagger string
		want                       bool
		invalid                    bool
	}{
		{"testing", "testing", "true", true, false}, {"production", "production", "false", false, false}, {"bad bool", "production", "wrong", false, true}, {"bad environment", "prodution", "false", false, true},
	} {
		t.Run(test.name, func(t *testing.T) {
			t.Setenv("THS_APP_ENVIRONMENT", test.environment)
			t.Setenv("THS_SWAGGER_ENABLED", test.swagger)
			cfg, err := Load("../../configs/config.yaml")
			if test.invalid {
				if err == nil {
					t.Fatal("invalid configuration accepted")
				}
				return
			}
			if err != nil {
				t.Fatal(err)
			}
			if cfg.Swagger.Enabled != test.want || cfg.App.Environment != test.environment {
				t.Fatalf("incorrect override: %+v", cfg)
			}
		})
	}
}
