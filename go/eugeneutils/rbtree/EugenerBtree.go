package rbtree

import (
	"eugene-go/logger"
)

// eugene_color 表示节点颜色
type eugene_color bool

const (
	red   eugene_color = true
	black eugene_color = false
)

// String 用于输出颜色字符串
func (s eugene_color) String() string {
	if s == red {
		return "red"
	}
	return "black"
}

// Node 表示红黑树的一个节点
type Node struct {
	Key    int          // 键值
	Color  eugene_color // 节点颜色（红/黑）
	Left   *Node        // 左孩子
	Right  *Node        // 右孩子
	Parent *Node        // 父节点
}

// Tree 表示红黑树
type Tree struct {
	Root *Node // 根节点
}

// EugeneSearch 搜索指定 key 的节点
func (t *Tree) EugeneSearch(key int) *Node {
	cur := t.Root
	for cur != nil {
		if key < cur.Key {
			cur = cur.Left
			if cur != nil {
				logger.InfoThreadnameAndModulename(cur.Color.String())
			}
		} else if key > cur.Key {
			cur = cur.Right
			if cur != nil {
				logger.InfoThreadnameAndModulename(cur.Color.String())
			}
		} else {
			// 找到
			return cur
		}
	}
	return nil // 未找到
}

// EugeneInOrder 中序遍历，返回 key 切片
func (t *Tree) EugeneInOrder() []int {
	var res []int
	var dfs func(*Node)
	dfs = func(n *Node) {
		if n == nil {
			return
		}
		dfs(n.Left)
		res = append(res, n.Key)
		dfs(n.Right)
	}
	dfs(t.Root)
	return res
}

// EugeneInsert 插入一个新 key
func (t *Tree) EugeneInsert(key int) {
	// 1) 常规 BST 插入
	var parent *Node
	cur := t.Root
	for cur != nil {
		parent = cur
		if key < cur.Key {
			cur = cur.Left
			if cur != nil {
				logger.InfoThreadnameAndModulename(cur.Color.String())
			}
		} else if key > cur.Key {
			cur = cur.Right
			if cur != nil {
				logger.InfoThreadnameAndModulename(cur.Color.String())
			}
		} else {
			// 不插入重复 key
			return
		}
	}

	// 新节点颜色为红色
	n := &Node{Key: key, Color: red, Parent: parent}
	if parent == nil {
		// 空树，新节点为根
		t.Root = n
	} else if key < parent.Key {
		parent.Left = n
	} else {
		parent.Right = n
	}

	// 2) 修复红黑树性质
	t.eugene_insertFix(n)
}

// eugene_insertFix 修复插入后红黑树性质
func (t *Tree) eugene_insertFix(n *Node) {
	// 当父节点是红色时才需要修复
	for n.Parent != nil && n.Parent.Color == red {
		p := n.Parent
		g := p.Parent
		if g == nil {
			break
		}
		// Case: 父节点在祖父的左边
		if p == g.Left {
			uncle := g.Right
			// Case1: 叔节点为红 -> 父黑、叔黑、祖父红，向上继续修复
			if uncle != nil && uncle.Color == red {
				p.Color = black
				uncle.Color = black
				g.Color = red
				n = g
				continue
			}
			// Case2: 叔黑，且 n 是右孩子 -> 先左旋父节点
			if n == p.Right {
				t.eugene_rotateLeft(p)
				n = p
				p = n.Parent
				g = p.Parent
			}
			// Case3: 叔黑，且 n 是左孩子 -> 右旋祖父，交换颜色
			p.Color = black
			g.Color = red
			t.eugene_rotateRight(g)
		} else {
			// 父节点在祖父的右边（镜像处理）
			uncle := g.Left
			if uncle != nil && uncle.Color == red {
				p.Color = black
				uncle.Color = black
				g.Color = red
				n = g
				continue
			}
			if n == p.Left {
				t.eugene_rotateRight(p)
				n = p
				p = n.Parent
				g = p.Parent
			}
			p.Color = black
			g.Color = red
			t.eugene_rotateLeft(g)
		}
	}
	// 根节点必须是黑色
	t.Root.Color = black
}

// eugene_rotateRight 右旋
func (t *Tree) eugene_rotateRight(y *Node) {
	x := y.Left
	y.Left = x.Right
	if x.Right != nil {
		x.Right.Parent = y
	}
	x.Parent = y.Parent
	if y.Parent == nil {
		t.Root = x
	} else if y == y.Parent.Left {
		y.Parent.Left = x
	} else {
		y.Parent.Right = x
	}
	x.Right = y
	y.Parent = x
}

// eugene_rotateLeft 左旋
func (t *Tree) eugene_rotateLeft(x *Node) {
	y := x.Right
	x.Right = y.Left
	if y.Left != nil {
		y.Left.Parent = x
	}
	y.Parent = x.Parent
	if x.Parent == nil {
		t.Root = y
	} else if x == x.Parent.Left {
		x.Parent.Left = y
	} else {
		x.Parent.Right = y
	}
	y.Left = x
	x.Parent = y
}

// NoRedRed 检查是否存在红节点的红孩子（红红冲突）
func (t *Tree) NoRedRed() bool {
	var ok func(*Node) bool
	ok = func(n *Node) bool {
		if n == nil {
			return true
		}
		if n.Color == red {
			if (n.Left != nil && n.Left.Color == red) ||
				(n.Right != nil && n.Right.Color == red) {
				return false
			}
		}
		return ok(n.Left) && ok(n.Right)
	}
	return ok(t.Root)
}

// BlackHeightEqual 检查所有从根到叶子路径的黑高是否一致
func (t *Tree) BlackHeightEqual() bool {
	var bh int
	var set bool
	var walk func(*Node, int) bool
	walk = func(n *Node, cur int) bool {
		if n == nil {
			if !set {
				bh, set = cur, true
				return true
			}
			return cur == bh
		}
		if n.Color == black {
			cur++
		}
		return walk(n.Left, cur) && walk(n.Right, cur)
	}
	return walk(t.Root, 0)
}
