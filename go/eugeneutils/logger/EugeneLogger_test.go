package logger

import (
	"os"
	"strings"
	"testing"
	"time"
	"path/filepath"
)

func TestSetLogFile(t *testing.T) {
	err := SetLogFile()
	if err != nil {
		t.Errorf("SetLogFile failed: %v", err)
	}
}

func TestSetLogFileBySelefPath(t *testing.T) {
	tempPath := filepath.Join("testlogs", "test.log")
	err := SetLogFileBySelefPath(tempPath)
	if err != nil {
		t.Errorf("SetLogFileBySelefPath failed: %v", err)
	}
	defer os.RemoveAll("testlogs")
}

func TestInfoLog(t *testing.T) {
	_ = SetLogFile()
	Info("main", "TestInfoLog", "This is an info message")
}

func TestInfoThreadname(t *testing.T) {
	InfoThreadname("TestInfoThreadname", "Thread name auto fill")
}

func TestInfoThreadnameAndModulename(t *testing.T) {
	InfoThreadnameAndModulename("Auto-filled module name test")
}

func TestInfoWritefle(t *testing.T) {
	filename := "testlogfile"
	InfoWritefle("This is a message written to file", filename)
	time.Sleep(100 * time.Millisecond) // Give time for writing

	data, err := os.ReadFile(filename + ".log")
	if err != nil {
		t.Errorf("failed to read log file: %v", err)
	}
	if !strings.Contains(string(data), "This is a message written to file") {
		t.Errorf("log file does not contain expected message")
	}
	os.Remove(filename + ".log")
}

func TestWarnErrorDebug(t *testing.T) {
	Warn("main", "TestWarn", "This is a warning")
	Error("main", "TestError", "This is an error")
	Debug("main", "TestDebug", "This is a debug message")
}
