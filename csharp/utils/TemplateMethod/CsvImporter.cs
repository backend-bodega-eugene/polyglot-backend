using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.TemplateMethod
{
    public class CsvImporter : DataImporter
    {
        protected override void ReadFile()
        {
            Console.WriteLine("读取 CSV 文件");
        }

        protected override void Validate()
        {
            Console.WriteLine("校验 CSV 数据");
        }

        protected override void Save()
        {
            Console.WriteLine("保存 CSV 数据到数据库");
        }

        protected override void AfterSave()
        {
            Console.WriteLine("CSV 导入完成，发送通知");
        }
    }
    class Program
    {
        static void Main()
        {
            DataImporter importer = new ExcelImporter();
            importer.Import();

            Console.WriteLine("----");

            importer = new CsvImporter();
            importer.Import();
        }
    }

}
