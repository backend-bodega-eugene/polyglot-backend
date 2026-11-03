package apkdownload

import "time"

// State contains private storage metadata. Never serialize it in API responses.
type State struct {
	ID           uint64 `gorm:"primaryKey"`
	StoredName   string
	VersionName  string
	ReleaseNotes string
	SizeBytes    int64
	SHA256       string `gorm:"column:sha256"`
	PasscodeHash string
	UploadedAt   *time.Time
	UpdatedAt    time.Time
}

func (State) TableName() string { return "apk_download_state" }

type Info struct {
	Available          bool       `json:"available"`          // Whether an APK and download passcode are configured.
	ReleaseKey         string     `json:"releaseKey"`         // Immutable server-generated China-time timestamp key, YYYYMMDDHHmmss.
	SizeBytes          int64      `json:"sizeBytes"`          // APK size in bytes.
	SHA256             string     `json:"sha256"`             // SHA-256 of the uploaded APK bytes.
	PasscodeConfigured bool       `json:"passcodeConfigured"` // Only presence, never the passcode or hash.
	UploadedAt         *time.Time `json:"uploadedAt"`         // Upload time; null before first upload.
}

func View(s State) Info {
	return Info{s.StoredName != "" && s.PasscodeHash != "", s.VersionName, s.SizeBytes, s.SHA256, s.PasscodeHash != "", s.UploadedAt}
}

type InfoResponse struct {
	Code    int    `json:"code"`    // Zero on success.
	Message string `json:"message"` // Result description.
	Data    Info   `json:"data"`    // Current published version metadata.
}
type PasscodeRequest struct {
	Passcode string `json:"passcode"` // Exact passcode, including Chinese; 5 to 64 Unicode characters, at most 256 UTF-8 bytes; not whitespace only.
}
type Audit struct {
	ID          uint64
	AdminID     uint64
	Action      string
	VersionName string
	SHA256      string `gorm:"column:sha256"`
	CreatedAt   time.Time
}

func (Audit) TableName() string { return "apk_download_audits" }
