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
	App                string     `json:"app"`                // 安装包标识：nanwang/alipay/tax/ths。
	Name               string     `json:"name"`               // 安装包名称：南网/支付宝/个税/同花顺。
	Available          bool       `json:"available"`          // Whether an APK and download passcode are configured.
	ReleaseKey         string     `json:"releaseKey"`         // Immutable server-generated China-time timestamp key, YYYYMMDDHHmmss.
	SizeBytes          int64      `json:"sizeBytes"`          // APK size in bytes.
	SHA256             string     `json:"sha256"`             // SHA-256 of the uploaded APK bytes.
	PasscodeConfigured bool       `json:"passcodeConfigured"` // Only presence, never the passcode or hash.
	UploadedAt         *time.Time `json:"uploadedAt"`         // Upload time; null before first upload.
}

func View(s State) Info {
	app, name := Apps[s.ID], Names[s.ID]
	return Info{app, name, s.StoredName != "" && s.PasscodeHash != "", s.VersionName, s.SizeBytes, s.SHA256, s.PasscodeHash != "", s.UploadedAt}
}

var Apps = map[uint64]string{1: "nanwang", 2: "alipay", 3: "tax", 4: "ths"}
var Names = map[uint64]string{1: "南网", 2: "支付宝", 3: "个税", 4: "同花顺"}

// Filename uses the upload date in China time, never the download date.
func Filename(s State) string {
	pinyin := map[uint64]string{1: "nanwang", 2: "zhifubao", 3: "geshui", 4: "tonghuashun"}
	date := ""
	if s.UploadedAt != nil {
		date = s.UploadedAt.In(time.FixedZone("Asia/Shanghai", 8*60*60)).Format("20060102")
	}
	return pinyin[s.ID] + date + ".apk"
}

type ListResponse struct {
	Code    int    `json:"code"`    // 成功为0。
	Message string `json:"message"` // 结果说明。
	Data    []Info `json:"data"`    // 按南网、支付宝、个税、同花顺顺序返回四项安装包信息。
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
	AppID       uint64
	AdminID     uint64
	Action      string
	VersionName string
	SHA256      string `gorm:"column:sha256"`
	CreatedAt   time.Time
}

func (Audit) TableName() string { return "apk_download_audits" }
