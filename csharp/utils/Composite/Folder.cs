using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Composite
{
    // 文件夹类：Composite节点
    public class Folder : FileSystemComponent
    {
        private string name;
        private List<FileSystemComponent> components = new List<FileSystemComponent>();

        public Folder(string name)
        {
            this.name = name;
        }

        // 添加子组件（文件或文件夹）
        public void Add(FileSystemComponent component)
        {
            components.Add(component);
        }

        // 移除子组件
        public void Remove(FileSystemComponent component)
        {
            components.Remove(component);
        }

        public override void ShowDetails()
        {
            Console.WriteLine($"Folder: {name}");
            foreach (var component in components)
            {
                component.ShowDetails();  // 递归调用，显示子文件和子文件夹的详情
            }
        }
    }

}
