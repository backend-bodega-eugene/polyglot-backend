// panicguard/guard.go
package panicguard

import (
	"context"
	_ "eugene-go/logger"
	"runtime/debug"
)

type Logger interface {
	InfoThreadnameAndModulename(message string)
}

type Config struct {
	//Log     Logger                                         // 默认用 log.Default()
	//Tag     string                                         // 统一标签，如"api"、"worker"
	OnPanic func(ctx context.Context, r any, stack []byte) // 可选: 告警/指标/自愈
}

// 空实现：避免没传 logger 时的 nil panic
// type nopLogger struct{}

// func (nopLogger) InfoThreadnameAndModulename(string) {}

func Guard(ctx context.Context, cfg *Config) func() {
	// var lg Logger = nopLogger{}
	// if cfg != nil && cfg.Log != nil {
	// 	lg = cfg.Log
	// }
	// tag := ""
	// if cfg != nil && cfg.Tag != "" {
	// 	tag = cfg.Tag
	// }
	return func() {

		if r := recover(); r != nil {
			stack := debug.Stack()
			//lg.InfoThreadnameAndModulename(prefix(tag))
			if cfg != nil && cfg.OnPanic != nil {
				cfg.OnPanic(ctx, r, stack)
			}
		}
	}
}

// func prefix(tag string) string {
// 	if tag == "" {
// 		return ""
// 	}
// 	return " [" + tag + "]"
// }
