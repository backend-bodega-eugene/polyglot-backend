using System.Threading;

namespace utils.SingleTon

{
    /// <summary>
    /// 单例模式
    /// </summary>
    public sealed class Singleton
    {
        /// <summary>
        /// 禁止外部实例化
        /// </summary>
        private Singleton() { }
        /// 锁定,禁止多线程访问
        private static readonly object padlock = new object();
        private static Singleton? singleton;
        /// <summary>
        /// 获取实例
        /// </summary>
        /// <returns></returns>
        public static Singleton GetInstance()
        {
            if (singleton != null) return singleton;
            lock (padlock)
            {
                return singleton ??= new Singleton();
            }
        }
        /// <summary>
        /// 示例方法而已
        /// </summary>
        /// <param name="uegene"></param>
        /// <returns></returns>
        public string GetEugene(string uegene)
        {
            Console.WriteLine(uegene + new Random().Next(1, 100));
            return uegene;
        }

    }
}
