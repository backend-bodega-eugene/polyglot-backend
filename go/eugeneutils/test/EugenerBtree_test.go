package test

import (
	"eugene-go/rbtree"
	"testing"
)

func TestRB_InsertAndProps(t *testing.T) {
	t.Run("basic", func(t *testing.T) {
		var tr rbtree.Tree
		for _, v := range []int{10, 5, 1, 7, 40, 50, 30, 20, 60} {
			tr.EugeneInsert(v)
			if !tr.NoRedRed() {
				t.Fatalf("red-red violated after %d", v)
			}
			if !tr.BlackHeightEqual() {
				t.Fatalf("black-height mismatch after %d", v)
			}
		}
		got := tr.EugeneInOrder()
		want := []int{1, 5, 7, 10, 20, 30, 40, 50, 60}
		if len(got) != len(want) {
			t.Fatalf("length mismatch")
		}
		for i := range got {
			if got[i] != want[i] {
				t.Fatalf("inorder wrong at %d: %v", i, got)
			}
		}
		// quick search checks
		if tr.EugeneSearch(30) == nil || tr.EugeneSearch(999) != nil {
			t.Fatalf("search failed")
		}
	})
}
