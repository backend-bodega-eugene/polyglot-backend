using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.TemplateMethod
{
    public abstract class DataImporter
    {
        // 模板方法：流程锁死
        public void Import()
        {
            ReadFile();
            Validate();
            Save();
            AfterSave(); // Hook
        }

        protected abstract void ReadFile();
        protected abstract void Validate();
        protected abstract void Save();

        // Hook：子类可选覆写
        protected virtual void AfterSave()
        {
            // 默认啥也不干
        }
    }

}
