using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.TemplateMethod
{
    public class ExcelImporter : DataImporter
    {
        protected override void ReadFile()
        {
            Console.WriteLine("读取 Excel 文件");
        }

        protected override void Validate()
        {
            Console.WriteLine("校验 Excel 数据");
        }

        protected override void Save()
        {
            Console.WriteLine("保存 Excel 数据到数据库");
        }
    }

}
