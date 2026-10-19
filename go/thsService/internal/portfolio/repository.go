package portfolio

import (
	"context"
	"errors"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

type Repository struct{ db *gorm.DB }

func NewRepository(db *gorm.DB) *Repository { return &Repository{db: db} }

func (r *Repository) NeedsHoldingUpgrade(ctx context.Context, userID uint64) (bool, error) {
	var account Account
	err := r.db.WithContext(ctx).Select("user_id", "holding_version").Where("user_id = ?", userID).Take(&account).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return false, nil
	}
	return account.HoldingVersion < 1, err
}

// Lock the user row, which exists even before the first portfolio is generated.
// This serializes initialization, replacement and history reads for one user.
func (r *Repository) withUser(ctx context.Context, userID uint64, fn func(*gorm.DB) error) error {
	return r.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var user struct{ ID uint64 }
		if err := tx.Table("users").Clauses(clause.Locking{Strength: "UPDATE"}).Select("id").Where("id = ? AND deleted_at IS NULL", userID).Take(&user).Error; err != nil {
			return err
		}
		return fn(tx)
	})
}

func (r *Repository) GetOrGenerate(ctx context.Context, userID uint64, reset bool, assets int64, generate func(int64) (Data, []Trade, error)) (Data, error) {
	var data Data
	err := r.withUser(ctx, userID, func(tx *gorm.DB) error {
		var account Account
		err := tx.Where("user_id = ?", userID).Take(&account).Error
		if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}
		if reset || errors.Is(err, gorm.ErrRecordNotFound) || account.HoldingVersion < 1 {
			var generated Data
			var trades []Trade
			if reset && err == nil && account.HoldingVersion >= 1 {
				generated, trades, err = rebuildExisting(tx, account, assets)
			} else {
				if !reset && err == nil {
					assets = decimalCents(account.TotalAssets)
				}
				generated, trades, err = generate(assets)
			}
			if err != nil {
				return err
			}
			if err := tx.Save(&generated.Account).Error; err != nil {
				return err
			}
			if err := tx.Create(&generated.Positions).Error; err != nil {
				return err
			}
			if len(trades) > 0 {
				if err := tx.Create(&trades).Error; err != nil {
					return err
				}
			}
			data = generated
			data.Positions = activePositions(generated.Positions)
			return nil
		}
		data = Data{Simulated: true, Account: account, Positions: make([]Position, 0)}
		return tx.Where("user_id = ? AND generation = ? AND quantity > 0", userID, account.Generation).Order("id ASC").Find(&data.Positions).Error
	})
	return data, err
}

func (r *Repository) Trades(ctx context.Context, userID uint64, page, pageSize int) (TradeList, error) {
	data := TradeList{Simulated: true, Items: make([]Trade, 0), Page: page, PageSize: pageSize}
	err := r.withUser(ctx, userID, func(tx *gorm.DB) error {
		var account Account
		if err := tx.Where("user_id = ?", userID).Take(&account).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return nil
			}
			return err
		}
		data.Generation = account.Generation
		query := tx.Model(&Trade{}).Where("user_id = ? AND generation = ?", userID, account.Generation)
		if err := query.Count(&data.Total).Error; err != nil {
			return err
		}
		return query.Order("traded_at DESC, id DESC").Offset((page - 1) * pageSize).Limit(pageSize).Find(&data.Items).Error
	})
	return data, err
}
