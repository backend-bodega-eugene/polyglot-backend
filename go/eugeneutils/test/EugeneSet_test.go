package test

import (
	"eugene-go/gset"
	"reflect"
	"sort"
	"testing"
)

func TestSetBasic(t *testing.T) {
	s := gset.New[int](true) // 并发安全版

	// Add
	s.Add(1)
	s.Add(2)
	s.Add(2) // 重复添加
	if s.Len() != 2 {
		t.Errorf("Len() = %d; want 2", s.Len())
	}

	// Has
	if !s.Has(1) || s.Has(99) {
		t.Errorf("Has() error: expected 1 in set and 99 not in set")
	}

	// Remove
	s.Remove(2)
	if s.Has(2) {
		t.Errorf("Remove() failed, 2 should not be in set")
	}

	// ToSlice
	sl := s.ToSlice()
	sort.Ints(sl)
	want := []int{1}
	if !reflect.DeepEqual(sl, want) {
		t.Errorf("ToSlice() = %v; want %v", sl, want)
	}

	// Clear
	s.Clear()
	if s.Len() != 0 {
		t.Errorf("Clear() failed, Len() = %d; want 0", s.Len())
	}
}

func TestSetUnion(t *testing.T) {
	a := gset.New[int](false, 1, 2, 3)
	b := gset.New[int](false, 3, 4, 5)
	u := gset.Union(a, b)

	got := u.ToSlice()
	sort.Ints(got)
	want := []int{1, 2, 3, 4, 5}

	if !reflect.DeepEqual(got, want) {
		t.Errorf("Union() = %v; want %v", got, want)
	}
}

func TestSetIntersect(t *testing.T) {
	a := gset.New[int](false, 1, 2, 3)
	b := gset.New[int](false, 2, 3, 4)
	i := gset.Intersect(a, b)

	got := i.ToSlice()
	sort.Ints(got)
	want := []int{2, 3}

	if !reflect.DeepEqual(got, want) {
		t.Errorf("Intersect() = %v; want %v", got, want)
	}
}

func TestSetDiff(t *testing.T) {
	a := gset.New[int](false, 1, 2, 3)
	b := gset.New[int](false, 2, 4)
	d := gset.Diff(a, b)

	got := d.ToSlice()
	sort.Ints(got)
	want := []int{1, 3}

	if !reflect.DeepEqual(got, want) {
		t.Errorf("Diff() = %v; want %v", got, want)
	}
}
