package repo

import (
	"context"
	"eugene-go-starter/internal/model"

	"github.com/jmoiron/sqlx"
)

type MenuMySQL struct{ DB *sqlx.DB }
func NewMenuRepoSQLX(db *sqlx.DB) MenuRepo { return &MenuMySQL{DB: db} }

func (r *MenuMySQL) ListAll(ctx context.Context) ([]model.Menu, error) {
	const q = `SELECT menu_id, parent_id, name, path, icon, sort, status
			   FROM menus WHERE status=1 ORDER BY sort ASC, menu_id ASC`
	var out []model.Menu
	return out, r.DB.SelectContext(ctx, &out, q)
}

func (r *MenuMySQL) ListByUser(ctx context.Context, userID int64) ([]model.Menu, error) {
	const q = `
SELECT m.menu_id, m.parent_id, m.name, m.path, m.icon, m.sort, m.status
FROM user_menu um
JOIN menus m ON um.menu_id = m.menu_id
WHERE um.user_id=? AND m.status=1
ORDER BY m.sort ASC, m.menu_id ASC`
	var out []model.Menu
	return out, r.DB.SelectContext(ctx, &out, q, userID)
}
