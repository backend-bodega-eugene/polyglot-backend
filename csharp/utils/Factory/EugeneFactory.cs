using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace utils.Factory
{
    /// <summary>
    /// 工厂模式
    /// </summary>
    public sealed class EugeneFactory
    {
        /// <summary>
        /// 禁止外部实例化
        /// </summary>
        private EugeneFactory() { }
        /// 锁定,禁止多线程访问
        private static readonly object objectLock = new object();
        private static EugeneFactory? eugeneFactory;
        /// <summary>
        /// 获取实例
        /// </summary>
        /// <returns></returns>
        public static EugeneFactory GetInstance()
        {
            if (eugeneFactory != null) return eugeneFactory;
            lock (objectLock)
            {
                return eugeneFactory ??= new EugeneFactory();
            }
        }
        public IEugene CreateEugene(string yourWantEugene)
        {
            IEugene? eugene =EugeneIndex.Instance.MyEugene()[yourWantEugene];
            // 这里可以添加一些公共逻辑
            if (eugene == null) 
            {
                throw new NotImplementedException("没有找到对应的Eugene");
            }
            return eugene;
        }

    }
}
