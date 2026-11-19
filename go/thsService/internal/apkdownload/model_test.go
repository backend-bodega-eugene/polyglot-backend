package apkdownload

import (
	"testing"
	"time"
)

func TestDownloadFilename(t *testing.T) {
	// UTC late evening is the next upload date in China.
	uploaded := time.Date(2026, 11, 4, 18, 30, 20, 0, time.UTC)
	for id, prefix := range map[uint64]string{1: "nanwang", 2: "zhifubao", 3: "geshui", 4: "tonghuashun"} {
		if got := Filename(State{ID: id, UploadedAt: &uploaded}); got != prefix+"20261105.apk" {
			t.Fatalf("filename %q", got)
		}
	}
}
