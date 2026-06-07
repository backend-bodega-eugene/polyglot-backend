Git 有三个世界：

1. 工作区 (Working Directory)
   你正在编辑的文件

2. 暂存区 (Index / Stage)
   git add 之后的文件

3. 仓库 (Repository)
   git commit 之后的历史

HEAD 指向当前所在的 commit
分支只是指向某个 commit 的指针


查看状态： git status
查看历史： git log --oneline


git add .
git commit -m "说明"
git push


查看所有历史移动： git reflog
回到某个状态： git reset --hard <hash>
